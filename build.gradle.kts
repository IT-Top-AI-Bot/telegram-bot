plugins {
    java
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.5"
}

group = "com.aquadev"
version = "0.0.1-SNAPSHOT"
description = "it-top-ai-telegram-bot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val telegramBotsVersion = "9.4.0"
val springCloudVersion by extra("2025.0.1")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-all")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-springboot-webhook-starter:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("it-top-ai-telegram-bot")
            buildArgs.addAll(
                "--no-fallback",
                "-H:+AddAllCharsets",
                "-H:IncludeLocales=ru,en"
            )
        }
    }
    toolchainDetection.set(false)
}

tasks.named<JavaExec>("processAot") {
    jvmArgs("-Dspring.profiles.active=kubernetes")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
