import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.noarg") version "1.9.25"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" // ktlint
    id("org.sonarqube") version "5.1.0.4882" // sonarqube
    id("com.epages.restdocs-api-spec") version "0.19.4" // restdocs + openapi
    id("org.jetbrains.kotlinx.kover") version "0.9.1" // kover
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // Jackson Kotlin (Spring Boot가 버전을 관리하므로 별도 버전 지정 제거)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Hibernate Validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    // OCI Object Storage
    implementation("com.oracle.oci.sdk:oci-java-sdk-common:3.55.3")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:3.55.3")
    implementation("com.oracle.oci.sdk:oci-java-sdk-addons-apache-configurator-jersey3:3.55.3")

    // Oracle JDBC
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    // JUnit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    // H2
    testImplementation("com.h2database:h2:2.2.220")
    // Spring Boot Test (JUnit5)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test")) // junit
    // REST Docs & OpenAPI
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
    // Mockk
    testImplementation("io.mockk:mockk:1.13.4")
    // Kotest Core 모듈 (필수)
    testImplementation("io.kotest:kotest-runner-junit5:5.7.0")
    // 빈 주입 사용 가능
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    // Kotest Assertions (선택적, 다양한 assert 기능 제공)
    testImplementation("io.kotest:kotest-assertions-core:5.7.0")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("koverXmlReport")
    finalizedBy("koverHtmlReport")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kr.kro.onboarding.OnBoardingApplicationKt"
    }
}

// OpenAPI 설정
openapi3 {
    setServer("http://localhost:8080")
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "json"
}

// ktlint 설정
ktlint {
    verbose.set(true)
}

// Swagger 문서 복사 태스크 등록
tasks.register<Copy>("copyOasToSwagger") {
    dependsOn("openapi3") // openapi3 태스크가 먼저 실행되도록 설정

    doFirst {
        val sourceFile = layout.buildDirectory.file("api-spec/openapi3.json").get().asFile
        println("Copying OAS file from: ${sourceFile.path}")
        delete("src/main/resources/static/swagger-ui/openapi3.json")
    }

    from(layout.buildDirectory.file("api-spec/openapi3.json").get().asFile)
    into("src/main/resources/static/swagger-ui/")
}

// build 태스크가 끝난 후 Swagger 문서 복사 태스크 실행
tasks.named("build") {
    finalizedBy("copyOasToSwagger")
}

// bootRun 태스크가 Swagger 문서 복사 태스크에 의존하도록 설정 + 디버그 옵션 적용
tasks.named<BootRun>("bootRun") {
    dependsOn("copyOasToSwagger")

    // JVM 디버그 옵션 설정
    jvmArgs =
        listOf(
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
        )
}

tasks.named("sonar") {
    dependsOn("koverXmlReport")
}

kover {
    reports.verify.rule {
        minBound(75)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Onboarding-serivce_BE-onboarding")
        property("sonar.organization", "onboarding-serivce")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "$projectDir/build/reports/kover/report.xml")
    }
}
