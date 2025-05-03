package com.michaelpetri.urlshortener.domain.value

import kotlin.io.encoding.ExperimentalEncodingApi

@JvmInline
value class ShortId(
    val value: ULong,
) {
    @OptIn(ExperimentalEncodingApi::class)
    fun encode(): String {
        var num = value
        val sb = StringBuilder()
        while (num > 0u) {
            val remainder = (num % BASE).toInt()
            sb.append(ALPHABET[remainder])
            num /= BASE
        }
        return sb.reverse().toString()
    }

    companion object {
        private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        private val BASE = ALPHABET.length.toULong()

        fun decode(value: String): ShortId {
            var result = 0UL

            for (char in value) {
                val index = ALPHABET.indexOf(char)
                require(index >= 0) { "Invalid character '$char' in Base58 alphabet." }
                result = result * BASE + index.toULong()
            }

            return ShortId(result)
        }
    }
}
