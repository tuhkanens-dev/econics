package dev.tuhkanens.econicscore.placeholder

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EconicsPlaceholderExpansion : PlaceholderExpansion() {

    private val plugin = Main.plugin

    override fun getIdentifier(): String = "econics"
    override fun getAuthor(): String = "TuhkanenS"
    override fun getVersion(): String = plugin.pluginMeta.version
    override fun persist(): Boolean = true

    private val cache: MutableMap<Pair<UUID, String>, BigDecimal> = ConcurrentHashMap()

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
        val key = uuid to currencyId

        if (!EconicsAPI.getAPI<CurrencyFileAPI>().hasCurrency(currencyId)) {
            return null
        }

        if (!cache.containsKey(key)) {
            EconicsAPI.getAPI<PlayerCurrencyAPI>()
                .getPlayerCurrency(uuid, currencyId)
                .thenAccept { result ->
                    if (result is EconicsResult.GetSuccess) {
                        cache[key] = result.data
                    }
                }
            return BigDecimal.ZERO.toPlainString()
        }

        val amount = cache[key] ?: BigDecimal.ZERO

        return if (useFormat) {
            EconicsAPI.getAPI<CurrencyFileAPI>().getFormatDecimalPattern(currencyId, amount)
                ?: amount.toPlainString()
        } else {
            amount.toPlainString()
        }
    }

    fun reload() {
        if (isRegistered) {
            unregister()
        }
        register()
    }

    fun updateCache(uuid: UUID, currencyId: String, newAmount: BigDecimal)
    {
        cache[uuid to currencyId] = newAmount
    }
}