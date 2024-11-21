plugins {
    kotlin("jvm") version "2.0.21" // Kotlin 플러그인 버전
    id("org.springframework.boot") version "3.3.5" // Spring Boot 플러그인
    kotlin("plugin.spring") version "2.0.21" // Kotlin Spring 플러그인
    id("io.spring.dependency-management") version "1.1.0" // 의존성 관리 플러그인
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // ktlint 플러그인
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // kotlin jackson 역직렬화 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    ktlint {
        verbose.set(true)
    }
}
