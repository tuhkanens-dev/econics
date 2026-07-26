package dev.tuhkanens.econicscore.placeholder

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.util.*

object EconicsPlaceholderExpansion : PlaceholderExpansion() {

    private val plugin = Main.plugin

    override fun getIdentifier(): String = "econics"
    override fun getAuthor(): String = "TuhkanenS"
    override fun getVersion(): String = plugin.pluginMeta.version
    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null

        val parts = params.split("_")

        return when (parts.size) {
            1 -> { // %econics_<currency>%
                getBalance(player.uniqueId, parts[0], useFormat = false)
            }
            2 -> when (parts[1]) {
                "format" -> { // %econics_<currency>_format%
                    getBalance(player.uniqueId, parts[0], useFormat = true)
                }
                else -> { // %econics_<player>_<currency>%
                    val target = plugin.server.getOfflinePlayer(parts[0])
                    getBalance(target.uniqueId, parts[1], useFormat = false)
                }
            }
            3 -> { // %econics_<player>_<currency>_format%
                if (parts[2] == "format") {
                    val target = plugin.server.getOfflinePlayer(parts[0])
                    getBalance(target.uniqueId, parts[1], useFormat = true)
                } else {
                    null
                }
            }

            else -> null
        }
    }

    private fun getBalance(uuid: UUID, currencyId: String, useFormat: Boolean): String? {
        val currencyData = EconicsAPI.getAPI<CurrencyFileAPI>().getCurrency(currencyId) ?: return null

        val amount = when (val result = EconicsAPI.getAPI<PlayerCurrencyAPI>().getPlayerCurrency(uuid, currencyId)) {
            is EconicsResult.GetSuccess -> result.data
            else -> currencyData.defaultAmount
        }

        return if (useFormat) {
            EconicsAPI.getAPI<CurrencyFileAPI>().getFormatDecimalPattern(currencyId, amount)
        } else {
            amount.toPlainString()
        }
    }

    fun registerPlaceholders() {
        if (plugin.server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            try {
                if (isRegistered) {
                    unregister()
                }
                register()
                plugin.logger.info("PlaceholderAPI placeholders registered")
            } catch (e: Exception) {
                plugin.logger.warning("Failed to register PlaceholderAPI placeholders: ${e.message}")
            }
        } else {
            plugin.logger.info("PlaceholderAPI not found, skipping placeholders")
        }
    }
}