# Multi-stage build for the Quarkus JVM (fast-jar) package.
# Used by Cloud Run's "deploy from source" continuous deployment (Cloud Build
# auto-detects this Dockerfile at the repo root and builds it directly).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies separately from source changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /work

RUN useradd --system --create-home --shell /bin/false quarkus
COPY --from=build --chown=quarkus:quarkus /workspace/target/quarkus-app/lib/ /work/lib/
COPY --from=build --chown=quarkus:quarkus /workspace/target/quarkus-app/*.jar /work/
COPY --from=build --chown=quarkus:quarkus /workspace/target/quarkus-app/app/ /work/app/
COPY --from=build --chown=quarkus:quarkus /workspace/target/quarkus-app/quarkus/ /work/quarkus/
USER quarkus

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
