package io.github.saneea.fileindexer.core.tokenizer

import java.io.Reader

class LettersDigitsTokenizer : Tokenizer {

    override fun parse(reader: Reader, listener: TokenizerListener) {

        TokenBuffer(listener).use { tokenBuffer ->
            while (true) {
                val codePoint = reader.read()
                if (codePoint == -1) {
                    break
                }

                val char = codePoint.toChar()

                if (char.isLetterOrDigit()) {
                    tokenBuffer.add(char)
                } else {
                    tokenBuffer.flush()
                }
            }
        }
    }

}
