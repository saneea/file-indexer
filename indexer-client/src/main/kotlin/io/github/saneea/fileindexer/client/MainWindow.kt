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

    private val listModel = DefaultListModel<String>()

    private val searchLabel = JLabel("Search:")

    private val searchField = JTextField()

    private val foundFilesList = JScrollPane(JList(listModel))

    private val refreshButton = JButton("Search again")
    private val settingsButton = JButton("Settings")

    init {

        registerControlsActions()

        //TODO remove debug folder registration
        fileIndexerService.watchDir(Paths.get("/home/saneea/code/file-indexer/01/tests"))

        createLayout()

        pack()
    }

    private fun registerControlsActions() {
        registerOnWindowClosedActions()

        searchField.addTextChangedListener {
            showFilesForSearchText()
        }

        refreshButton.addActionListener {
            showFilesForSearchText()
        }
    }

    private fun createLayout() {
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout

        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

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
    }

    private fun registerOnWindowClosedActions() {
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                fileIndexerService.close()
            }
        })
    }

    private fun showFilesForSearchText() {
        val searchText = searchField.text.trim()

        listModel.clear()

        val filesForToken = fileIndexerService.getFilesForToken(searchText)

        filesForToken
            .map(Path::toString)
            .forEach(listModel::addElement)
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

