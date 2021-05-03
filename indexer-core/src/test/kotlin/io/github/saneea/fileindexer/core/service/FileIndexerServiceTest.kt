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
import kotlin.io.path.createDirectories
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

    @Test
    fun testTrackDirs() {
        val dirs = (0..2).map { root.resolve("dir_$it") }

        dirs.forEach(Path::createDirectories)

        val files = dirs.map { dir ->
            (0..2).map { fileId ->
                dir.resolve("file_$fileId.txt")
            }
        }

        files[0][0].writeText("hello world", charset)

        (0..1).map { dirs[it] }.forEach(service::addObservable)
        sleep()
        assertEquals(setOf(files[0][0]), service.getFilesForToken("world"))

        files[0][1].writeText("my world", charset)
        sleep()
        assertEquals(setOf(files[0][0], files[0][1]), service.getFilesForToken("world"))

        files[1][1].writeText("my world in folder1/file1", charset)
        sleep()
        assertEquals(setOf(files[0][0], files[0][1], files[1][1]), service.getFilesForToken("world"))

        files[2][0].writeText("world for file from untracked dir", charset)
        sleep()
        assertEquals(setOf(files[0][0], files[0][1], files[1][1]), service.getFilesForToken("world"))
    }

    @Test
    fun testTrackFileChange() {
        val file = root.resolve("file.txt")

        service.addObservable(file)

        file.writeText("hello world", charset)
        sleep()

        assertEquals(setOf(file), service.getFilesForToken("hello"))
        assertEquals(setOf(file), service.getFilesForToken("world"))
        assertEquals(emptySet(), service.getFilesForToken("my"))

        file.writeText("my world", charset)
        sleep()

        assertEquals(emptySet(), service.getFilesForToken("hello"))
        assertEquals(setOf(file), service.getFilesForToken("world"))
        assertEquals(setOf(file), service.getFilesForToken("my"))
    }

    private fun sleep() = Thread.sleep(waitTime)
}