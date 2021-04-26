package io.github.saneea.fileindexer.client

import java.nio.file.Path
import javax.swing.*

class SettingsDialog(private val ownerWindow: MainWindow) :
    JDialog(ownerWindow, "Settings", false) {

    private val addPathButton = JButton("+ path")
    private val delPathButton = JButton("- path")

    private val currentWatchEntriesListModel = DefaultListModel<Path>()
    private val currentWatchEntriesList = JList(currentWatchEntriesListModel)
    private val currentWatchEntriesListWithScroll = JScrollPane(currentWatchEntriesList)

    private var selectedEntry: Path?
        get() = currentWatchEntriesList.selectedValue
        set(value) {
            if (value != null) {
                currentWatchEntriesList.setSelectedValue(value, true)
            }
        }

    init {
        registerButtonsActions()
        updateDelPathButtonsSettings()
        createLayout()
        pack()
    }

    private fun updateDelPathButtonsSettings() {
        delPathButton.isEnabled = selectedEntry != null
    }

    private fun registerButtonsActions() {
        currentWatchEntriesList.addListSelectionListener { updateDelPathButtonsSettings() }

        registerAddPathButtonAction()
        registerDelPathButtonAction(delPathButton)
    }

    private fun registerDelPathButtonAction(button: JButton) {
        button.addActionListener {
            val selectedEntryLocal = selectedEntry
            if (selectedEntryLocal != null) {
                ownerWindow.fileIndexerService.removeObservable(selectedEntryLocal)
                currentWatchEntriesListModel.removeElement(selectedEntryLocal)
            }
        }
    }

    private fun registerAddPathButtonAction() {
        addPathButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                if (!currentWatchEntriesListModel.contains(pathToAdd)) {
                    ownerWindow.fileIndexerService.addObservable(pathToAdd)
                    currentWatchEntriesListModel.addElement(pathToAdd)
                }
                selectedEntry = pathToAdd
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
                        .addComponent(addPathButton)
                        .addComponent(delPathButton)
                )
                .addComponent(currentWatchEntriesListWithScroll)
        )

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(addPathButton)
                        .addComponent(delPathButton)
                )
                .addComponent(currentWatchEntriesListWithScroll)
        )
    }

}
