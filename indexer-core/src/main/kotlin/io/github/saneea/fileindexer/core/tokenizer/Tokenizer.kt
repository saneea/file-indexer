package io.github.saneea.fileindexer.core.tokenizer

import java.io.Reader

interface Tokenizer {

    fun parse(reader: Reader, listener: TokenizerListener)

}