package io.github.saneea.fileindexer.core.filewatcher

import java.nio.file.*

enum class FSEventKind {
    CREATE, DELETE, MODIFY
}

typealias FSWatcherListener = (FSEventKind, Path) -> Unit

class FSWatcherService(val listener: FSWatcherListener) : AutoCloseable {

    private val watchService = FileSystems.getDefault().newWatchService()

    private val backgroundThread = Thread(this::handleWatchKeys, "FSWatcher")

    init {
        backgroundThread.isDaemon = true
        backgroundThread.start()
    }

    fun watchDir(dirPath: Path) {
        dirPath.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        Files.walk(dirPath).use { filesStream ->
            filesStream
                .filter(Files::isRegularFile)
                .forEach { filePath ->
                    listener(FSEventKind.CREATE, filePath)
                }
        }
    }

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
                    try {
                        listener(watchEvent.kind().toFSEventKind(), parentDirPath.resolve(childPath))
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

private fun <T> WatchEvent.Kind<T>.toFSEventKind() =
    when (this) {
        StandardWatchEventKinds.ENTRY_CREATE -> FSEventKind.CREATE
        StandardWatchEventKinds.ENTRY_DELETE -> FSEventKind.DELETE
        StandardWatchEventKinds.ENTRY_MODIFY -> FSEventKind.MODIFY
        else -> throw IllegalArgumentException("Unknown WatchEvent.Kind: $this")
    }
