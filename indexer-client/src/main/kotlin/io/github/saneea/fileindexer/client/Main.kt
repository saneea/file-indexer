package io.github.saneea.fileindexer.client

import javax.swing.JFrame

import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val mainWindow = MainWindow()
        mainWindow.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        mainWindow.isVisible = true
    }
}
