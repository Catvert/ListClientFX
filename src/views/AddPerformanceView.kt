package views

import Customer
import Data
import Performance
import Worker
import com.jfoenix.controls.JFXTimePicker
import javafx.scene.control.TextArea
import tornadofx.*
import java.time.LocalDate

class AddPerformanceView(worker: Worker, performanceBuildFinish: (performance: Performance) -> Unit) : View("Ajouter une prestation - ${worker.name}") {
    private var startPicker by singleAssign<JFXTimePicker>()
    private var endPicker by singleAssign<JFXTimePicker>()
    private var commentArea by singleAssign<TextArea>()

    private var customer: Customer? = null

    override val root = hbox {
        add(SearchListView(Data.customers, false, {}, {
            customer = it
        }))

        vbox {
            hbox {
                label("Début")
                startPicker = JFXTimePicker()
                startPicker.setIs24HourView(true)
                add(startPicker)
            }
            hbox {
                label("Fin")
                endPicker = JFXTimePicker()
                endPicker.setIs24HourView(true)
                add(endPicker)
            }

            commentArea = textarea {
                promptText = "Commentaire"
            }

            button("Ajouter") {
                action {
                    if (customer != null) {
                        performanceBuildFinish(Performance(worker.uuid, customer!!.uuid, startPicker.value.atDate(LocalDate.now()), endPicker.value.atDate(LocalDate.now()), commentArea.text))
                        close()
                    }
                }
            }
        }
    }
}