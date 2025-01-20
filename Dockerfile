# Dockerfile
FROM azul/zulu-openjdk-alpine:21
WORKDIR /app

# 필요한 패키지 설치
RUN apk add --no-cache curl unzip zip bash

# Gradle 수동 설치
ENV GRADLE_VERSION=8.12
RUN curl -s -L https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip && \
    mv gradle-${GRADLE_VERSION} /opt/gradle && \
    ln -s /opt/gradle/bin/gradle /usr/bin/gradle && \
    rm gradle-${GRADLE_VERSION}-bin.zip

# wait-for-it.sh 복사 및 실행 권한 부여
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# entrypoint.sh 복사 및 실행 권한 부여
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# 필요한 Gradle 파일만 복사하여 의존성 미리 다운로드
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# Gradle 의존성 사전 다운로드
RUN ./gradlew dependencies

# 디버깅 포트 개방
EXPOSE 8080
EXPOSE 5005

# ENTRYPOINT 설정
ENTRYPOINT ["bash", "/app/entrypoint.sh"]
