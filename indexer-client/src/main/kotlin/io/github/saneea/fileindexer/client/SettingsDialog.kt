package io.github.saneea.fileindexer.client

import java.nio.file.Path
import javax.swing.*

class SettingsDialog(private val ownerWindow: MainWindow) :
    JDialog(ownerWindow, "Settings", false) {

    private val addDirButton = JButton("+ dir")
    private val delDirButton = JButton("- dir")
    private val addFileButton = JButton("+ file")
    private val delFileButton = JButton("- file")

    private val currentWatchEntriesListModel = DefaultListModel<Path>()
    private val currentWatchEntriesList = JScrollPane(JList(currentWatchEntriesListModel))

    init {
        registerButtonActions()
        initButtonsSettings()
        createLayout()
        pack()
    }

    private fun initButtonsSettings() {
        //TODO disable these buttons for now (there were no backend implementation for them)
        delDirButton.isEnabled = false
        addFileButton.isEnabled = false
        delFileButton.isEnabled = false
    }

    private fun registerButtonActions() {
        registerAddDirButtonAction()
    }

    private fun registerAddDirButtonAction() {
        addDirButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                ownerWindow.fileIndexerService.watchDir(pathToAdd)
                currentWatchEntriesListModel.addElement(pathToAdd)
            }
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
                        .addComponent(addDirButton)
                        .addComponent(delDirButton)
                        .addComponent(addFileButton)
                        .addComponent(delFileButton)
                )
                .addComponent(currentWatchEntriesList)
        )

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(addDirButton)
                        .addComponent(delDirButton)
                        .addComponent(addFileButton)
                        .addComponent(delFileButton)
                )
                .addComponent(currentWatchEntriesList)
        )
    }

}
