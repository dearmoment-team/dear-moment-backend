FROM azul/zulu-openjdk-alpine:17
WORKDIR /app

RUN apk add --no-cache curl unzip zip bash

# sdkman & gradle 설치
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install gradle"

COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# ──────────────────────────────────────────────────
# 필요한 Gradle 파일만 복사하여 의존성 미리 다운로드
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# Gradle 의존성 사전 다운로드
RUN bash -c "source /root/.sdkman/bin/sdkman-init.sh && ./gradlew dependencies"

# ──────────────────────────────────────────────────
# 실제 소스코드는 docker-compose.yml에서 볼륨 마운트
# (개발 환경이므로 COPY . . 대신 볼륨 마운트를 사용)

# (3) wait-for-it.sh에서 DB 기동 기다린 뒤 -> swagger 생성 후 -> bootRun
ENTRYPOINT ["sh", "-c", "/app/wait-for-it.sh db:1521 -- bash -c 'source /root/.sdkman/bin/sdkman-init.sh && ./gradlew prepareSwagger && ./gradlew bootRun'"]
