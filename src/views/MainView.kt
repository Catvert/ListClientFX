package views

import Customer
import Data
import LCApp
import Performance
import Worker
import com.jfoenix.controls.JFXTimePicker
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import tornadofx.*
import java.awt.Desktop
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.Comparator

private enum class TabMode {
    Workers, Customers
}

class MainView : View("Liste des clients") {
    private var customerSelected: Property<Customer?> = SimpleObjectProperty(null)
    private var workerSelected: Property<Worker?> = SimpleObjectProperty(null)

    private val showPerformances = SortedFilteredList<Performance>()

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

    val workersView = SearchListView(Data.workers, true, {
        val addWorkerView = AddWorkerView({
            Data.workers.add(it)
        })
        openInternalWindow(addWorkerView)
    }, {
        workerSelected.value = it
        updatePerformance(Data.getPerformances(it))
    })

    val customersView = SearchListView(Data.customers, true, {
        val addCustomerView = AddCustomerView({
            Data.customers.add(it)
        })

        openInternalWindow(addCustomerView)
    }, {
        customerSelected.value = it

        updatePerformance(Data.getPerformances(it))
        rightForm.clear()

        rightForm.fieldset {
            field("Nom") {
                textfield(it.name) {
                    textProperty().addListener { _, _, _ ->
                        if (!text.isBlank())
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
                        if (LCApp.customersFolder != null) {
                            try {
                                Desktop.getDesktop().open(File(LCApp.customersFolder!!.path + "/${it.name}"))
                            } catch(e: Exception) {
                                println(e)

                                if(LCApp.customersFolder?.exists()?: false) {
                                    val alert = Alert(Alert.AlertType.INFORMATION, "Le dossier du client n'existe pas ! Voulez-vous créer un répertoire pour ce client dans le dossier des clients ?", ButtonType.YES, ButtonType.NO)
                                    alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                                    val result = alert.showAndWait()

                                    if(result.isPresent && result.get() == ButtonType.YES) {
                                        LCApp.createDirectoryCustomer(customerSelected.value!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    })

    private val performancesTable = let {
        val datePattern = "dd-MM-yyyy HH:mm:ss"

        showPerformances.sortedItems.comparator = Comparator<Performance>({ p, p1 -> p.startDate.compareTo(p1.startDate) }).reversed()

        val table = TableView(showPerformances.sortedItems)
        table.prefHeight = 1000.0
        table.columns.add(firstColumnPerformance)
        table.placeholder = Label("Aucune prestation disponible")

        table.column<Performance, LocalDateTime>("Début") {
            it.tableColumn.prefWidth = 150.0

            it.tableColumn.setCellFactory {
                object : TableCell<Performance, LocalDateTime>() {
                    override fun updateItem(item: LocalDateTime?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (item == null && empty) {
                            text = ""
                        } else {
                            text = item!!.format(DateTimeFormatter.ofPattern(datePattern))
                        }
                    }
                }
            }

            ReadOnlyObjectWrapper<LocalDateTime>(it.value.startDate)
        }
        table.column<Performance, LocalDateTime>("Fin") {
            it.tableColumn.prefWidth = 150.0

            it.tableColumn.setCellFactory {
                object : TableCell<Performance, LocalDateTime>() {
                    override fun updateItem(item: LocalDateTime?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (item == null && empty) {
                            text = ""
                        } else {
                            text = item!!.format(DateTimeFormatter.ofPattern(datePattern))
                        }
                    }
                }
            }

            ReadOnlyObjectWrapper<LocalDateTime>(it.value.endDate)
        }
        table.column<Performance, String>("Durée de la prestation") {
            it.tableColumn.prefWidth = 125.0

            val duration = it.value.duration()

            val hours = duration.toHours()
            val minutes = if (hours == 0L) duration.toMinutes() else duration.toMinutes() - hours * 60

            ReadOnlyStringWrapper("$hours h $minutes m -> ${String.format("%.2f", duration.toMinutes() / 60.0)}")
        }
        val commentColumn = table.column("Commentaire", Performance::comment)
        commentColumn.prefWidth = 200.0

        table.column<Performance, HBox>("Supprimer") {
            it.tableColumn.prefWidth = 50.0

            val hbox = HBox(10.0)

            val removeButton = Button("-")

            removeButton.action {
                val alert = Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette prestation ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
                alert.title = "Confirmation de la suppression"

                val result = alert.showAndWait()

                if (result.isPresent && result.get() == ButtonType.YES) {
                    Data.performances.remove(it.value)

                    when (tabMode) {
                        TabMode.Workers -> updatePerformance(Data.getPerformances(workerSelected.value))
                        TabMode.Customers -> updatePerformance(Data.getPerformances(customerSelected.value))
                    }
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
                            firstColumnPerformance.prefWidth = 175.0

                            centerVbox.add(performancesTable)
                        }
                        TabMode.Customers -> {
                            firstColumnPerformance.text = "Employé"
                            firstColumnPerformance.prefWidth = 100.0

                            centerVbox.hbox {
                                customerSelected.addListener { _, _, _ ->
                                    isDisable = customerSelected.value == null
                                }

                                isDisable = true

                                alignment = Pos.CENTER
                                minWidth = 700.0
                                paddingTop = 10.0
                                spacing = 10.0

                                val workerCombobox = combobox(values = Data.workers.toList()) { promptText = "Employé" }

                                val date = datepicker {
                                    prefWidth = 120.0
                                    value = LocalDate.now()
                                }

                                val startPicker = JFXTimePicker()
                                startPicker.promptText = "Début"
                                startPicker.setIs24HourView(true)
                                startPicker.prefWidth = 100.0
                                add(startPicker)

                                val endPicker = JFXTimePicker()
                                endPicker.promptText = "Fin"
                                endPicker.setIs24HourView(true)
                                endPicker.prefWidth = 100.0
                                add(endPicker)

                                val commentField = textfield {
                                    promptText = "Commentaire"
                                }

                                button("+") {
                                    action {
                                        if (customerSelected.value != null) {
                                            if (workerCombobox.selectedItem != null) {
                                                if (startPicker.value != null && endPicker.value != null && startPicker.value <= endPicker.value) {
                                                    Data.performances.add(Performance(
                                                            workerCombobox.selectedItem!!.uuid,
                                                            customerSelected.value!!.uuid,
                                                            startPicker.value.atDate(date.value),
                                                            endPicker.value.atDate(date.value), commentField.text))
                                                    updatePerformance(Data.getPerformances(customerSelected.value))
                                                } else {
                                                    Alert(Alert.AlertType.INFORMATION, "Les dates sélectionnées sont invalides !", ButtonType.OK).show()
                                                }
                                            } else {
                                                Alert(Alert.AlertType.INFORMATION, "Aucun employé sélectionné pour la prestation !", ButtonType.OK).show()
                                            }
                                        } else {
                                            Alert(Alert.AlertType.INFORMATION, "Aucun client sélectionné ! (Double clique requis)", ButtonType.OK).show()
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

    init {
        root.setPrefSize(1250.0, 400.0)

        customerSelected.addListener { _, _, _ ->
            if (customerSelected.value == null)
                customersView.listView.selectionModel.clearSelection()
        }
    }
}