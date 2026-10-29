package ir.sardyar.coldstorage.domain.model

enum class VehicleType(val titleFa: String) {
    PICKUP_VAN("وانت بار"),
    NISSAN("نیسان وانت"),
    KHAVAR("خاور / کامیونت"),
    TRUCK("کامیون تک / جفت"),
    TRAILER("تریلی / کانتینر یخچال‌دار"),
    OTHER("سایر")
}

enum class FeeCalculationType(val titleFa: String) {
    PER_BOX("بر حسب تعداد جعبه"),
    PER_VEHICLE("به ازای کل ماشین")
}

/**
 * مدل‌های سبد سردخانه با اولویت و ترتیب انتخابی
 */
enum class BoxType(val titleFa: String, val avgWeightKg: Double) {
    TWO_ROW("دو ردیفه", 14.0),
    THREE_ROW("سه ردیفه", 18.0),
    SINGLE_ROW("تک ردیفه", 8.0),
    KG_20("۲۰ کیلویی", 20.0),
    KG_30("۳۰ کیلویی", 30.0),
    OTHER("سایر", 15.0)
}

/**
 * دسته‌بندی اصلی سیب (سیب زرد و سیب قرمز)
 */
enum class AppleCategory(val titleFa: String) {
    YELLOW("سیب زرد"),
    RED("سیب قرمز"),
    CUSTOM("سایر (نوع دلخواه)");

    companion object {
        // برای سازگاری با رکوردهای قبلی
        val GOLDEN_PREMIUM = YELLOW
        val RED_GRADE_ONE = RED
        val EXPORT_GRADE = RED
        val FALLEN_OR_INDUSTRIAL = CUSTOM
    }
}
