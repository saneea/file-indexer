package io.github.saneea.fileindexer.client

import java.nio.file.Path
import javax.swing.*

data class WatchEntry(
    val path: Path,
    val isFile: Boolean
) {
    override fun toString(): String {
        return "${if (isFile) "File" else "Dir"}: $path"
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

    private var selectedEntry: WatchEntry?
        get() = currentWatchEntriesList.selectedValue
        set(value) {
            if (value != null) {
                currentWatchEntriesList.setSelectedValue(value, true)
            }
        }

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
                ownerWindow.fileIndexerService.removeObservable(selectedEntry!!.path)
                currentWatchEntriesListModel.removeElement(selectedEntry)
            }
        }
    }

    private fun updateDelFSEntryButton(button: JButton, isFile: Boolean) {
        button.isEnabled = selectedEntry != null && isFile == selectedEntry!!.isFile
    }

    private fun registerAddDirButtonAction() {
        addDirButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                val watchEntry = WatchEntry(pathToAdd, false)
                if (!currentWatchEntriesListModel.contains(watchEntry)) {
                    ownerWindow.fileIndexerService.addObservable(pathToAdd)
                    currentWatchEntriesListModel.addElement(watchEntry)
                }
                selectedEntry = watchEntry
            }
        }
    }

    private fun registerAddFileButtonAction() {
        addFileButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.FILES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                val watchEntry = WatchEntry(pathToAdd, true)
                if (!currentWatchEntriesListModel.contains(watchEntry)) {
                    ownerWindow.fileIndexerService.addObservable(pathToAdd)
                    currentWatchEntriesListModel.addElement(watchEntry)
                }
                selectedEntry = watchEntry
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
