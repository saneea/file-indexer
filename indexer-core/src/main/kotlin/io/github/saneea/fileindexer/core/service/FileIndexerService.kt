package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.filewatcher.FSEventKind
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path

class FileIndexerService(private val tokenizer: Tokenizer) : AutoCloseable {

    private val fsWatcher = FSWatcherService(::onFSEvent)

    private val tokensToFiles = TokensToFilesMap()

    override fun close() = fsWatcher.close()

    fun registerDir(dirPath: Path) = fsWatcher.registerDir(dirPath)

    fun unregisterDir(dirPath: Path) = fsWatcher.unregisterDir(dirPath)

    fun registerFile(filePath: Path) = fsWatcher.registerFile(filePath)

    fun unregisterFile(filePath: Path) = fsWatcher.unregisterFile(filePath)

    fun getFilesForToken(token: String) = tokensToFiles.getFilesForToken(token)

    private fun onFSEvent(event: FSEventKind, path: Path) {
        when (event) {
            FSEventKind.DELETE -> tokensToFiles.removeFile(path)

            FSEventKind.CREATE, FSEventKind.MODIFY -> {
                tokensToFiles.removeFile(path)
                parseFile(path)
            }

            FSEventKind.START_WATCH_DIR -> addFilesFromDir(path)

            FSEventKind.STOP_WATCH_DIR -> removeFilesFromDir(path)

            FSEventKind.START_WATCH_FILE -> parseFile(path)

            FSEventKind.STOP_WATCH_FILE -> tokensToFiles.removeFile(path)
        }
    }

    private fun removeFilesFromDir(dirPath: Path) =
        tokensToFiles.removeFilesFromDir(dirPath)

    private fun addFilesFromDir(dirPath: Path) {
        Files.walk(dirPath, 1).use {
            it
                .filter(Files::isRegularFile)
                .forEach(this::parseFile)
        }
    }

    private fun parseFile(path: Path) {
        FileInputStream(path.toFile())
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                tokenizer.parse(reader) { token: String ->
                    tokensToFiles.addFileForToken(token, path)
                }
            }
    }

}