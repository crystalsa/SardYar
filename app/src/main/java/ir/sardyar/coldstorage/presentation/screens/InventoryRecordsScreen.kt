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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sardyar.coldstorage.core.theme.AppleGreenLight
import ir.sardyar.coldstorage.core.theme.BorderColor
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
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.FeeCalculationType
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.presentation.InventoryUiState
import ir.sardyar.coldstorage.presentation.components.BadgePill

@Composable
fun InventoryRecordsScreen(
    state: InventoryUiState,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (RecordType?) -> Unit,
    onCategoryFilterChange: (AppleCategory?) -> Unit,
    onDeleteRecord: (String) -> Unit,
    onEditRecord: (ColdStorageEntry) -> Unit = {},
    onNavigateToNewRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    var recordToDelete by remember { mutableStateOf<ColdStorageEntry?>(null) }
    val expandedRecordIds = remember { mutableStateListOf<String>() }
    val expandedCargoCodes = remember { mutableStateListOf<String>() }
    var isSortAscending by remember { mutableStateOf(false) }

    // گروه‌بندی رکوردهای فیلترشده بر اساس کد بار با قابلیت سورت صعودی و نزولی
    val cargoGroups = remember(state.filteredRecords, isSortAscending) {
        state.filteredRecords.groupBy {
            if (it.cargoCode.isBlank()) "NO_CODE_${it.farmerName}" else it.cargoCode
        }.map { (key, records) ->
            val firstRecord = records.first()
            val totalIn = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
            val totalOut = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
            val totalCount = records.sumOf { it.boxCount }
            val sortedRecords = if (isSortAscending) {
                records.sortedWith(compareBy<ColdStorageEntry> { it.dateShamsi }.thenBy { it.timeStr }.thenBy { it.createdAt })
            } else {
                records.sortedWith(compareByDescending<ColdStorageEntry> { it.dateShamsi }.thenByDescending { it.timeStr }.thenByDescending { it.createdAt })
            }
            InventoryCargoGroup(
                groupKey = key,
                cargoCode = firstRecord.cargoCode,
                farmerName = firstRecord.farmerName,
                totalBoxes = totalCount,
                inBoxes = totalIn,
                outBoxes = totalOut,
                records = sortedRecords
            )
        }.sortedWith(
            if (isSortAscending) compareBy { it.records.firstOrNull()?.createdAt ?: 0L }
            else compareByDescending { it.records.firstOrNull()?.createdAt ?: 0L }
        )
    }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("حذف رکورد", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily) },
            text = {
                Text(
                    "آیا از حذف قبض شماره ${recordToDelete!!.id} مربوط به «${recordToDelete!!.farmerName}» اطمینان دارید؟",
                    fontFamily = VazirmatnFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRecord(recordToDelete!!.id)
                    recordToDelete = null
                }) {
                    Text("حذف", color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("انصراف", fontFamily = VazirmatnFamily)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // نوار جستجو
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("جستجو بر اساس نام باغدار، کارگر، راننده، سالن یا پلاک...", fontSize = 12.sp, fontFamily = VazirmatnFamily) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // فیلتر ورود / خروج
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterTabButton(
                        text = "همه موارد",
                        isSelected = state.selectedTypeFilter == null,
                        color = EmeraldPrimary,
                        onClick = { onTypeFilterChange(null) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterTabButton(
                        text = "ورودی‌ها",
                        isSelected = state.selectedTypeFilter == RecordType.ENTRY,
                        color = EntryGreen,
                        onClick = { onTypeFilterChange(RecordType.ENTRY) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterTabButton(
                        text = "خروجی‌ها",
                        isSelected = state.selectedTypeFilter == RecordType.EXIT,
                        color = ExitOrange,
                        onClick = { onTypeFilterChange(RecordType.EXIT) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // فیلتر انواع سیب
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedCategoryFilter == null,
                        onClick = { onCategoryFilterChange(null) },
                        label = { Text("همه ارقام", fontFamily = VazirmatnFamily) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldContainer,
                            selectedLabelColor = EmeraldPrimary
                        )
                    )
                    AppleCategory.values().forEach { cat ->
                        FilterChip(
                            selected = state.selectedCategoryFilter == cat,
                            onClick = {
                                onCategoryFilterChange(if (state.selectedCategoryFilter == cat) null else cat)
                            },
                            label = { Text(cat.titleFa, fontFamily = VazirmatnFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // شمارش تعداد رکوردها و دکمه سورت
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعداد: ${JalaliDateHelper.toPersianDigits(state.filteredRecords.size.toString())} بار (${JalaliDateHelper.toPersianDigits(cargoGroups.size.toString())} کد)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontFamily = VazirmatnFamily
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable { isSortAscending = !isSortAscending }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "مرتب‌سازی",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSortAscending) "قدیمی‌ترین" else "جدیدترین",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldPrimary,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // فهرست رکوردها (گروه‌بندی شده کرکره‌ای بر اساس کد بار)
            if (cargoGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(14.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "موردی با این مشخصات یافت نشد.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }
            } else {
                items(cargoGroups, key = { it.groupKey }) { group ->
                    val isGroupExpanded = expandedCargoCodes.contains(group.groupKey)
                    InventoryCargoGroupCard(
                        group = group,
                        isExpanded = isGroupExpanded,
                        onToggleExpand = {
                            if (isGroupExpanded) expandedCargoCodes.remove(group.groupKey)
                            else expandedCargoCodes.add(group.groupKey)
                        },
                        expandedRecordIds = expandedRecordIds,
                        onToggleRecordExpand = { recordId ->
                            if (expandedRecordIds.contains(recordId)) expandedRecordIds.remove(recordId)
                            else expandedRecordIds.add(recordId)
                        },
                        onEditRecord = onEditRecord,
                        onDeleteRecord = { recordToDelete = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // دکمه FAB ثبت جدید
        FloatingActionButton(
            onClick = onNavigateToNewRecord,
            containerColor = EmeraldPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ثبت عملیات جدید", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
            }
        }
    }
}

@Composable
private fun FilterTabButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .background(if (isSelected) color else color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) color else color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = VazirmatnFamily
        )
    }
}

@Composable
private fun RecordItemCard(
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isEntry) AppleGreenLight else Color(0xFFFFEDD5), RoundedCornerShape(10.dp)),
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
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "کد ${record.cargoCode} • ${record.farmerName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = VazirmatnFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${record.dateWeekday}، ${record.dateShamsi} • ساعت ${record.timeStr}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = VazirmatnFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BadgePill(text = record.id, backgroundColor = SurfaceVariant, textColor = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "باز کردن جزئیات",
                        tint = TextSecondary,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "اتاق ${record.roomNumber}، پارت ${record.partNumber} • ${record.displayAppleName} (${record.boxType.titleFa})",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = VazirmatnFamily
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${record.vehicleType.titleFa} | پلاک: ${record.plateNumber.ifBlank { "ندارد" }}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = VazirmatnFamily
                        )
                    }
                    if (record.workers.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "کارگران: ${JalaliDateHelper.toPersianDigits(record.workers.size.toString())} نفر | کرایه: ${NumberFormatHelper.formatToman(record.totalHandlingFee.toLong(), true)}",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (isEntry) "+" else "-"}${NumberFormatHelper.formatBoxes(record.boxCount, true)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isEntry) EntryGreen else ExitOrange,
                            fontFamily = VazirmatnFamily
                        )
                        if (record.netWeightKg > 0) {
                            Text(
                                text = NumberFormatHelper.formatWeightTon(record.netWeightKg, true),
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // دکمه ویرایش مداد کنار سطل آشغال
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "ویرایش بار", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    // دکمه سطل آشغال
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف بار", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // بخش بازشونده جزئیات فاکتور
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

                    // مشخصات راننده و خودرو
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("نام راننده:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text(
                                text = record.driverName.ifBlank { "ثبت نشده" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("شماره پلاک:", fontSize = 11.sp, color = TextMuted, fontFamily = VazirmatnFamily)
                            Text(
                                text = record.plateNumber.ifBlank { "ندارد" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // اطلاعات وزن باسکول
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // هزینه تخلیه / بارگیری
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "کرایه تخلیه/بارگیری (${record.feeCalculationType.titleFa}):",
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // کارگران و وضعیت تسویه
                    if (record.workers.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تیم کارگری (${JalaliDateHelper.toPersianDigits(record.workers.size.toString())} نفر):",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = if (record.isWorkersFullySettled) "کاملاً تسویه شده" else "دارای مانده تسویه",
                                fontSize = 10.sp,
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
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(if (w.isSettled) EntryGreen else ExitOrange, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${w.name} (${w.role.titleFa})", fontSize = 11.sp, fontFamily = VazirmatnFamily)
                                }
                                if (w.isSettled) {
                                    Text(
                                        text = "تسویه شد" + (if (!w.settlementDate.isNullOrBlank()) " (${w.settlementDate})" else "") + " • ${NumberFormatHelper.format(w.paidAmount.toLong(), true)} ت",
                                        fontSize = 10.sp,
                                        color = EntryGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = VazirmatnFamily
                                    )
                                } else {
                                    Text(
                                        text = "پرداختی: ${NumberFormatHelper.format(w.paidAmount.toLong(), true)} ت" + (if (w.remainingWage > 0) " (مانده: ${NumberFormatHelper.format(w.remainingWage.toLong(), true)} ت)" else ""),
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (record.notes.isNotBlank()) {
                        Text("یادداشت: ${record.notes}", fontSize = 11.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // دکمه‌های عملیاتی ویرایش و حذف درون کرکره
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
                            Text("حذف بار", fontSize = 12.sp, fontFamily = VazirmatnFamily)
                        }
                    }
                }
            }
        }
    }
}

/**
 * مدل داده‌ای گروه‌بندی بارها بر اساس کد بار در صفحه ثبت بار
 */
data class InventoryCargoGroup(
    val groupKey: String,
    val cargoCode: String,
    val farmerName: String,
    val totalBoxes: Int,
    val inBoxes: Int,
    val outBoxes: Int,
    val records: List<ColdStorageEntry>
)

/**
 * کارت آکاردئونی سرگروه کد بار در صفحه ثبت بار (مشابه صفحه وضعیت روز با محدودیت ۵ بار اولیه و دکمه بیشتر)
 */
@Composable
private fun InventoryCargoGroupCard(
    group: InventoryCargoGroup,
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
            // ردیف هدر کد بار
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // سمت راست: برچسب کد و نام باغدار
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
                            text = "${JalaliDateHelper.toPersianDigits(group.records.size.toString())} فاکتور ثبت شده",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = VazirmatnFamily
                        )
                    }
                }

                // سمت چپ: مجموع جعبه‌ها و فلش کرکره
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
                            contentDescription = "باز کردن فاکتورهای این کد",
                            tint = if (isExpanded) EmeraldPrimary else TextSecondary,
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }
                }
            }

            // فاکتورهای درون کرکره کد بار با محدودیت اولیه ۵ عدد و دکمه نمایش بیشتر
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                var showAllRecords by remember(group.groupKey) { mutableStateOf(false) }
                val visibleRecords = if (showAllRecords || group.records.size <= 5) group.records else group.records.take(5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

                    visibleRecords.forEach { record ->
                        val isRecordExpanded = expandedRecordIds.contains(record.id)
                        RecordItemCard(
                            record = record,
                            isExpanded = isRecordExpanded,
                            onToggleExpand = { onToggleRecordExpand(record.id) },
                            onEdit = { onEditRecord(record) },
                            onDelete = { onDeleteRecord(record) }
                        )
                    }

                    if (group.records.size > 5) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface)
                                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                                .clickable { showAllRecords = !showAllRecords }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (showAllRecords) Icons.Default.ArrowUpward else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showAllRecords) "نمایش کمتر (۵ فاکتور اول)" else "نمایش فاکتورهای بیشتر (${JalaliDateHelper.toPersianDigits((group.records.size - 5).toString())} فاکتور دیگر)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

