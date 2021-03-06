import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.layout.Region
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import tornadofx.*
import views.MainView
import java.io.*

class LCApp : App(MainView::class) {
    private var canSave = true

    companion object {
        var customersFolder: File? = null

        val dataDirectory = File("data")
        val workersJsonFile = File(dataDirectory.absolutePath + "/workers.json")
        val customersJsonFile = File(dataDirectory.absolutePath + "/customers.json")
        val prefJsonFile = File(dataDirectory.absolutePath + "/prefs.json")
        val lockFile = File(dataDirectory.absolutePath + "/lock.lck")


        fun createDirectoryCustomer(customer: Customer) {
            if(customersFolder != null && customersFolder!!.exists()) {
                val customerFolder = File(customersFolder!!.absolutePath + "/${customer.name}")

                val dirs = mutableListOf("TVA", "Courrier", "PDF")

                when(customer.isCompany) {
                    false -> dirs += "I.P.P"
                    true -> dirs += listOf("I.SOC", "Bilan")
                }
                try {
                    if(customerFolder.mkdir()) {
                        dirs.forEach {
                            File(customerFolder.absolutePath + "/$it").mkdir()
                        }
                    }
                } catch(e: IOException) { println(e) }
            }
        }
    }

    init {
        if (lockFile.exists()) {
            val alert = Alert(Alert.AlertType.WARNING, "Le fichier lock existe ce qui signifie que le programme est déjà lancé (sur cette ordinateur ou en réseau) ! Il sera IMPOSSIBLE de sauvegarder les modifications. Si ce programme n'est lancé sur aucun ordinateur, vous pouvez supprimer le fichier ce trouvant à l'emplacement : ${lockFile.absolutePath}")
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE
            alert.showAndWait()
            canSave = false
        } else {
            try {
                if(!dataDirectory.exists())
                    dataDirectory.mkdir()
                lockFile.createNewFile()
                lockFile.deleteOnExit()
            } catch(e: IOException) { println(e) }

        }

        Data.loadData()

        if(prefJsonFile.exists()) {
            val prefReader = JsonReader(FileReader(prefJsonFile))

            try {
                prefReader.beginObject()
                if (prefReader.nextName() == "customersFolder") {
                    val file = File(prefReader.nextString())
                    if (file.exists() && file.isDirectory) {
                        customersFolder = file
                    }
                }
                prefReader.endObject()
            } catch(e: IOException) {
                println(e)
            } finally {
                prefReader.close()
            }
        }

        if(customersFolder == null || !customersFolder!!.exists()) {
            val dialog = DirectoryChooser()
            dialog.title = "Choisir le dossier parent des clients"
            customersFolder = dialog.showDialog(null)
        }
    }

    override fun start(stage: Stage) {
        super.start(stage)

        try {
            addStageIcon(Image(FileInputStream("icon.png")))
        } catch(e: IOException) { println(e) }
    }

    override fun stop() {
        super.stop()

        if (canSave)
            Data.saveData()

        val prefWriter = JsonWriter(FileWriter(prefJsonFile))
        try {
            prefWriter.beginObject()
            prefWriter.name("customersFolder").value(customersFolder?.absolutePath?: "")
            prefWriter.endObject()

            prefWriter.flush()
        } catch(e: Exception) { println(e) }
        finally {
            prefWriter.close()
        }
    }
}