package com.example.saga.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class HexagonalArchitectureTest {

    private val services = listOf("order", "inventory", "payment", "shipping")
    private val imported = services.associateWith { ClassFileImporter().importPackages(basePackage(it)) }

    @Test
    fun `infrastructure는 feature에 의존하지 않는다`() {
        val classes = ClassFileImporter().importPackages(PLATFORM)
        assertTrue(classes.isNotEmpty())
        noClasses().should().dependOnClassesThat()
            .resideInAnyPackage(*services.map { "${basePackage(it)}.." }.toTypedArray()).check(classes)
    }

    @Test
    fun `공개 계약은 기술과 내부 레이어를 참조하지 않는다`() {
        services.forEach { service ->
            val classes = ClassFileImporter().importPackages("${basePackage(service)}.contract")
            assertTrue(classes.isNotEmpty(), "$service 공개 계약이 없다")
            noClasses().should().dependOnClassesThat().resideOutsideOfPackages(
                "${basePackage(service)}.contract..", "java..", "kotlin..", "org.jetbrains.annotations..",
            ).check(classes)
        }
    }

    @TestFactory
    fun `서비스는 헥사고날 규칙을 지킨다`(): List<DynamicTest> = services.flatMap { service ->
        val classes = imported.getValue(service)
        listOf(dynamicTest("$service: 검사할 클래스가 층마다 있다") { assertLayersImported(service, classes) }) +
            rules(service).map { rule -> dynamicTest("$service: ${rule.description}") { rule.check(classes) } }
    }

    private fun rules(service: String): List<ArchRule> {
        val base = basePackage(service)
        val privateTypesOfOtherServices = object : DescribedPredicate<JavaClass>("다른 BC의 contract 밖 타입") {
            override fun test(type: JavaClass): Boolean = (services - service).any {
                val otherBase = basePackage(it)
                (type.packageName == otherBase || type.packageName.startsWith("$otherBase.")) &&
                    type.packageName != "$otherBase.contract" && !type.packageName.startsWith("$otherBase.contract.")
            }
        }

        val layerRules = listOf(
            noClasses().that().resideInAPackage("$base.domain..")
                .should().dependOnClassesThat()
                .resideOutsideOfPackages("$base.domain..", "java..", "kotlin..", "org.jetbrains.annotations..")
                .because("domain은 자기 자신과 언어 표준만 본다"),
            noClasses().that().resideInAPackage("$base.application..")
                .should().dependOnClassesThat().resideInAPackage("$base.adapter..")
                .because("application은 어댑터와 조립 코드를 보지 않는다"),
            noClasses().that().resideInAPackage("$base.application..")
                .should().dependOnClassesThat(forbiddenInApplication)
                .because("application이 쓰는 프레임워크는 @Service와 @Transactional뿐이다"),
            noClasses().that().resideInAPackage("$base.adapter.in..")
                .should().dependOnClassesThat().resideInAPackage("$base.adapter.out..")
                .because("인바운드 어댑터는 아웃바운드 어댑터를 직접 쓰지 않는다"),
            noClasses().that().resideInAPackage("$base.adapter.out..")
                .should().dependOnClassesThat().resideInAPackage("$base.adapter.in..")
                .because("아웃바운드 어댑터는 인바운드 어댑터를 보지 않는다"),
            noClasses().that().resideInAPackage("$base.adapter..")
                .should().dependOnClassesThat().resideInAnyPackage("$base.application.service..", "$base.application.saga.orchestrator..")
                .because("어댑터는 유스케이스 구현이 아니라 포트를 본다"),
            noClasses().that().resideOutsideOfPackage("$base.adapter..")
                .should().dependOnClassesThat().resideInAPackage("$PLATFORM..")
                .because("infrastructure를 쓰는 것은 어댑터뿐이다"),
            noClasses()
                .should().dependOnClassesThat(privateTypesOfOtherServices)
                .because("다른 BC에서는 contract만 import한다"),
            classes().that().haveNameMatching(".*\\.(Order|Stock|Payment|Shipment|OrderStockRecord|OrderPaymentCancellation)Repository")
                .should().resideInAPackage("$base.application.port.out..")
                .because("Aggregate Repository는 application의 output port에 둔다"),
            noClasses().that().resideInAPackage("$base.application..")
                .should().dependOnClassesThat().haveNameMatching(".*\\.(Inbox.*|ProcessedEvent.*|Idempotency.*)")
                .because("Inbox와 HTTP 멱등성은 infrastructure와 인바운드 어댑터에서 처리한다"),
            noClasses().that().resideInAPackage("$base.application..")
                .should().haveSimpleNameEndingWith("Policy")
                .because("보상 정책은 domain에 둔다"),
            noClasses().that().resideOutsideOfPackage("$base.adapter.in.web..")
                .should().dependOnClassesThat().resideInAPackage("$PLATFORM.idempotency..")
                .because("HTTP 멱등성은 웹 인바운드 어댑터에만 적용한다"),
            noClasses().that().resideInAnyPackage("$base.adapter.in.web.request..", "$base.adapter.in.web.response..")
                .should().dependOnClassesThat().resideInAnyPackage("$base.application..", "$base.domain..")
                .because("웹 DTO 변환은 mapper가 담당한다"),
        )

        val containmentRules = technologyHomes.map { (technology, homes) ->
            noClasses().that().resideOutsideOfPackages(*homes.map { "$base.$it.." }.toTypedArray())
                .should().dependOnClassesThat().resideInAnyPackage(*technology.packages.toTypedArray())
                .because("${technology.name}은(는) ${homes.joinToString()} 밖으로 나가지 않는다")
        }

        return (layerRules + containmentRules).map { it.allowEmptyShould(true) }
    }

    private fun assertLayersImported(service: String, classes: JavaClasses) {
        listOf("domain", "application", "adapter.in", "adapter.out").forEach { layer ->
            val prefix = "${basePackage(service)}.$layer."
            assertTrue(classes.any { it.packageName.startsWith(prefix) || "${it.packageName}." == prefix }, "$prefix 아래에 클래스가 없다")
        }
    }

    private fun basePackage(service: String) = "com.example.saga.$service"

    private data class Technology(val name: String, val packages: List<String>)

    companion object {
        private const val PLATFORM = "com.example.saga.infrastructure"

        private val technologyHomes = mapOf(
            Technology("JPA", listOf("jakarta.persistence..", "org.hibernate..", "org.springframework.data.."))
                to listOf("adapter.out.persistence"),
            Technology("Kafka", listOf("org.apache.kafka..", "org.springframework.kafka.."))
                to listOf("adapter.in.messaging", "adapter.out.messaging"),
            Technology("웹·HTTP", listOf("org.springframework.web..", "org.springframework.http..", "jakarta.servlet.."))
                to listOf("adapter.in.web", "adapter.out.client"),
            Technology("회로 차단기", listOf("io.github.resilience4j.."))
                to listOf("adapter.out.client"),
            Technology("UUID 생성기", listOf("com.github.f4b6a3.."))
                to listOf("adapter.out.identifier"),
        )

        private val allowedSpringInApplication = setOf(
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional",

            "org.springframework.transaction.annotation.Propagation",
            "org.springframework.transaction.annotation.Isolation",
        )

        private val technicalPrefixes = listOf(
            "com.amazonaws.", "com.fasterxml.jackson.", "tools.jackson.", "com.mongodb.", "com.mysql.", "com.querydsl.",
            "com.zaxxer.", "feign.", "io.lettuce.", "io.micrometer.", "jakarta.persistence.", "jakarta.servlet.",
            "java.io.File", "java.io.RandomAccessFile", "java.net.HttpURLConnection", "java.net.Socket",
            "java.net.URLConnection", "java.net.http.", "java.nio.channels.", "java.nio.file.", "java.sql.",
            "javax.persistence.", "javax.servlet.", "javax.sql.", "okhttp3.", "org.apache.http.", "org.apache.ibatis.",
            "org.apache.kafka.", "org.hibernate.", "org.jooq.", "org.mybatis.", "org.postgresql.",
            "org.springframework.", "reactor.", "redis.clients.", "retrofit2.", "software.amazon.awssdk.",
        )

        private val forbiddenInApplication = object : DescribedPredicate<JavaClass>("application에 허용되지 않은 기술 타입") {
            override fun test(javaClass: JavaClass): Boolean =
                javaClass.name !in allowedSpringInApplication && technicalPrefixes.any { javaClass.name.startsWith(it) }
        }
    }
}
