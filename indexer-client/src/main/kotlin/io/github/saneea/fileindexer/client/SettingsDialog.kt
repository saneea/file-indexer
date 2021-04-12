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
    private val currentWatchEntriesList = JList(currentWatchEntriesListModel)
    private val currentWatchEntriesListWithScroll = JScrollPane(currentWatchEntriesList)

    private val selectedEntry: Path?
        get() = currentWatchEntriesList.selectedValue

    init {
        registerUIActions()
        initButtonsSettings()
        createLayout()
        pack()
    }

    private fun initButtonsSettings() {
        updateDelDirButton()

        //TODO disable these buttons for now (there were no backend implementation for them)
        addFileButton.isEnabled = false
        delFileButton.isEnabled = false
    }

    private fun registerUIActions() {
        registerButtonActions()
    }

    private fun registerButtonActions() {
        currentWatchEntriesList.addListSelectionListener { updateDelDirButton() }

        registerAddDirButtonAction()
        registerDelDirButtonAction()
    }

    private fun registerDelDirButtonAction() {
        delDirButton.addActionListener {
            if (selectedEntry != null) {
                ownerWindow.fileIndexerService.unregisterDir(selectedEntry!!)
                currentWatchEntriesListModel.removeElement(selectedEntry)
            }
        }
    }

    private fun updateDelDirButton() {
        delDirButton.isEnabled = selectedEntry != null
    }

    private fun registerAddDirButtonAction() {
        addDirButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                ownerWindow.fileIndexerService.registerDir(pathToAdd)
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
                .addComponent(currentWatchEntriesListWithScroll)
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
                .addComponent(currentWatchEntriesListWithScroll)
        )
    }

}
