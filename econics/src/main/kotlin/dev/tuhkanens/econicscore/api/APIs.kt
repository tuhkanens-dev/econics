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
        EconicsAPI.registerAPI<CurrencyFileAPI>(CurrencyFileImpl())
        EconicsAPI.registerAPI<CurrencyAPI>(CurrencyImpl())
        EconicsAPI.registerAPI<PlayerAPI>(PlayerImpl())
        EconicsAPI.registerAPI<PlayerCurrencyAPI>(PlayerCurrencyImpl())
    }

}