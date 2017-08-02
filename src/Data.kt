import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Data {
    companion object {
        val workers = SortedFilteredList<Worker>()
        val customers = SortedFilteredList<Customer>()
        val performances: ObservableList<Performance> = FXCollections.observableArrayList()

        private var actualWorkerUUID: UUID = UUID.randomUUID()

        private val customerGson = GsonBuilder().registerTypeAdapter<Customer> {
            read {
                beginObject()

                var uuid = UUID.randomUUID()
                var customerName = ""
                var address = ""
                var town = ""
                var tva = ""
                var phone = ""
                var email = ""
                var info = ""
                var isCompany = false

                /* Code pour convertir depuis la version 2.0 de ListClient
                while(hasNext()) {
                    when(nextName()) {
                        "GUID" -> uuid = UUID.fromString(nextString())
                        "Name" -> customerName = nextString()
                        "Address" -> address = nextString()
                        "Commune" -> town = nextString()
                        "Tva" -> tva = nextString()
                        "PhoneNumber" -> phone = nextString()
                        "Email" -> email = nextString()
                        "Info" -> info = nextString()
                        "IsEntreprise" -> isCompany = nextBoolean()
                    }
                }*/

                while (hasNext()) {
                    when (nextName()) {
                        "UUID" -> uuid = UUID.fromString(nextString())
                        "name" -> customerName = nextString()
                        "address" -> address = nextString()
                        "town" -> town = nextString()
                        "TVA" -> tva = nextString()
                        "phone" -> phone = nextString()
                        "email" -> email = nextString()
                        "info" -> info = nextString()
                        "isCompany" -> isCompany = nextBoolean()
                    }
                }

                endObject()

                Customer(uuid, customerName, address, town, tva, phone, email, info, isCompany)
            }

            write {
                beginObject()
                name("UUID").value(it.uuid.toString())
                name("name").value(it.name)
                name("address").value(it.address)
                name("town").value(it.town)
                name("TVA").value(it.tva)
                name("phone").value(it.phoneNumber)
                name("email").value(it.email)
                name("info").value(it.info)
                name("isCompany").value(it.isCompany)
                endObject()
            }
        }.setPrettyPrinting().create()


        private val performanceGson = GsonBuilder().registerTypeAdapter<Performance> {
            read {
                beginObject()

                var customerUUID: UUID? = null
                var startPerformance: LocalDateTime? = null
                var endPerformance: LocalDateTime? = null
                var comment = ""

                /* Code pour convertir depuis la version 2.0 de ListClient
                while(hasNext()) {
                    val name = nextName()
                    when(name) {
                        "ClientName" -> nextString()
                        "ClientGUID" -> customerUUID = UUID.fromString(nextString())
                        "StartPrestation" -> startPerformance = LocalDateTime.parse(nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        "EndPrestation" -> endPerformance = LocalDateTime.parse(nextString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        "Date" -> nextString()
                        "Comment" -> comment = nextString()
                    }
                }*/

                while (hasNext()) {
                    val name = nextName()
                    when (name) {
                        "customerUUID" -> customerUUID = UUID.fromString(nextString())
                        "start" -> startPerformance = LocalDateTime.parse(nextString(), DateTimeFormatter.ISO_DATE_TIME)
                        "end" -> endPerformance = LocalDateTime.parse(nextString(), DateTimeFormatter.ISO_DATE_TIME)
                        "comment" -> comment = nextString()
                    }
                }

                endObject()

                Performance(actualWorkerUUID, customerUUID!!, startPerformance!!, endPerformance!!, comment)
            }

            write {
                beginObject()

                name("customerUUID").value(it.customerUUID.toString())
                name("start").value(it.startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                name("end").value(it.endDate.format(DateTimeFormatter.ISO_DATE_TIME))
                name("comment").value(it.comment)

                endObject()
            }
        }.setPrettyPrinting().create()

        private val workerGson = GsonBuilder().registerTypeAdapter<Worker> {
            read {
                beginObject()

                var name = ""
                actualWorkerUUID = UUID.randomUUID()

                while (hasNext()) {
                    when (nextName()) {
                        "name" -> name = nextString()
                        "performances" -> {
                            val perf = performanceGson.fromJson<List<Performance>>(this)
                            performances.addAll(perf)
                        }
                    }
                }

                endObject()

                Worker(name, actualWorkerUUID)
            }

            write {
                beginObject()

                name("name").value(it.name)

                name("performances").beginArray()
                getPerformances(it).forEach {
                    performanceGson.toJson(it, Performance::class.java, this)
                }
                endArray()

                endObject()
            }
        }.setPrettyPrinting().create()

        fun getPerformances(worker: Worker?): ObservableList<Performance> {
            val list = FXCollections.observableArrayList<Performance>()

            performances.forEach {
                if (worker?.uuid == it.workerUUID)
                    list += it
            }

            val c = Comparator<Performance>({ p, p1 -> p.startDate.compareTo(p1.startDate) })
            FXCollections.sort(list, c.reversed())

            return list
        }

        fun getPerformances(customer: Customer?): ObservableList<Performance> {
            val list = FXCollections.observableArrayList<Performance>()

            performances.forEach {
                if (customer?.uuid == it.customerUUID)
                    list += it
            }

            return list
        }

        fun saveData() {
            val customerWriter = JsonWriter(FileWriter(LCApp.customersJsonFile))

            customerWriter.beginArray()
            customers.items.forEach {
                customerGson.toJson(it, Customer::class.java, customerWriter)
            }
            customerWriter.endArray()
            customerWriter.close()

            val workersWriter = JsonWriter(FileWriter(LCApp.workersJsonFile))
            workersWriter.beginArray()
            workers.items.forEach {
                workerGson.toJson(it, Worker::class.java, workersWriter)
            }
            workersWriter.endArray()
            workersWriter.close()
        }

        fun loadData() {
            if(LCApp.customersJsonFile.exists() && LCApp.workersJsonFile.exists()) {
                val customersReader = JsonReader(FileReader(LCApp.customersJsonFile))
                customers.addAll(customerGson.fromJson<List<Customer>>(customersReader))
                customersReader.close()

                val workersReader = JsonReader(FileReader(LCApp.workersJsonFile))
                workers.addAll(workerGson.fromJson<List<Worker>>(workersReader))
                workersReader.close()
            }
        }
    }
}