#!/bin/bash
set -e

# DB가 준비될 때까지 대기
/app/wait-for-it.sh db:1521 -- echo "DB is up"

# Gradle 태스크 실행: OpenAPI 생성 및 Swagger 복사
./gradlew openapi3 copyOasToSwagger

# Spring Boot 애플리케이션 시작
./gradlew bootRun
