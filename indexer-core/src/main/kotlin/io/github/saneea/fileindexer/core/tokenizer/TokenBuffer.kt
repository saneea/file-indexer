package io.github.saneea.fileindexer.core.tokenizer

const val TOKEN_SIZE_LIMIT = 32

class TokenBuffer(var listener: TokenizerListener) : AutoCloseable {
    private val charBuffer = StringBuilder()

    fun add(c: Char) {
        charBuffer.append(c)
        if (charBuffer.length >= TOKEN_SIZE_LIMIT) {
            flush()
        }
    }

    fun flush() {
        if (charBuffer.isNotEmpty()) {
            listener(charBuffer.toString())
            charBuffer.clear()
        }
    }

    override fun close() {
        flush()
    }
}