package io.github.saneea.fileindexer.core.filewatcher

import io.github.saneea.fileindexer.core.common.FSEventKind
import io.github.saneea.fileindexer.core.common.merge
import java.nio.file.Path
import java.util.*

data class FSEvent(
    val eventKind: FSEventKind,
    val path: Path
)

class FSEventsBuffer {

    private val events = LinkedList<FSEvent>()

    @Synchronized
    fun pollEvent(): FSEvent? = events.poll()

    @Synchronized
    fun addEvent(event: FSEvent) {
        val samePath: (FSEvent) -> Boolean = { it.path == event.path }

        val finalEventKind = events
            .filter(samePath)
            .plus(event)
            .map { it.eventKind }
            .merge()

        events.removeIf(samePath)

        if (finalEventKind != null) {
            events.add(FSEvent(finalEventKind, event.path))
        }
    }

    fun takeEvent(): FSEvent {
        while (true) {
            val event = pollEvent()
            if (event != null) {
                return event
            }
            Thread.sleep(1000)
        }
    }
}
