package dev.tuhkanens.econicscore.placeholder

import dev.tuhkanens.econicscore.Main

object EconicsPlaceholder {

    private val plugin = Main.plugin
    private var hasPlaceholderAPI: Boolean = false

    fun load() {
        if (plugin.server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            try {
                hasPlaceholderAPI = true
                EconicsPlaceholderExpansion.load()
                plugin.logger.info("PlaceholderAPI placeholders registered")
            } catch (e: Exception) {
                plugin.logger.warning("Failed to register PlaceholderAPI placeholders: ${e.message}")
            }
        } else {
            plugin.logger.info("PlaceholderAPI not found, skipping placeholders")
        }
    }

    fun reload() {
        if (hasPlaceholderAPI) {
            EconicsPlaceholderExpansion.reload()
        }
    }

    fun hasPAPI(): Boolean = hasPlaceholderAPI

}