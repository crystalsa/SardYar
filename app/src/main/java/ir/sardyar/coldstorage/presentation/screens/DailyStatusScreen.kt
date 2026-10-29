package ir.sardyar.coldstorage.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.FeeCalculationType
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.presentation.InventoryUiState
import ir.sardyar.coldstorage.presentation.components.FacilitySelectorChip
import ir.sardyar.coldstorage.presentation.components.JalaliDatePickerDialog

@Composable
fun DailyStatusScreen(
    state: InventoryUiState,
    onNavigateToNewRecord: (RecordType) -> Unit = {},
    onEditRecord: (ColdStorageEntry) -> Unit = {},
    onDeleteRecord: (String) -> Unit = {},
    onOpenFacilityManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dayOffset by remember { mutableIntStateOf(0) }
    var selectedTypeFilter by remember { mutableStateOf<RecordType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val expandedCargoCodes = remember { mutableStateListOf<String>() }
    val expandedRecordIds = remember { mutableStateListOf<String>() }
    var recordToDelete by remember { mutableStateOf<ColdStorageEntry?>(null) }

    // تاریخ جاری انتخاب‌شده
    val selectedJalali = JalaliDateHelper.getJalaliForOffset(dayOffset)
    val selectedDateShamsiStr = JalaliDateHelper.formatShort(selectedJalali, toPersianDigits = false)
    val selectedDateFull = JalaliDateHelper.formatFullWithDay(selectedJalali, toPersianDigits = true)

    // بارهای تاریخ انتخاب‌شده
    val dayRecords = state.records.filter { r ->
        r.dateShamsi == selectedDateShamsiStr
    }

    // فیلتر داخلی صفحه (ورودی / خروجی / جستجوی راننده، پلاک، سیب، کارگر، کد بار)
    val displayedRecords = dayRecords.filter { r ->
        val typeMatch = (selectedTypeFilter == null || r.operationType == selectedTypeFilter)
        if (!typeMatch) return@filter false
        if (searchQuery.isBlank()) true
        else {
            val q = searchQuery.trim().lowercase()
            r.driverName.lowercase().contains(q) ||
            r.plateNumber.lowercase().contains(q) ||
            r.displayAppleName.lowercase().contains(q) ||
            r.farmerName.lowercase().contains(q) ||
            r.cargoCode.lowercase().contains(q) ||
            r.id.lowercase().contains(q) ||
            r.workers.any { it.name.lowercase().contains(q) }
        }
    }

    // گروه‌بندی بارهای روز بر اساس کد بار (سطح اول آکاردئون)
    val cargoGroups = remember(displayedRecords) {
        displayedRecords.groupBy {
            if (it.cargoCode.isBlank()) "NO_CODE_${it.farmerName}" else it.cargoCode
        }.map { (key, records) ->
            val firstRecord = records.first()
            val totalIn = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
            val totalOut = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
            val totalCount = records.sumOf { it.boxCount }
            DailyCargoGroup(
                groupKey = key,
                cargoCode = firstRecord.cargoCode,
                farmerName = firstRecord.farmerName,
                totalBoxes = totalCount,
                inBoxes = totalIn,
                outBoxes = totalOut,
                records = records
            )
        }
    }

    // آمارهای محاسباتی روز انتخاب‌شده
    val inBoxes = dayRecords.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
    val outBoxes = dayRecords.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
    val inWeightTon = dayRecords.filter { it.operationType == RecordType.ENTRY }.sumOf { it.netWeightKg } / 1000.0
    val outWeightTon = dayRecords.filter { it.operationType == RecordType.EXIT }.sumOf { it.netWeightKg } / 1000.0

    // تعداد ماشین‌های تسویه شده و کل ماشین‌ها
    val totalTrucks = dayRecords.size
    val settledTrucks = dayRecords.count { it.isWorkersFullySettled }

    // کل دستمزد پرداختی به کارگران در این روز
    val totalWorkersPaidToday = dayRecords.sumOf { it.totalWorkersPaid }
    val totalWorkersRemainingToday = dayRecords.sumOf { it.totalWorkersRemaining }

    // دیالوگ تایید حذف رکورد
    if (recordToDelete != null) {
        val entry = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(42.dp)
                )
            },
            title = {
                Text(
                    text = "تایید حذف بار از سردخانه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = VazirmatnFamily
                )
            },
            text = {
                Column {
                    Text(
                        text = "آیا از حذف قبض «${entry.id}» مربوط به «${entry.farmerName}» با تعداد ${JalaliDateHelper.toPersianDigits(entry.boxCount.toString())} جعبه اطمینان دارید؟",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontFamily = VazirmatnFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "این عملیات غیرقابل بازگشت است و موجودی انبار به‌روزرسانی خواهد شد.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = VazirmatnFamily
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecord(entry.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بله، حذف کن", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("انصراف", fontFamily = VazirmatnFamily)
                }
            }
        )
    }

    // دیالوگ انتخاب تاریخ تقویم شمسی
    if (isDatePickerOpen) {
        JalaliDatePickerDialog(
            initialJalali = selectedJalali,
            onDateSelected = { jalali ->
                dayOffset = JalaliDateHelper.getOffsetForJalali(jalali.year, jalali.month, jalali.day)
                isDatePickerOpen = false
            },
            onDismiss = { isDatePickerOpen = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دفترچه روزانه سردخانه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = TextPrimary,
                    fontFamily = VazirmatnFamily
                )
                FacilitySelectorChip(
                    currentFacility = state.activeFacility,
                    isAllSelected = state.activeFacilityId == null || state.activeFacilityId == "ALL",
                    onClick = onOpenFacilityManager
                )
            }
        }

        // ۱. هدر گاه‌شمار افقی شمسی (Horizontal Persian Date Bar)
        item {
            PersianDateNavigator(
                selectedOffset = dayOffset,
                selectedDateFull = selectedDateFull,
                allRecords = state.records,
                onOffsetChange = { dayOffset = it },
                onOpenCalendar = { isDatePickerOpen = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ۲. کارت برجسته خلاصه عملکرد همان روز (Daily Highlight Card)
        item {
            DailyHighlightCard(
                selectedDateFull = selectedDateFull,
                isToday = dayOffset == 0,
                inBoxes = inBoxes,
                inWeightTon = inWeightTon,
                outBoxes = outBoxes,
                outWeightTon = outWeightTon,
                totalTrucks = totalTrucks,
                settledTrucks = settledTrucks,
                totalWorkersPaidToday = totalWorkersPaidToday,
                totalWorkersRemainingToday = totalWorkersRemainingToday
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // موتور جستجو و فیلتر سریع درون روز
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        "جستجوی راننده، نوع سیب، نام کارگر یا پلاک در این روز...",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = VazirmatnFamily
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // فیلتر سریع درون روز (همه، فقط ورودی، فقط خروجی) - با طراحی اصلاح شده بدون شکست کلمات
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دفترچه بارهای این تاریخ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = VazirmatnFamily
                    )
                    Box(
                        modifier = Modifier
                            .background(SurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${JalaliDateHelper.toPersianDigits(cargoGroups.size.toString())} کد • ${JalaliDateHelper.toPersianDigits(displayedRecords.size.toString())} بار",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("همه موارد", fontSize = 11.5.sp, fontFamily = VazirmatnFamily, maxLines = 1) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == RecordType.ENTRY,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == RecordType.ENTRY) null else RecordType.ENTRY
                        },
                        label = { Text("ورودی‌ها", fontSize = 11.5.sp, fontFamily = VazirmatnFamily, maxLines = 1) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppleGreenLight,
                            selectedLabelColor = EntryGreen
                        )
                    )
                    FilterChip(
                        selected = selectedTypeFilter == RecordType.EXIT,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == RecordType.EXIT) null else RecordType.EXIT
                        },
                        label = { Text("خروجی‌ها", fontSize = 11.5.sp, fontFamily = VazirmatnFamily, maxLines = 1) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFEDD5),
                            selectedLabelColor = ExitOrange
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ۳. لیست بارهای همان روز به صورت آکاردئون دو مرحله‌ای (سطح اول: کد بار و کشاورز)
        if (cargoGroups.isEmpty()) {
            item {
                EmptyDayState(
                    dayOffset = dayOffset,
                    onAddNew = { onNavigateToNewRecord(RecordType.ENTRY) }
                )
            }
        } else {
            items(cargoGroups, key = { it.groupKey }) { group ->
                val isExpanded = expandedCargoCodes.contains(group.groupKey)
                DailyCargoGroupCard(
                    group = group,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        if (isExpanded) {
                            expandedCargoCodes.remove(group.groupKey)
                        } else {
                            expandedCargoCodes.add(group.groupKey)
                        }
                    },
                    expandedRecordIds = expandedRecordIds,
                    onToggleRecordExpand = { recordId ->
                        if (expandedRecordIds.contains(recordId)) {
                            expandedRecordIds.remove(recordId)
                        } else {
                            expandedRecordIds.add(recordId)
                        }
                    },
                    onEditRecord = onEditRecord,
                    onDeleteRecord = { recordToDelete = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

/**
 * هدر گاه‌شمار افقی شمسی با قابلیت پیمایش روزها
 */
@Composable
private fun PersianDateNavigator(
    selectedOffset: Int,
    selectedDateFull: String,
    allRecords: List<ColdStorageEntry>,
    onOffsetChange: (Int) -> Unit,
    onOpenCalendar: () -> Unit
) {
    val listState = rememberLazyListState()

    // بازه روزها متمرکز بر تاریخ انتخاب شده
    val offsets = remember(selectedOffset) { (selectedOffset - 10..selectedOffset + 10).toList() }

    LaunchedEffect(selectedOffset) {
        val selectedIndex = offsets.indexOf(selectedOffset)
        if (selectedIndex != -1) {
            listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(18.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column {
            // نوار بالایی هدر: فلش‌ها و عنوان تاریخ بولد
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onOffsetChange(selectedOffset + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "روز بعد",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenCalendar)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "تقویم",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedDateFull,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                    if (selectedOffset != 0) {
                        Text(
                            text = "انتخاب از تقویم / بازگشت به امروز",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFamily
                        )
                    } else {
                        Text(
                            text = "امروز (لمس کنید جهت تقویم و انتخاب سال)",
                            fontSize = 10.5.sp,
                            color = EntryGreen,
                            fontWeight = FontWeight.Medium,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                IconButton(
                    onClick = { onOffsetChange(selectedOffset - 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "روز قبل",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // نوار کارت‌های افقی گاه‌شمار شمسی
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(offsets) { offset ->
                    val j = JalaliDateHelper.getJalaliForOffset(offset)
                    val dateStr = JalaliDateHelper.formatShort(j, toPersianDigits = false)
                    val isSelected = offset == selectedOffset
                    val isToday = offset == 0
                    val hasRecords = allRecords.any { it.dateShamsi == dateStr }

                    val bgColor = if (isSelected) EmeraldPrimary else if (isToday) EmeraldContainer.copy(alpha = 0.5f) else SurfaceVariant
                    val textColor = if (isSelected) Color.White else TextPrimary
                    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.85f) else TextSecondary
                    val borderColor = if (isSelected) EmeraldPrimary else if (isToday) EmeraldPrimary.copy(alpha = 0.4f) else BorderColor

                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { onOffsetChange(offset) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // نام روز هفته (دوشنبه، ...)
                            Text(
                                text = j.dayOfWeek,
                                fontSize = 10.sp,
                                color = subTextColor,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // شماره روز بولد (مثلاً ۶)
                            Text(
                                text = JalaliDateHelper.toPersianDigits(j.day.toString()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                fontFamily = VazirmatnFamily
                            )
                            // نام ماه
                            Text(
                                text = JalaliDateHelper.getMonthName(j.month),
                                fontSize = 9.sp,
                                color = subTextColor,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // نشانگر نقطه در صورت وجود بار در این روز
                            if (hasRecords) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isSelected) Color.White else EmeraldPrimary, CircleShape)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * کارت برجسته خلاصه وضعیت روزانه
 */
@Composable
private fun DailyHighlightCard(
    selectedDateFull: String,
    isToday: Boolean,
    inBoxes: Int,
    inWeightTon: Double,
    outBoxes: Int,
    outWeightTon: Double,
    totalTrucks: Int,
    settledTrucks: Int,
    totalWorkersPaidToday: Double,
    totalWorkersRemainingToday: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(EmeraldContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "خلاصه عملکرد دفترچه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        Text(
                            text = if (isToday) "آمار امروز سردخانه" else "آمار روز انتخابی",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // شبکه ۴تایی کارت‌های شاخص
            Row(modifier = Modifier.fillMaxWidth()) {
                // ۱. جمع جعبه‌های ورودی
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(AppleGreenLight.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .border(1.dp, EntryGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ورودی روز", color = EntryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = EntryGreen, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = NumberFormatHelper.formatBoxes(inBoxes, true),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        Text(
                            text = "${NumberFormatHelper.formatTonnage(inWeightTon, true)} تن",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // ۲. جمع جعبه‌های خروجی
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFFFEDD5).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .border(1.dp, ExitOrange.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("خروجی روز", color = ExitOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ExitOrange, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = NumberFormatHelper.formatBoxes(outBoxes, true),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        Text(
                            text = "${NumberFormatHelper.formatTonnage(outWeightTon, true)} تن",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // ۳. ماشین‌های تسویه شده
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(ColdIceLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .border(1.dp, ColdIce.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ماشین‌های تسویه", color = ColdIce, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ColdIce, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${JalaliDateHelper.toPersianDigits(settledTrucks.toString())} از ${JalaliDateHelper.toPersianDigits(totalTrucks.toString())} ماشین",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        Text(
                            text = if (totalTrucks > 0 && settledTrucks == totalTrucks) "همگی تسویه شدند" else "تسویه کارگری",
                            fontSize = 10.sp,
                            color = if (totalTrucks > 0 && settledTrucks == totalTrucks) EntryGreen else TextMuted,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // ۴. کل دستمزد پرداختی به کارگران
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SurfaceVariant, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("پرداختی کارگران", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${NumberFormatHelper.format(totalWorkersPaidToday.toLong(), true)} تومان",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        if (totalWorkersRemainingToday > 0) {
                            Text(
                                text = "مانده: ${NumberFormatHelper.format(totalWorkersRemainingToday.toLong(), true)} ت",
                                fontSize = 10.sp,
                                color = ExitOrange,
                                fontFamily = VazirmatnFamily
                            )
                        } else {
                            Text(
                                text = "بدون مانده بدهی",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * مدل داده گروه بارهای روز بر اساس کد بار (مرحله اول آکاردئون)
 */
private data class DailyCargoGroup(
    val groupKey: String,
    val cargoCode: String,
    val farmerName: String,
    val totalBoxes: Int,
    val inBoxes: Int,
    val outBoxes: Int,
    val records: List<ColdStorageEntry>
)

/**
 * کارت مرحله اول: گروه کد بار و کشاورز با مجموع جعبه‌های روز
 * با لمس این کارت، لیست بارهای ثبت‌شده آن کد در آن روز باز می‌شود.
 */
@Composable
private fun DailyCargoGroupCard(
    group: DailyCargoGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedRecordIds: List<String>,
    onToggleRecordExpand: (String) -> Unit,
    onEditRecord: (ColdStorageEntry) -> Unit,
    onDeleteRecord: (ColdStorageEntry) -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "groupArrowRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(
            if (isExpanded) 1.5.dp else 1.dp,
            if (isExpanded) EmeraldPrimary.copy(alpha = 0.55f) else BorderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp)
    ) {
        Column {
            // سربرگ سطح اول: کد بار + اسم کشاورز + مجموع جعبه‌ها
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // سمت راست: کد بار و نام کشاورز
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (group.cargoCode.isNotBlank()) EmeraldContainer else Color(0xFFFEF3C7),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (group.cargoCode.isNotBlank()) EmeraldPrimary.copy(alpha = 0.35f) else Color(0xFFF59E0B).copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (group.cargoCode.isNotBlank()) "کد ${group.cargoCode}" else "بدون کد",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (group.cargoCode.isNotBlank()) EmeraldPrimary else Color(0xFFD97706),
                            fontFamily = VazirmatnFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.farmerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${JalaliDateHelper.toPersianDigits(group.records.size.toString())} بار در این روز",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                // سمت چپ: مجموع جعبه‌های آن روز
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مجموع: ",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = "${NumberFormatHelper.format(group.totalBoxes.toLong(), true)} جعبه",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        if (group.inBoxes > 0 && group.outBoxes > 0) {
                            Text(
                                text = "ورود: ${group.inBoxes} • خروج: ${group.outBoxes}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = VazirmatnFamily
                            )
                        } else if (group.inBoxes > 0) {
                            Text(
                                text = "ورودی",
                                fontSize = 10.sp,
                                color = EntryGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFamily
                            )
                        } else {
                            Text(
                                text = "خروجی",
                                fontSize = 10.sp,
                                color = ExitOrange,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "باز کردن بارهای این کد",
                            tint = if (isExpanded) EmeraldPrimary else TextSecondary,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            // کرکره مرحله اول: نمایش بارهای ثبت شده این کد در آن روز
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

                    group.records.forEach { record ->
                        val isRecordExpanded = expandedRecordIds.contains(record.id)
                        ExpandableDailyRecordCard(
                            record = record,
                            isExpanded = isRecordExpanded,
                            onToggleExpand = { onToggleRecordExpand(record.id) },
                            onEdit = { onEditRecord(record) },
                            onDelete = { onDeleteRecord(record) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * آیتم بار به صورت کارت تاشو مدرن (سطح دوم آکاردئون - جزئیات فاکتور)
 */
@Composable
private fun ExpandableDailyRecordCard(
    record: ColdStorageEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isEntry = record.operationType == RecordType.ENTRY
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(
                1.dp,
                if (isExpanded) (if (isEntry) EntryGreen.copy(alpha = 0.5f) else ExitOrange.copy(alpha = 0.5f)) else BorderColor,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column {
            // هدر تاشو بار (مرحله دوم)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // آیکون ورود / خروج با بج رنگی
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isEntry) AppleGreenLight else Color(0xFFFFEDD5),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEntry) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isEntry) EntryGreen else ExitOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEntry) "ورود بار" else "خروج بار",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isEntry) EntryGreen else ExitOrange,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // قبض شماره
                            Box(
                                modifier = Modifier
                                    .background(SurfaceVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "قبض ${record.id}",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFamily,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ساعت ${record.timeStr}",
                                fontSize = 10.5.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${record.displayAppleName} (${record.boxType.titleFa}) • اتاق ${record.roomNumber}، پارت ${record.partNumber}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ستون اطلاعات سمت چپ (تعداد جعبه و فلش)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (isEntry) "+" else "-"}${NumberFormatHelper.formatBoxes(record.boxCount, true)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = if (isEntry) EntryGreen else ExitOrange,
                            fontFamily = VazirmatnFamily
                        )
                        if (record.netWeightKg > 0) {
                            Text(
                                text = "${NumberFormatHelper.formatTonnage(record.netWeightKg / 1000.0, true)} تن",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "مشاهده فاکتور و جزئیات",
                            tint = TextSecondary,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            // بخش بازشونده با انیمیشن (Accordion Details)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // اطلاعات ماشین و راننده
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("مشخصات راننده و خودرو:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text(
                                text = if (record.driverName.isNotBlank()) record.driverName else "ثبت نشده",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("شماره پلاک:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text(
                                text = if (record.plateNumber.isNotBlank()) record.plateNumber else "ندارد",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // کد بار کشاورز
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("کد بار کشاورز (مرجع بار):", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text("کد ${record.cargoCode}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontFamily = VazirmatnFamily)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("محل استقرار بار:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text("اتاق ${record.roomNumber} • پارت ${record.partNumber}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontFamily = VazirmatnFamily)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // اطلاعات وزن باسکول (در صورت ثبت)
                    if (record.netWeightKg > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface, RoundedCornerShape(10.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("وزن ناخالص", fontSize = 10.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                                    Text(NumberFormatHelper.formatWeightKg(record.grossWeightKg, true), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("افت سبدها", fontSize = 10.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                                    Text(NumberFormatHelper.formatWeightKg(record.tareWeightKg, true), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("وزن خالص نهایی", fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                                    Text(
                                        NumberFormatHelper.formatWeightKg(record.netWeightKg, true),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary,
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // جزئیات هزینه تخلیه/بارگیری
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "هزینه تخلیه/بارگیری (${record.feeCalculationType.titleFa}):",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = VazirmatnFamily
                                )
                                Text(
                                    text = "${NumberFormatHelper.format(record.totalHandlingFee.toLong(), true)} تومان",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            if (record.feeCalculationType == FeeCalculationType.PER_BOX && record.unitRate > 0) {
                                Text(
                                    text = "نرخ هر جعبه: ${NumberFormatHelper.format(record.unitRate.toLong(), true)} تومان",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // لیست کارگران و وضعیت تسویه حساب
                    if (record.workers.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تیم کارگری (${JalaliDateHelper.toPersianDigits(record.workers.size.toString())} نفر):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = if (record.isWorkersFullySettled) "وضعیت: کاملاً تسویه شده" else "وضعیت: دارای مانده پرداخت",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record.isWorkersFullySettled) EntryGreen else ExitOrange,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        record.workers.forEach { w ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (w.isSettled) EntryGreen else ExitOrange, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${w.name} (${w.role.titleFa})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (w.isSettled) {
                                        Text(
                                            text = "تسویه شد" + (if (!w.settlementDate.isNullOrBlank()) " (${w.settlementDate})" else "") + " • ${NumberFormatHelper.format(w.paidAmount.toLong(), true)} ت",
                                            fontSize = 10.5.sp,
                                            color = EntryGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = VazirmatnFamily
                                        )
                                    } else {
                                        Text(
                                            text = "پرداختی: ${NumberFormatHelper.format(w.paidAmount.toLong(), true)} ت",
                                            fontSize = 10.5.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = VazirmatnFamily
                                        )
                                        if (w.remainingWage > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "(مانده: ${NumberFormatHelper.format(w.remainingWage.toLong(), true)} ت)",
                                                fontSize = 10.sp,
                                                color = ExitOrange,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = VazirmatnFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // توضیحات اختیاری
                    if (record.notes.isNotBlank()) {
                        Text("یادداشت:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                        Text(
                            text = record.notes,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = VazirmatnFamily
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // دکمه‌های عملیاتی: ویرایش و حذف
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش بار", fontSize = 12.sp, fontFamily = VazirmatnFamily)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف رکورد", fontSize = 12.sp, fontFamily = VazirmatnFamily)
                        }
                    }
                }
            }
        }
    }
}

/**
 * وضعیت خالی هنگام نبود بار در تاریخ انتخابی (Empty State)
 */
@Composable
private fun EmptyDayState(
    dayOffset: Int,
    onAddNew: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(18.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "هیچ ورودی یا خروجی برای این تاریخ ثبت نشده است",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = VazirmatnFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "می‌توانید با دکمه زیر برای این روز بار ثبت کنید یا روز دیگری را از گاه‌شمار بالا انتخاب فرمایید.",
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = VazirmatnFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ثبت بار جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = VazirmatnFamily
                )
            }
        }
    }
}
