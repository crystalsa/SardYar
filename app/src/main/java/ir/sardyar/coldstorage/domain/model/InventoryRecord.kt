package ir.sardyar.coldstorage.domain.model

data class InventoryRecord(
    val id: String,
    val recordType: RecordType,
    val farmerName: String,
    val farmerPhone: String,
    val variety: AppleVariety,
    val roomPartCode: String = "1.1",
    val boxCount: Int,
    val grossWeightKg: Double,
    val tareWeightKg: Double,
    val netWeightKg: Double,
    val feePerKgOrMonth: Double,
    val driverName: String,
    val plateNumber: String,
    val dateShamsi: String,
    val dateWeekday: String,
    val timeStr: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)
