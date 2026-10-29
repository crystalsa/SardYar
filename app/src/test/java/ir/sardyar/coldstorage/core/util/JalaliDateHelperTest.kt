package ir.sardyar.coldstorage.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class JalaliDateHelperTest {

    @Test
    fun testConversionKnownDate() {
        // 2024-10-27 is 1403-08-06 (6 Aban 1403)
        val j = JalaliDateHelper.gregorianToJalali(2024, 10, 27, Calendar.SUNDAY)
        assertEquals(1403, j.year)
        assertEquals(8, j.month)
        assertEquals(6, j.day)
    }

    @Test
    fun testConversionToday2026() {
        // 2026-10-27 is 1405-08-05 (5 Aban 1405)
        val j = JalaliDateHelper.gregorianToJalali(2026, 10, 27, Calendar.TUESDAY)
        assertEquals(1405, j.year)
        assertEquals(8, j.month)
        assertEquals(5, j.day)
    }
}
