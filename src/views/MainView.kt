package views

import com.jfoenix.controls.JFXTimePicker
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.Callback
import tornadofx.*
import java.time.format.DateTimeFormatter

import Customer
import Performance
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.awt.Desktop
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

private enum class TabMode {
    Workers, Customers
}

class MainView : View("Liste des clients") {
    private var customerSelected: Customer? = null

    private val showPerformances = FXCollections.observableArrayList<Performance>()

    private val centerVbox = vbox {
        spacing = 10.0
    }
    private val rightForm = form { }

    private var firstColumnPerformance = let {
        val column = TableColumn<Performance, String>("Client")
        column.cellValueFactory = Callback {
            when (tabMode) {
                TabMode.Workers -> ReadOnlyStringWrapper(it.value.customer()?.name)
                TabMode.Customers -> ReadOnlyStringWrapper(it.value.worker()?.name)
            }
        }
        column
    }

    private var tabMode = TabMode.Workers

    val workersView = SearchListView(Data.workers,true, {
        val addWorkerView = AddWorkerView({
            Data.workers.add(it)

            updateListsView()
        })
        openInternalWindow(addWorkerView)
    }, {
        updatePerformance(Data.getPerformances(it))
    })

    val customersView = SearchListView(Data.customers, true, {
        val addCustomerView = AddCustomerView({
            Data.customers.add(it)

            updateListsView()
        })

        openInternalWindow(addCustomerView)
    }, {
        customerSelected = it

        updatePerformance(Data.getPerformances(it))
        rightForm.clear()

        rightForm.fieldset {
            field("Nom") {
                textfield(it.name) {
                    textProperty().addListener { _, _, _ ->
                        if(!text.isBlank())
                            it.name = text
                    }
                }
            }
            field("Adresse") {
                textfield(it.address) {
                    textProperty().addListener { _, _, _ ->
                        it.address = text
                    }
                }
            }
            field("Commune") {
                textfield(it.town) {
                    textProperty().addListener { _, _, _ ->
                        it.town = text
                    }
                }
            }
            field("N° TVA") {
                textfield(it.tva) {
                    textProperty().addListener { _, _, _ ->
                        it.tva = text
                    }
                }
            }
            field("N° Téléphone") {
                textfield(it.phoneNumber) {
                    textProperty().addListener { _, _, _ ->
                        it.phoneNumber = text
                    }
                }
            }
            field("Email") {
                textfield(it.email) {
                    textProperty().addListener { _, _, _ ->
                        it.email = text
                    }
                }
            }
            field("Informations") {
                textfield(it.info) {
                    textProperty().addListener { _, _, _ ->
                        it.info = text
                    }
                }
            }
            field("Entreprise") {
                checkbox {
                    isSelected = it.isCompany

                    selectedProperty().addListener { _, _, _ ->
                        it.isCompany = isSelected
                    }
                }
            }
            field("Dossier") {
                button("Ouvrir") {
                    action {
                        if(LCApp.customersFolder != null) {
                            try {
                                Desktop.getDesktop().open(File(LCApp.customersFolder!!.path + "/${it.name}"))
                            } catch(e: Exception) {
                                println(e)

                                Alert(Alert.AlertType.ERROR, "Le dossier du client ${it.name} n'existe pas !").show()
                            }
                        }
                    }
                }
            }
        }
    })

    private val performancesTable = let {
        val datePattern = "dd-MM-yyyy HH:mm:ss"

        val table = TableView(showPerformances)
        table.prefHeight = 1000.0
        table.columns.add(firstColumnPerformance)

        table.column<Performance, LocalDateTime>("Début") {
            it.tableColumn.prefWidth = 150.0

            it.tableColumn.setCellFactory { object : TableCell<Performance, LocalDateTime>() {
                override fun updateItem(item: LocalDateTime?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if(item == null && empty) {
                        text = ""
                    }
                    else {
                        text = item!!.format(DateTimeFormatter.ofPattern(datePattern))
                    }
                }
            } }

            ReadOnlyObjectWrapper<LocalDateTime>(it.value.startDate)
        }
        table.column<Performance, LocalDateTime>("Fin") {
            it.tableColumn.prefWidth = 150.0

            it.tableColumn.setCellFactory {  object : TableCell<Performance, LocalDateTime>() {
                override fun updateItem(item: LocalDateTime?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if(item == null && empty) {
                        text = ""
                    }
                    else {
                        text = item!!.format(DateTimeFormatter.ofPattern(datePattern))
                    }
                }
            } }

            ReadOnlyObjectWrapper<LocalDateTime>(it.value.endDate)
        }
        table.column<Performance, String>("Durée de la prestation") {
            val duration = it.value.duration()

            val hours = duration.toHours()
            val minutes = if (hours == 0L) duration.toMinutes() else duration.toMinutes() - hours * 60

            ReadOnlyStringWrapper("$hours h $minutes m")
        }
        table.column("Commentaire", Performance::comment)

        table.column<Performance, HBox>("Modifier") {
            val hbox = HBox(10.0)

            val removeButton = Button("-")

            removeButton.action {
                val alert = Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette prestation ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                alert.title = "Confirmation de la suppression"

                val result = alert.showAndWait()

                if(result.isPresent && result.get() == ButtonType.YES) {
                    Data.performances.remove(it.value)
                    updatePerformance(Data.getPerformances(customerSelected))
                }
            }

            hbox.add(removeButton)

            ReadOnlyObjectWrapper<HBox>(hbox)
        }

        table
    }

    override val root = borderpane {
        left {
            tabpane {
                selectionModel.selectedItemProperty().addListener { _, _, _ ->
                    tabMode = if (selectionModel.selectedIndex == 0) TabMode.Workers else TabMode.Customers

                    centerVbox.clear()
                    rightForm.clear()

                    when (tabMode) {
                        TabMode.Workers -> {
                            firstColumnPerformance.text = "Client"

                            centerVbox.add(performancesTable)
                        }
                        TabMode.Customers -> {
                            firstColumnPerformance.text = "Employé"

                            centerVbox.hbox {
                                spacing = 10.0

                                val workerCombobox = combobox(values = Data.workers.toList()) { }

                                label("Début")

                                val startPicker = JFXTimePicker()
                                startPicker.setIs24HourView(true)
                                startPicker.prefWidth = 100.0
                                add(startPicker)

                                label("Fin")

                                val endPicker = JFXTimePicker()
                                endPicker.setIs24HourView(true)
                                endPicker.prefWidth = 100.0
                                add(endPicker)

                                val commentField = textfield {
                                    promptText = "Commentaire"
                                }

                                button("+") {
                                    action {
                                        if(workerCombobox.selectedItem != null) {
                                            if(startPicker.value != null && endPicker.value != null && startPicker.value <= endPicker.value) {
                                                Data.performances.add(Performance(
                                                        workerCombobox.selectedItem!!.uuid,
                                                        customerSelected!!.uuid,
                                                        startPicker.value.atDate(LocalDate.now()),
                                                        endPicker.value.atDate(LocalDate.now()), commentField.text))
                                                updatePerformance(Data.getPerformances(customerSelected))
                                            }
                                            else {
                                                Alert(Alert.AlertType.INFORMATION, "Les dates sélectionnées sont invalides !", ButtonType.OK).show()
                                            }
                                        }
                                        else {
                                            Alert(Alert.AlertType.INFORMATION, "Aucun employé sélectionné pour la prestation !", ButtonType.OK).show()
                                        }
                                    }
                                }
                            }

                            centerVbox.add(performancesTable)
                            VBox.setVgrow(performancesTable, Priority.ALWAYS)
                        }
                    }
                    updatePerformance(showPerformances)
                }

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tab("Employés") {
                    add(workersView)
                }
                tab("Clients") {
                    add(customersView)
                }
            }

        }

        center = centerVbox

        right = rightForm
    }

    private fun updatePerformance(performances: ObservableList<Performance>) {
        showPerformances.clear()
        showPerformances.addAll(performances)
    }

    private fun updateListsView() {
        workersView.updateSearchList()
        customersView.updateSearchList()
    }

    init {
        this.root.setPrefSize(1100.0, 400.0)
    }
}