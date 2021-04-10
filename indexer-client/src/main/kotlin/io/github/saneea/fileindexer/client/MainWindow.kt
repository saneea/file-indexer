package io.github.saneea.fileindexer.client

import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*

class MainWindow : JFrame() {

    init {
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

            for (i in 1..searchText.length) {
                listModel.addElement(searchText)
            }
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
