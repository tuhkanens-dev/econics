package dev.tuhkanens.econicsapi.result

sealed interface EconicsResult<out T> {
    data class GetSuccess<T>(val data: T): EconicsResult<T>
    data class Failure(val error: String): EconicsResult<Nothing>

    object Already : EconicsResult<Nothing>
    object Success : EconicsResult<Nothing>
    object NotFound : EconicsResult<Nothing>
}