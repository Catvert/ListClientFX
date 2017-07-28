import java.util.*

class Customer(val uuid: UUID,
               var name: String,
               var address: String,
               var town: String,
               var tva: String,
               var phoneNumber: String,
               var email: String,
               var info: String,
               var isCompany: Boolean) {
    override fun toString(): String {
        return name
    }
}