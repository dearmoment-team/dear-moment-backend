#!/bin/bash
set -e

# DB 준비 대기
/app/wait-for-it.sh deardb_high:1522 -t 60 -- echo "DB is up"

# Gradle 태스크 실행: OpenAPI 생성 및 Swagger 복사
./gradlew openapi3 copyOasToSwagger

# Spring Boot 애플리케이션 시작 (bootRun에만 디버그 옵션 전달)
./gradlew bootRun -PjvmArgs="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

