package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.FileIndexerService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.tokenizer.WhitespaceTokenizer
import java.nio.file.Paths

fun main() {

    val tokenizer: Tokenizer = WhitespaceTokenizer()

    FileIndexerService(tokenizer)
        .use {
            it.watchDir(Paths.get("/home/saneea/code/file-indexer/01/tests"))

            Thread.sleep(300000)
        }

}