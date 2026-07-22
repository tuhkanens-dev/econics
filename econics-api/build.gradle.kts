plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "dev.tuhkanens.econicsapi"
version = "1.0.0"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}