import com.github.salomonbrys.kotson.*
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.layout.Region
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import tornadofx.*
import views.MainView
import java.io.File
import java.io.FileReader

class LCApp : App(MainView::class, Styles::class) {
    private var canSave = true

    companion object {
        var customersFolder: File? = null

        val workersJsonFile = File("data/workers.json")
        val customersJsonFile = File("data/customers.json")
        val prefJsonFile = File("data/prefs.json")
        val lockFile = File("data/lock.lck")
    }

    init {
        if(!workersJsonFile.exists() || !customersJsonFile.exists()) {
            Alert(Alert.AlertType.ERROR, "Les fichiers de données sont introuvables ! :(", ButtonType.OK).showAndWait()
            canSave = false
            stop()
        }

        if(lockFile.exists()) {
            val alert = Alert(Alert.AlertType.WARNING, "Le fichier lock existe ce qui signifie que le programme est déjà lancé (sur cette ordinateur ou en réseau) ! Il sera IMPOSSIBLE de sauvegarder les modifications. Si ce programme n'est lancé sur aucun ordinateur, vous pouvez supprimer le fichier ce trouvant à l'emplacement : ${lockFile.absolutePath}")
            alert.isResizable = true
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE
            alert.showAndWait()
            canSave = false
        }
        else {
            lockFile.createNewFile()
            lockFile.deleteOnExit()
        }

        Data.loadData()

        try {
            val prefReader = JsonReader(FileReader(prefJsonFile))

            prefReader.beginObject()
            if(prefReader.nextName() == "customersFolder") {
                val file = File(prefReader.nextString())
                if(file.exists() && file.isDirectory) {
                    customersFolder = file
                }
            }
            prefReader.endObject()
        } catch(e: Exception) { println(e) }
    }

    override fun stop() {
        super.stop()

        if(canSave)
            Data.saveData()
    }
}