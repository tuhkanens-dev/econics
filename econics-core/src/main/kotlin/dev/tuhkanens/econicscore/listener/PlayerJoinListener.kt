package dev.tuhkanens.econicscore.listener

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicsapi.result.EconicsResult
import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.manager.CurrencyManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.math.BigDecimal

class PlayerJoinListener : Listener {

    private val plugin = Main.plugin

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        val playerAPI = EconicsAPI.getAPI<PlayerAPI>()

        when (val result = playerAPI.ensurePlayer(uuid, event.player.name)) {
            is EconicsResult.Success -> {
                CurrencyManager.getCurrencies().forEach { (currencyId, data) ->
                    if (data.defaultAmount > BigDecimal.ZERO) {
                        EconicsAPI.getAPI<PlayerCurrencyAPI>().setPlayerCurrency(uuid, currencyId, data.defaultAmount)
                    }
                }
            }
            is EconicsResult.Failure -> plugin.logger.severe(result.error)
            else -> {}
        }
    }

}