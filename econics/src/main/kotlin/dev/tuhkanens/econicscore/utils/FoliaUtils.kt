package dev.tuhkanens.econicscore.utils

import com.tcoded.folialib.FoliaLib
import dev.tuhkanens.econicscore.Main

object FoliaUtils {

    private val plugin = Main.plugin
    private val foliaLib: FoliaLib by lazy { FoliaLib(plugin) }

    fun getLib(): FoliaLib = foliaLib
    fun getUnsupportedCommandsMessage(): String {
        return "Cannot reload currency commands: Folia does not support registering or unregistering commands after the server has started. Please restart the server to apply changes to your commands."
    }

}