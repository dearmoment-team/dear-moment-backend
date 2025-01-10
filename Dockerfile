# Dockerfile
FROM azul/zulu-openjdk-alpine:17
WORKDIR /app

RUN apk add --no-cache curl unzip zip bash

# sdkman & gradle 설치
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install gradle"

COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# 소스 및 Gradle 파일 복사
COPY . .

# Gradle 의존성 사전 다운로드(옵션)
RUN bash -c "source /root/.sdkman/bin/sdkman-init.sh && ./gradlew dependencies"

# wait-for-it.sh에서 DB 기동 기다렸다가 bootRun
ENTRYPOINT ["sh", "-c", "/app/wait-for-it.sh db:1521 -- bash -c 'source /root/.sdkman/bin/sdkman-init.sh && ./gradlew bootRun'"]
