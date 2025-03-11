#!/bin/bash
set -e

# DB 준비 대기
/app/wait-for-it.sh db:1521 -t 60 -- echo "DB is up"

# Spring Boot 애플리케이션 시작 (bootRun에만 디버그 옵션 전달)
./gradlew bootRun -PjvmArgs="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
