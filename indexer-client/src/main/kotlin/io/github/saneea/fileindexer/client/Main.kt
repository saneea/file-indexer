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
        mainWindow.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        mainWindow.isVisible = true
    }
}
