package ir.sardyar.coldstorage.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sardyar.coldstorage.core.theme.BorderColor
import ir.sardyar.coldstorage.core.theme.EmeraldContainer
import ir.sardyar.coldstorage.core.theme.EmeraldPrimary
import ir.sardyar.coldstorage.core.theme.Surface
import ir.sardyar.coldstorage.core.theme.SurfaceVariant
import ir.sardyar.coldstorage.core.theme.TextMuted
import ir.sardyar.coldstorage.core.theme.TextPrimary
import ir.sardyar.coldstorage.core.theme.TextSecondary
import ir.sardyar.coldstorage.core.theme.VazirmatnFamily
import ir.sardyar.coldstorage.core.util.JalaliDateHelper

/**
 * دیالوگ تقویم شمسی با قابلیت انتخاب سال، ماه و روز
 */
@Composable
fun JalaliDatePickerDialog(
    initialJalali: JalaliDateHelper.Jalali = JalaliDateHelper.getTodayJalali(),
    onDateSelected: (JalaliDateHelper.Jalali) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialJalali.year) }
    var selectedMonth by remember { mutableIntStateOf(initialJalali.month) }
    var selectedDay by remember { mutableIntStateOf(initialJalali.day) }

    val daysInMonth = JalaliDateHelper.getDaysInMonth(selectedYear, selectedMonth)
    // اطمینان از اینکه روز انتخابی خارج از محدوده ماه نباشد
    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    val currentJalali = JalaliDateHelper.getJalali(selectedYear, selectedMonth, selectedDay)
    val formattedCurrent = JalaliDateHelper.formatFullWithDay(currentJalali, toPersianDigits = true)

    val monthsList = listOf(
        1 to "فروردین", 2 to "اردیبهشت", 3 to "خرداد",
        4 to "تیر", 5 to "مرداد", 6 to "شهریور",
        7 to "مهر", 8 to "آبان", 9 to "آذر",
        10 to "دی", 11 to "بهمن", 12 to "اسفند"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "انتخاب تاریخ شمسی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = VazirmatnFamily
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // هدر نمایش تاریخ جاری انتخاب‌شده
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedCurrent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = EmeraldPrimary,
                        fontFamily = VazirmatnFamily,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // انتخاب سال با دکمه‌های چپ و راست و نمایش سال
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "سال بعد", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    Text(
                        text = "سال ${JalaliDateHelper.toPersianDigits(selectedYear.toString())}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontFamily = VazirmatnFamily
                    )

                    IconButton(
                        onClick = { selectedYear-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "سال قبل", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // انتخاب ماه (شبکه ۳ ستونه ماه ها)
                Text(
                    text = "انتخاب ماه:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(monthsList) { (mNum, mName) ->
                        val isSelected = selectedMonth == mNum
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary else Surface)
                                .border(1.dp, if (isSelected) EmeraldPrimary else BorderColor, RoundedCornerShape(8.dp))
                                .clickable { selectedMonth = mNum }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // انتخاب روز
                Text(
                    text = "انتخاب روز ماه (${JalaliDateHelper.toPersianDigits(daysInMonth.toString())} روز):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items((1..daysInMonth).toList()) { d ->
                        val isSelected = selectedDay == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary else Surface)
                                .border(1.dp, if (isSelected) EmeraldPrimary else BorderColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { selectedDay = d }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = JalaliDateHelper.toPersianDigits(d.toString()),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalJalali = JalaliDateHelper.getJalali(selectedYear, selectedMonth, selectedDay)
                    onDateSelected(finalJalali)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تایید تاریخ", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        val today = JalaliDateHelper.getTodayJalali()
                        selectedYear = today.year
                        selectedMonth = today.month
                        selectedDay = today.day
                    }
                ) {
                    Text("امروز", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                }
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = TextSecondary, fontFamily = VazirmatnFamily)
                }
            }
        }
    )
}
