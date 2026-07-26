package dev.tuhkanens.econicscore

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import dev.tuhkanens.econicscore.api.APIs
import dev.tuhkanens.econicscore.command.CurrencyCommand
import dev.tuhkanens.econicscore.command.EconicsCommand
import dev.tuhkanens.econicscore.listener.PlayerJoinListener
import dev.tuhkanens.econicscore.manager.ConfigManager
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.DatabaseManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholderExpansion
import dev.tuhkanens.econicscore.utils.EconicsAsync

class Bootstrap(private val plugin: Main) {

    fun load() {
        CommandAPI.onLoad(CommandAPIPaperConfig(plugin))
    }

    fun enable() {
        CommandAPI.onEnable()

        createDataFolder()

        ConfigManager.init()
        MessagesManager.init()
        DatabaseManager.connect()

        APIs().register()

        CurrencyManager.load()

        registerListeners()
        registerCommands()

        EconicsPlaceholderExpansion.registerPlaceholders()
    }

    fun disable() {
        DatabaseManager.disconnect()
        CommandAPI.onDisable()
        EconicsAsync.shutdown()
    }

    private fun registerListeners() {
        plugin.server.pluginManager.registerEvents(PlayerJoinListener(), plugin)
    }

    private fun registerCommands() {
        EconicsCommand().register()
        CurrencyCommand.register()
    }

    private fun createDataFolder() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdir()
        }
    }
}