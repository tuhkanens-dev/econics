package dev.tuhkanens.econicscore.api

import dev.tuhkanens.econicsapi.EconicsAPI
import dev.tuhkanens.econicsapi.api.CurrencyAPI
import dev.tuhkanens.econicsapi.api.CurrencyFileAPI
import dev.tuhkanens.econicsapi.api.PlayerAPI
import dev.tuhkanens.econicsapi.api.PlayerCurrencyAPI
import dev.tuhkanens.econicscore.api.implementation.CurrencyFileImpl
import dev.tuhkanens.econicscore.api.implementation.CurrencyImpl
import dev.tuhkanens.econicscore.api.implementation.PlayerCurrencyImpl
import dev.tuhkanens.econicscore.api.implementation.PlayerImpl

class APIs {

    fun register() {
        EconicsAPI.register(CurrencyAPI::class.java, CurrencyImpl())
        EconicsAPI.register(PlayerAPI::class.java, PlayerImpl())
        EconicsAPI.register(PlayerCurrencyAPI::class.java, PlayerCurrencyImpl())
        EconicsAPI.register(CurrencyFileAPI::class.java, CurrencyFileImpl())
    }

}