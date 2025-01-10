import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.8.22"
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring") version "1.8.22"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // ktlint
    id("org.sonarqube") version "5.1.0.4882" // sonarqube
    id("com.epages.restdocs-api-spec") version "0.18.4" // restdocs + openapi
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
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // Jackson Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // Oracle JDBC
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    // REST Docs & OpenAPI
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.4")

    // Mockk
    testImplementation("io.mockk:mockk:1.13.4")

    // Hibernate Validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("javax.validation:validation-api:2.0.1.Final")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // JUnit5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    // H2
    testImplementation("com.h2database:h2:2.2.220")
    // Spring Boot Test (JUnit5)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kr.kro.onboarding.OnBoardingApplicationKt"
    }
}

openapi3 {
    setServer("https://localhost:8080")
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "json" // or json
}

tasks {
    // Kotlin/JUnit5
    withType<Test> {
        useJUnitPlatform()
        ktlint {
            verbose.set(true)
        }
    }

    // Swagger 문서 복사
    register<Copy>("copyOasToSwagger") {
        doFirst {
            println("Copying OAS file from: ${layout.buildDirectory.get().asFile}/api-spec/openapi3.json")
        }
        // 기존 OAS 파일 삭제
        delete("src/main/resources/static/swagger-ui/openapi3.json")
        // 복제할 OAS 파일
        from("${layout.buildDirectory.get().asFile}/api-spec/openapi3.json")
        // 복사될 위치
        into("src/main/resources/static/swagger-ui/.")
        // openapi3 Task 먼저 실행
        dependsOn("openapi3")

        doLast {
            val copiedFile = file("src/main/resources/static/swagger-ui/openapi3.yaml")
            if (copiedFile.exists()) {
                copiedFile.setWritable(true) // 파일을 쓰기 가능으로 설정
            }
        }
    }

    // build가 끝난 후 Swagger 문서 복사
    build {
        finalizedBy("copyOasToSwagger")
    }

    // (1) swagger 준비 태스크: openapi3 → copyOasToSwagger 순서 보장
    register("prepareSwagger") {
        dependsOn("copyOasToSwagger")
    }

    // (2) bootRun은 prepareSwagger 완료 후 실행
    named("bootRun") {
        dependsOn("prepareSwagger")
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Onboarding-serivce_BE-onboarding")
        property("sonar.organization", "onboarding-serivce")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
