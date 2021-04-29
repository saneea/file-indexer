package io.github.saneea.fileindexer.core.filewatcher

import io.github.saneea.fileindexer.core.filewatcher.FSEventKind.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Paths

class FSEventsBufferTest {

    private val p1 = Paths.get("1")
    private val p2 = Paths.get("2")

    @Test
    fun testEmpty() {
        val buf = FSEventsBuffer()
        assertNull(buf.pollEvent())
    }

    @Test
    fun testOneEvent() {
        val buf = FSEventsBuffer()
        buf.addEvent(FSEvent(CREATE, p1))
        assertEquals(FSEvent(CREATE, p1), buf.pollEvent())
        assertNull(buf.pollEvent())
    }

    @Test
    fun testCreateDelete() {
        val buf = FSEventsBuffer()
        buf.addEvent(FSEvent(CREATE, p1))
        buf.addEvent(FSEvent(DELETE, p1))
        assertNull(buf.pollEvent())
    }

    @Test
    fun testDeleteCreate() {
        val buf = FSEventsBuffer()
        buf.addEvent(FSEvent(DELETE, p1))
        buf.addEvent(FSEvent(CREATE, p1))
        assertEquals(FSEvent(MODIFY, p1), buf.pollEvent())
        assertNull(buf.pollEvent())
    }

    @Test
    fun testCreateDeleteMixed() {
        val buf = FSEventsBuffer()

        buf.addEvent(FSEvent(DELETE, p1))
        buf.addEvent(FSEvent(CREATE, p1))

        buf.addEvent(FSEvent(CREATE, p2))
        buf.addEvent(FSEvent(DELETE, p2))

        assertEquals(FSEvent(MODIFY, p1), buf.pollEvent())
        assertNull(buf.pollEvent())
    }

    @Test
    fun testCreateDeleteMany() {
        val buf = FSEventsBuffer()

        buf.addEvent(FSEvent(CREATE, p1))
        buf.addEvent(FSEvent(DELETE, p1))
        buf.addEvent(FSEvent(CREATE, p1))
        buf.addEvent(FSEvent(DELETE, p1))

        assertNull(buf.pollEvent())
    }

    @Test
    fun testHideModify() {
        val buf = FSEventsBuffer()

        buf.addEvent(FSEvent(CREATE, p1))
        buf.addEvent(FSEvent(MODIFY, p1))
        buf.addEvent(FSEvent(DELETE, p1))

        assertNull(buf.pollEvent())
    }

}