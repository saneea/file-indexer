package io.github.saneea.fileindexer.core.tokenizer

import java.io.Reader

class WhitespaceTokenizer : Tokenizer {

    override fun parse(reader: Reader, listener: TokenizerListener) {

        TokenBuffer(listener).use { tokenBuffer ->
            while (true) {
                val codePoint = reader.read()
                if (codePoint == -1) {
                    break
                }

                val char = codePoint.toChar()

                if (char.isWhitespace()) {
                    tokenBuffer.flush()
                } else {
                    tokenBuffer.add(char)
                }
            }
        }
    }

}
