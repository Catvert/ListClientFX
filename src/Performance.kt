import tornadofx.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

fun UUID.toCustomer(): Customer? = Data.customers.firstOrNull { it.uuid == this }

class Performance(val workerUUID: UUID, val customerUUID: UUID, val startDate: LocalDateTime, val endDate: LocalDateTime, val comment: String) {
    fun customer() = Data.customers.firstOrNull { it.uuid == customerUUID }
    fun worker() = Data.workers.firstOrNull { it.uuid == workerUUID }

    init {

    }

    fun duration() = Duration.between(startDate, endDate)
}