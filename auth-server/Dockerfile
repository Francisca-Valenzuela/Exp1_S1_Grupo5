# syntax=docker/dockerfile:1
# ---------- Etapa 1: compilación (Maven + JDK 21) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# La caché de ~/.m2 acelera las compilaciones siguientes
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests clean package

# ---------- Etapa 2: ejecución (solo JRE 21, imagen liviana) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/*.jar app.jar
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 9000
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
