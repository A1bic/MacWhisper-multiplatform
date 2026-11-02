ARG JAVA_VERSION=21
ARG ALPINE_VERSION=3.24

FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine-${ALPINE_VERSION} AS build
WORKDIR /src

COPY gradlew settings.gradle.kts gradle.properties ./
COPY gradle gradle
COPY buildSrc buildSrc
COPY server server

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :server:app:dockerLayout --no-daemon -q

ARG JAVA_MODULES=java.base,java.desktop,java.logging,java.management,java.naming,java.security.jgss,java.sql,java.xml,jdk.unsupported,jdk.crypto.ec,jdk.zipfs
RUN jlink --add-modules "$JAVA_MODULES" \
        --strip-debug --no-man-pages --no-header-files --compress=zip-6 \
        --output /jre

FROM alpine:${ALPINE_VERSION}
ENV JAVA_HOME=/opt/java \
    PATH=/opt/java/bin:$PATH \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /jre /opt/java

WORKDIR /app
COPY --from=build /src/server/app/build/docker/libs libs
COPY --from=build /src/server/app/build/docker/app app

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app/app/*:/app/libs/*", "ru.kovalenko.MacWhisperApplicationKt"]
