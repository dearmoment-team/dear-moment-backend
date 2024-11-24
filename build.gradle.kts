plugins {
    kotlin("jvm") version "2.0.21" // Kotlin 플러그인 버전
    id("org.springframework.boot") version "3.3.5" // Spring Boot 플러그인
    kotlin("plugin.spring") version "2.0.21" // Kotlin Spring 플러그인
    id("io.spring.dependency-management") version "1.1.0" // 의존성 관리 플러그인
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // ktlint 플러그인
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // kotlin jackson 역직렬화 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")

    // REST Docs, Mock
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.4")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

openapi3 {
    this.setServer("https://localhost:8080") // list로 넣을 수 있어 각종 환경의 URL을 넣을 수 있음!
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "yaml" // or json
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        ktlint {
            verbose.set(true)
        }
    }

    register<Copy>("copyOasToSwagger") {
        delete("src/main/resources/static/swagger-ui/openapi3.yaml") // 기존 OAS 파일 삭제
        from("${layout.buildDirectory.get().asFile}/api-spec/openapi3.yaml") // 복제할 OAS 파일 지정
        into("src/main/resources/static/swagger-ui/.") // 타겟 디렉터리로 파일 복제
        dependsOn("openapi3") // openapi3 Task가 먼저 실행되도록 설정
    }

    build {
        finalizedBy("copyOasToSwagger") // build 작업 후 copyOasToSwagger 실행
    }
}
