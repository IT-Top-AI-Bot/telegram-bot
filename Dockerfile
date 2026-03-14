# syntax=docker/dockerfile:1.7

############################
# Stage: Native compilation with Azul NIK 25
############################
FROM azul/nik:25-jdk AS builder
WORKDIR /build

# Copy Gradle wrapper and build descriptors first — cached until they change
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# Copy sources and compile native binary
COPY src/ src/
RUN ./gradlew nativeCompile -x test --no-daemon

############################
# Stage: Minimal runtime (no JVM required)
############################
FROM ubuntu:24.04
WORKDIR /application

RUN groupadd -r spring && useradd -r -g spring spring \
    && apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/*

USER spring:spring

COPY --from=builder /build/build/native/nativeCompile/tg-bot ./app

EXPOSE 8080

ENTRYPOINT ["./app", "-Djava.net.preferIPv4Stack=true"]
