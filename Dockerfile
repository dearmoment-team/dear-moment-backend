# ----- Stage 1: Build -----
# Gradle 이미지(빌드용)를 사용합니다.
FROM gradle:8.12.0-jdk21 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle .. .
RUN rm -rf /home/gradle/.gradle/caches/jars-9 && ./gradlew clean bootJar --no-daemon --rerun-tasks --stacktrace

# ----- Stage 2: Runtime -----
# 실행용 이미지: Zulu OpenJDK 21
FROM azul/zulu-openjdk:21
WORKDIR /app
# 빌드 단계에서 생성된 jar 파일을 복사합니다.
# (빌드 산출물의 이름은 프로젝트에 따라 다를 수 있으니 실제 파일명에 맞게 수정)
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
# 외부 포트 8080을 노출합니다.
EXPOSE 8080
# 컨테이너 시작 시 JAR 파일을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
