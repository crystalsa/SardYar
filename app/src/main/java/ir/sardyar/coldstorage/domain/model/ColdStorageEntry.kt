package ir.sardyar.coldstorage.domain.model

/**
 * مدل اصلی ورود و خروج سردخانه مبتنی بر سیستم کد بار (اتاق . پارت)
 * کد ۳.۸ مرجع اصلی بار است که از طرف سردخانه به کشاورز داده می‌شود (سمت چپ: اتاق ۳، سمت راست: پارت ۸)
 */
data class ColdStorageEntry(
    val id: String,
    val cargoCode: String = "1.1", // کد اصلی بار تحویل‌شده به کشاورز (مثلاً 3.8: اتاق ۳ پارت ۸)
    val facilityId: String = "FAC-01",
    val operationType: RecordType,
    val farmerName: String, // نام کشاورز / صاحب بار (یک شخص ممکن است چند کد بار مانند 3.8 و 3.9 داشته باشد)
    val farmerPhone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dateShamsi: String,
    val dateWeekday: String,
    val timeStr: String,
    val appleCategory: AppleCategory,
    val customAppleName: String = "",
    val isGradeTwo: Boolean = false, // آیا سیب درجه ۲ است؟
    val boxType: BoxType = BoxType.TWO_ROW, // مدل سبد (دو ردیفه، سه ردیفه، تک ردیفه، ۲۰ کیلویی، ۳۰ کیلویی، سایر)
    val boxCount: Int,
    val grossWeightKg: Double = 0.0,
    val tareWeightKg: Double = 0.0, // افت سبدها (کیلوگرم)
    val netWeightKg: Double = if (grossWeightKg > 0) (grossWeightKg - tareWeightKg).coerceAtLeast(0.0) else 0.0,
    val estimatedTonnage: Double = if (netWeightKg > 0) (netWeightKg / 1000.0) else 0.0,
    val vehicleType: VehicleType = VehicleType.NISSAN,
    val plateNumber: String = "",
    val driverName: String = "",
    val feeCalculationType: FeeCalculationType = FeeCalculationType.PER_BOX,
    val unitRate: Double = 0.0,
    val workers: List<WorkerEntry> = emptyList(),
    val notes: String = ""
) {
    // شماره اتاق از سمت چپ کد (مثلاً از 3.8 عدد 3)
    val roomNumber: String
        get() = cargoCode.substringBefore('.').trim()

    // شماره پارت از سمت راست کد (مثلاً از 3.8 عدد 8)
    val partNumber: String
        get() = if (cargoCode.contains('.')) cargoCode.substringAfter('.').trim() else ""

    // جزئیات کد بار جهت راهنمایی کاربر
    val displayCargoDetails: String
        get() = if (cargoCode.isNotBlank()) "اتاق $roomNumber • پارت $partNumber" else ""

    // سازگاری با سایر بخش‌ها
    val lotCode: String
        get() = cargoCode

    val roomPartCode: String
        get() = cargoCode

    val displayRoomPart: String
        get() = displayCargoDetails

    val displayAppleName: String
        get() {
            val base = if (appleCategory == AppleCategory.CUSTOM && customAppleName.isNotBlank()) {
                customAppleName.trim()
            } else {
                appleCategory.titleFa
            }
            return if (isGradeTwo) "$base (درجه ۲)" else base
        }

    val totalHandlingFee: Double
        get() = if (feeCalculationType == FeeCalculationType.PER_BOX) {
            boxCount * unitRate
        } else {
            unitRate
        }

    val totalWorkersWage: Double
        get() = workers.sumOf { it.agreedWage }

    val totalWorkersPaid: Double
        get() = workers.sumOf { it.paidAmount }

    val totalWorkersRemaining: Double
        get() = workers.sumOf { it.remainingWage }

    val isWorkersFullySettled: Boolean
        get() = workers.isEmpty() || workers.all { it.isSettled }

    // آیا باسکول (وزن ناخالص) برای این بار ثبت شده است؟
    val hasWeightRecorded: Boolean
        get() = grossWeightKg > 0.0
}

/**
 * خلاصه موجودی سبدها بر پایه کد بار (مثلا کد 3.8 یا 3.9)
 */
data class CargoCodeStockBalance(
    val cargoCode: String, // کد بار داده شده به کشاورز (مثلا 3.8)
    val ownerName: String, // نام صاحب بار
    val appleName: String,
    val totalInBoxes: Int,
    val totalOutBoxes: Int,
    val remainingBoxes: Int,
    val totalNetWeightKg: Double
) {
    val roomNumber: String
        get() = cargoCode.substringBefore('.').trim()

    val partNumber: String
        get() = if (cargoCode.contains('.')) cargoCode.substringAfter('.').trim() else ""

    val displayDetails: String
        get() = "اتاق $roomNumber • پارت $partNumber"

    // سازگاری نام فیلدها
    val lotCode: String
        get() = cargoCode

    val roomPartCode: String
        get() = cargoCode
}

// برای سازگاری عقب‌رو
typealias LotStockBalance = CargoCodeStockBalance
