import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "2.3.21" apply false
    kotlin("plugin.spring") version "2.3.21" apply false
    kotlin("plugin.jpa") version "2.3.21" apply false
    id("org.springframework.boot") version "4.0.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    group = "com.example.saga"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    dependencies {
        if (!project.name.endsWith("-contract")) {
            add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
            add("implementation", "org.springframework.boot:spring-boot-starter-kafka")
            add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
            add("implementation", "org.springframework.boot:spring-boot-starter-jdbc")
            add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
            add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
            add("implementation", "tools.jackson.module:jackson-module-kotlin")
            add("implementation", "com.github.f4b6a3:uuid-creator:6.1.0")
            add("runtimeOnly", "com.mysql:mysql-connector-j")
        }

        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    if (name.endsWith("-service") || name == "infrastructure") {
        val sourceSets = extensions.getByType<SourceSetContainer>()
        val integrationTest = sourceSets.create("integrationTest") {
            compileClasspath += sourceSets["main"].output
            runtimeClasspath += sourceSets["main"].output
        }
        configurations["integrationTestImplementation"].extendsFrom(configurations["testImplementation"])
        configurations["integrationTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

        dependencies {
            add("integrationTestImplementation", "org.springframework.boot:spring-boot-starter-test")
            add("integrationTestImplementation", "org.testcontainers:testcontainers-mysql")
        }

        tasks.register<Test>("integrationTest") {
            description = "실제 MySQL에서 잠금과 경합을 검증한다."
            group = "verification"
            testClassesDirs = integrationTest.output.classesDirs
            classpath = integrationTest.runtimeClasspath
            shouldRunAfter("test")
        }
    }

    tasks.named<Jar>("jar") {
        enabled = project.name.endsWith("-contract")
    }

    tasks.named<BootJar>("bootJar") {
        archiveFileName.set("app.jar")
        enabled = !project.name.endsWith("-contract")
    }
}

project(":infrastructure") {
    tasks.named<BootJar>("bootJar") {
        enabled = false
    }
    tasks.named<Jar>("jar") {
        enabled = true
    }
}

project(":architecture-test") {
    tasks.named<BootJar>("bootJar") {
        enabled = false
    }
}
