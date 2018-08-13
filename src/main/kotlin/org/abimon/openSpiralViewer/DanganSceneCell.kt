package org.abimon.openSpiralViewer

import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.TextArea


open class DanganSceneCell : TableCell<Pair<String, Any>, Any>() {
    private var editingGraphic: Node? = null

    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()

            val localEditingGraphic = editingGraphic
            val localItem = item

            when (localItem) {
                is String -> {
                    if (localEditingGraphic !is TextArea) {
                        if (localEditingGraphic != null)
                            println("We have a string, yet we're editing with $localEditingGraphic")
                        else
                            editingGraphic = newTextArea(localItem)
                    }

                    text = null
                    graphic = editingGraphic

                    tableView.isEditable = false
                }
            }
        }
    }

    override fun updateItem(item: Any?, empty: Boolean) {
        super.updateItem(item, empty)
        val localEditingGraphic = editingGraphic

        if (item == null || empty) {
            super.setText(null)
            super.setGraphic(null)
        } else {
            when (item) {
                is String -> {
                    if (isEditing) {
                        if (localEditingGraphic is TextArea)
                            localEditingGraphic.text = item

                        text = null
                        graphic = localEditingGraphic
                    } else {
                        text = item
                        graphic = null
                        editingGraphic = newTextArea(item)
                    }
                }
            }
        }
    }

    override fun cancelEdit() {
        super.cancelEdit()

        val localItem = item

        when (localItem) {
            is String -> {
                text = localItem
                graphic = null
            }
        }

        tableView.isEditable = true
    }

    override fun commitEdit(newValue: Any?) {
        super.commitEdit(newValue)

        tableView.isEditable = true
    }

    private fun newTextArea(content: String): TextArea {
        val textArea = TextArea(content)
        textArea.minWidth = this.width - this.graphicTextGap * 2
        textArea.focusedProperty().addListener { _, _, newValue -> if (!newValue) commitEdit(textArea.text) }

        return textArea
    }
}