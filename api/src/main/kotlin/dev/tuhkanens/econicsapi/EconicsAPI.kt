package dev.tuhkanens.econicsapi

import java.util.concurrent.ConcurrentHashMap

object EconicsAPI {

    val registry: ConcurrentHashMap<Class<*>, Any> = ConcurrentHashMap()

    inline fun <reified T : Any> registerAPI(implementation: T) {
        registry[T::class.java] = implementation
    }

    inline fun <reified T : Any> getAPI(): T {
        val implementation: Any = registry[T::class.java]
            ?: throw IllegalStateException("API '${T::class.java.name}' is not registered!")
        return implementation as T
    }

}