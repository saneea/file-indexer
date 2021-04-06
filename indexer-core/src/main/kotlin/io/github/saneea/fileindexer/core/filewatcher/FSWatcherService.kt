package io.github.saneea.fileindexer.core.filewatcher

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

class FSWatcherService : AutoCloseable {

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
        for (watchEvent in watchKey.pollEvents()) {
            val kind = watchEvent.kind()
            val context = watchEvent.context()
            println("$kind: $context")
        }
    }

    override fun close() {
        backgroundThread.interrupt()
        backgroundThread.join()

        watchService.close()
    }

}