plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
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
val springCloudVersion by extra("2025.1.1")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-all")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-springboot-webhook-starter:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
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
            imageName.set("tg-bot")
            buildArgs.addAll(
                "--no-fallback",
                "-H:+ReportExceptionStackTraces",
                "-J-Xmx6g"
            )
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
