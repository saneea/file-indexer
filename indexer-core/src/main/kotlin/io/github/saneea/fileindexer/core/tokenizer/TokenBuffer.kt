package io.github.saneea.fileindexer.core.tokenizer

class TokenBuffer(var listener: TokenizerListener) : AutoCloseable {
    private val charBuffer = StringBuilder()

    fun add(c: Char) {
        charBuffer.append(c)
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