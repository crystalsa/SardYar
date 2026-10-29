package ir.sardyar.coldstorage.domain.model

import androidx.compose.ui.graphics.Color
import ir.sardyar.coldstorage.core.theme.AppleGreenLight
import ir.sardyar.coldstorage.core.theme.EntryGreen
import ir.sardyar.coldstorage.core.theme.ExitOrange

enum class RecordType(
    val titleFa: String,
    val shortTitle: String,
    val primaryColor: Color,
    val lightColor: Color
) {
    ENTRY("ورود به سردخانه", "ورود", EntryGreen, AppleGreenLight),
    EXIT("ترخیص / خروج", "خروج", ExitOrange, Color(0xFFFFEDD5))
}
