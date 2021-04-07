package io.github.saneea.fileindexer.core.tokenizer

@FunctionalInterface
interface TokenizerListener {

    fun onToken(token: String)

}