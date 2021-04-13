package io.github.saneea.fileindexer.client

import java.nio.file.Path
import javax.swing.*

data class WatchEntry(
    val isFile: Boolean,
    val path: Path
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
        updateDelDirButton()
        updateDelFileButton()
    }

    private fun registerUIActions() {
        registerButtonActions()
    }

    private fun registerButtonActions() {
        currentWatchEntriesList.addListSelectionListener { updateButtonsSettings() }

        registerAddDirButtonAction()
        registerDelDirButtonAction()

        registerAddFileButtonAction()
        registerDelFileButtonAction()
    }

    private fun registerDelDirButtonAction() {
        delDirButton.addActionListener {
            if (selectedEntry != null) {
                ownerWindow.fileIndexerService.unregisterDir(selectedEntry!!.path)
                currentWatchEntriesListModel.removeElement(selectedEntry)
            }
        }
    }

    private fun registerDelFileButtonAction() {
        delFileButton.addActionListener {
            if (selectedEntry != null) {
                ownerWindow.fileIndexerService.unregisterFile(selectedEntry!!.path)
                currentWatchEntriesListModel.removeElement(selectedEntry)
            }
        }
    }

    private fun updateDelDirButton() {
        delDirButton.isEnabled = selectedEntry != null && !selectedEntry!!.isFile
    }

    private fun updateDelFileButton() {
        delFileButton.isEnabled = selectedEntry != null && selectedEntry!!.isFile
    }

    private fun registerAddDirButtonAction() {
        addDirButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                ownerWindow.fileIndexerService.registerDir(pathToAdd)
                currentWatchEntriesListModel.addElement(WatchEntry(false, pathToAdd))
            }
        }
    }

    private fun registerAddFileButtonAction() {
        addFileButton.addActionListener {
            val fileChooserDialog = JFileChooser()
            fileChooserDialog.fileSelectionMode = JFileChooser.FILES_ONLY
            if (fileChooserDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val pathToAdd = fileChooserDialog.selectedFile.toPath()
                ownerWindow.fileIndexerService.registerFile(pathToAdd)
                currentWatchEntriesListModel.addElement(WatchEntry(true, pathToAdd))
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
