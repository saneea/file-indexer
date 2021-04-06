package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.FileIndexerService

fun main() {

    FileIndexerService()
        .use {
            println(it.info)
        }

}