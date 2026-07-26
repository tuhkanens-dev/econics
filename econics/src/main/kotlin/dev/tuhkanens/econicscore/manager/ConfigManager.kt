package dev.tuhkanens.econicscore.manager

import dev.tuhkanens.econicscore.Main
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object ConfigManager {

    private val plugin = Main.plugin

    private lateinit var root: ConfigurationNode
    private lateinit var yaml: YamlConfigurationLoader
    private lateinit var file: File

    private const val FILE_NAME = "config.yml"

    fun init() {
        file = File("${plugin.dataFolder}/$FILE_NAME")

        if (!file.exists()) {
            file.parentFile.mkdirs()
            plugin.saveResource(FILE_NAME, false)
        }

        load()
    }

    fun get() = root
    fun reload() = load()

    private fun load() {
        yaml = YamlConfigurationLoader.builder()
            .path(file.toPath())
            .build()
        root = yaml.load()
    }

}