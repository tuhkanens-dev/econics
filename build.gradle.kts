import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.serialization") version "2.4.0" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
    `maven-publish`
}

allprojects {
    group = "dev.tuhkanens.econics"
    version = "2.2.2"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.tcoded.com/releases")
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }
}

tasks {
    named("build") {
        dependsOn(subprojects.map { it.tasks.named("build") })
    }
    named("clean") {
        dependsOn(subprojects.map { it.tasks.named("clean") })
    }
}