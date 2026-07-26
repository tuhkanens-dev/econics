package dev.tuhkanens.econicscore.listener

import dev.tuhkanens.econicscore.Main
import dev.tuhkanens.econicscore.manager.CurrencyManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        CurrencyManager.setPlayerDefaultCurrencies(event.player)
    }

}