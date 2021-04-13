package io.github.saneea.fileindexer.core.filewatcher

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

typealias FSWatcherListener = (FSEventKind, Path) -> Unit

class FSWatcherService(val listener: FSWatcherListener) : AutoCloseable {

    private val watchService = FileSystems.getDefault().newWatchService()

    private val backgroundThread = Thread(this::handleWatchKeys, "FSWatcher")

    private val watchFilters = ConcurrentHashMap<Path, ConcurrentHashMap<WatchDirFilter, Any>>()

    init {
        backgroundThread.isDaemon = true
        backgroundThread.start()
    }

    fun registerFile(filePath: Path) {
        val dirPath = filePath.parent
        getWatchFiltersForDir(dirPath)[OneFileWatchDirFilter(filePath)] = Any()
        registerDirInternal(dirPath)
        listener(FSEventKind.START_WATCH_FILE, filePath)
    }

    fun unregisterFile(filePath: Path) {
        val dirPath = filePath.parent
        getWatchFiltersForDir(dirPath).removeIf {
            it is OneFileWatchDirFilter && it.allowedFilePath == filePath
        }
        unregisterDirInternal(dirPath)
        listener(FSEventKind.STOP_WATCH_FILE, filePath)
    }

    fun registerDir(dirPath: Path) {
        getWatchFiltersForDir(dirPath)[AllFilesWatchDirFilter()] = Any()
        registerDirInternal(dirPath)
        listener(FSEventKind.START_WATCH_DIR, dirPath)
    }

    fun unregisterDir(dirPath: Path) {
        getWatchFiltersForDir(dirPath).removeIf(WatchDirFilter::isAllFiles)
        unregisterDirInternal(dirPath)
        listener(FSEventKind.STOP_WATCH_DIR, dirPath)
    }

    private fun registerDirInternal(dirPath: Path) {
        dirPath.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
    }

    private fun unregisterDirInternal(dirPath: Path) {
        //TODO implement
    }

    private fun getWatchFiltersForDir(dirPath: Path) = watchFilters.computeIfAbsent(dirPath) { ConcurrentHashMap() }

    private fun handleWatchKeys() {
        try {
            while (true) {
                val watchKey = watchService.take()
                if (watchKey != null) {
                    handleWatchEvents(watchKey)
                }
                watchKey.reset()
            }
        } catch (e: InterruptedException) {
            //it is normal ending of background thread
        }
    }

    private fun handleWatchEvents(watchKey: WatchKey) {
        val parentDirPath = watchKey.watchable()
        if (parentDirPath is Path) {
            for (watchEvent in watchKey.pollEvents()) {
                val childPath = watchEvent.context()
                if (childPath is Path) {
                    val resolvedFilePath = parentDirPath.resolve(childPath)
                    if (isFileAllowed(resolvedFilePath)) {
                        try {
                            listener(watchEvent.kind().toFSEventKind(), resolvedFilePath)
                        } catch (e: Exception) {
                            //TODO log this exception
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun isFileAllowed(filePath: Path) =
        watchFilters[filePath.parent]?.keys?.any { it.isFileAllowed(filePath) } ?: false

    override fun close() {
        backgroundThread.interrupt()
        backgroundThread.join()

        watchService.close()
    }

}

private fun <T> WatchEvent.Kind<T>.toFSEventKind() =
    when (this) {
        StandardWatchEventKinds.ENTRY_CREATE -> FSEventKind.CREATE
        StandardWatchEventKinds.ENTRY_DELETE -> FSEventKind.DELETE
        StandardWatchEventKinds.ENTRY_MODIFY -> FSEventKind.MODIFY
        else -> throw IllegalArgumentException("Unknown WatchEvent.Kind: $this")
    }
