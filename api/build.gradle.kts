plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    compileOnly("org.jetbrains.exposed:exposed-core:1.3.1")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:1.3.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
