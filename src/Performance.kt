import java.time.Duration
import java.time.LocalDateTime
import java.util.*

fun UUID.toCustomer() = Data.customers.firstOrNull { it.uuid == this }
fun UUID.toWorker() = Data.workers.firstOrNull { it.uuid == this }

class Performance(val workerUUID: UUID, val customerUUID: UUID, val startDate: LocalDateTime, val endDate: LocalDateTime, val comment: String) {
    fun customer() = Data.customers.firstOrNull { it.uuid == customerUUID }
    fun worker() = Data.workers.firstOrNull { it.uuid == workerUUID }

    fun duration() = Duration.between(startDate, endDate)
}