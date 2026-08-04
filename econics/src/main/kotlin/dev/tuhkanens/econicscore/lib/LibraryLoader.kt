package dev.tuhkanens.econicscore.lib

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.PaperLibraryManager
import com.alessiodp.libby.relocation.Relocation
import dev.tuhkanens.econicscore.Main

class LibraryLoader {

    private val plugin = Main.plugin

    private val libraryManager: LibraryManager = PaperLibraryManager(plugin)

    fun loadLibraries() {
        libraryManager.addMavenCentral()

        libraryManager.loadLibraries(
            // sqlite
            Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.49.0.0")
                .build(),

            // mysql
            Library.builder()
                .groupId("com.mysql")
                .artifactId("mysql-connector-j")
                .version("9.7.0")
                .relocate(
                    Relocation.builder()
                        .pattern("com{}mysql{}jdbc")
                        .relocatedPattern("dev{}tuhkanens{}econics{}libs{}mysql")
                        .build()
                )
                .build(),

            // hikari
            Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("7.1.0")
                .relocate(
                    Relocation.builder()
                        .pattern("com{}zaxxer{}hikari")
                        .relocatedPattern("dev{}tuhkanens{}econics{}libs{}hikari")
                        .build()
                )
                .build(),

            // configurate-yaml
            Library.builder()
                .groupId("org{}spongepowered")
                .artifactId("configurate-yaml")
                .version("4.2.0")
                .relocate(
                    Relocation.builder()
                        .pattern("org{}spongepowered{}configurate")
                        .relocatedPattern("dev{}tuhkanens{}econics{}libs{}configurate")
                        .build()
                )
                .resolveTransitiveDependencies(true)
                .build(),

            // commandapi paper shade
            Library.builder()
                .groupId("dev{}jorel")
                .artifactId("commandapi-paper-shade")
                .version("11.2.0")
                .relocate(
                    Relocation.builder()
                        .pattern("dev{}jorel{}commandapi")
                        .relocatedPattern("dev{}tuhkanens{}econics{}libs{}commandapi")
                        .build()
                )
                .build(),

            // commandapi kotlin paper
            Library.builder()
                .groupId("dev{}jorel")
                .artifactId("commandapi-kotlin-paper")
                .version("11.2.0")
                .relocate(
                    Relocation.builder()
                        .pattern("dev{}jorel{}commandapi")
                        .relocatedPattern("dev{}tuhkanens{}econics{}libs{}commandapi")
                        .build()
                )
                .build(),
        )
    }
}