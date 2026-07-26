package dev.tuhkanens.econicscore.event

import org.bukkit.Bukkit
import org.bukkit.event.Event

object EventSuppressor {
    private val suppress = ThreadLocal.withInitial { false }

    fun callEvent(event: Event) {
        if (suppress.get()) return

        suppress.set(true)
        try {
            Bukkit.getPluginManager().callEvent(event)
        } finally {
            suppress.set(false)
        }
    }
}