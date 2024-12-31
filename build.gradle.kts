import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.8.22" // 안정적인 Kotlin 버전
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring") version "1.8.22"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot 기본 모듈
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Jackson Kotlin 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // Database (Oracle)
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    // REST Docs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.4")

    // REST Assured (REST API 테스트)
    testImplementation("io.rest-assured:rest-assured:5.3.0")

    // Mocking 라이브러리
    testImplementation("io.mockk:mockk:1.13.4")

    // Hibernate Validator (Bean Validation)
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("javax.validation:validation-api:2.0.1.Final")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // 최신 버전 springdoc-openapi-starter-webmvc-ui
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

    // H2 Database for Testing
    testImplementation("com.h2database:h2:2.2.220")

    // Spring Boot 테스트 (JUnit 5 지원 포함)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine") // JUnit 4 제외
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kr.kro.onboarding.OnBoardingApplicationKt"
    }
}

tasks.withType<Test> {
    useJUnitPlatform() // JUnit 5 사용
}
