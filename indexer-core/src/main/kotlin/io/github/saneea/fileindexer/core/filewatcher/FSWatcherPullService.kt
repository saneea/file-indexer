package io.github.saneea.fileindexer.core.filewatcher

import io.github.saneea.fileindexer.core.common.FSEventKind
import java.io.File
import java.nio.file.Path


typealias FilesDiff = Map<Path, FSEventKind>

private typealias FilesSnapshot = Map<Path, FileInfo>
private typealias MutableFilesSnapshot = MutableMap<Path, FileInfo>

class FSWatcherPullService(
    private val delayBetweenScansMillis: Long,
    private val listener: (FilesDiff) -> Unit
) : AutoCloseable {

    private val backgroundThread = Thread(this::scanForever, "FS Watcher")

    private val observables: MutableSet<Path> = HashSet()

    private var currentFilesSnapshot: FilesSnapshot = emptyMap()

    init {
        backgroundThread.isDaemon = true
        backgroundThread.start()
    }

    override fun close() {
        backgroundThread.interrupt()
        backgroundThread.join()
    }

    fun addObservable(path: Path) =
        synchronized(observables) {
            observables.add(path)
            rescanFiles()
        }

    fun removeObservable(path: Path) =
        synchronized(observables) {
            observables.remove(path)
            rescanFiles()
        }

    private fun scanForever() =
        try {
            while (true) {

                synchronized(observables) {
                    rescanFiles()
                }

                Thread.sleep(delayBetweenScansMillis)
            }
        } catch (ignore: InterruptedException) {
            //it is normal ending of background thread
        }

    private fun rescanFiles() {
        val newFilesSnapshot = observables
            .map(Path::toFile)
            .map(this::findFilesFromDirOrFile)
            .merge()

        val diff = compare(currentFilesSnapshot, newFilesSnapshot)

        currentFilesSnapshot = newFilesSnapshot

        if (diff.isNotEmpty()) {
            listener(diff)
        }
    }

    private fun compare(
        old: FilesSnapshot,
        new: FilesSnapshot
    ): FilesDiff {
        val allOldPaths = old.keys
        val allNewPaths = new.keys

        val onlyNewFiles = allNewPaths subtract allOldPaths
        val onlyMissedFiles = allOldPaths subtract allNewPaths
        val maybeChangedFiles = allOldPaths intersect allNewPaths
        val onlyChangedFiles = maybeChangedFiles.filter { old[it] != new[it] }

        val result = HashMap<Path, FSEventKind>()
        result.put(onlyNewFiles to FSEventKind.CREATE)
        result.put(onlyMissedFiles to FSEventKind.DELETE)
        result.put(onlyChangedFiles to FSEventKind.MODIFY)

        return result
    }

    private fun findFilesFromDirOrFile(rootDirOrFile: File) =
        when {
            rootDirOrFile.isFile -> mapOf(fileEntry(rootDirOrFile))
            rootDirOrFile.isDirectory -> findFilesFromDir(rootDirOrFile)
            else -> emptyMap()
        }

    private fun findFilesFromDir(dir: File): FilesSnapshot =
        dir.listFiles()
            ?.filterNotNull()
            ?.map { findFilesFromDirOrFile(it) }
            ?.merge()
            ?: emptyMap()

    private fun Iterable<FilesSnapshot>.merge(): FilesSnapshot {
        val result: MutableFilesSnapshot = HashMap()
        forEach(result::plusAssign)
        return result
    }

    private fun fileEntry(file: File): Pair<Path, FileInfo> =
        file.toPath() to FileInfo(file.lastModified(), file.length())
}

private fun <K, V> MutableMap<K, V>.put(keysAndValue: Pair<Iterable<K>, V>) =
    keysAndValue.first.forEach { this[it] = keysAndValue.second }

private data class FileInfo(
    val time: Long,
    val size: Long
)
