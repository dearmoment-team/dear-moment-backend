import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    kotlin("plugin.spring") version "1.9.25"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" // ktlint
    id("org.sonarqube") version "5.1.0.4882" // sonarqube
    id("com.epages.restdocs-api-spec") version "0.19.4" // restdocs + openapi
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
    // Oracle JDBC
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    // REST Docs & OpenAPI
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4") // 플러그인 버전에 맞게 업데이트

    // Mockk
    testImplementation("io.mockk:mockk:1.13.4")
    // Hibernate Validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    // JUnit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    // H2
    testImplementation("com.h2database:h2:2.2.220")
    // Spring Boot Test (JUnit5)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
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
    format = "json" // 또는 "json"
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

// bootRun 태스크가 Swagger 문서 복사 태스크에 의존하도록 설정
tasks.named("bootRun") {
    dependsOn("copyOasToSwagger")
}

sonar {
    properties {
        property("sonar.projectKey", "Onboarding-serivce_BE-onboarding")
        property("sonar.organization", "onboarding-serivce")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
