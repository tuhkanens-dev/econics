plugins {
    kotlin("jvm") version "2.4.0"
}

group = "dev.tuhkanens"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    build {
        dependsOn(subprojects.map { it.tasks.named("build") })
    }
    clean {
        dependsOn(subprojects.map { it.tasks.named("clean") })
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}