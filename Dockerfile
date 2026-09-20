# syntax=docker/dockerfile:1
# Compose provides named context "infra" (../minimart-infra).
# Local: docker build --build-context infra=../minimart-infra .

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace/service

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src
COPY --from=infra . /workspace/minimart-infra

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN chmod +x gradlew \
	&& printf '\norg.gradle.java.installations.auto-download=false\n' >> gradle.properties

RUN --mount=type=cache,target=/root/.gradle \
	./gradlew bootJar --no-daemon -x test \
	&& mkdir -p /out \
	&& cp build/libs/*.jar /out/app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home --uid 10001 app
COPY --from=build --chown=app:app /out/app.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
