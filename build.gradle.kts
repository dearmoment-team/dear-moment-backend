plugins {
	kotlin("jvm") version "2.0.21"   // Kotlin 플러그인 버전
	id("org.springframework.boot") version "3.3.5"   // Spring Boot 플러그인
	kotlin("plugin.spring") version "2.0.21"   // Kotlin Spring 플러그인
	id("io.spring.dependency-management") version "1.1.0"   // 의존성 관리 플러그인
	id("org.sonarqube") version "5.1.0.4882"        // sonarqube
}


group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}



// sonarqube
sonar {
  properties {
    property("sonar.projectKey", "Onboarding-serivce_BE-onboarding")
    property("sonar.organization", "onboarding-serivce")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}
