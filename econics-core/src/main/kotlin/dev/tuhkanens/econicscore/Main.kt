package dev.tuhkanens.econicscore

import dev.tuhkanens.econicscore.lib.LibraryLoader
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    private lateinit var bootstrap: Bootstrap

    companion object {
        lateinit var instance: Main
            private set
    }

    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    override fun onLoad() {
        instance = this

        LibraryLoader().loadLibraries()

        bootstrap = Bootstrap(instance)
        bootstrap.load()
    }

    override fun onEnable() {
        bootstrap.enable()
    }

    override fun onDisable() {
        bootstrap.disable()
    }
}