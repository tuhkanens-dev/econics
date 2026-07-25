package dev.tuhkanens.econicscore.lib

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.PaperLibraryManager
import dev.tuhkanens.econicscore.Main

class LibraryLoader {

    private val plugin = Main.plugin

    private val libraryManager: LibraryManager = PaperLibraryManager(plugin)

    fun loadLibraries() {
        libraryManager.addMavenCentral()

        libraryManager.loadLibraries(
            Library.builder()
                .groupId("org{}jetbrains{}kotlin")
                .artifactId("kotlin-stdlib")
                .version("2.4.0")
                .build(),
            Library.builder()
                .groupId("org{}jetbrains{}kotlin")
                .artifactId("kotlin-reflect")
                .version("2.4.0")
                .build(),
            Library.builder()
                .groupId("org{}jetbrains{}kotlinx")
                .artifactId("kotlinx-coroutines-core")
                .version("1.11.0")
                .build(),
            Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.49.0.0")
                .relocate("org{}sqlite", "dev{}tuhkanens{}econics{}libs{}sqlite")
                .build(),
            Library.builder()
                .groupId("mysql")
                .artifactId("mysql-connector-java")
                .version("5.1.6")
                .relocate("com{}mysql{}jdbc", "dev{}tuhkanens{}econics{}libs{}mysql")
                .build(),
            Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("7.1.0")
                .relocate("com{}zaxxer{}hikari", "dev{}tuhkanens{}econics{}libs{}hikari")
                .build(),
            Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-yaml")
                .version("4.2.0")
                .relocate("org{}spongepowered{}configurate", "dev{}tuhkanens{}econics{}libs{}configurate")
                .resolveTransitiveDependencies(true)
                .build(),
            Library.builder()
                .groupId("dev{}jorel")
                .artifactId("commandapi-paper-shade")
                .version("11.2.0")
                .relocate("dev{}jorel{}commandapi", "dev{}tuhkanens{}econics{}libs{}commandapi")
                .resolveTransitiveDependencies(true)
                .build(),
            Library.builder()
                .groupId("dev{}jorel")
                .artifactId("commandapi-kotlin-paper")
                .version("11.2.0")
                .relocate("dev{}jorel{}commandapi", "dev{}tuhkanens{}econics{}libs{}commandapi")
                .resolveTransitiveDependencies(true)
                .build(),
        )
    }

}