package dev.tuhkanens.econicscore.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

object EconicsAsync {

    private val executor = Executors.newFixedThreadPool(
        4,
        ThreadFactoryBuilder()
            .setNameFormat("econics-db-%d")
            .setDaemon(true)
            .build()
    )

    fun <T> supply(block: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(block, executor)
    }

    fun shutdown() {
        executor.shutdown()
    }

    fun <T> runAsync(block: () -> T): T {
        return try {
            supply(block).join()
        } catch (e: Exception) {
            throw e.cause ?: e
        }
    }

}