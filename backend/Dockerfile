# ---- build stage ----
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# 현재 backend 디렉터리 전체를 컨테이너 /app 으로 복사
COPY . .

# Gradle(전역 설치되어 있음)로 빌드
RUN gradle clean bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75"

# 위에서 빌드된 JAR 복사 (backend 단일 프로젝트라면 build/libs 에 생성됨)
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
