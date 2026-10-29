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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sardyar.coldstorage.core.theme.AppleGreen
import ir.sardyar.coldstorage.core.theme.AppleGreenLight
import ir.sardyar.coldstorage.core.theme.BorderColor
import ir.sardyar.coldstorage.core.theme.ColdIce
import ir.sardyar.coldstorage.core.theme.ColdIceLight
import ir.sardyar.coldstorage.core.theme.EmeraldContainer
import ir.sardyar.coldstorage.core.theme.EmeraldDark
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
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.presentation.InventoryUiState
import ir.sardyar.coldstorage.presentation.components.BadgePill
import ir.sardyar.coldstorage.presentation.components.FacilitySelectorChip
import java.util.Calendar

/**
 * مدل داده‌ای جزئیات کارنامه هر کد بار در داشبورد
 */
data class CargoCodeDetailedItem(
    val cargoCode: String,
    val ownerName: String,
    val roomNumber: String,
    val partNumber: String,
    val totalInBoxes: Int,
    val totalOutBoxes: Int,
    val remainingBoxes: Int,
    val remainingNetWeightKg: Double,
    val totalHandlingFees: Double,
    val totalWorkersPaid: Double,
    val totalWorkersRemaining: Double,
    val appleBreakdown: List<AppleVarietyStockInfo>,
    val entries: List<ColdStorageEntry>
)

/**
 * تفکیک نوع سیب و مدل سبد درون هر کد بار
 */
data class AppleVarietyStockInfo(
    val appleName: String,
    val boxTypeTitle: String,
    val inBoxes: Int,
    val outBoxes: Int,
    val remainingBoxes: Int
)

enum class TimeRangeFilter(val titleFa: String) {
    TODAY("امروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه"),
    ALL_TIME("کل دوره")
}

@Composable
fun DashboardScreen(
    state: InventoryUiState,
    onNavigateToNewRecord: (RecordType) -> Unit,
    onNavigateToRecords: () -> Unit,
    onOpenFacilityManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(TimeRangeFilter.ALL_TIME) }
    var searchQuery by remember { mutableStateOf("") }
    val expandedCargoCodes = remember { mutableStateListOf<String>() }

    val today = JalaliDateHelper.getTodayJalali()
    val todayFull = JalaliDateHelper.formatFullWithDay(today)

    // محاسبه بازه زمانی
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = cal.timeInMillis
    val startOfWeek = now - (7L * 24 * 3600 * 1000)
    val startOfMonth = now - (30L * 24 * 3600 * 1000)

    val filteredByRange = state.records.filter { r ->
        when (selectedRange) {
            TimeRangeFilter.TODAY -> r.createdAt >= startOfToday || r.dateShamsi == JalaliDateHelper.formatShort(today, false)
            TimeRangeFilter.THIS_WEEK -> r.createdAt >= startOfWeek
            TimeRangeFilter.THIS_MONTH -> r.createdAt >= startOfMonth
            TimeRangeFilter.ALL_TIME -> true
        }
    }

    // آمار محاسبه شده بر اساس بازه انتخابی
    val rangeEntryBoxes = filteredByRange.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
    val rangeExitBoxes = filteredByRange.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
    val rangeEntryTrips = filteredByRange.count { it.operationType == RecordType.ENTRY }
    val rangeExitTrips = filteredByRange.count { it.operationType == RecordType.EXIT }
    val rangeHandlingFees = filteredByRange.sumOf { it.totalHandlingFee }
    val rangeWorkerWagesPaid = filteredByRange.sumOf { it.totalWorkersPaid }

    // محاسبه کارنامه تفکیکی موجودی و هزینه‌های هر کد بار
    val cargoCodeSummaries = remember(state.records) {
        val grouped = state.records.groupBy { it.cargoCode }
        grouped.map { (code, entries) ->
            val inEntries = entries.filter { it.operationType == RecordType.ENTRY }
            val outEntries = entries.filter { it.operationType == RecordType.EXIT }

            val totalIn = inEntries.sumOf { it.boxCount }
            val totalOut = outEntries.sumOf { it.boxCount }
            val remaining = (totalIn - totalOut).coerceAtLeast(0)

            val inWeight = inEntries.sumOf { it.netWeightKg }
            val outWeight = outEntries.sumOf { it.netWeightKg }
            val remainingWeight = (inWeight - outWeight).coerceAtLeast(0.0)

            val firstEntry = entries.firstOrNull()
            val owner = firstEntry?.farmerName ?: "نامشخص"
            val roomNum = code.substringBefore('.').trim()
            val partNum = if (code.contains('.')) code.substringAfter('.').trim() else ""

            val handlingFees = entries.sumOf { it.totalHandlingFee }
            val workersPaid = entries.sumOf { it.totalWorkersPaid }
            val workersRemaining = entries.sumOf { it.totalWorkersRemaining }

            // تفکیک نوع سیب و مدل سبد
            val varietyGroups = entries.groupBy { Pair(it.displayAppleName, it.boxType.titleFa) }
            val appleBreakdown = varietyGroups.map { (pair, vEntries) ->
                val vIn = vEntries.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
                val vOut = vEntries.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
                val vRemaining = (vIn - vOut).coerceAtLeast(0)
                AppleVarietyStockInfo(
                    appleName = pair.first,
                    boxTypeTitle = pair.second,
                    inBoxes = vIn,
                    outBoxes = vOut,
                    remainingBoxes = vRemaining
                )
            }.sortedByDescending { it.remainingBoxes }

            CargoCodeDetailedItem(
                cargoCode = code,
                ownerName = owner,
                roomNumber = roomNum,
                partNumber = partNum,
                totalInBoxes = totalIn,
                totalOutBoxes = totalOut,
                remainingBoxes = remaining,
                remainingNetWeightKg = remainingWeight,
                totalHandlingFees = handlingFees,
                totalWorkersPaid = workersPaid,
                totalWorkersRemaining = workersRemaining,
                appleBreakdown = appleBreakdown,
                entries = entries.sortedByDescending { it.createdAt }
            )
        }.sortedWith(compareByDescending<CargoCodeDetailedItem> { it.remainingBoxes }.thenBy { it.cargoCode })
    }

    val filteredCargoSummaries = remember(cargoCodeSummaries, searchQuery) {
        if (searchQuery.isBlank()) cargoCodeSummaries
        else {
            val q = searchQuery.trim().lowercase()
            cargoCodeSummaries.filter { item ->
                item.cargoCode.lowercase().contains(q) ||
                item.ownerName.lowercase().contains(q) ||
                item.roomNumber.contains(q) ||
                item.partNumber.contains(q) ||
                item.appleBreakdown.any { it.appleName.lowercase().contains(q) || it.boxTypeTitle.lowercase().contains(q) } ||
                item.entries.any { it.driverName.lowercase().contains(q) || it.plateNumber.lowercase().contains(q) }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToNewRecord(RecordType.ENTRY) },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp)) },
                text = {
                    Text(
                        "ثبت ورود / خروج جدید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = VazirmatnFamily
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ۱. سربرگ بالا و تاریخ امروز
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(EmeraldPrimary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "داشبورد مدیریت سردخانه",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontFamily = VazirmatnFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = todayFull,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = VazirmatnFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FacilitySelectorChip(
                            currentFacility = state.activeFacility,
                            isAllSelected = state.activeFacilityId == null || state.activeFacilityId == "ALL",
                            onClick = onOpenFacilityManager
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۲. فیلتر سریع بازه زمانی برای کارت‌ها
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRangeFilter.values().forEach { range ->
                        val isSelected = selectedRange == range
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRange = range },
                            label = {
                                Text(
                                    range.titleFa,
                                    fontFamily = VazirmatnFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۳. کارت بزرگ قهرمان: کل موجودی فعلی سیب در سردخانه
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldDark, EmeraldPrimary)),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "کل موجودی فعلی سیب در سردخانه",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ظرفیت: ${NumberFormatHelper.formatWeightTon(state.totalCapacityKg, true)}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "مجموع سبد/جعبه موجود",
                                    color = Color(0xFFD1E7DD),
                                    fontSize = 11.sp,
                                    fontFamily = VazirmatnFamily
                                )
                                Text(
                                    text = NumberFormatHelper.formatBoxes(state.currentStockBoxes, true),
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "وزن خالص تخمینی",
                                    color = Color(0xFFD1E7DD),
                                    fontSize = 11.sp,
                                    fontFamily = VazirmatnFamily
                                )
                                Text(
                                    text = NumberFormatHelper.formatWeightKg(state.currentStockWeightKg, true),
                                    color = AppleGreenLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("درصد اشغال سالن‌ها", color = Color(0xFFE2E8F0), fontSize = 11.sp, fontFamily = VazirmatnFamily)
                            Text(
                                "${JalaliDateHelper.toPersianDigits(String.format("%.1f", state.occupancyPercentage))}٪",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { state.occupancyPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(7.dp),
                            color = AppleGreen,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۴. شبکه کارت‌های آمار کلیدی (Metric Cards)
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DashboardMetricCard(
                        title = "کل جعبه‌های ورودی",
                        value = NumberFormatHelper.formatBoxes(rangeEntryBoxes, true),
                        subtitle = "در ${JalaliDateHelper.toPersianDigits(rangeEntryTrips.toString())} نوبت تخلیه بار",
                        icon = Icons.Default.ArrowDownward,
                        accentColor = EntryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DashboardMetricCard(
                        title = "کل جعبه‌های خروجی",
                        value = NumberFormatHelper.formatBoxes(rangeExitBoxes, true),
                        subtitle = "در ${JalaliDateHelper.toPersianDigits(rangeExitTrips.toString())} نوبت ترخیص و خروج",
                        icon = Icons.Default.ArrowUpward,
                        accentColor = ExitOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DashboardMetricCard(
                        title = "هزینه تخلیه/بارگیری",
                        value = NumberFormatHelper.formatToman(rangeHandlingFees.toLong(), true),
                        subtitle = "کرایه ثبت‌شده ماشین‌ها",
                        icon = Icons.Default.LocalShipping,
                        accentColor = ColdIce,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DashboardMetricCard(
                        title = "پرداختی به کارگران",
                        value = NumberFormatHelper.formatToman(rangeWorkerWagesPaid.toLong(), true),
                        subtitle = "تسویه‌شده با کارگران",
                        icon = Icons.Default.Payments,
                        accentColor = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ۵. موتور جستجو و فیلتر سریع بر اساس کد بار، کشاورز، نوع سیب
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "جستجوی کد بار (مثلاً 3.8)، نام کشاورز، نوع سیب، راننده...",
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
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۶. بخش مجموع موجودی سبدها بر اساس کد بارها (کارت‌های بازشو کرکره‌ای)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مجموع موجودی سبدها بر اساس کد بارها",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgePill(
                            text = "${JalaliDateHelper.toPersianDigits(filteredCargoSummaries.size.toString())} کد",
                            backgroundColor = EmeraldContainer.copy(alpha = 0.5f),
                            textColor = EmeraldPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (filteredCargoSummaries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isBlank()) "هنوز هیچ باری در این سردخانه ثبت نشده است." else "موردی با این عبارت جستجو یافت نشد.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "برای ثبت سریع، دکمه «ثبت ورود / خروج جدید» را بزنید.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
            } else {
                items(filteredCargoSummaries, key = { it.cargoCode }) { cargoItem ->
                    val isExpanded = expandedCargoCodes.contains(cargoItem.cargoCode)
                    ExpandableCargoCodeCard(
                        item = cargoItem,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            if (isExpanded) expandedCargoCodes.remove(cargoItem.cargoCode)
                            else expandedCargoCodes.add(cargoItem.cargoCode)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = VazirmatnFamily
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = VazirmatnFamily
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = VazirmatnFamily
            )
        }
    }
}

/**
 * کارت کرکره‌ای اختصاصی هر کد بار با جزئیات کامل و خطوط تفکیک‌شده رنگارنگ
 */
@Composable
private fun ExpandableCargoCodeCard(
    item: CargoCodeDetailedItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")
    val hasStock = item.remainingBoxes > 0
    val isNoCode = item.cargoCode.startsWith("بدون کد")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(
                1.5.dp,
                if (isExpanded) EmeraldPrimary else if (hasStock) BorderColor else BorderColor.copy(alpha = 0.5f),
                RoundedCornerShape(18.dp)
            )
    ) {
        Column {
            // سربرگ اصلی کارت (Header) - همیشه قابل مشاهده
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .background(
                        if (isExpanded) EmeraldContainer.copy(alpha = 0.15f)
                        else if (hasStock) Surface
                        else SurfaceVariant.copy(alpha = 0.4f)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // سمت راست: بج کد بار + نام صاحب بار
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // بج شیک کد بار (گرادیانت سبز/زمردی یا کهربایی برای بدون کد)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isNoCode) Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309)))
                                else Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldDark))
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isNoCode) "بدون کد" else "کد ${item.cargoCode}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = VazirmatnFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = item.ownerName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = VazirmatnFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isNoCode) Color(0xFFFEF3C7) else ColdIceLight.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isNoCode) "ثبت با نام شخص" else "اتاق ${item.roomNumber} • پارت ${item.partNumber}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNoCode) Color(0xFFB45309) else ColdIce,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${JalaliDateHelper.toPersianDigits(item.entries.size.toString())} تراکنش",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // سمت چپ: مانده موجودی + آیکون بازشو
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مانده:",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormatHelper.formatBoxes(item.remainingBoxes, true),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasStock) EntryGreen else TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Text(
                            text = if (hasStock) "${NumberFormatHelper.formatTonnage(item.remainingNetWeightKg / 1000.0, true)} تن" else "تخلیه کامل",
                            fontSize = 10.sp,
                            fontWeight = if (hasStock) FontWeight.Bold else FontWeight.Normal,
                            color = if (hasStock) EmeraldPrimary else TextMuted,
                            fontFamily = VazirmatnFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isExpanded) EmeraldContainer else SurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                            tint = if (isExpanded) EmeraldPrimary else TextSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }

            // بخش کرکره‌ای (Accordion Details)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.35f))
                        .padding(14.dp)
                ) {
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // ===== سطر ۱: گردش کلی سبدها (سه کارت رنگی برجسته) =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ۱. ورودی کل (سبز چمنی)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("کل ورودی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontFamily = VazirmatnFamily)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = NumberFormatHelper.formatBoxes(item.totalInBoxes, true),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1B5E20),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        // ۲. خروجی کل (نارنجی / کهربایی)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("کل خروجی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontFamily = VazirmatnFamily)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = NumberFormatHelper.formatBoxes(item.totalOutBoxes, true),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFBF360C),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        // ۳. مانده در سردخانه (آبی یخی / لاجوردی)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2FE))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("مانده سردخانه", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1), fontFamily = VazirmatnFamily)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = NumberFormatHelper.formatBoxes(item.remainingBoxes, true),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0C4A6E),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    }

                    // نوار پیشرفت تخلیه (Progress Bar رنگی)
                    if (item.totalInBoxes > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val exitRatio = (item.totalOutBoxes.toFloat() / item.totalInBoxes.toFloat()).coerceIn(0f, 1f)
                        val percentInt = (exitRatio * 100).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "درصد ترخیص از سردخانه:",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = "${JalaliDateHelper.toPersianDigits(percentInt.toString())}٪ ترخیص شده",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentInt == 100) Color(0xFF059669) else ExitOrange,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        // نوار درصد ترخیص با پر شدن از چپ به راست (LTR)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            LinearProgressIndicator(
                                progress = { exitRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ExitOrange,
                                trackColor = Color(0xFFC8E6C9)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ===== سطر ۲: تفکیک موجودی بر اساس نوع سیب و مدل سبد (کارت‌های بنفش/صورتی/زرد رنگارنگ) =====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(13.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "موجودی به تفکیک نوع سیب و مدل سبد:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B21B6),
                            fontFamily = VazirmatnFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.appleBreakdown.forEach { variety ->
                            val isYellow = variety.appleName.contains("زرد")
                            val isRed = variety.appleName.contains("قرمز")
                            val isGrade2 = variety.appleName.contains("درجه ۲") || variety.appleName.contains("درجه 2")

                            val chipBg = when {
                                isGrade2 -> Color(0xFFFAF5FF)
                                isYellow -> Color(0xFFFEFCE8)
                                isRed -> Color(0xFFFFF1F2)
                                else -> Color(0xFFF0FDF4)
                            }
                            val chipBorder = when {
                                isGrade2 -> Color(0xFFD8B4FE)
                                isYellow -> Color(0xFFFDE047)
                                isRed -> Color(0xFFFDA4AF)
                                else -> Color(0xFF86EFAC)
                            }
                            val chipAccent = when {
                                isGrade2 -> Color(0xFF7E22CE)
                                isYellow -> Color(0xFFA16207)
                                isRed -> Color(0xFFBE123C)
                                else -> Color(0xFF15803D)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(chipAccent)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${variety.appleName} (${variety.boxTypeTitle})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = chipAccent,
                                        fontFamily = VazirmatnFamily
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // ورود به رنگ سبز
                                    Text(
                                        text = "ورود: ${JalaliDateHelper.toPersianDigits(variety.inBoxes.toString())}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A),
                                        fontFamily = VazirmatnFamily
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "|",
                                        fontSize = 10.sp,
                                        color = BorderColor,
                                        fontFamily = VazirmatnFamily
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // خروج به رنگ قرمز پررنگ
                                    Text(
                                        text = "خروج: ${JalaliDateHelper.toPersianDigits(variety.outBoxes.toString())}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFDC2626),
                                        fontFamily = VazirmatnFamily
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // مانده با رنگ متن سیاه
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "مانده: ${NumberFormatHelper.formatBoxes(variety.remainingBoxes, true)}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black,
                                            fontFamily = VazirmatnFamily
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ===== سطر ۳: هزینه‌ها و دستمزدهای پرداختی تا این لحظه (کادر مالی شکیل) =====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(13.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "هزینه‌ها و دستمزدهای پرداختی تا این لحظه:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3730A3),
                            fontFamily = VazirmatnFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // کرایه ماشین‌ها
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF))
                                .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("کرایه بارگیری/تخلیه:", fontSize = 10.sp, color = Color(0xFF4338CA), fontFamily = VazirmatnFamily)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = NumberFormatHelper.formatToman(item.totalHandlingFees.toLong(), true),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF312E81),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        // دستمزد کارگران
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECFDF5))
                                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("پرداختی به کارگران:", fontSize = 10.sp, color = Color(0xFF065F46), fontFamily = VazirmatnFamily)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = NumberFormatHelper.formatToman(item.totalWorkersPaid.toLong(), true),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF064E3B),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    }

                    // وضعیت تسویه کارگری
                    Spacer(modifier = Modifier.height(6.dp))
                    if (item.totalWorkersRemaining > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مانده طلب تسویه‌نشده کارگران:", fontSize = 10.5.sp, color = Color(0xFF991B1B), fontFamily = VazirmatnFamily)
                                }
                                Text(
                                    text = NumberFormatHelper.formatToman(item.totalWorkersRemaining.toLong(), true),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626),
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0FDF4))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تمامی دستمزدهای کارگری این کد تا به این لحظه تسویه شده‌اند.", fontSize = 10.sp, color = Color(0xFF15803D), fontFamily = VazirmatnFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}
