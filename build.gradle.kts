import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.8.22" // 안정적인 Kotlin 버전
    id("org.springframework.boot") version "3.3.5"
    kotlin("plugin.spring") version "1.8.22"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // ktlint 플러그인
    id("org.sonarqube") version "5.1.0.4882" // sonarqube
    id("com.epages.restdocs-api-spec") version "0.18.4" // rest doc + openapi3
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

    // REST Docs & OpenAPI
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.4")

    // Mocking 라이브러리
    testImplementation("io.mockk:mockk:1.13.4")

    // Hibernate Validator (Bean Validation)
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("javax.validation:validation-api:2.0.1.Final")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // H2 Database for Testing
    testImplementation("com.h2database:h2:2.2.220")

    // Spring Boot 테스트 (JUnit 5 지원)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine") // JUnit4 제외
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kr.kro.onboarding.OnBoardingApplicationKt"
    }
}

openapi3 {
    // OpenAPI 설정
    setServer("https://localhost:8080") // 필요 시 여러 환경(Dev/Prod 등) URL을 추가 가능
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "yaml" // or json
}

tasks {
    // Kotlin/JUnit5 세팅
    withType<Test> {
        useJUnitPlatform()
        ktlint {
            verbose.set(true)
        }
    }

    // Swagger 문서 복사
    register<Copy>("copyOasToSwagger") {
        doFirst {
            println("Copying OAS file from: ${layout.buildDirectory.get().asFile}/api-spec/openapi3.yaml")
        }
        // 기존 OAS 파일 삭제
        delete("src/main/resources/static/swagger-ui/openapi3.yaml")
        // 복제할 OAS 파일 지정
        from("${layout.buildDirectory.get().asFile}/api-spec/openapi3.yaml")
        // 타겟 디렉터리로 파일 복제
        into("src/main/resources/static/swagger-ui/.")
        // openapi3 Task가 먼저 실행되도록 설정
        dependsOn("openapi3")
    }

    // 전체 빌드가 끝난 후 Swagger 문서 복사(빌드 산출물 확정)
    build {
        finalizedBy("copyOasToSwagger")
    }

    // ---------- 핵심: 별도 devRun 태스크로 "테스트 → Swagger 복사 → 부트런" 순서 ----------
    register("devRun") {
        group = "application"
        description = "Runs test, copies OAS, then starts Spring Boot."
        dependsOn("test")
        dependsOn("copyOasToSwagger")
        dependsOn("bootRun")
    }
    // ----------------------------------------------------------------------------
}

sonar {
    properties {
        property("sonar.projectKey", "Onboarding-serivce_BE-onboarding")
        property("sonar.organization", "onboarding-serivce")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
