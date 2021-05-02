package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.common.FSEventKind
import io.github.saneea.fileindexer.core.filewatcher.FSEvent
import io.github.saneea.fileindexer.core.filewatcher.FSEventsBuffer
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherPullService
import io.github.saneea.fileindexer.core.filewatcher.FilesDiff
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.utils.index.*
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class FileIndexerService(private val tokenizer: Tokenizer) : AutoCloseable {

    private val log = LoggerFactory.getLogger(FileIndexerService::class.java)

    private val fsWatcher = FSWatcherPullService(1000, ::putFSEventsToQueue)

    private val processFSEventsThread = Thread(::readFSEventsFromQueue, "Process FS Events")

    private val fsEventsBuffer = FSEventsBuffer()

    private val fileTokenIndexRef = AtomicReference<FileTokenIndex>(emptyIndexTree())

    override fun close() {
        fsWatcher.close()
        processFSEventsThread.interrupt()
        processFSEventsThread.join()
    }

    init {
        processFSEventsThread.isDaemon = true
        processFSEventsThread.start()
    }

    fun addObservable(dirPath: Path) = fsWatcher.addObservable(dirPath)

    fun removeObservable(filePath: Path) = fsWatcher.removeObservable(filePath)

    fun getFilesForToken(token: String): Set<Path> =
        fileTokenIndexRef.get().selectResult(token.asIterable()) ?: Collections.emptySet()

    private fun putFSEventsToQueue(diff: FilesDiff) {
        for ((path, event) in diff) {
            fsEventsBuffer.addEvent(FSEvent(event, path))
        }
    }

    private fun readFSEventsFromQueue() {
        try {
            while (true) {
                val event = fsEventsBuffer.takeEvent()
                log.info("start process ${event.eventKind} for ${event.path}")
                try {
                    onFSEvent(event.eventKind, event.path)
                    log.info("success finish process ${event.eventKind} for ${event.path}")
                } catch (e: Exception) {
                    log.info("exception during processing ${event.eventKind} for ${event.path}", e)
                }
            }
        } catch (ignore: InterruptedException) {
            //it is expected case
        }
    }

    private fun onFSEvent(event: FSEventKind, path: Path) {
        when (event) {
            FSEventKind.DELETE -> removeFile(path)

            FSEventKind.CREATE -> addFileTokensToIndex(path)

            FSEventKind.MODIFY -> {
                removeFile(path)
                addFileTokensToIndex(path)
            }
        }
    }

    private fun addFileTokensToIndex(path: Path) {
        val tokens = parseFile(path)
        val fileTokenIndexBuilder = IndexTreeNodeBuilder(fileTokenIndexRef.get())
        tokens.forEach { token -> fileTokenIndexBuilder.addFileForToken(token, path) }
        fileTokenIndexRef.set(fileTokenIndexBuilder.build())
    }

    private fun parseFile(path: Path): Set<String> {
        val tokens = HashSet<String>()
        FileInputStream(path.toFile())
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                tokenizer.parse(reader, tokens::add)
            }
        return tokens
    }

    private fun removeFile(path: Path) {
        val fileTokenIndexOrig = fileTokenIndexRef.get()
        val branchesWithFile = fileTokenIndexOrig
            .findBranchesForResult { paths ->
                paths != null && paths.contains(path)
            }

        val fileTokenIndexBuilder = IndexTreeNodeBuilder(fileTokenIndexOrig)

        for (branchSequenceWithFile in branchesWithFile.keys) {
            val treeNodeForFile = fileTokenIndexBuilder.getOrCreateBranch(branchSequenceWithFile)
            treeNodeForFile.nodeResult = treeNodeForFile.nodeResult!! - setOf(path)
        }

        fileTokenIndexRef.set(fileTokenIndexBuilder.build())
    }
}

private fun IndexTreeNodeBuilder<Char, Set<Path>>.addFileForToken(token: String, path: Path) {
    val branchBuilder = getOrCreateBranch(token.asIterable())
    val branchNodeResult = branchBuilder.nodeResult ?: setOf()
    branchBuilder.nodeResult = branchNodeResult + setOf(path)
}
