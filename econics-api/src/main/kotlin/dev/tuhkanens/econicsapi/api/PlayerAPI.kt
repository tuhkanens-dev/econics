package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.result.EconicsResult
import java.util.UUID

interface PlayerAPI {
    fun getPlayers(): EconicsResult<List<UUID>>
    fun getLocalPlayers(): EconicsResult<List<UUID>>
    fun addPlayer(uuid: UUID, playerName: String): EconicsResult<Nothing>
    fun removePlayer(uuid: UUID): EconicsResult<Nothing>
    fun updatePlayerName(uuid: UUID, newPlayerName: String): EconicsResult<Nothing>
    fun ensurePlayer(uuid: UUID, playerName: String): EconicsResult<Nothing>
    fun hasPlayer(uuid: UUID): EconicsResult<Nothing>
    fun hasLocalPlayer(uuid: UUID): EconicsResult<Nothing>
}