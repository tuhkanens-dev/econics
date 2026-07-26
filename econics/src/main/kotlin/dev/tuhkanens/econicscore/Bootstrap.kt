package dev.tuhkanens.econicscore

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import dev.tuhkanens.econicscore.api.APIs
import dev.tuhkanens.econicscore.command.CurrencyCommand
import dev.tuhkanens.econicscore.command.EconicsCommand
import dev.tuhkanens.econicscore.listener.PlayerCurrencyChangeListener
import dev.tuhkanens.econicscore.listener.PlayerJoinListener
import dev.tuhkanens.econicscore.manager.ConfigManager
import dev.tuhkanens.econicscore.manager.CurrencyManager
import dev.tuhkanens.econicscore.manager.DatabaseManager
import dev.tuhkanens.econicscore.manager.MessagesManager
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholder
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class Bootstrap(private val plugin: JavaPlugin) {

    companion object {
        val miniMessage: MiniMessage = MiniMessage.miniMessage()
    }

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

        EconicsPlaceholder.load()
    }

    fun disable() {
        DatabaseManager.disconnect()
        CommandAPI.onDisable()
    }

    private fun registerListeners() {
        plugin.server.pluginManager.apply {
            registerEvents(PlayerJoinListener(), plugin)
            registerEvents(PlayerCurrencyChangeListener(), plugin)
        }
    }

    private fun registerCommands() {
        CurrencyCommand.register()
        EconicsCommand().register()
    }

    private fun createDataFolder() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdir()
        }
    }
}