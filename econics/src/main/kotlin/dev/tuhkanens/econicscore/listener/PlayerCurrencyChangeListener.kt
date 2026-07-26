package dev.tuhkanens.econicscore.listener

import dev.tuhkanens.econicsapi.event.PlayerCurrencyChangeEvent
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholder
import dev.tuhkanens.econicscore.placeholder.EconicsPlaceholderExpansion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerCurrencyChangeListener : Listener {

    @EventHandler
    fun onCurrencyChange(event: PlayerCurrencyChangeEvent) {
        if (EconicsPlaceholder.hasPAPI()) {
            EconicsPlaceholderExpansion.updateCache(event.uniqueId, event.currencyId, event.amount)
        }
    }

}