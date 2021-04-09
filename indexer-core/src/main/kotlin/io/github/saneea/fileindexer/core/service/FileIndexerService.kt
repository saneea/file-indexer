package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.filewatcher.FSEventKind
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.tokenizer.TokenizerListener
import java.io.FileInputStream
import java.nio.file.Path

class FileIndexerService(private val tokenizer: Tokenizer) : AutoCloseable {

    private val fsWatcher = FSWatcherService(::onFSEvent)

    private val tokensToFiles = TokensToFilesMap()

    override fun close() {
        fsWatcher.close()
    }

    fun watchDir(dirPath: Path) =
        fsWatcher.watchDir(dirPath)

    fun getFilesForToken(token: String) =
        tokensToFiles.getFilesForToken(token)

    private fun onFSEvent(event: FSEventKind, path: Path) {
        when (event) {
            FSEventKind.DELETE -> tokensToFiles.removeFile(path)

            FSEventKind.CREATE, FSEventKind.MODIFY -> {
                tokensToFiles.removeFile(path)
                parseFile(path)
            }
        }
    }

    private fun parseFile(path: Path) {
        FileInputStream(path.toFile())
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                val tokenizerListener: TokenizerListener = object : TokenizerListener {
                    override fun onToken(token: String) {
                        tokensToFiles.addFileForToken(token, path)
                    }
                }
                tokenizer.parse(reader, tokenizerListener)
            }
    }

}