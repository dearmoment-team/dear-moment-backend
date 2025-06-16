import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.noarg") version "1.9.25"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("org.sonarqube") version "5.1.0.4882"
    id("com.epages.restdocs-api-spec") version "0.19.4"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("io.sentry.jvm.gradle") version "5.2.0"
    id("com.google.cloud.tools.jib") version "3.4.5"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common:$ociSdkVersion")
    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:$ociSdkVersion")
    implementation("com.oracle.oci.sdk:oci-java-sdk-addons-apache-configurator-jersey3:$ociSdkVersion")
    implementation("org.springframework.boot:spring-boot-starter-mail:3.3.0")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    implementation("com.oracle.database.security:oraclepki:23.3.0.23.09")
    implementation("com.oracle.database.security:osdt_core:21.17.0.0")
    implementation("com.oracle.database.security:osdt_cert:21.17.0.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:$jdslVersion")
    implementation("com.linecorp.kotlin-jdsl:hibernate-support:$jdslVersion")
    implementation("io.viascom.nanoid:nanoid:1.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.h2database:h2:2.2.220")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.0")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.0")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:$fixtureMonkeyVersion")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:$fixtureMonkeyVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("koverXmlReport", "koverHtmlReport")
}

ktlint {
    verbose.set(true)
}

tasks.named<BootRun>("bootRun") {
    jvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
}

jib {
    from {
        image = "azul/zulu-openjdk-alpine:21-jre"
    }
    to {
        image = "ghcr.io/dearmoment-team/dear-moment-backend:latest"
    }
    container {
        jvmFlags =
            listOf(
                "-Xms128m",
                "-Xmx256m",
                "-XX:+UseContainerSupport",
                "-XX:MaxRAMPercentage=75.0",
            )
        ports = listOf("8080")
        creationTime = "USE_CURRENT_TIMESTAMP"
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

kover {
    reports.verify.rule {
        minBound(70)
    }
}

springBoot {
    mainClass.set("kr.kro.dearmoment.DearMomentApplicationKt")
}
