package dev.tuhkanens.econicscore.utils

import java.util.concurrent.CompletableFuture

object EconicsScheduler {

    private val foliaLib = FoliaUtils.getLib()

    fun <T> supplyAsync(block: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        foliaLib.scheduler.runAsync {
            try {
                future.complete(block())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    fun runSync(block: () -> Unit) {
        foliaLib.scheduler.runNextTick { block() }
    }

}