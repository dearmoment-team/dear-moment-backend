FROM azul/zulu-openjdk-alpine:17
WORKDIR /app

RUN apk add --no-cache curl unzip zip bash

# sdkman & gradle 설치
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install gradle"

COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# ──────────────────────────────────────────────────
# 개발 환경이라면, 굳이 전부 복사하지 않아도 됩니다.
# 예: 빌드 스크립트(Gradle)만 복사 후 의존성 사전 다운로드
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# Gradle 의존성 사전 다운로드(옵션)
RUN bash -c "source /root/.sdkman/bin/sdkman-init.sh && ./gradlew dependencies"

# ──────────────────────────────────────────────────
# 나머지 소스는 docker-compose.yml에서 볼륨 마운트로 들어올 예정
# 개발 환경 기준이라면 COPY . . 로 전부 복사해도 결국 마운트가 덮어쓰게 됨.
# 필요하다면 아래 주석을 풀어서 활용할 수도 있습니다.
# COPY . .

# wait-for-it.sh에서 DB 기동 기다렸다가 bootRun
ENTRYPOINT ["sh", "-c", "/app/wait-for-it.sh db:1521 -- bash -c 'source /root/.sdkman/bin/sdkman-init.sh && ./gradlew bootRun'"]
