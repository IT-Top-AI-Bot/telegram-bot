# syntax=docker/dockerfile:1.7

############################
# Stage: native compilation
############################
FROM ghcr.io/graalvm/native-image-community:25 AS builder
WORKDIR /build

# Install musl for fully static binary
RUN microdnf install -y musl-gcc musl-devel zlib-static && microdnf clean all

COPY gradlew gradlew.bat* ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew

# Cache dependencies separately from source
COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon -q 2>/dev/null || true

COPY src/ src/
RUN ./gradlew nativeCompile --no-daemon

############################
# Stage: minimal runtime (no system libs needed for static binary)
############################
FROM gcr.io/distroless/static-debian12:nonroot
WORKDIR /application

COPY --from=builder /build/build/native/nativeCompile/it-top-ai-telegram-bot .

EXPOSE 8080

ENTRYPOINT ["/application/it-top-ai-telegram-bot"]
