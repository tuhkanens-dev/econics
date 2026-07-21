package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicscore.Main
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object ConfigManager {

    private val instance: Main = Main.instance

    private lateinit var root: ConfigurationNode
    private lateinit var yaml: YamlConfigurationLoader
    private lateinit var file: File

    private const val FILE = "config.yml"

    fun init() {

        file = File("${instance.dataFolder}/$FILE")

        if (!file.exists()) {
            file.parentFile.mkdirs()
            instance.javaClass.getResourceAsStream("/$FILE")?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        this.loadYaml()

    }

    fun get() = root

    fun reload() = loadYaml()

    private fun loadYaml() {
        yaml = YamlConfigurationLoader.builder()
            .path(file.toPath())
            .build()
        root = yaml.load()
    }

}