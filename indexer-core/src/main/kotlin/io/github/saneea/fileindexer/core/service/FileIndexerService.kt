package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.common.FSEventKind
import io.github.saneea.fileindexer.core.filewatcher.FSWatcherPullService
import io.github.saneea.fileindexer.core.filewatcher.FilesDiff
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.utils.index.*
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicReference

typealias FileTokenIndex = IndexTreeNode<Char, Set<Path>>

class FileIndexerService(private val tokenizer: Tokenizer) : AutoCloseable {

    private val fsWatcher = FSWatcherPullService(1000, ::onFSEvents)

    private val fileTokenIndexRef = AtomicReference<FileTokenIndex>(emptyIndexTree())

    override fun close() = fsWatcher.close()

    fun addObservable(dirPath: Path) = fsWatcher.addObservable(dirPath)

    fun removeObservable(filePath: Path) = fsWatcher.removeObservable(filePath)

    fun getFilesForToken(token: String): Set<Path> =
        fileTokenIndexRef.get().selectResult(token.asIterable()) ?: Collections.emptySet()

    private fun onFSEvents(diff: FilesDiff) {
        for ((path, event) in diff) {
            onFSEvent(event, path)
        }
    }

    private fun onFSEvent(event: FSEventKind, path: Path) {
        when (event) {
            FSEventKind.DELETE -> removeFile(path)

            FSEventKind.CREATE, FSEventKind.MODIFY -> {
                removeFile(path)
                parseFile(path)
            }
        }
    }

    private fun parseFile(path: Path) {

        val fileTokenIndexBuilder = IndexTreeNodeBuilder(fileTokenIndexRef.get())

        FileInputStream(path.toFile())
            .bufferedReader(Charsets.UTF_8)
            .use { reader ->
                tokenizer.parse(reader) { token: String ->
                    fileTokenIndexBuilder.addFileForToken(token, path)
                }
            }

        fileTokenIndexRef.set(fileTokenIndexBuilder.build())
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
