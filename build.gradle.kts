plugins {
    java
    jacoco
    id("org.sonarqube") version "7.2.3.7755"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.5"
}

jacoco {
    toolVersion = "0.8.14"
}

sourceSets {
    create("local") {
        java.srcDir("src/local/java")
        compileClasspath += sourceSets.main.get().output + configurations["compileClasspath"]
        runtimeClasspath += sourceSets.main.get().output + configurations["runtimeClasspath"]
    }
}

sonar {
    properties {
        property("sonar.projectKey", "IT-Top-AI-Bot_telegram-bot")
        property("sonar.organization", "it-top-ai-bot")
    }
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

val telegramBotsVersion = "9.5.0"
val springCloudVersion by extra("2025.1.1")

val localImplementation by configurations.getting

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-all")
    implementation("org.telegram:telegrambots-springboot-webhook-starter:$telegramBotsVersion")
    implementation("org.telegram:telegrambots-client:$telegramBotsVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    localImplementation("org.telegram:telegrambots-springboot-longpolling-starter:$telegramBotsVersion")
    "localCompileOnly"("org.projectlombok:lombok")
    "localAnnotationProcessor"("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.register<JavaExec>("bootRunLocal") {
    group = "application"
    description = "Run the application locally with long polling support"
    classpath = sourceSets["local"].runtimeClasspath
    mainClass.set("com.aquadev.ittopaitelegrambot.ItTopAiTelegramBotApplication")
}
