dependencies {
    implementation(project(":infrastructure"))
    implementation(project(":order-contract"))
    implementation(project(":inventory-contract"))
    implementation(project(":payment-contract"))
    implementation(project(":shipping-contract"))

    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")
}
