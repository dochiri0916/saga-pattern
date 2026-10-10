val serviceClasses = listOf("order-service", "inventory-service", "payment-service", "shipping-service")
    .map { rootProject.project(":$it").extensions.getByType<SourceSetContainer>()["main"].output }

dependencies {
    add("testImplementation", "com.tngtech.archunit:archunit:1.4.1")
    serviceClasses.forEach { add("testRuntimeOnly", it) }
}
