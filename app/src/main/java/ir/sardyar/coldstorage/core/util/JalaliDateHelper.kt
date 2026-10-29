package ir.sardyar.coldstorage.core.util

import java.util.Calendar

object JalaliDateHelper {

    private val persianWeekdays = arrayOf(
        "شنبه",
        "یکشنبه",
        "دوشنبه",
        "سه‌شنبه",
        "چهارشنبه",
        "پنج‌شنبه",
        "جمعه"
    )

    private val persianMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    data class Jalali(val year: Int, val month: Int, val day: Int, val dayOfWeek: String)

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int, dayOfWeekIdx: Int): Jalali {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }

        // Calendar.SATURDAY is 7, Calendar.SUNDAY is 1
        val weekdayName = when (dayOfWeekIdx) {
            Calendar.SATURDAY -> persianWeekdays[0]
            Calendar.SUNDAY -> persianWeekdays[1]
            Calendar.MONDAY -> persianWeekdays[2]
            Calendar.TUESDAY -> persianWeekdays[3]
            Calendar.WEDNESDAY -> persianWeekdays[4]
            Calendar.THURSDAY -> persianWeekdays[5]
            Calendar.FRIDAY -> persianWeekdays[6]
            else -> persianWeekdays[0]
        }

        return Jalali(jy, jm, jd, weekdayName)
    }

    fun getTodayJalali(): Jalali {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getJalaliForOffset(dayOffset: Int): Jalali {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getMonthName(month: Int): String {
        return if (month in 1..12) persianMonths[month - 1] else ""
    }

    fun isLeapJalaliYear(year: Int): Boolean {
        val r = year % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapJalaliYear(year)) 30 else 29
            else -> 30
        }
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1
        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4)
        for (i in 0 until jm2) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd2
        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097
        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }
        val gDaysInMonth = intArrayOf(
            31, if (leap || (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 12 && gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        val gd = gDayNo + 1
        return Triple(gy, gm + 1, gd)
    }

    fun getJalali(year: Int, month: Int, day: Int): Jalali {
        val (gy, gm, gd) = jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance().apply {
            set(gy, gm - 1, gd)
        }
        return gregorianToJalali(gy, gm, gd, cal.get(Calendar.DAY_OF_WEEK))
    }

    fun getOffsetForJalali(year: Int, month: Int, day: Int): Int {
        val (gy, gm, gd) = jalaliToGregorian(year, month, day)
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(gy, gm - 1, gd)
        }
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
        return Math.round(diffMillis.toDouble() / (24 * 60 * 60 * 1000.0)).toInt()
    }

    fun parseShortDate(dateStr: String): Jalali? {
        val english = toEnglishDigits(dateStr).trim()
        val parts = english.split('/')
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return getJalali(y, m, d)
    }

    fun formatFullWithDay(jalali: Jalali, toPersianDigits: Boolean = true): String {
        val monthName = if (jalali.month in 1..12) persianMonths[jalali.month - 1] else ""
        val text = "${jalali.dayOfWeek}، ${jalali.day} $monthName ${jalali.year}"
        return if (toPersianDigits) toPersianDigits(text) else text
    }

    fun formatShort(jalali: Jalali, toPersianDigits: Boolean = true): String {
        val y = jalali.year.toString().padStart(4, '0')
        val m = jalali.month.toString().padStart(2, '0')
        val d = jalali.day.toString().padStart(2, '0')
        val text = "$y/$m/$d"
        return if (toPersianDigits) toPersianDigits(text) else text
    }

    fun toPersianDigits(input: String): String {
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        var out = input
        for (i in 0..9) {
            out = out.replace(en[i], fa[i])
        }
        return out
    }

    fun toEnglishDigits(input: String): String {
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var out = input
        for (i in 0..9) {
            out = out.replace(fa[i], en[i])
        }
        return out
    }
}
