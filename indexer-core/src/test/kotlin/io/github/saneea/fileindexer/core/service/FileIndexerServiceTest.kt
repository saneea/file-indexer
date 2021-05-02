package io.github.saneea.fileindexer.core.service

import io.github.saneea.fileindexer.core.tokenizer.LettersDigitsTokenizer
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText
import kotlin.test.assertEquals

@ExperimentalPathApi
class FileIndexerServiceTest {

    private val tokenizer: Tokenizer = LettersDigitsTokenizer()

    private val service = FileIndexerService(tokenizer)

    private val charset = service.defaultCharset

    private val waitTime = 2500L

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var root: Path

    @Before
    fun setUp() {
        root = tempFolder.root.toPath()
    }

    @After
    fun tearDown() {
        service.close()
    }

    @Test
    fun testJustCreateAndCloseService() {
    }

    @Test
    fun testTrackFiles() {
        val files = (0..2).map { root.resolve("file_$it.txt") }

        files[1].writeText("hello world", charset)

        (0..1).map { files[it] }.forEach(service::addObservable)
        sleep()
        assertEquals(setOf(files[1]), service.getFilesForToken("world"))

        files[0].writeText("my world", charset)
        sleep()
        assertEquals(setOf(files[0], files[1]), service.getFilesForToken("world"))

        files[2].writeText("world for untracked file", charset)
        sleep()
        assertEquals(setOf(files[0], files[1]), service.getFilesForToken("world"))

        files[1].deleteExisting()
        sleep()
        assertEquals(setOf(files[0]), service.getFilesForToken("world"))

        service.removeObservable(files[0])
        sleep()
        assertEquals(emptySet(), service.getFilesForToken("world"))
    }

    private fun sleep() = Thread.sleep(waitTime)
}