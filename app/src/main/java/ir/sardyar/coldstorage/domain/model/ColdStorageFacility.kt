package ir.sardyar.coldstorage.domain.model

import androidx.compose.ui.graphics.Color

/**
 * مدل مدیریت سردخانه‌ها (چند سردخانه‌ای)
 */
data class ColdStorageFacility(
    val id: String,
    val name: String,
    val labelColorHex: String = "#10B981", // رنگ شناسه/برچسب سردخانه به فرمت Hex
    val managerName: String = "",
    val capacityBoxes: Int = 100000,
    val phone: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val composeColor: Color
        get() = try {
            val cleanHex = labelColorHex.removePrefix("#")
            val colorLong = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorLong)
            } else {
                Color(colorLong)
            }
        } catch (_: Exception) {
            Color(0xFF10B981)
        }

    companion object {
        // پالت رنگ‌های شکیل، استاندارد و متریال ۳ برای برچسب سردخانه‌ها
        val PRESET_COLORS = listOf(
            PresetColor("#10B981", "سبز زمردی"),
            PresetColor("#0EA5E9", "آبی یخی"),
            PresetColor("#6366F1", "نیلی رویال"),
            PresetColor("#F59E0B", "کهربایی زرین"),
            PresetColor("#EF4444", "یاقوتی قرمز"),
            PresetColor("#8B5CF6", "بنفش کریستال"),
            PresetColor("#14B8A6", "فیروزه‌ای"),
            PresetColor("#F97316", "نارنجی آفتابی")
        )
    }
}

data class PresetColor(
    val hex: String,
    val titleFa: String
)
