package dev.tuhkanens.econicsapi.api

import dev.tuhkanens.econicsapi.result.EconicsResult
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PlayerAPI {
    fun getPlayers(): CompletableFuture<EconicsResult<List<UUID>>>
    fun getLocalPlayers(): CompletableFuture<EconicsResult<List<UUID>>>
    fun addPlayer(uuid: UUID, playerName: String): CompletableFuture<EconicsResult<Nothing>>
    fun removePlayer(uuid: UUID): CompletableFuture<EconicsResult<Nothing>>
    fun updatePlayerName(uuid: UUID, newPlayerName: String): CompletableFuture<EconicsResult<Nothing>>
    fun ensurePlayer(uuid: UUID, playerName: String): CompletableFuture<EconicsResult<Nothing>>
    fun hasPlayer(uuid: UUID): CompletableFuture<EconicsResult<Nothing>>
    fun hasLocalPlayer(uuid: UUID): CompletableFuture<EconicsResult<Nothing>>
}