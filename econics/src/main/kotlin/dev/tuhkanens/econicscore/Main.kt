package dev.tuhkanens.econicscore

import dev.tuhkanens.econicscore.lib.LibraryLoader
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    private lateinit var bootstrap: Bootstrap

    companion object {
        lateinit var plugin: Main
            private set
    }

    override fun onLoad() {
        plugin = this

        try {
            LibraryLoader().loadLibraries()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onEnable() {
        bootstrap = Bootstrap(this)
        bootstrap.load()
        bootstrap.enable()
    }

    override fun onDisable() {
        if (::bootstrap.isInitialized) {
            bootstrap.disable()
        }
    }
}