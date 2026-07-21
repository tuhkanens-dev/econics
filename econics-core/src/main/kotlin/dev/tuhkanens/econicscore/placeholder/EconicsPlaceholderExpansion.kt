package dev.tuhkanens.econicscore.placeholder

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.manager.CurrencyManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.util.*

class EconicsPlaceholderExpansion(private val plugin: Main) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "econics"

    override fun getAuthor(): String = "TuhkanenS"

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null

        val parts = params.split("_")

        return when (parts.size) {
            1 -> {
                val currencyId = parts[0].lowercase()
                getFormattedBalance(player.uniqueId, currencyId)
            }
            2 -> {
                val targetPlayerName = parts[0]
                val currencyId = parts[1].lowercase()

                val targetPlayer = plugin.server.getOfflinePlayer(targetPlayerName)
                getFormattedBalance(targetPlayer.uniqueId, currencyId)
            }
            else -> null
        }
    }

    private fun getFormattedBalance(uuid: UUID, currencyId: String): String? {
        val currencyData = CurrencyManager.getCurrencies()[currencyId] ?: return null
        val api = EconicsAPI.getAPI<PlayerCurrencyAPI>()

        return when (val result = api.getPlayerCurrency(uuid, currencyId)) {
            is EconicsResult.GetSuccess -> EconicsAPI.getAPI<CurrencyFileAPI>().getFormatDecimalPattern(currencyId, result.data)
            is EconicsResult.Failure,
            is EconicsResult.Already,
            is EconicsResult.NotFound,
            is EconicsResult.Success -> EconicsAPI.getAPI<CurrencyFileAPI>().getFormatDecimalPattern(currencyId, currencyData.defaultAmount)
        }
    }
}