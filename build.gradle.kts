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
    id("io.sentry.jvm.gradle") version "5.2.0" // sentry
    id("com.google.cloud.tools.jib") version "3.4.5" // jib 플러그인
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

val ociSdkVersion by extra("3.55.3")
val fixtureMonkeyVersion by extra("1.1.9")
val jdslVersion by extra("3.5.5")

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
    implementation("com.oracle.oci.sdk:oci-java-sdk-common:$ociSdkVersion")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:$ociSdkVersion")
    implementation("com.oracle.oci.sdk:oci-java-sdk-addons-apache-configurator-jersey3:$ociSdkVersion")
    // mail
    implementation("org.springframework.boot:spring-boot-starter-mail:3.3.0")

    // Oracle JDBC
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    implementation("com.oracle.database.security:oraclepki:23.3.0.23.09")
    implementation("com.oracle.database.security:osdt_core:21.17.0.0")
    implementation("com.oracle.database.security:osdt_cert:21.17.0.0")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")

    // JDSL
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:hibernate-support:$jdslVersion")

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
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    // Fixture
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:$fixtureMonkeyVersion")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:$fixtureMonkeyVersion")

    // 코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // 시큐리티
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt:0.9.1") // Spring Json-Web-Token
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    //    compileOnly('org.projectlombok:lombok')

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("koverXmlReport")
    finalizedBy("koverHtmlReport")
}

// ktlint 설정
ktlint {
    verbose.set(true)
}

// bootRun 태스크가 Swagger 문서 복사 태스크에 의존하도록 설정 + 디버그 옵션 적용
tasks.named<BootRun>("bootRun") {
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

// 실제 메인 클래스의 FQCN을 지정합니다.
springBoot {
    mainClass.set("kr.kro.dearmoment.DearMomentApplicationKt")
}
