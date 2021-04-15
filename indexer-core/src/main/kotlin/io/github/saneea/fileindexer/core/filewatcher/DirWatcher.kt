package io.github.saneea.fileindexer.core.filewatcher

import io.github.saneea.fileindexer.core.utils.UsageRegistry
import java.nio.file.*

class DirWatcher(val listener: (EventKind, Path) -> Unit) : AutoCloseable {

    enum class EventKind {
        CREATE, DELETE, MODIFY
    }

    data class Registration(
        private val watchKeyRegistry: UsageRegistry<WatchKey>,
        private val watchKey: WatchKey
    ) {
        init {
            watchKeyRegistry.inUse(watchKey)
        }

        fun cancel() = watchKeyRegistry.free(watchKey, WatchKey::cancel)
    }

    private val watchService = FileSystems.getDefault().newWatchService()

    private val backgroundThread = Thread(this::handleWatchKeys, "FSWatcher")

    private val watchKeyRegistry = UsageRegistry<WatchKey>()

    init {
        backgroundThread.isDaemon = true
        backgroundThread.start()
    }

    fun register(dirPath: Path) =
        Registration(
            watchKeyRegistry,
            dirPath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )
        )

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
                    val eventKind = watchEvent.kind().toDirWatcherEventKind()
                    val resolvedFilePath = parentDirPath.resolve(childPath)
                    try {
                        listener(eventKind, resolvedFilePath)
                    } catch (e: Exception) {
                        //TODO log this exception
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun close() {
        backgroundThread.interrupt()
        backgroundThread.join()

        watchService.close()
    }
}

private fun <T> WatchEvent.Kind<T>.toDirWatcherEventKind() =
    when (this) {
        StandardWatchEventKinds.ENTRY_CREATE -> DirWatcher.EventKind.CREATE
        StandardWatchEventKinds.ENTRY_DELETE -> DirWatcher.EventKind.DELETE
        StandardWatchEventKinds.ENTRY_MODIFY -> DirWatcher.EventKind.MODIFY
        else -> throw IllegalArgumentException("Unknown WatchEvent.Kind: $this")
    }
