# ---- build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# 캐시 최적화(필요 시): ./gradlew dependencies
RUN ./gradlew clean bootJar --no-daemon

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75"
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
