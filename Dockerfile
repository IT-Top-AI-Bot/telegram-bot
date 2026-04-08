# syntax=docker/dockerfile:1.7

############################
# Stage: native compilation
############################
FROM ghcr.io/graalvm/native-image-community:25 AS builder
WORKDIR /build

COPY gradlew gradlew.bat* ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew

# Cache dependencies separately from source
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon -q 2>/dev/null || true

COPY src/ src/
RUN ./gradlew nativeCompile --no-daemon

############################
# Stage: minimal runtime
############################
FROM debian:13.4-slim
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /application
COPY --from=builder /build/build/native/nativeCompile/telegram-bot .

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["/application/telegram-bot"]
