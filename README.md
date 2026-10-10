# Saga Pattern

주문 → 재고 → 결제 → 배송 흐름으로 사가 패턴을 구현한 Kotlin 2.3.21 / Java 21 / Spring Boot 4.0.0 / Kafka 멀티 모듈 프로젝트입니다.

주문 서비스가 오케스트레이터입니다. 주문을 확정하기 전에 결과를 알아야 하는 단계(재고 차감, 결제)는 동기 HTTP로 부르고, 확정 이후의 단계(배송 등록)는 이벤트로 넘깁니다.

## 구조

```text
saga-pattern/
├── platform/            # 공통 기술 지원 (웹 오류 응답, 트랜잭셔널 아웃박스, 컨슈머 오류 처리, 중복 처리 방지)
├── order-service/       # 주문 서비스 (사가 오케스트레이터)
├── inventory-service/   # 재고 서비스
├── payment-service/     # 결제 서비스
├── shipping-service/    # 배송 서비스
├── architecture-test/   # 네 서비스의 아키텍처 규칙 검사 (ArchUnit)
├── build.gradle.kts     # 서비스 공통 의존성 및 빌드 설정 (Kotlin DSL)
├── Dockerfile           # 서비스 공통 런타임 이미지
├── nginx/               # 주문 서비스 앞단 로드 밸런서 설정
└── compose.yml          # 서비스별 3개 인스턴스 + 서비스별 MySQL 1개 + Kafka 3개 노드 + 로드 밸런서
```

각 서비스는 헥사고날 구조입니다. `domain`, `application`(`port/in`, `port/out`, `service`, `saga`), `adapter`(`in/web`, `in/messaging`, `in/scheduling`, `out/persistence`, `out/client`, `out/messaging`)로 나뉩니다.

## 흐름

### 주문 (`POST /orders`)

한 요청 안에서 다음 순서로 진행하고 결과를 돌려줍니다. 전체를 감싸는 트랜잭션은 없고, 단계마다 사가 상태를 짧은 트랜잭션으로 남깁니다. 다른 서비스를 부르는 동안에는 DB 트랜잭션을 쥐지 않습니다.

1. 주문(PENDING)과 사가를 저장합니다.
2. 재고 서비스에 차감을 요청합니다. (`POST /order-stocks/{orderId}/decrease`)
3. 결제 서비스에 결제를 요청합니다. (`POST /payments`)
4. 주문을 COMPLETED로 바꾸고 아웃박스로 `order.completed` 이벤트를 발행합니다.
5. 배송 서비스가 이벤트를 받아 배송을 등록합니다. 주문 서비스는 이 결과를 기다리지 않습니다.

| 응답 | 의미 |
| --- | --- |
| 201 | 결제까지 끝나 주문이 완료됐습니다. |
| 409 `ORDER_REJECTED` | 재고 부족, 결제 거절 등으로 주문을 취소했습니다. 되돌릴 것은 모두 되돌렸고 `reason`에 사유가 있습니다. |
| 202 | 되돌리는 중이라 결과가 정해지지 않았습니다. 주문을 다시 조회해 확인합니다. |
| 422 `IDEMPOTENCY_KEY_REUSED` | 같은 멱등 키로 다른 내용을 보냈습니다. 주문을 만들지 않았습니다. |

`Idempotency-Key` 헤더(64자 이하)를 붙이면 같은 키의 요청은 주문을 한 번만 만듭니다. 응답을 받지 못해 다시 보낼 때 같은 키를 씁니다. 다시 온 요청은 진행시키지 않고 처음 만든 주문의 지금 결과(201, 409, 202)로 답합니다. 키는 24시간 보관합니다. 헤더가 없으면 요청마다 새 주문을 만듭니다.

호출 결과를 알 수 없으면(응답 시간 초과, 연결 끊김) 처리됐을 수 있다고 보고 항상 되돌립니다. 요청을 보내지 못했으면(어느 인스턴스에도 연결하지 못함, 회로가 열림) 처리되지 않은 것이 확실하므로 그 단계는 되돌리지 않고 바로 거절합니다. (`reason`: `INVENTORY_UNAVAILABLE`, `PAYMENT_UNAVAILABLE`) 되돌리는 호출이 실패하면 복구 스케줄러가 될 때까지 다시 시도합니다. 주문 요청이 도중에 죽어 남은 사가도 복구 스케줄러가 되돌립니다.

### 취소 (`POST /orders/{orderId}/cancel`)

완료된 주문은 배송 취소 → 결제 취소 → 재고 복원 순으로 되돌린 뒤 CANCELLED가 됩니다. 배송 서비스가 취소를 거절하면(이미 출고, `ALREADY_SHIPPED`) 아무것도 되돌리지 않고 409 `ORDER_CANCEL_REJECTED`로 답합니다. 다 되돌렸으면 200, 아직 되돌리는 중이면 202입니다.

완료된 주문은 주문한 때부터 7일 안에만 취소할 수 있습니다. (`order.cancel.allowed-for`) 지나면 409 `ORDER_CANCEL_REJECTED`(`CANCEL_PERIOD_EXPIRED`)로 답합니다. 재고 서비스는 이 기간보다 넉넉히 긴 30일 뒤에 차감 기록을 지웁니다. (`inventory.order-stock-records.cleanup.decreased-retention`) 차감 기록이 지워진 주문은 재고를 되돌릴 수 없으므로 두 값의 순서를 지켜야 합니다.

### 중복과 경합 처리

- 재고·결제·배송의 주문 단위 API는 같은 주문으로 여러 번 불러도 한 번만 처리합니다. (`order_stock_records`, `payments.order_id`, `order_payment_cancellations`, `shipments.order_id`)
- 주문 서비스는 사가 행을 잠그고 기대한 상태일 때만 옮기므로, 주문 요청·취소·복구가 같은 주문을 동시에 다뤄도 전이는 한 번만 일어납니다.
- 결제는 결제사 승인 전에 PENDING으로 먼저 등록합니다. 승인을 기다리는 사이 취소되면 받은 승인을 다시 취소합니다.
- 같은 주문의 요청이 여럿 겹칠 때는 기록을 "없으면 넣고, 있든 없든 배타 잠금"(`insert … on duplicate key update`)으로 잡아 한 줄로 세웁니다. `insert ignore`로 확인한 뒤 `select … for update`로 잠그면, 중복 확인 때 잡힌 공유 잠금을 서로 배타 잠금으로 올리려다 교착이 생깁니다.
- 주문 서비스는 인스턴스 목록을 번갈아 부르고, 연결하지 못했을 때만 다른 인스턴스로 다시 시도합니다.
- 참여 서비스별로 회로 차단기(Resilience4j)를 둡니다. 최근 20번의 호출 중 절반 이상이 결과를 알 수 없이 끝나면 10초 동안 보내지 않고 바로 실패시킵니다. 업무상 거절은 실패로 세지 않습니다.
- 이벤트는 트랜잭셔널 아웃박스로 발행하고(at-least-once), 받는 쪽은 `processed_events`로 중복을 거릅니다.

### 토픽

| 토픽 | 발행 | 구독 | 용도 |
| --- | --- | --- | --- |
| `order.completed` | 주문 | 배송 | 확정된 주문 → 배송 등록 |
| `inventory.stock-changed` | 재고 | 주문 | 재고의 최신 상태 → 주문 서비스의 단가 스냅샷 |
| `order.dead-letters`, `shipping.dead-letters` | 소비하는 서비스 | - | 처리할 수 없는 메시지 격리 (`POST /actuator/deadletters`로 재처리) |

## 전체 실행

Java 21과 Docker Compose가 필요합니다. 프로젝트 디렉터리에서 실행합니다.

```bash
./gradlew build
docker compose up -d --build
docker compose ps
```

| 서비스 | 인스턴스 수 | 호스트 포트 |
| --- | --- | --- |
| 주문 | 3 | 18080, 18081, 18082 |
| 재고 | 3 | 18090, 18091, 18092 |
| 결제 | 3 | 18100, 18101, 18102 |
| 배송 | 3 | 18110, 18111, 18112 |
| 주문 로드 밸런서 (nginx) | 1 | 18000 |
| Kafka | 3 | 29092, 39092, 49092 |
| 주문 MySQL | 1 | 13306 |
| 재고 MySQL | 1 | 13316 |
| 결제 MySQL | 1 | 13326 |
| 배송 MySQL | 1 | 13336 |

MySQL 사용자명/비밀번호는 모두 `admin`/`admin`입니다. root 비밀번호도 `admin`입니다. DB 이름은 `order_db`, `inventory_db`, `payment_db`, `shipping_db`이며, 같은 서비스의 세 인스턴스가 DB 하나를 함께 씁니다.

`ddl-auto: validate`라서 테이블은 자동으로 만들어지지 않습니다. 엔티티에 맞는 DDL을 DB에 직접 실행해야 합니다.

주문 서비스는 로드 밸런서(18000)로 부르거나 인스턴스에 직접 붙습니다. 로드 밸런서는 연결하지 못했을 때만 다른 인스턴스로 다시 보냅니다. 요청을 보낸 뒤에 인스턴스가 죽으면 502로 답하므로, 그때는 클라이언트가 같은 `Idempotency-Key`로 다시 보냅니다.

```bash
curl http://localhost:18000/actuator/health

# 재고 등록 → 주문 → 취소
curl -X POST localhost:18090/stocks -H 'content-type: application/json' -d '{"name":"sample","price":1000,"quantity":10}'
curl -X POST localhost:18000/orders -H 'content-type: application/json' -H 'Idempotency-Key: my-key-1' \
  -d '{"items":[{"stockId":"<stockId>","quantity":2}]}'
curl -X POST localhost:18000/orders/<orderId>/cancel

# 출고 처리 (이후에는 주문을 취소할 수 없습니다)
curl -X POST localhost:18110/order-shipments/<orderId>/ship
```

### 실패 흐름 만들어 보기

- 결제 거절: 가짜 결제사는 금액이 1,000,000을 넘으면 거절합니다. (`payment.gateway.reject-over`)
- 느린 결제사: `PAYMENT_GATEWAY_APPROVE_DELAY=4s docker compose up -d payment-1 payment-2 payment-3`처럼 승인 응답을 늦출 수 있습니다. 주문 서비스의 응답 대기 시간(3초)보다 길게 주면 응답 시간 초과 후 되돌리는 흐름을 볼 수 있습니다. 주문을 계속 보내면 회로가 열려, 이후 주문은 기다리지 않고 `PAYMENT_UNAVAILABLE`로 바로 거절됩니다.
- 취소 기한: `ORDER_CANCEL_ALLOWED_FOR=5s docker compose up -d order-1 order-2 order-3`처럼 줄여서 기한이 지난 취소의 거절을 볼 수 있습니다.
- 서비스 장애: `docker compose stop payment-1 payment-2 payment-3` 또는 `docker kill`로 인스턴스를 내립니다.
- 멈춘 사가: 지표 `order.saga.stalled.count`가 0이 아니면 되돌리기가 끝나지 않는 사가가 있는 것입니다.

## 테스트

```bash
./gradlew test             # 단위 테스트 (스프링 컨텍스트, DB 없음) + 아키텍처 규칙 검사
./gradlew integrationTest  # DB 통합 테스트 (Docker 필요)
```

단위 테스트는 스프링 컨텍스트 없이 도메인과 애플리케이션 서비스를 직접 만들어 검증합니다. 포트는 메모리 구현으로 대신합니다. 사가 상태 전이, 참여 서비스 응답(성공·거절·결과 불명·보내지 못함)별 진행, 멱등 키, 취소 기한, 결제의 승인 대기 중 취소, 주문 단위 차감·복원·배송의 중복 처리를 다룹니다.

통합 테스트(`src/integrationTest`)는 Testcontainers로 MySQL 8.4를 띄우고 애플리케이션 서비스와 영속성 어댑터만 올려, 여러 스레드를 같은 순간에 출발시켜 잠금과 경합을 검증합니다. 같은 사가의 동시 전이, 복구 작업의 선점, 같은 멱등 키의 동시 요청, 결제 대 취소, 차감 대 복원, 출고 대 취소, 같은 요청이 여럿 겹칠 때의 교착 여부를 다룹니다. 테이블은 엔티티로 만들고(`ddl-auto: create`), Kafka와 참여 서비스 호출은 포함하지 않습니다. `./gradlew build`에는 포함되지 않습니다.

아키텍처 규칙(`architecture-test`)은 컴파일된 클래스의 의존을 읽어 네 서비스에 같은 규칙을 적용합니다. 의존은 `adapter → application → domain` 한 방향으로만 흐르고 `config`만 전체를 조립합니다. domain은 자기 자신과 언어 표준만 보고, application이 쓰는 프레임워크는 `@Service`와 `@Transactional`뿐이며, 인바운드와 아웃바운드 어댑터는 서로를 보지 않고 유스케이스 구현 대신 포트를 봅니다. JPA·Kafka·웹 같은 기술은 정해진 어댑터 패키지 밖으로 나가지 않고, 서비스는 다른 서비스의 코드를 보지 않습니다.

실제 장애(인스턴스 종료, 느린 응답)는 compose 환경에서 따로 확인합니다.

## 로컬 개발

Kafka와 DB를 Docker로 실행하고, 필요한 서비스를 IDE 또는 Gradle로 실행할 수 있습니다.

```bash
docker compose up -d kafka-1 kafka-2 kafka-3 order-db inventory-db payment-db shipping-db
./gradlew :order-service:bootRun
# 별도 터미널에서 각각
./gradlew :inventory-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :shipping-service:bootRun
```

로컬 기본 HTTP 포트는 주문 8080, 재고 8081, 결제 8082, 배송 8083입니다. `SERVER_PORT`, `KAFKA_BOOTSTRAP_SERVERS`, `SPRING_DATASOURCE_URL` 환경 변수로 변경할 수 있습니다. 주문 서비스가 부르는 주소의 기본값은 compose의 호스트 포트(18090 등)이므로, 재고·결제·배송을 로컬에서 실행할 때는 `INVENTORY_BASE_URLS`, `PAYMENT_BASE_URLS`, `SHIPPING_BASE_URLS`를 맞춰 줍니다.

```bash
INVENTORY_BASE_URLS=http://localhost:8081 PAYMENT_BASE_URLS=http://localhost:8082 SHIPPING_BASE_URLS=http://localhost:8083 \
  ./gradlew :order-service:bootRun
```

## 참고

- 서비스별 consumer group은 `order-service`, `shipping-service`입니다. 재고·결제 서비스는 메시지를 소비하지 않습니다.
- Kafka는 KRaft의 broker/controller 겸용 노드 3개로 구성했습니다. 기본 파티션 수와 복제 계수는 3, 최소 ISR은 2입니다. 토픽은 발행하는 서비스가 시작할 때 만듭니다.
- 아웃박스 릴레이가 Kafka 트랜잭션으로 발행하므로 컨슈머는 `read_committed`로 읽습니다.

## 종료

```bash
docker compose down
# Kafka와 MySQL 데이터도 삭제하려면
# docker compose down -v
```
