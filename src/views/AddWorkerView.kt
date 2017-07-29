package views

import Worker
import javafx.scene.control.TextField
import tornadofx.*
import java.util.*

class AddWorkerView(workerBuildFinish: (worker: Worker) -> Unit) : View("Ajouter un employé") {
    private var nameField by singleAssign<TextField>()

    override val root = form {
        fieldset("Informations") {
            field("Nom") {
                nameField = textfield { }
            }
        }

        button("Ajouter") {
            action {
                workerBuildFinish(Worker(nameField.text, UUID.randomUUID()))
                close()
            }
        }
    }
}