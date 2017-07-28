package views

import javafx.scene.control.TextField
import tornadofx.*
import Customer
import javafx.scene.control.CheckBox
import java.util.*

class AddCustomerView(customerBuildFinish: (customer: Customer) -> Unit): View("Ajouter un client") {
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
                emailField = textfield {  }
            }
            field("Informations") {
                infoField = textfield {  }
            }
            field("Entreprise") {
                isCompanyCkBox = checkbox {  }
            }
        }
        button("Ajouter") {
            action {
                customerBuildFinish(Customer(UUID.randomUUID(), nameField.text, addressField.text, townField.text, tvaField.text, phoneField.text, emailField.text, infoField.text, isCompanyCkBox.isSelected))
                close()
            }
        }
    }
}