package io.github.saneea.fileindexer.core.tokenizer

import java.io.Reader

typealias TokenizerListener = (String) -> Unit

interface Tokenizer {

    fun parse(reader: Reader, listener: TokenizerListener)

}