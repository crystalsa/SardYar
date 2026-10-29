package ir.sardyar.coldstorage.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import ir.sardyar.coldstorage.core.util.BackupRestoreHelper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sardyar.coldstorage.core.theme.AppleGreenLight
import ir.sardyar.coldstorage.core.theme.BorderColor
import ir.sardyar.coldstorage.core.theme.ColdIce
import ir.sardyar.coldstorage.core.theme.ColdIceLight
import ir.sardyar.coldstorage.core.theme.EmeraldContainer
import ir.sardyar.coldstorage.core.theme.EmeraldPrimary
import ir.sardyar.coldstorage.core.theme.EntryGreen
import ir.sardyar.coldstorage.core.theme.ExitOrange
import ir.sardyar.coldstorage.core.theme.Surface
import ir.sardyar.coldstorage.core.theme.SurfaceVariant
import ir.sardyar.coldstorage.core.theme.TextMuted
import ir.sardyar.coldstorage.core.theme.TextPrimary
import ir.sardyar.coldstorage.core.theme.TextSecondary
import ir.sardyar.coldstorage.core.theme.VazirmatnFamily
import ir.sardyar.coldstorage.core.util.JalaliDateHelper
import ir.sardyar.coldstorage.core.util.NumberFormatHelper
import ir.sardyar.coldstorage.core.util.ReportExportHelper
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.presentation.InventoryUiState

enum class ReportKind(val titleFa: String) {
    SYSTEM_OVERALL("گزارش کلی سیستم"),
    DAILY_SPECIFIC("گزارش روزانه خاص"),
    BY_APPLE_VARIETY("گزارش تفکیک نوع سیب"),
    WORKERS_AND_EXPENSES("حساب کارگران و هزینه‌ها")
}

enum class ReportDateRange(val titleFa: String) {
    TODAY("امروز"),
    THIS_WEEK("هفته جاری"),
    THIS_MONTH("ماه جاری"),
    ALL_TIME("کل دوره")
}

@Composable
fun ReportsScreen(
    state: InventoryUiState,
    onRestoreRecords: (List<ColdStorageEntry>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val success = BackupRestoreHelper.writeBackupToUri(context, uri, state.records)
            if (success) {
                Toast.makeText(context, "فایل پشتیبان با موفقیت در مسیر انتخاب‌شده ذخیره شد.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "خطا در ذخیره فایل پشتیبان در مسیر انتخاب‌شده.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pickJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = BackupRestoreHelper.readTextFromUri(context, uri)
            if (text != null) {
                val list = BackupRestoreHelper.parseBackupJson(text)
                if (list != null && list.isNotEmpty()) {
                    onRestoreRecords(list)
                    Toast.makeText(context, "${JalaliDateHelper.toPersianDigits(list.size.toString())} رکورد با موفقیت بازیابی و بارگذاری شد.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "فایل پشتیبان انتخاب‌شده نامعتبر یا خالی است.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var selectedReportKind by remember { mutableStateOf(ReportKind.SYSTEM_OVERALL) }
    var selectedDateRange by remember { mutableStateOf(ReportDateRange.ALL_TIME) }
    var selectedAppleCategory by remember { mutableStateOf<AppleCategory?>(null) }

    val todayJalali = JalaliDateHelper.getTodayJalali()
    val todayStr = JalaliDateHelper.formatShort(todayJalali, toPersianDigits = false)

    // محاسبه فیلتر بر اساس بازه زمانی
    val filteredByDate = state.records.filter { r ->
        when (selectedDateRange) {
            ReportDateRange.TODAY -> r.dateShamsi == todayStr
            ReportDateRange.THIS_WEEK -> {
                val diffDays = (System.currentTimeMillis() - r.createdAt) / (1000 * 60 * 60 * 24)
                diffDays <= 7
            }
            ReportDateRange.THIS_MONTH -> {
                val diffDays = (System.currentTimeMillis() - r.createdAt) / (1000 * 60 * 60 * 24)
                diffDays <= 30
            }
            ReportDateRange.ALL_TIME -> true
        }
    }

    // فیلتر نهایی بر اساس نوع گزارش انتخابی
    val finalRecords = filteredByDate.filter { r ->
        when (selectedReportKind) {
            ReportKind.SYSTEM_OVERALL -> true
            ReportKind.DAILY_SPECIFIC -> r.dateShamsi == todayStr
            ReportKind.BY_APPLE_VARIETY -> selectedAppleCategory == null || r.appleCategory == selectedAppleCategory
            ReportKind.WORKERS_AND_EXPENSES -> r.workers.isNotEmpty() || r.totalHandlingFee > 0
        }
    }

    // آمارهای کلیدی گزارش فیلترشده
    val inBoxes = finalRecords.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
    val outBoxes = finalRecords.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
    val netStock = (inBoxes - outBoxes).coerceAtLeast(0)
    val totalWeightTon = finalRecords.sumOf { it.netWeightKg } / 1000.0
    val totalHandlingFees = finalRecords.sumOf { it.totalHandlingFee }
    val totalWorkersPaid = finalRecords.sumOf { it.totalWorkersPaid }
    val totalWorkersRemaining = finalRecords.sumOf { it.totalWorkersRemaining }

    val reportTitleDesc = when (selectedReportKind) {
        ReportKind.SYSTEM_OVERALL -> "گزارش جامع گردش و موجودی انبار سیب"
        ReportKind.DAILY_SPECIFIC -> "گزارش روزانه (${JalaliDateHelper.formatFullWithDay(todayJalali, toPersianDigits = true)})"
        ReportKind.BY_APPLE_VARIETY -> "گزارش تفکیکی بر اساس رقم سیب (${selectedAppleCategory?.titleFa ?: "کلیه ارقام"})"
        ReportKind.WORKERS_AND_EXPENSES -> "صورت حساب تیم کارگری و هزینه‌های تخلیه/بارگیری"
    }

    val dateRangeDesc = "${selectedDateRange.titleFa} (تا تاریخ ${JalaliDateHelper.formatShort(todayJalali, toPersianDigits = true)})"

    // تفکیک امانی باغداران
    val farmerSummary = mutableMapOf<String, Pair<Double, Int>>()
    state.records.forEach { r ->
        val current = farmerSummary.getOrDefault(r.farmerName, Pair(0.0, 0))
        if (r.operationType == RecordType.ENTRY) {
            farmerSummary[r.farmerName] = Pair(current.first + r.netWeightKg, current.second + r.boxCount)
        } else {
            farmerSummary[r.farmerName] = Pair((current.first - r.netWeightKg).coerceAtLeast(0.0), (current.second - r.boxCount).coerceAtLeast(0))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // ۱. کارت فیلترهای هوشمند گزارش‌گیری
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(EmeraldContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("تنظیم فیلترهای گزارش‌گیری", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = VazirmatnFamily)
                            Text("نوع گزارش و بازه زمانی دلخواه را انتخاب کنید", fontSize = 11.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // انتخاب نوع گزارش (Report Kind)
                    Text("۱. نوع گزارش:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = VazirmatnFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportKind.values().forEach { kind ->
                            val isSel = selectedReportKind == kind
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedReportKind = kind },
                                label = { Text(kind.titleFa, fontSize = 11.sp, fontFamily = VazirmatnFamily) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // انتخاب بازه زمانی شمسی
                    Text("۲. بازه زمانی:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = VazirmatnFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportDateRange.values().forEach { range ->
                            val isSel = selectedDateRange == range
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedDateRange = range },
                                label = { Text(range.titleFa, fontSize = 11.sp, fontFamily = VazirmatnFamily) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldContainer,
                                    selectedLabelColor = EmeraldPrimary
                                )
                            )
                        }
                    }

                    // در صورت انتخاب گزارش بر اساس نوع سیب، چیپس انتخاب رقم
                    AnimatedVisibility(visible = selectedReportKind == ReportKind.BY_APPLE_VARIETY) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("۳. انتخاب رقم سیب:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = VazirmatnFamily)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedAppleCategory == null,
                                    onClick = { selectedAppleCategory = null },
                                    label = { Text("همه ارقام", fontSize = 11.sp, fontFamily = VazirmatnFamily) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldContainer,
                                        selectedLabelColor = EmeraldPrimary
                                    )
                                )
                                AppleCategory.values().forEach { cat ->
                                    val isSel = selectedAppleCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedAppleCategory = cat },
                                        label = { Text(cat.titleFa, fontSize = 11.sp, fontFamily = VazirmatnFamily) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldContainer,
                                            selectedLabelColor = EmeraldPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ۲. کارت دکمه‌های استخراج اکسل و PDF و پیش‌نمایش چاپ
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "خروجی اسناد رسمی و فایل‌ها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontFamily = VazirmatnFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "شامل ${JalaliDateHelper.toPersianDigits(finalRecords.size.toString())} رکورد منطبق بر فیلتر انتخابی",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = VazirmatnFamily
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ردیف اول دکمه‌ها: اکسل و PDF رسمی
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (finalRecords.isEmpty()) {
                                    Toast.makeText(context, "هیچ رکوردی در این بازه برای خروجی وجود ندارد.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val file = ReportExportHelper.exportToExcelCsv(context, finalRecords, reportTitleDesc, dateRangeDesc)
                                Toast.makeText(context, "فایل اکسل ذخیره شد و آماده اشتراک است.", Toast.LENGTH_SHORT).show()
                                ReportExportHelper.shareFile(context, file, "text/csv", "گزارش اکسل سردخانه سردیار")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("خروجی Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = VazirmatnFamily)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (finalRecords.isEmpty()) {
                                    Toast.makeText(context, "هیچ رکوردی در این بازه برای خروجی وجود ندارد.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val pdfFile = ReportExportHelper.generateA4Pdf(context, finalRecords, reportTitleDesc, dateRangeDesc)
                                Toast.makeText(context, "فایل PDF رسمی A4 با موفقیت ایجاد شد.", Toast.LENGTH_SHORT).show()
                                ReportExportHelper.shareFile(context, pdfFile, "application/pdf", "گزارش رسمی سردخانه سردیار")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("خروجی PDF رسمی", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = VazirmatnFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // دکمه پیش‌نمایش چاپ (Print Preview)
                    OutlinedButton(
                        onClick = {
                            if (finalRecords.isEmpty()) {
                                Toast.makeText(context, "هیچ رکوردی در این بازه برای چاپ وجود ندارد.", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val pdfFile = ReportExportHelper.generateA4Pdf(context, finalRecords, reportTitleDesc, dateRangeDesc)
                            ReportExportHelper.printPdf(context, pdfFile, "چاپ گزارش عملکرد سردیار")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پیش‌نمایش چاپ و چاپگر سردخانه (Print Preview)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary, fontFamily = VazirmatnFamily)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // بخش پشتیبان‌گیری و بازیابی اطلاعات (Backup & Restore)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(ColdIceLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = ColdIce, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("پشتیبان‌گیری و بازیابی اطلاعات (Backup & Restore)", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = VazirmatnFamily)
                            Text("خروجی کامل دیتابیس به صورت JSON جهت انتقال به گوشی دیگر", fontSize = 11.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (state.records.isEmpty()) {
                                    Toast.makeText(context, "دیتابیس خالی است و رکوردی برای پشتیبان‌گیری وجود ندارد.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                createBackupLauncher.launch(BackupRestoreHelper.getDefaultBackupFileName())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("پشتیبان‌گیری (JSON)", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = VazirmatnFamily)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                pickJsonLauncher.launch("application/json")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = ColdIce, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بازیابی اطلاعات (Restore)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ColdIce, fontFamily = VazirmatnFamily)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ۳. کارت‌های خلاصه آماری بازه انتخابی
        item {
            Text(
                text = "خلاصه شاخص‌های گزارش فیلترشده",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                fontFamily = VazirmatnFamily
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(AppleGreenLight.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .border(1.dp, EntryGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("ورودی دوره", color = EntryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(NumberFormatHelper.formatBoxes(inBoxes, true), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontFamily = VazirmatnFamily)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFFFEDD5).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .border(1.dp, ExitOrange.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("خروجی دوره", color = ExitOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(NumberFormatHelper.formatBoxes(outBoxes, true), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontFamily = VazirmatnFamily)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(ColdIceLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .border(1.dp, ColdIce.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("موجودی دوره", color = ColdIce, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(NumberFormatHelper.formatBoxes(netStock, true), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontFamily = VazirmatnFamily)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ۴. تراز مالی تخلیه و کارگران
        item {
            Text("تراز مالی تخلیه/بارگیری و تیم کارگری", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = VazirmatnFamily)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    ReportBalanceRow("مجموع کل کرایه/هزینه تخلیه محاسبه شده:", "${NumberFormatHelper.format(totalHandlingFees.toLong(), true)} تومان", EmeraldPrimary, isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)
                    ReportBalanceRow("مجموع کل دستمزد پرداخت‌شده به کارگران:", "${NumberFormatHelper.format(totalWorkersPaid.toLong(), true)} تومان", EntryGreen)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderColor)
                    ReportBalanceRow("مجموع مانده‌طلب تسویه‌نشده کارگران:", "${NumberFormatHelper.format(totalWorkersRemaining.toLong(), true)} تومان", ExitOrange, isBold = true)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ۵. پیش‌نمایش جدول رکوردهای گزارش (Table Preview)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فهرست ردیف‌های گزارش (${JalaliDateHelper.toPersianDigits(finalRecords.size.toString())} مورد)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontFamily = VazirmatnFamily
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (finalRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ رکوردی با فیلترهای انتخابی یافت نشد.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = VazirmatnFamily
                    )
                }
            }
        } else {
            items(finalRecords, key = { it.id }) { record ->
                val isEntry = record.operationType == RecordType.ENTRY
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isEntry) EntryGreen else ExitOrange, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(record.farmerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = VazirmatnFamily)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(record.id, fontSize = 10.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            }
                            Text(
                                text = NumberFormatHelper.formatBoxes(record.boxCount, true),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isEntry) EntryGreen else ExitOrange,
                                fontFamily = VazirmatnFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "کد بار: ${record.cargoCode} (اتاق ${record.roomNumber}، پارت ${record.partNumber}) • ${record.displayAppleName}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = "${record.dateShamsi} ${record.timeStr}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }

                        if (record.workers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "کارگران: ${record.workers.size} نفر | پرداختی: ${NumberFormatHelper.format(record.totalWorkersPaid.toLong(), true)} ت",
                                fontSize = 10.sp,
                                color = EmeraldPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ۶. تفکیک امانی باغداران
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("موجودی امانی باغداران و صاحبان بار", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = VazirmatnFamily)
            Spacer(modifier = Modifier.height(10.dp))
            farmerSummary.forEach { (farmer, data) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(farmer, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = VazirmatnFamily)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(NumberFormatHelper.formatWeightKg(data.first, true), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary, fontFamily = VazirmatnFamily)
                            Text(NumberFormatHelper.formatBoxes(data.second, true), fontSize = 11.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun ReportBalanceRow(
    title: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, fontFamily = VazirmatnFamily)
        Text(value, fontSize = if (isBold) 14.sp else 12.sp, fontWeight = FontWeight.ExtraBold, color = color, fontFamily = VazirmatnFamily)
    }
}
