package ir.sardyar.coldstorage.domain.model

import androidx.compose.ui.graphics.Color
import ir.sardyar.coldstorage.core.theme.AppleGrannySmith
import ir.sardyar.coldstorage.core.theme.AppleGrannySmithLight
import ir.sardyar.coldstorage.core.theme.AppleRed
import ir.sardyar.coldstorage.core.theme.AppleRedLight
import ir.sardyar.coldstorage.core.theme.AppleYellow
import ir.sardyar.coldstorage.core.theme.AppleYellowLight

enum class AppleVariety(
    val titleFa: String,
    val titleEn: String,
    val primaryColor: Color,
    val lightColor: Color
) {
    RED_DELICIOUS("سیب قرمز استخوانی", "Red Delicious", AppleRed, AppleRedLight),
    GOLDEN_DELICIOUS("سیب زرد لبنانی", "Golden Delicious", AppleYellow, AppleYellowLight),
    GRANNY_SMITH("سیب ترش سبز", "Granny Smith", AppleGrannySmith, AppleGrannySmithLight),
    SUMMER_APPLE("سیب گلاب / تابستانه", "Summer Apple", Color(0xFFE11D48), Color(0xFFFFE4E6)),
    OTHER("سایر ارقام", "Other Variety", Color(0xFF64748B), Color(0xFFF1F5F9))
}
