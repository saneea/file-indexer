package io.github.saneea.fileindexer.core.filewatcher

import io.github.saneea.fileindexer.core.utils.Cache
import io.github.saneea.fileindexer.core.utils.removeIf
import java.nio.file.*
import java.util.concurrent.ConcurrentHashMap

enum class FSEventKind {
    CREATE, DELETE, MODIFY, START_WATCH_DIR, STOP_WATCH_DIR, START_WATCH_FILE, STOP_WATCH_FILE
}

interface WatchDirFilter {
    val isAllFiles: Boolean
    fun isFileAllowed(filePath: Path): Boolean
}

class AllFilesWatchDirFilter : WatchDirFilter {
    override val isAllFiles get() = true
    override fun isFileAllowed(filePath: Path) = true
}

class OneFileWatchDirFilter(val allowedFilePath: Path) : WatchDirFilter {
    override val isAllFiles get() = false
    override fun isFileAllowed(filePath: Path) = allowedFilePath == filePath
}

data class WatchEntry(
    val isFile: Boolean,
    val path: Path
)

typealias FSWatcherListener = (FSEventKind, Path) -> Unit

class FSWatcherService(val listener: FSWatcherListener) : AutoCloseable {

    private val dirWatcher = DirWatcher(this::handleDirWatchEvents)

    private val watchFilters = ConcurrentHashMap<Path, ConcurrentHashMap<WatchDirFilter, Any>>()

    private val watchEntriesCache = Cache<WatchEntry, Registration>()

    data class Registration(
        val watchEntry: WatchEntry,
        internal val dirRegistration: DirWatcher.Registration,
        private val service: FSWatcherService
    ) {
        fun cancel() = service.cancelRegistration(this)
    }

    fun registerFile(filePath: Path): Registration =
        watchEntriesCache.getOrCreate(
            WatchEntry(isFile = true, filePath)
        ) { watchEntry ->
            val dirPath = filePath.parent
            getWatchFiltersForDir(dirPath)[OneFileWatchDirFilter(filePath)] = Any()
            val registration = dirWatcher.register(dirPath)
            listener(FSEventKind.START_WATCH_FILE, filePath)

            Registration(
                watchEntry, registration, this
            )
        }

    private fun unregisterFile(filePath: Path) {
        val dirPath = filePath.parent
        getWatchFiltersForDir(dirPath).removeIf {
            it is OneFileWatchDirFilter && it.allowedFilePath == filePath
        }
        listener(FSEventKind.STOP_WATCH_FILE, filePath)
    }

    fun registerDir(dirPath: Path): Registration =
        watchEntriesCache.getOrCreate(
            WatchEntry(isFile = false, dirPath)
        ) { watchEntry ->
            getWatchFiltersForDir(dirPath)[AllFilesWatchDirFilter()] = Any()
            val registration = dirWatcher.register(dirPath)
            listener(FSEventKind.START_WATCH_DIR, dirPath)

            Registration(
                watchEntry, registration, this
            )
        }

    private fun unregisterDir(dirPath: Path) {
        getWatchFiltersForDir(dirPath).removeIf(WatchDirFilter::isAllFiles)
        listener(FSEventKind.STOP_WATCH_DIR, dirPath)
    }

    private fun cancelRegistration(registration: Registration) =
        watchEntriesCache.free(registration.watchEntry) { watchEntry, _ ->
            registration.dirRegistration.cancel()
            if (watchEntry.isFile) {
                unregisterFile(watchEntry.path)
            } else {
                unregisterDir(watchEntry.path)
            }
        }

    private fun getWatchFiltersForDir(dirPath: Path) = watchFilters.computeIfAbsent(dirPath) { ConcurrentHashMap() }

    private fun handleDirWatchEvents(eventKind: DirWatcher.EventKind, path: Path) {
        if (isFileAllowed(path)) {
            listener(eventKind.toFSEventKind(), path)
        }
    }

    private fun isFileAllowed(filePath: Path) =
        watchFilters[filePath.parent]?.keys?.any { it.isFileAllowed(filePath) } ?: false

    override fun close() = dirWatcher.close()

}

private fun DirWatcher.EventKind.toFSEventKind() =
    when (this) {
        DirWatcher.EventKind.CREATE -> FSEventKind.CREATE
        DirWatcher.EventKind.DELETE -> FSEventKind.DELETE
        DirWatcher.EventKind.MODIFY -> FSEventKind.MODIFY
    }
