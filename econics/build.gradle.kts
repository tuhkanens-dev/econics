plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.tuhkanens.econicscore"
version = "2.2.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "helpchat"
    }
    maven("https://repo.tcoded.com/releases") {
        name = "tcoded-releases"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation("net.flectone:libby-core:2.0.0")
    implementation("net.flectone:libby-paper:2.0.0")
    implementation("net.flectone:libby-bukkit:2.0.0")

    implementation("org.jetbrains.exposed:exposed-migration-jdbc:1.3.1")
    implementation("org.jetbrains.exposed:exposed-core:1.3.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    implementation("com.tcoded:FoliaLib:0.5.1")

    compileOnly("org.xerial:sqlite-jdbc:3.49.0.0")
    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("com.zaxxer:HikariCP:7.1.0")

    compileOnly("org.spongepowered:configurate-yaml:4.2.0")

    compileOnly("dev.jorel:commandapi-paper-shade:11.2.0")
    compileOnly("dev.jorel:commandapi-kotlin-paper:11.2.0")

    compileOnly("me.clip:placeholderapi:2.12.3")

    implementation(project(":api"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        mergeServiceFiles()

        relocate("com.alessiodp.libby", "dev.tuhkanens.econics.libs.libby")
        relocate("org.jetbrains.exposed", "dev.tuhkanens.econics.libs.exposed")
        relocate("dev.jorel.commandapi", "dev.tuhkanens.econics.libs.commandapi")
        relocate("org.spongepowered.configurate", "dev.tuhkanens.econics.libs.configurate")
        relocate("com.zaxxer.hikari", "dev.tuhkanens.econics.libs.hikari")
        relocate("com.tcoded.folialib", "dev.tuhkanens.econics.folialib")
    }
    build {
        dependsOn("shadowJar")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}