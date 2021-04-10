package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.service.FileIndexerService
import io.github.saneea.fileindexer.core.tokenizer.Tokenizer
import io.github.saneea.fileindexer.core.tokenizer.WhitespaceTokenizer
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*

class MainWindow : JFrame() {

    private val tokenizer: Tokenizer = WhitespaceTokenizer()

    private val fileIndexerService = FileIndexerService(tokenizer)

    init {

        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                fileIndexerService.close()
            }
        })

        //TODO remove debug folder registration
        fileIndexerService.watchDir(Paths.get("/home/saneea/code/file-indexer/01/tests"))

        val listModel = DefaultListModel<String>()
        val foundFilesList = JScrollPane(JList(listModel))

        val layout = GroupLayout(contentPane)
        contentPane.layout = layout

        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        val searchLabel = JLabel("Search:")
        val searchField = JTextField()


        val searchTextListener: (String) -> Unit = { searchText ->
            listModel.clear()

            val filesForToken = fileIndexerService.getFilesForToken(searchText)

            filesForToken
                .map(Path::toString)
                .forEach(listModel::addElement)
        }
        searchField.addTextChangedListener(searchTextListener)

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { _ ->
            listModel.add(0, "refresh")
        }

        val settingsButton = JButton("Settings")

        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(searchLabel)
                        .addComponent(searchField)
                )
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(refreshButton)
                        .addComponent(settingsButton)
                )
                .addComponent(foundFilesList)
        )

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(searchLabel)
                        .addComponent(searchField)
                )
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(refreshButton)
                        .addComponent(settingsButton)
                )
                .addComponent(foundFilesList)
        )

        layout.linkSize(SwingConstants.VERTICAL, searchField)

        pack()
    }

}

private fun JTextField.addTextChangedListener(listener: (String) -> Unit) {
    fun textChanged() = listener(this.text)
    this.addKeyListener(object : KeyListener {
        override fun keyTyped(e: KeyEvent?) = textChanged()
        override fun keyPressed(e: KeyEvent?) = textChanged()
        override fun keyReleased(e: KeyEvent?) = textChanged()
    })
}
