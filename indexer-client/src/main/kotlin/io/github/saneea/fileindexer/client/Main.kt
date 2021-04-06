package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.FileIndexerService
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherService
import java.nio.file.Paths

fun main() {

    FileIndexerService()
        .use {
            println(it.info)
        }

    FSWatcherService().use {

        it.watchDir(Paths.get("/home/saneea/code/file-indexer/01/tests"))

        Thread.sleep(300000)

    }
}