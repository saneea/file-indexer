package io.github.saneea.fileindexer.client

import io.github.saneea.fileindexer.core.filewatcher.FSWatcherService
import javax.swing.*

data class WatchEntry(
    val registration: FSWatcherService.Registration
) {
    override fun toString(): String {
        val entry = registration.watchEntry
        return "${if (entry.isFile) "File" else "Dir"}: ${entry.path}"
    }
}

class SettingsDialog(private val ownerWindow: MainWindow) :
    JDialog(ownerWindow, "Settings", false) {

    private val addDirButton = JButton("+ dir")
    private val delDirButton = JButton("- dir")
    private val addFileButton = JButton("+ file")
    private val delFileButton = JButton("- file")

    private val currentWatchEntriesListModel = DefaultListModel<WatchEntry>()
    private val currentWatchEntriesList = JList(currentWatchEntriesListModel)
    private val currentWatchEntriesListWithScroll = JScrollPane(currentWatchEntriesList)

    private val selectedEntry: WatchEntry?
        get() = currentWatchEntriesList.selectedValue

    init {
        registerUIActions()
        initButtonsSettings()
        createLayout()
        pack()
    }

    private fun initButtonsSettings() {
        updateButtonsSettings()
    }

    private fun updateButtonsSettings() {
        updateDelFSEntryButton(delDirButton, isFile = false)
        updateDelFSEntryButton(delFileButton, isFile = true)
    }

    private fun registerUIActions() {
        registerButtonActions()
    }

    private fun registerButtonActions() {
        currentWatchEntriesList.addListSelectionListener { updateButtonsSettings() }

        registerAddDirButtonAction()
        registerDelFSEntryButtonAction(delDirButton)

        registerAddFileButtonAction()
        registerDelFSEntryButtonAction(delFileButton)
    }

    private fun registerDelFSEntryButtonAction(button: JButton) {
        button.addActionListener {
            if (selectedEntry != null) {
                selectedEntry!!.registration.cancel()
                currentWatchEntriesListModel.removeElement(selectedEntry)
            }
        }
    }

    private fun updateDelFSEntryButton(button: JButton, isFile: Boolean) {
        button.isEnabled = selectedEntry != null && isFile == selectedEntry!!.registration.watchEntry.isFile
    }

    private fun registerAddDirButtonAction() {
        addDirButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                val watcherRegistration = ownerWindow.fileIndexerService.registerDir(pathToAdd)
                currentWatchEntriesListModel.addElement(WatchEntry(watcherRegistration))
            }
        }
    }

    private fun registerAddFileButtonAction() {
        addFileButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.FILES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                val watcherRegistration = ownerWindow.fileIndexerService.registerFile(pathToAdd)
                currentWatchEntriesListModel.addElement(WatchEntry(watcherRegistration))
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
