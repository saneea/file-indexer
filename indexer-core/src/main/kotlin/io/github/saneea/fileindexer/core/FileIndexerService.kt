package io.github.saneea.fileindexer.core

import io.github.saneea.fileindexer.core.filewatcher.FSEventKind
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.tokenizer.TokenizerListener
import java.io.FileInputStream
import java.nio.file.Path

class FileIndexerService(private val tokenizer: Tokenizer) : AutoCloseable {

    private val fsWatcher = FSWatcherService(::onFSEvent)

    override fun close() {
        fsWatcher.close()
    }

    fun watchDir(dirPath: Path) =
        fsWatcher.watchDir(dirPath)

    private fun onFSEvent(event: FSEventKind, path: Path) {

        print("$event: $path")
        when (event) {
            FSEventKind.DELETE -> {
                //TODO Nothing for now
            }

            FSEventKind.CREATE, FSEventKind.MODIFY -> {
                print(", tokens:")
                parseFile(path)
            }
        }
        println()
    }

    private fun parseFile(path: Path) {
        FileInputStream(path.toFile())
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                val tokenizerListener: TokenizerListener = object : TokenizerListener {
                    override fun onToken(token: String) {
                        print(" '$token',")
                    }
                }
                tokenizer.parse(reader, tokenizerListener)
            }
    }

}