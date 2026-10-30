package ir.sardyar.coldstorage.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sardyar.coldstorage.domain.model.BoxType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import ir.sardyar.coldstorage.domain.model.FeeCalculationType
import ir.sardyar.coldstorage.domain.model.LotStockBalance
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.domain.model.VehicleType
import ir.sardyar.coldstorage.domain.model.WorkerEntry
import ir.sardyar.coldstorage.domain.model.WorkerRole
import ir.sardyar.coldstorage.presentation.components.JalaliDatePickerDialog

data class WorkerFormItem(
    val name: String = "",
    val role: WorkerRole = WorkerRole.UNLOADER,
    val agreedWage: String = "",
    val paidAmount: String = "",
    val isSettled: Boolean = false,
    val settlementDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecordScreen(
    initialType: RecordType = RecordType.ENTRY,
    editingRecord: ColdStorageEntry? = null,
    facilities: List<ColdStorageFacility> = emptyList(),
    defaultFacilityId: String? = null,
    lotBalances: List<LotStockBalance> = emptyList(),
    allRecords: List<ColdStorageEntry> = emptyList(),
    onSave: (ColdStorageEntry) -> Unit,
    onBack: () -> Unit
) {
    val todayJalali = remember { JalaliDateHelper.getTodayJalali() }
    var selectedJalali by remember(editingRecord?.id) {
        mutableStateOf(
            if (editingRecord != null) {
                JalaliDateHelper.parseShortDate(editingRecord.dateShamsi) ?: todayJalali
            } else todayJalali
        )
    }
    var dayOffset by remember(editingRecord?.id) {
        mutableIntStateOf(
            if (editingRecord != null) {
                val parsed = JalaliDateHelper.parseShortDate(editingRecord.dateShamsi)
                if (parsed != null) JalaliDateHelper.getOffsetForJalali(parsed.year, parsed.month, parsed.day) else 0
            } else 0
        )
    }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    var selectedFacilityId by remember(editingRecord?.id) {
        mutableStateOf(editingRecord?.facilityId ?: (if (defaultFacilityId != null && defaultFacilityId != "ALL") defaultFacilityId else facilities.firstOrNull()?.id ?: "FAC-01"))
    }
    var recordType by remember(editingRecord?.id) { mutableStateOf(editingRecord?.operationType ?: initialType) }
    var isNoCode by remember(editingRecord?.id) {
        mutableStateOf(editingRecord?.cargoCode?.startsWith("بدون کد") == true || editingRecord?.cargoCode?.startsWith("بدون-کد") == true)
    }
    val knownCodeMap = remember(lotBalances) {
        lotBalances.associate { it.cargoCode.trim() to it.ownerName.trim() }
    }
    var cargoCode by remember(editingRecord?.id) {
        mutableStateOf(if (isNoCode) "" else (editingRecord?.cargoCode ?: ""))
    }
    var farmerName by remember(editingRecord?.id) {
        mutableStateOf(editingRecord?.farmerName ?: "")
    }
    var autoFilledOwnerName by remember(editingRecord?.id) {
        mutableStateOf(if (!isNoCode && knownCodeMap.containsKey(cargoCode.trim())) knownCodeMap[cargoCode.trim()] ?: "" else "")
    }
    var farmerPhone by remember(editingRecord?.id) { mutableStateOf(editingRecord?.farmerPhone ?: "") }
    var selectedCategory by remember(editingRecord?.id) { mutableStateOf(editingRecord?.appleCategory ?: AppleCategory.YELLOW) }
    var isGradeTwo by remember(editingRecord?.id) { mutableStateOf(editingRecord?.isGradeTwo ?: false) }
    var selectedBoxType by remember(editingRecord?.id) { mutableStateOf(editingRecord?.boxType ?: BoxType.TWO_ROW) }
    var customAppleName by remember(editingRecord?.id) { mutableStateOf(editingRecord?.customAppleName ?: "") }

    // سوابق بارهای این کد جهت تفکیک دقیق موجودی هر مدل جعبه
    val currentCodeRecords = remember(cargoCode, allRecords) {
        if (cargoCode.isNotBlank()) {
            allRecords.filter { it.cargoCode.trim() == cargoCode.trim() }
        } else emptyList()
    }
    val stockByBoxType = remember(currentCodeRecords) {
        BoxType.values().associateWith { bType ->
            val inB = currentCodeRecords.filter { it.operationType == RecordType.ENTRY && it.boxType == bType }.sumOf { it.boxCount }
            val outB = currentCodeRecords.filter { it.operationType == RecordType.EXIT && it.boxType == bType }.sumOf { it.boxCount }
            (inB - outB).coerceAtLeast(0)
        }
    }

    // شمارشگر جعبه (پیش‌فرض صفر)
    var boxCountInt by remember(editingRecord?.id) { mutableIntStateOf(editingRecord?.boxCount ?: 0) }
    var isManualBoxDialogShow by remember { mutableStateOf(false) }
    var manualBoxInput by remember(editingRecord?.id) { mutableStateOf(if ((editingRecord?.boxCount ?: 0) > 0) editingRecord?.boxCount.toString() else "") }

    // ورودی‌های باسکول (پیش‌فرض غیرفعال و خالی)
    var isScaleEnabled by remember(editingRecord?.id) {
        mutableStateOf((editingRecord?.grossWeightKg ?: 0.0) > 0.0)
    }
    var grossWeight by remember(editingRecord?.id) {
        mutableStateOf<String>(if ((editingRecord?.grossWeightKg ?: 0.0) > 0) editingRecord?.grossWeightKg?.toInt().toString() else "")
    }
    var tareWeight by remember(editingRecord?.id) {
        mutableStateOf<String>(if ((editingRecord?.tareWeightKg ?: 0.0) > 0) editingRecord?.tareWeightKg?.toString() ?: "" else "")
    }

    // وسیله نقلیه
    var selectedVehicle by remember(editingRecord?.id) { mutableStateOf(editingRecord?.vehicleType ?: VehicleType.NISSAN) }
    var driverName by remember(editingRecord?.id) { mutableStateOf(editingRecord?.driverName ?: "") }
    var plateNumber by remember(editingRecord?.id) { mutableStateOf(editingRecord?.plateNumber ?: "") }

    // محاسبه هزینه تخلیه/بارگیری (پیش‌فرض خالی / بدون پیش‌فرض عددی اشتباه)
    var feeCalcType by remember(editingRecord?.id) { mutableStateOf(editingRecord?.feeCalculationType ?: FeeCalculationType.PER_BOX) }
    var unitRateStr by remember(editingRecord?.id) {
        mutableStateOf(if ((editingRecord?.unitRate ?: 0.0) > 0) editingRecord?.unitRate?.toInt().toString() else "")
    }

    // لیست کارگران (پیش‌فرض صفر کارگر)
    val workersList = remember(editingRecord?.id) {
        mutableStateListOf<WorkerFormItem>().apply {
            if (editingRecord != null && editingRecord.workers.isNotEmpty()) {
                editingRecord.workers.forEach { w ->
                    add(
                        WorkerFormItem(
                            name = w.name,
                            role = w.role,
                            agreedWage = if (w.agreedWage > 0) w.agreedWage.toInt().toString() else "",
                            paidAmount = if (w.paidAmount > 0) w.paidAmount.toInt().toString() else "",
                            isSettled = w.isSettled,
                            settlementDate = w.settlementDate ?: ""
                        )
                    )
                }
            }
        }
    }

    var notes by remember(editingRecord?.id) { mutableStateOf(editingRecord?.notes ?: "") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedRecordSummary by remember { mutableStateOf<ColdStorageEntry?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val grossVal = NumberFormatHelper.parseDouble(grossWeight)
    val tareVal = NumberFormatHelper.parseDouble(tareWeight)
    val netCalculated = (grossVal - tareVal).coerceAtLeast(0.0)
    val unitRate = NumberFormatHelper.parseDouble(unitRateStr)

    // محاسبه خودکار مبلغ کل تخلیه/بارگیری
    val totalHandlingFee = if (feeCalcType == FeeCalculationType.PER_BOX) {
        boxCountInt * unitRate
    } else {
        unitRate
    }

    var vehicleExpanded by remember { mutableStateOf(false) }

    // دیالوگ تقویم شمسی برای انتخاب سال، ماه و روز
    if (isDatePickerOpen) {
        JalaliDatePickerDialog(
            initialJalali = selectedJalali,
            onDateSelected = { jalali ->
                selectedJalali = jalali
                dayOffset = JalaliDateHelper.getOffsetForJalali(jalali.year, jalali.month, jalali.day)
                isDatePickerOpen = false
            },
            onDismiss = { isDatePickerOpen = false }
        )
    }

    // دیالوگ ورود دستی تعداد جعبه
    if (isManualBoxDialogShow) {
        AlertDialog(
            onDismissRequest = { isManualBoxDialogShow = false },
            title = {
                Text(
                    text = "ورود دستی تعداد جعبه",
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFamily
                )
            },
            text = {
                Column {
                    Text(
                        text = "عدد دلخواه را تایپ کنید:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontFamily = VazirmatnFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualBoxInput,
                        onValueChange = { manualBoxInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = NumberFormatHelper.parseInt(manualBoxInput)
                        if (parsed >= 0) {
                            boxCountInt = parsed
                        }
                        isManualBoxDialogShow = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ثبت عدد", fontFamily = VazirmatnFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isManualBoxDialogShow = false }) {
                    Text("انصراف", fontFamily = VazirmatnFamily)
                }
            }
        )
    }

    // دیالوگ انیمیشن موفقیت
    if (showSuccessDialog && savedRecordSummary != null) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EntryGreen,
                    modifier = Modifier.size(54.dp)
                )
            },
            title = {
                Text(
                    text = "عملیات با موفقیت ثبت شد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = VazirmatnFamily
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "قبض شماره ${savedRecordSummary!!.id} با موفقیت در انبار ذخیره شد.",
                        fontSize = 13.sp,
                        fontFamily = VazirmatnFamily,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صاحب بار:", fontSize = 12.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                                Text(savedRecordSummary!!.farmerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("تعداد جعبه:", fontSize = 12.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                                Text(NumberFormatHelper.formatBoxes(savedRecordSummary!!.boxCount, true), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("کرایه تخلیه:", fontSize = 12.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                                Text(NumberFormatHelper.formatToman(savedRecordSummary!!.totalHandlingFee.toLong(), true), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontFamily = VazirmatnFamily)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تایید و بازگشت به داشبورد", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editingRecord != null) "ویرایش بار (${editingRecord.id})"
                        else if (recordType == RecordType.ENTRY) "فرم ورود سیب (تخلیه در سردخانه)"
                        else "فرم خروج سیب (بارگیری و ترخیص)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = VazirmatnFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ۱. سوئیچ ورودی / خروجی
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    val isEntry = recordType == RecordType.ENTRY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isEntry) EntryGreen else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { recordType = RecordType.ENTRY }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = if (isEntry) Color.White else TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "تحویل ورودی (تخلیه)",
                                color = if (isEntry) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (!isEntry) ExitOrange else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { recordType = RecordType.EXIT }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = if (!isEntry) Color.White else TextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "بار خروجی (بارگیری)",
                                color = if (!isEntry) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // تاریخ ثبت بار با قابلیت انتخاب سال، ماه و روز
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // بالای صفحه: تاریخ امروز نوشته شده باشد
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EntryGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تاریخ امروز: ${JalaliDateHelper.formatFullWithDay(todayJalali, toPersianDigits = true)}",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            if (dayOffset != 0) {
                                Text(
                                    text = "تنظیم روی امروز",
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirmatnFamily,
                                    modifier = Modifier
                                        .clickable {
                                            dayOffset = 0
                                            selectedJalali = todayJalali
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // کنترلر انتخاب روزها و ماه‌های دیگر
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    dayOffset++
                                    selectedJalali = JalaliDateHelper.getJalaliForOffset(dayOffset)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "روز بعد", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }

                            // کلیک روی کادر تاریخ جهت باز شدن تقویم شمسی
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldContainer.copy(alpha = 0.45f))
                                    .clickable { isDatePickerOpen = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = JalaliDateHelper.formatFullWithDay(selectedJalali, toPersianDigits = true),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary,
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                                Text(
                                    text = "برای انتخاب از تقویم (سال، ماه یا روز) لمس کنید",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = VazirmatnFamily
                                )
                            }

                            IconButton(
                                onClick = {
                                    dayOffset--
                                    selectedJalali = JalaliDateHelper.getJalaliForOffset(dayOffset)
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "روز قبل", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // پیام خطای اعتبارسنجی
            if (validationError != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = validationError ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirmatnFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ۲. مشخصات کد بار و صاحب کالا
            item {
                SectionCard(title = "کد بار کشاورز و مشخصات صاحب کالا") {
                    // تیک‌باکس استثنا: ثبت بدون کد
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNoCode) Color(0xFFFEF3C7) else SurfaceVariant)
                            .border(1.dp, if (isNoCode) Color(0xFFF59E0B) else BorderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                isNoCode = !isNoCode
                                if (isNoCode) {
                                    validationError = null
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isNoCode,
                            onCheckedChange = { isNoCode = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ثبت بدون کد",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNoCode) Color(0xFF92400E) else TextPrimary,
                            fontFamily = VazirmatnFamily
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isNoCode) {
                        // انتخاب سریع از کدهای شناخته‌شده سردخانه
                        if (lotBalances.isNotEmpty()) {
                            Text(
                                text = if (recordType == RecordType.EXIT) "انتخاب از کدهای موجود سردخانه (الزامی):" else "انتخاب از کدهای موجود:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontFamily = VazirmatnFamily
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val sourceLots = if (recordType == RecordType.EXIT) lotBalances.filter { it.remainingBoxes > 0 } else lotBalances
                                sourceLots.forEach { bal ->
                                    val isCurrent = cargoCode == bal.cargoCode
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) EmeraldPrimary else SurfaceVariant)
                                            .border(1.dp, if (isCurrent) EmeraldPrimary else BorderColor, RoundedCornerShape(10.dp))
                                            .clickable {
                                                cargoCode = bal.cargoCode
                                                farmerName = bal.ownerName
                                                autoFilledOwnerName = bal.ownerName
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "کد ${bal.cargoCode} • ${bal.ownerName.take(18)}",
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) Color.White else TextPrimary,
                                            fontFamily = VazirmatnFamily
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        CustomLargeField(
                            label = if (recordType == RecordType.EXIT) "کد بار کشاورز (انتخاب از کدهای موجود سردخانه) *" else "کد بار کشاورز (اتاق . پارت) * - مرجع اصلی بار",
                            value = cargoCode,
                            onValueChange = { inputCode ->
                                cargoCode = inputCode
                                val matched = knownCodeMap[inputCode.trim()]
                                if (matched != null && matched.isNotBlank()) {
                                    farmerName = matched
                                    autoFilledOwnerName = matched
                                } else {
                                    autoFilledOwnerName = ""
                                }
                            },
                            placeholder = "مثال: 3.8 (سمت چپ اتاق ۳ ، سمت راست پارت ۸)",
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // پیام تکمیل خودکار نام کشاورز از روی کد بار (در صورت وجود)
                        if (autoFilledOwnerName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "شناسایی خودکار: کد $cargoCode متعلق به «$autoFilledOwnerName» است و نام کشاورز تنظیم شد.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        } else if (recordType == RecordType.ENTRY && cargoCode.isNotBlank() && !knownCodeMap.containsKey(cargoCode.trim())) {
                            // در ثبت ورودی: پیام کد جدید و ساخته شدن کشاورز جدید
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "این کد از قبل وجود ندارد و کشاورز جدید ساخته می‌شود.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8),
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        } else if (recordType == RecordType.EXIT && cargoCode.isNotBlank() && !knownCodeMap.containsKey(cargoCode.trim())) {
                            // در ثبت خروجی: اخطار ممانعت از ایجاد کد ناموجود
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "در خروج کشاورز جدید ساخته نمی‌شود و فرم خروج فقط باید از کدهای موجود انتخاب شود.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C),
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        }

                        // نمایش تفسیر کد بار به صورت زنده
                        val roomNum = cargoCode.substringBefore('.').trim()
                        val partNum = if (cargoCode.contains('.')) cargoCode.substringAfter('.').trim() else ""
                        if (roomNum.isNotBlank() && partNum.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "کد بار مرجع: $cargoCode • اتاق $roomNum (چپ) | پارت $partNum (راست)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }

                        // وضعیت موجودی کد انتخابی در صورت عملیات خروج
                        if (recordType == RecordType.EXIT && cargoCode.isNotBlank()) {
                            val matchedBal = lotBalances.firstOrNull { it.cargoCode == cargoCode }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (matchedBal != null) {
                                val isOverLimit = boxCountInt > matchedBal.remainingBoxes
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isOverLimit) Color(0xFFFEE2E2) else EmeraldContainer.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .border(1.dp, if (isOverLimit) Color(0xFFEF4444) else EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "موجودی فعلی کد $cargoCode: ${NumberFormatHelper.formatBoxes(matchedBal.remainingBoxes, true)} (${NumberFormatHelper.formatTonnage(matchedBal.totalNetWeightKg / 1000.0, true)} تن)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverLimit) Color(0xFFDC2626) else EmeraldPrimary,
                                            fontFamily = VazirmatnFamily
                                        )
                                        if (isOverLimit) {
                                            Text(
                                                text = "هشدار: تعداد خروجی ($boxCountInt جعبه) از موجودی کد $cargoCode بیشتر است!",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDC2626),
                                                fontFamily = VazirmatnFamily
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "توجه: سابقه‌ای برای کد $cargoCode در این سردخانه ثبت نشده است.",
                                    fontSize = 11.sp,
                                    color = ExitOrange,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                        }
                    } else {
                        // حالت بدون کد بار فعال است
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "حالت ثبت بدون کد فعال است: این بار با نام شخص ثبت شده و در موجودی‌ها بر اساس نام شخص دسته‌بندی می‌شود.",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    CustomLargeField(
                        label = if (isNoCode) "نام کشاورز / صاحب بار (الزامی - مرجع بار) *" else if (recordType == RecordType.ENTRY) "نام کشاورز / صاحب بار *" else "نام کشاورز / صاحب بار (تثبیت‌شده) *",
                        value = farmerName,
                        onValueChange = { farmerName = it },
                        placeholder = "مثال: مهندس رستمی"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomLargeField(
                        label = "شماره تماس همراه",
                        value = farmerPhone,
                        onValueChange = { farmerPhone = it },
                        placeholder = "مثال: 09149876543",
                        keyboardType = KeyboardType.Phone
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۳. انتخاب نوع سیب و تعیین درجه ۲
            item {
                SectionCard(title = "نوع و کیفیت سیب") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            Pair(AppleCategory.YELLOW, "سیب زرد"),
                            Pair(AppleCategory.RED, "سیب قرمز"),
                            Pair(AppleCategory.CUSTOM, "+ نوع سفارشی")
                        )
                        categories.forEach { (cat, title) ->
                            val isSel = selectedCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        title,
                                        fontFamily = VazirmatnFamily,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldContainer,
                                    selectedLabelColor = EmeraldPrimary
                                )
                            )
                        }
                    }
                    if (selectedCategory == AppleCategory.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CustomLargeField(
                            label = "ثبت نوع سفارشی سیب *",
                            value = customAppleName,
                            onValueChange = { customAppleName = it },
                            placeholder = "مثال: سیب قندک، کهنز، گالا..."
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // تیک‌باکس سیب درجه ۲
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isGradeTwo) EmeraldContainer.copy(alpha = 0.35f) else SurfaceVariant)
                            .border(1.dp, if (isGradeTwo) EmeraldPrimary else BorderColor, RoundedCornerShape(12.dp))
                            .clickable { isGradeTwo = !isGradeTwo }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isGradeTwo,
                            onCheckedChange = { isGradeTwo = it },
                            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "سیب درجه ۲ (کیفیت پادرختی / درجه دو)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGradeTwo) EmeraldPrimary else TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                            Text(
                                text = "رنگ سیب از گزینه‌های بالا (زرد یا قرمز) تعیین می‌شود",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۴. مدل سبد (اجباری) و تعداد جعبه (با شمارشگر ممتد شتاب‌دار و تپ دستی)
            item {
                SectionCard(title = "مدل سبد و تعداد جعبه") {
                    Text(
                        text = "مدل سبد (انتخاب الزامی):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = VazirmatnFamily
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // مدل سبدها به ترتیب درخواستی: دو ردیفه، سه ردیفه، تک ردیفه، ۲۰ کیلویی، ۳۰ کیلویی، سایر
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val boxTypesOrdered = listOf(
                            BoxType.TWO_ROW,
                            BoxType.THREE_ROW,
                            BoxType.SINGLE_ROW,
                            BoxType.KG_20,
                            BoxType.KG_30,
                            BoxType.OTHER
                        )
                        boxTypesOrdered.forEach { bType ->
                            val isSel = selectedBoxType == bType
                            val typeStock = stockByBoxType[bType] ?: 0
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedBoxType = bType },
                                label = {
                                    Column {
                                        Text(
                                            bType.titleFa,
                                            fontFamily = VazirmatnFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (recordType == RecordType.EXIT && !isNoCode) {
                                            Text(
                                                text = "موجودی: $typeStock",
                                                fontSize = 9.sp,
                                                color = if (typeStock > 0) (if (isSel) Color.White else EntryGreen) else Color.Red,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = VazirmatnFamily
                                            )
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldContainer,
                                    selectedLabelColor = EmeraldPrimary
                                )
                            )
                        }
                    }

                    // در حالت خروج: نمایش موجودی مشخص مدل سبد انتخاب شده
                    if (recordType == RecordType.EXIT && !isNoCode) {
                        val currentAvailable = stockByBoxType[selectedBoxType] ?: 0
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (currentAvailable > 0) EmeraldContainer.copy(alpha = 0.4f) else Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                                .border(1.dp, if (currentAvailable > 0) EmeraldPrimary.copy(alpha = 0.35f) else Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "موجودی سبد «${selectedBoxType.titleFa}» این کد:",
                                    fontSize = 11.5.sp,
                                    fontFamily = VazirmatnFamily,
                                    color = if (currentAvailable > 0) TextPrimary else Color(0xFFB91C1C)
                                )
                                Text(
                                    text = "$currentAvailable جعبه",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = VazirmatnFamily,
                                    color = if (currentAvailable > 0) EntryGreen else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("تعداد کل سبد / جعبه (${selectedBoxType.titleFa})", fontSize = 12.sp, color = TextSecondary, fontFamily = VazirmatnFamily)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                RepeatingCounterButton(
                                    icon = Icons.Default.Remove,
                                    contentDescription = "کاهش یکی یکی یا ممتد",
                                    enabled = boxCountInt > 0,
                                    onClickStep = {
                                        if (boxCountInt > 0) boxCountInt--
                                    }
                                )

                                Spacer(modifier = Modifier.width(20.dp))

                                // تپ روی عدد درشت جهت باز شدن فیلد ورود دستی
                                Box(
                                    modifier = Modifier
                                        .background(EmeraldContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            manualBoxInput = if (boxCountInt > 0) boxCountInt.toString() else ""
                                            isManualBoxDialogShow = true
                                        }
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = NumberFormatHelper.format(boxCountInt, true),
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary,
                                        fontFamily = VazirmatnFamily
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                RepeatingCounterButton(
                                    icon = Icons.Default.Add,
                                    contentDescription = "افزایش یکی یکی یا ممتد",
                                    onClickStep = {
                                        boxCountInt++
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "با یک تپ ۱ عدد کم/زیاد می‌شود • با نگه‌داشتن ممتد سرعت شتاب می‌گیرد • برای ورود دستی روی عدد لمس کنید",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // دکمه‌های پرسرعت اضافه کردن رند (+10, +50, +100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAddButton(text = "+۱۰", onClick = { boxCountInt += 10 }, modifier = Modifier.weight(1f))
                        QuickAddButton(text = "+۵۰", onClick = { boxCountInt += 50 }, modifier = Modifier.weight(1f))
                        QuickAddButton(text = "+۱۰۰", onClick = { boxCountInt += 100 }, modifier = Modifier.weight(1f))
                        QuickAddButton(text = "صفر", onClick = { boxCountInt = 0 }, color = Color.Red, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // تیک باکس فعال‌سازی وزن باسکول و افت سبد (پیش‌فرض غیرفعال)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isScaleEnabled) EmeraldPrimary.copy(alpha = 0.08f) else SurfaceVariant)
                            .border(
                                1.dp,
                                if (isScaleEnabled) EmeraldPrimary.copy(alpha = 0.35f) else BorderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                isScaleEnabled = !isScaleEnabled
                                if (!isScaleEnabled) {
                                    grossWeight = ""
                                    tareWeight = ""
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isScaleEnabled,
                                onCheckedChange = { checked ->
                                    isScaleEnabled = checked
                                    if (!checked) {
                                        grossWeight = ""
                                        tareWeight = ""
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ثبت وزن باسکول و افت سبدها",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isScaleEnabled) EmeraldPrimary else TextPrimary,
                                    fontFamily = VazirmatnFamily
                                )
                                Text(
                                    text = if (isScaleEnabled) "تنظیمات باسکول فعال است (وارد کردن اختیاری)" else "غیرفعال (اکثراً باسکول گرفته نمی‌شود)",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = if (isScaleEnabled) EmeraldPrimary else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // نمایش فیلدهای باسکول و افت فقط در صورت فعال بودن تیک‌باکس
                    AnimatedVisibility(
                        visible = isScaleEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            // ورودی باسکول و وزن خالص
                            Row(modifier = Modifier.fillMaxWidth()) {
                                CustomLargeField(
                                    label = "وزن ناخالص باسکول (kg)",
                                    value = grossWeight,
                                    onValueChange = { grossWeight = it },
                                    placeholder = "مثال: ۸,۴۰۰",
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                CustomLargeField(
                                    label = "افت سبد ها (kg)",
                                    value = tareWeight,
                                    onValueChange = { tareWeight = it },
                                    placeholder = "مثال: ۸۴۰",
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val grossValParsed = NumberFormatHelper.parseDouble(grossWeight)
                            val tareValParsed = NumberFormatHelper.parseDouble(tareWeight)
                            val finalNetWeight = if (grossValParsed > 0.0) (grossValParsed - tareValParsed).coerceAtLeast(0.0) else 0.0
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EmeraldPrimary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "وزن خالص نهایی (${selectedBoxType.titleFa}):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        fontFamily = VazirmatnFamily
                                    )
                                    Text(
                                        text = if (finalNetWeight > 0.0) NumberFormatHelper.formatWeightKg(finalNetWeight, true) else "ثبت نشده (بدون باسکول)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (finalNetWeight > 0.0) EmeraldPrimary else TextMuted,
                                        fontFamily = VazirmatnFamily
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۵. بخش خودرو و نحوه محاسبه هزینه تخلیه/بارگیری
            item {
                SectionCard(title = "اطلاعات وسیله نقلیه و کرایه تخلیه/بارگیری") {
                    ExposedDropdownMenuBox(
                        expanded = vehicleExpanded,
                        onExpandedChange = { vehicleExpanded = !vehicleExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedVehicle.titleFa,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع وسیله نقلیه *", fontFamily = VazirmatnFamily) },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EmeraldPrimary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = vehicleExpanded,
                            onDismissRequest = { vehicleExpanded = false }
                        ) {
                            VehicleType.values().forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v.titleFa, fontFamily = VazirmatnFamily) },
                                    onClick = {
                                        selectedVehicle = v
                                        vehicleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomLargeField(
                            label = "نام راننده",
                            value = driverName,
                            onValueChange = { driverName = it },
                            placeholder = "مثال: محسن بهرامی",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        CustomLargeField(
                            label = "پلاک خودرو",
                            value = plateNumber,
                            onValueChange = { plateNumber = it },
                            placeholder = "مثال: ۴۳-ج-۵۱۲",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("نحوه محاسبه هزینه تخلیه/بارگیری:", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = feeCalcType == FeeCalculationType.PER_BOX,
                            onClick = { feeCalcType = FeeCalculationType.PER_BOX },
                            label = { Text("بر اساس تعداد جعبه", fontFamily = VazirmatnFamily) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = feeCalcType == FeeCalculationType.PER_VEHICLE,
                            onClick = { feeCalcType = FeeCalculationType.PER_VEHICLE },
                            label = { Text("به ازای هر سرویس ماشین", fontFamily = VazirmatnFamily) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldContainer,
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CustomLargeField(
                        label = if (feeCalcType == FeeCalculationType.PER_BOX) "نرخ واحد هر جعبه (تومان)" else "کرایه کل این سرویس ماشین (تومان)",
                        value = unitRateStr,
                        onValueChange = { unitRateStr = it },
                        placeholder = if (feeCalcType == FeeCalculationType.PER_BOX) "مثال: ۳,۵۰۰" else "مثال: ۲,۵۰۰,۰۰۰",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // باکس محاسبه آنی مبلغ کل تخلیه/بارگیری
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColdIceLight, RoundedCornerShape(14.dp))
                            .border(1.5.dp, ColdIce.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = ColdIce, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "مبلغ کل تخلیه/بارگیری:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ColdIce,
                                    fontFamily = VazirmatnFamily
                                )
                            }
                            Text(
                                NumberFormatHelper.formatToman(totalHandlingFee.toLong(), true),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = ColdIce,
                                fontFamily = VazirmatnFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ۶. بخش تیم کارگری و میانبر «تقسیم مساوی هزینه بین کارگران»
            item {
                SectionCard(title = "تیم کارگری و دستمزدها") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                workersList.add(
                                    WorkerFormItem(
                                        name = "",
                                        role = WorkerRole.UNLOADER,
                                        agreedWage = "",
                                        paidAmount = "",
                                        isSettled = false,
                                        settlementDate = ""
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("افزودن کارگر", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                        }

                        // دکمه میانبر جادویی: تقسیم مساوی هزینه بین کارگران
                        Button(
                            onClick = {
                                if (workersList.isNotEmpty() && totalHandlingFee > 0) {
                                    val equalShare = (totalHandlingFee / workersList.size).toLong().toString()
                                    for (i in 0 until workersList.size) {
                                        val cur = workersList[i]
                                        workersList[i] = cur.copy(
                                            agreedWage = equalShare,
                                            paidAmount = equalShare
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تقسیم مساوی بین کارگران", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // لیست کارگران ثبت‌شده با امکان تعیین وضعیت تسویه و تاریخ تسویه
            itemsIndexed(workersList) { idx, w ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(SurfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "کارگر ${JalaliDateHelper.toPersianDigits((idx + 1).toString())}" + (if (w.name.isNotBlank()) ": ${w.name}" else ""),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = VazirmatnFamily
                            )
                            IconButton(onClick = { workersList.removeAt(idx) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CustomLargeField(
                                label = "نام کارگر",
                                value = w.name,
                                onValueChange = { workersList[idx] = w.copy(name = it) },
                                placeholder = "مثال: علی احمدی",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CustomLargeField(
                                label = "دستمزد توافقی (تومان)",
                                value = w.agreedWage,
                                onValueChange = { workersList[idx] = w.copy(agreedWage = it) },
                                placeholder = "مبلغ توافقی",
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomLargeField(
                            label = "مبلغ پرداختی (تومان)",
                            value = w.paidAmount,
                            onValueChange = { workersList[idx] = w.copy(paidAmount = it) },
                            placeholder = "مبلغ پرداختی به کارگر",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // تیک‌باکس تسویه کارگر
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val newSettled = !w.isSettled
                                    workersList[idx] = w.copy(
                                        isSettled = newSettled,
                                        settlementDate = if (newSettled) (w.settlementDate.ifBlank { JalaliDateHelper.formatShort(selectedJalali, toPersianDigits = false) }) else ""
                                    )
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = w.isSettled,
                                onCheckedChange = { checked ->
                                    workersList[idx] = w.copy(
                                        isSettled = checked,
                                        settlementDate = if (checked) (w.settlementDate.ifBlank { JalaliDateHelper.formatShort(selectedJalali, toPersianDigits = false) }) else ""
                                    )
                                },
                                colors = CheckboxDefaults.colors(checkedColor = EntryGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تسویه",
                                fontSize = 12.sp,
                                fontWeight = if (w.isSettled) FontWeight.Bold else FontWeight.Normal,
                                color = if (w.isSettled) EntryGreen else TextPrimary,
                                fontFamily = VazirmatnFamily
                            )
                        }

                        // در صورت زدن تیک تسویه: فعال شدن کادر انتخاب تاریخ تسویه
                        if (w.isSettled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomLargeField(
                                label = "تاریخ تسویه حساب دستمزد",
                                value = w.settlementDate,
                                onValueChange = { workersList[idx] = w.copy(settlementDate = it) },
                                placeholder = "مثال: ۱۴۰۵/۰۸/۰۷"
                            )
                        }
                    }
                }
            }

            // ۷. یادداشت اختیاری و دکمه ذخیره بزرگ
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionCard(title = "توضیحات و مشخصات بار") {
                    CustomLargeField(
                        label = "یادداشت اختیاری",
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "مثال: سیب واکس خورده، سبد سفید درجه یک، سورت عالی"
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                // دکمه ذخیره نهایی
                Button(
                    onClick = {
                        if (!isNoCode && cargoCode.isBlank()) {
                            validationError = "لطفاً کد بار کشاورز (مانند 3.8) را وارد کنید یا در صورت تمایل تیک «بدون کد» را بزنید."
                            return@Button
                        }
                        if (recordType == RecordType.EXIT) {
                            if (isNoCode) {
                                val hasNoCodeLot = lotBalances.any { it.cargoCode.startsWith("بدون کد") && it.remainingBoxes > 0 }
                                if (!hasNoCodeLot) {
                                    validationError = "در هنگام ثبت بار خروجی، هیچ بار بدون کدی در انبار موجود نیست. فرم خروج فقط باید از کدهای موجود ثبت شود."
                                    return@Button
                                }
                            } else {
                                val existingBal = lotBalances.firstOrNull { it.cargoCode.trim() == cargoCode.trim() }
                                if (existingBal == null) {
                                    validationError = "در هنگام ثبت بار خروجی، امکان ایجاد کشاورز یا کد جدید وجود ندارد و فرم خروج فقط باید از کدهای موجود انتخاب شود."
                                    return@Button
                                }
                                val currentTypeStock = stockByBoxType[selectedBoxType] ?: 0
                                if (currentTypeStock <= 0) {
                                    validationError = "موجودی مدل سبد «${selectedBoxType.titleFa}» برای کد ${cargoCode.trim()} صفر است. نمی‌توانید از مدل سبدی که موجودی ندارد خروج ثبت کنید."
                                    return@Button
                                }
                                if (boxCountInt > currentTypeStock) {
                                    validationError = "تعداد خروجی ($boxCountInt جعبه) از مانده مدل سبد «${selectedBoxType.titleFa}» این کد ($currentTypeStock جعبه) بیشتر است. موجودی مدل جعبه‌ها جداگانه محاسبه می‌شوند."
                                    return@Button
                                }
                            }
                        }
                        if (farmerName.isBlank()) {
                            validationError = "لطفاً نام کشاورز / صاحب بار را وارد کنید."
                            return@Button
                        }
                        if (boxCountInt <= 0) {
                            validationError = "تعداد جعبه نمی‌تواند صفر یا منفی باشد."
                            return@Button
                        }
                        validationError = null

                        val finalCargoCode = if (isNoCode) "بدون کد (${farmerName.trim()})" else cargoCode.trim()
                        val gross = if (isScaleEnabled) NumberFormatHelper.parseDouble(grossWeight) else 0.0
                        val tare = if (isScaleEnabled) NumberFormatHelper.parseDouble(tareWeight) else 0.0
                        val net = if (gross > 0.0) (gross - tare).coerceAtLeast(0.0) else 0.0

                        val parsedWorkers = workersList.filter { it.name.isNotBlank() }.mapIndexed { index, w ->
                            WorkerEntry(
                                id = "W-${System.currentTimeMillis()}-$index",
                                name = w.name.trim(),
                                role = w.role,
                                agreedWage = NumberFormatHelper.parseDouble(w.agreedWage),
                                paidAmount = NumberFormatHelper.parseDouble(w.paidAmount),
                                isSettled = w.isSettled,
                                settlementDate = if (w.isSettled) w.settlementDate.trim().ifBlank { JalaliDateHelper.formatShort(selectedJalali, toPersianDigits = false) } else null
                            )
                        }

                        val entry = ColdStorageEntry(
                            id = editingRecord?.id ?: "CS-${System.currentTimeMillis().toString().takeLast(5)}",
                            cargoCode = finalCargoCode,
                            facilityId = selectedFacilityId,
                            operationType = recordType,
                            farmerName = farmerName.trim(),
                            farmerPhone = farmerPhone.trim(),
                            createdAt = editingRecord?.createdAt ?: System.currentTimeMillis(),
                            dateShamsi = JalaliDateHelper.formatShort(selectedJalali, toPersianDigits = false),
                            dateWeekday = selectedJalali.dayOfWeek,
                            timeStr = editingRecord?.timeStr ?: "۱۲:۰۰",
                            appleCategory = selectedCategory,
                            customAppleName = customAppleName.trim(),
                            isGradeTwo = isGradeTwo,
                            boxType = selectedBoxType,
                            boxCount = boxCountInt,
                            grossWeightKg = gross,
                            tareWeightKg = tare,
                            netWeightKg = net,
                            estimatedTonnage = net / 1000.0,
                            vehicleType = selectedVehicle,
                            driverName = driverName.trim(),
                            plateNumber = plateNumber.trim(),
                            feeCalculationType = feeCalcType,
                            unitRate = unitRate,
                            workers = parsedWorkers,
                            notes = notes.trim()
                        )

                        onSave(entry)
                        savedRecordSummary = entry
                        showSuccessDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recordType == RecordType.ENTRY) EntryGreen else ExitOrange
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (editingRecord != null) "ذخیره تغییرات بار"
                        else if (recordType == RecordType.ENTRY) "تایید و ثبت ورود به سردخانه"
                        else "تایید و صدور حواله خروج",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        fontFamily = VazirmatnFamily
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    text: String,
    onClick: () -> Unit,
    color: Color = EmeraldPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            fontFamily = VazirmatnFamily
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary, fontFamily = VazirmatnFamily)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun CustomLargeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, fontFamily = VazirmatnFamily)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = TextMuted, fontFamily = VazirmatnFamily) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface,
                unfocusedContainerColor = SurfaceVariant,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderColor
            )
        )
    }
}

/**
 * دکمه شمارشگر با قابلیت تپ تک‌تایی و نگه‌داشتن ممتد با شتاب خودکار متناسب با زمان نگه‌داشتن
 */
@Composable
private fun RepeatingCounterButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClickStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClickStep by rememberUpdatedState(onClickStep)

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            currentOnClickStep()
            delay(350L) // تاخیر اولیه پیش از فعال شدن تکرار خودکار
            var currentDelay = 140L
            var stepCount = 0
            while (isActive && isPressed && enabled) {
                currentOnClickStep()
                stepCount++
                currentDelay = when {
                    stepCount > 40 -> 15L  // بیشترین سرعت پس از چند ثانیه نگه‌داشتن
                    stepCount > 25 -> 30L
                    stepCount > 15 -> 60L
                    stepCount > 6 -> 100L
                    else -> 140L
                }
                delay(currentDelay)
            }
        }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (enabled) EmeraldPrimary else SurfaceVariant)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else TextMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}
