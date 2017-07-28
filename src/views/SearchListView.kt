package views

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class SearchListView<T>(val list: ObservableList<T>, addAddRemoveButtons: Boolean, private val onAddButtonClicked: () -> Unit, private val onSelected: (value: T) -> Unit) : View() {
    private val searchList = FXCollections.observableArrayList<T>(list)
    private var lastSelected: T? = null

    override val root = vbox {

        useMaxHeight = true
        hbox {
            spacing = 10.0
            textfield {
                promptText = "Rechercher.."

                textProperty().addListener { _, _, new ->
                    if (new.isBlank())
                        updateSearchList(list)
                    else {
                        searchList.clear()

                        list.forEach { item ->
                            val splits = item.toString().split(" ")

                            for (split in splits) {
                                if (split.startsWith(new, true)) {
                                    searchList += item
                                    break
                                }
                            }
                        }
                    }
                }
            }

            if (addAddRemoveButtons) {
                button("+") {
                    action {
                        onAddButtonClicked()
                    }
                }
                button("-") {
                    action {
                        if (lastSelected != null) {
                            showConfirmationDeleteDialog(lastSelected!!)
                        }
                    }
                }
            }
        }

        val list = listview(searchList) {
            onUserDelete {
                showConfirmationDeleteDialog(it)
            }

            onUserSelect {
                lastSelected = it
                onSelected(it)
            }
        }

        VBox.setVgrow(list, Priority.ALWAYS)

    }

    private fun showConfirmationDeleteDialog(toDelete: T) {
        val confirmation = Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ${toDelete.toString()} ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        confirmation.title = "Confirmation de la suppression"

        val result = confirmation.showAndWait()

        if(result.isPresent && result.get() == ButtonType.YES) {
            list.remove(toDelete)
            updateSearchList(list)
        }
    }

    private fun updateSearchList(newList: ObservableList<T>) {
        searchList.clear()
        searchList.addAll(newList)

        FXCollections.sort(searchList, { c, c1 -> c.toString().compareTo(c1.toString()) })
    }

    fun updateSearchList() {
        updateSearchList(list)
    }

    init {
        FXCollections.sort(searchList, { c, c1 -> c.toString().compareTo(c1.toString()) })
    }
}