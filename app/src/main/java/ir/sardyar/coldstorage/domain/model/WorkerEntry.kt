package ir.sardyar.coldstorage.domain.model

enum class WorkerRole(val titleFa: String) {
    UNLOADER("تخلیه‌کار"),
    LOADER("بارگیر / بارگیری"),
    STACKER("چیدمان در سالن"),
    FORKLIFT_OPERATOR("راننده لیفتراک"),
    SCALE_OPERATOR("متصدی باسکول"),
    OTHER("سایر")
}

data class WorkerEntry(
    val id: String,
    val name: String,
    val role: WorkerRole,
    val agreedWage: Double, // دستمزد توافقی (تومان)
    val paidAmount: Double = 0.0, // مبلغ پرداخت شده (تومان)
    val isSettled: Boolean = false, // وضعیت تسویه با کارگر
    val settlementDate: String? = null // تاریخ تسویه حساب
) {
    val remainingWage: Double
        get() = if (isSettled) 0.0 else (agreedWage - paidAmount).coerceAtLeast(0.0)
}
