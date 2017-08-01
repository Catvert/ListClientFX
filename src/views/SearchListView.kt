package views

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import java.util.*

class SearchListView<T>(val list: SortedFilteredList<T>, addAddRemoveButtons: Boolean, private val onAddButtonClicked: () -> Unit, private val onSelected: (value: T) -> Unit) : View() {
    private var lastSelected: T? = null

    var listView by singleAssign<ListView<T>>()
        private set

    override val root = vbox {

        useMaxHeight = true
        hbox {
            spacing = 10.0
            textfield {
                promptText = "Rechercher.."

                list.filterWhen(textProperty(), { query, item ->
                    item.toString().split(" ").any { it.startsWith(query, true) }
                })
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

        listView = listview(list.sortedItems) {
            onUserDelete {
                showConfirmationDeleteDialog(it)
            }

            onUserSelect(clickCount = 1) {
                lastSelected = it
                onSelected(it)
            }
        }

        VBox.setVgrow(listView, Priority.ALWAYS)
    }

    private fun showConfirmationDeleteDialog(toDelete: T) {
        val confirmation = Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ${toDelete.toString()} ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        confirmation.title = "Confirmation de la suppression"

        val result = confirmation.showAndWait()

        if (result.isPresent && result.get() == ButtonType.YES) {
            list.remove(toDelete)
        }
    }

    init {
        list.sortedItems.comparator = Comparator { t, t1 -> t.toString().compareTo(t1.toString()) }
    }
}