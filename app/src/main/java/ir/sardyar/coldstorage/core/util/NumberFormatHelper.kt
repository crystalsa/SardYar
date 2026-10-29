package ir.sardyar.coldstorage.core.util

import java.text.DecimalFormat

object NumberFormatHelper {

    private val formatter = DecimalFormat("#,###")

    fun format(value: Number?, toPersian: Boolean = true): String {
        if (value == null) return if (toPersian) "۰" else "0"
        val formatted = formatter.format(value)
        return if (toPersian) JalaliDateHelper.toPersianDigits(formatted) else formatted
    }

    fun formatWeightKg(kg: Double?, toPersian: Boolean = true): String {
        if (kg == null) return if (toPersian) "۰ کیلوگرم" else "0 kg"
        return "${format(kg, toPersian)} کیلوگرم"
    }

    fun formatWeightTon(kg: Double?, toPersian: Boolean = true): String {
        if (kg == null || kg == 0.0) return if (toPersian) "۰ تن" else "0 ton"
        val tons = kg / 1000.0
        val text = if (tons % 1.0 == 0.0) tons.toLong().toString() else String.format("%.2f", tons)
        val withComma = formatter.format(text.toDoubleOrNull() ?: 0.0)
        return if (toPersian) "${JalaliDateHelper.toPersianDigits(withComma)} تن" else "$withComma تن"
    }

    fun formatBoxes(count: Int?, toPersian: Boolean = true): String {
        if (count == null) return if (toPersian) "۰ جعبه" else "0 boxes"
        return "${format(count, toPersian)} جعبه"
    }

    fun formatToman(amount: Number?, toPersian: Boolean = true): String {
        if (amount == null) return if (toPersian) "۰ تومان" else "0 Toman"
        return "${format(amount.toLong(), toPersian)} تومان"
    }

    fun formatTonnage(tons: Double?, toPersian: Boolean = true): String {
        if (tons == null || tons == 0.0) return if (toPersian) "۰" else "0"
        val text = if (tons % 1.0 == 0.0) tons.toLong().toString() else String.format("%.2f", tons)
        val withComma = formatter.format(text.toDoubleOrNull() ?: 0.0)
        return if (toPersian) JalaliDateHelper.toPersianDigits(withComma) else withComma
    }

    fun parseDouble(input: String): Double {
        val cleaned = JalaliDateHelper.toEnglishDigits(input)
            .replace(",", "")
            .replace(" ", "")
            .trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    fun parseInt(input: String): Int {
        return parseDouble(input).toInt()
    }
}
