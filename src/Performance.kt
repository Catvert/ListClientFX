import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class Performance(val workerUUID: UUID, val customerUUID: UUID, val startDate: LocalDateTime, val endDate: LocalDateTime, val comment: String) {
    fun customer() = Data.customers.items.firstOrNull { it.uuid == customerUUID }
    fun worker() = Data.workers.items.firstOrNull { it.uuid == workerUUID }

    fun duration() = Duration.between(startDate, endDate)
}