package views

import Customer
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import tornadofx.*
import java.util.*

class AddCustomerView(customerBuildFinish: (customer: Customer) -> Unit) : View("Ajouter un client") {
    private var nameField by singleAssign<TextField>()
    private var addressField by singleAssign<TextField>()
    private var townField by singleAssign<TextField>()
    private var tvaField by singleAssign<TextField>()
    private var phoneField by singleAssign<TextField>()
    private var emailField by singleAssign<TextField>()
    private var infoField by singleAssign<TextField>()
    private var isCompanyCkBox by singleAssign<CheckBox>()

    override val root = form {
        fieldset("Informations") {
            field("Nom") {
                nameField = textfield {}
            }
            field("Adresse") {
                addressField = textfield { }
            }
            field("Commune") {
                townField = textfield { }
            }
            field("N° TVA") {
                tvaField = textfield { }
            }
            field("N° Téléphone") {
                phoneField = textfield { }
            }
            field("Email") {
                emailField = textfield { }
            }
            field("Informations") {
                infoField = textfield { }
            }
            field("Entreprise") {
                isCompanyCkBox = checkbox { }
            }
        }
        button("Ajouter") {
            action {
                val customer = Customer(UUID.randomUUID(), nameField.text, addressField.text, townField.text, tvaField.text, phoneField.text, emailField.text, infoField.text, isCompanyCkBox.isSelected)

                if(LCApp.customersFolder?.exists()?: false) {
                    val alert = Alert(Alert.AlertType.INFORMATION, "Voulez-vous créer un répertoire pour ce client dans le dossier des clients ?", ButtonType.YES, ButtonType.NO)
                    alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                    val result = alert.showAndWait()

                    if(result.isPresent && result.get() == ButtonType.YES) {
                        LCApp.createDirectoryCustomer(customer)
                    }
                }

                customerBuildFinish(customer)
                close()
            }
        }
    }
}