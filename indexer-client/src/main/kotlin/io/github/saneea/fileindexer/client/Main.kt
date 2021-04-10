package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.service.FileIndexerService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.tokenizer.WhitespaceTokenizer
import java.nio.file.Paths
import javax.swing.JFrame

import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val mainWindow = MainWindow()
        mainWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainWindow.isVisible = true
    }
}

fun mainTest() {

    //TODO remove this method

    val tokens = arrayOf("abc", "123")

    val tokenizer: Tokenizer = WhitespaceTokenizer()

    FileIndexerService(tokenizer)
        .use {
            it.watchDir(Paths.get("/home/saneea/code/file-indexer/01/tests"))

            for (i in 1..300) {
                for (token in tokens) {
                    it.printFilesForToken(token)
                }
                Thread.sleep(1000)
            }
        }

}

fun FileIndexerService.printFilesForToken(token: String) {
    println("$token: ${this.getFilesForToken(token)}")
}