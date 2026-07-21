package dev.tuhkanens.econicscore.listener

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
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

    private val instance = Main.instance

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        when (EconicsAPI.getAPI<PlayerAPI>().hasPlayer(uuid)) {
            is EconicsResult.Success -> {
                EconicsAPI.getAPI<PlayerAPI>().updatePlayerName(uuid, event.player.name)
            }
            else -> {
                when (val result = EconicsAPI.getAPI<PlayerAPI>().addPlayer(uuid, event.player.name)) {
                    is EconicsResult.Success -> {
                        CurrencyManager.getCurrencies().forEach { (currencyId, data) ->
                            if (data.defaultAmount > BigDecimal.ZERO) {
                                EconicsAPI.getAPI<PlayerCurrencyAPI>().setPlayerCurrency(uuid, currencyId, data.defaultAmount)
                            }
                        }
                    }
                    is EconicsResult.Failure -> instance.logger.severe(result.error)
                    else -> {}
                }
            }
        }
    }

}