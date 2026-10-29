package ir.sardyar.coldstorage.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import ir.sardyar.coldstorage.core.util.NumberFormatHelper
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility

/**
 * دکمه چیپ انتخاب سردخانه در هدر برنامه
 */
@Composable
fun FacilitySelectorChip(
    currentFacility: ColdStorageFacility?,
    isAllSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = if (isAllSelected || currentFacility == null) {
        EmeraldPrimary
    } else {
        currentFacility.composeColor
    }

    val title = if (isAllSelected) {
        "همه سردخانه‌ها (تجمیعی)"
    } else {
        currentFacility?.name ?: "انتخاب سردخانه"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, labelColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // نقطه رنگی لیبل سردخانه
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(labelColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = VazirmatnFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "تغییر سردخانه",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * مودال مدیریت سردخانه‌ها و سوییچ بین آن‌ها
 */
@Composable
fun FacilityManagementDialog(
    facilities: List<ColdStorageFacility>,
    activeFacilityId: String?,
    onSelectFacility: (String?) -> Unit,
    onAddNewFacility: () -> Unit,
    onEditFacility: (ColdStorageFacility) -> Unit,
    onDeleteFacility: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var facilityToDelete by remember { mutableStateOf<ColdStorageFacility?>(null) }

    if (facilityToDelete != null) {
        AlertDialog(
            onDismissRequest = { facilityToDelete = null },
            title = {
                Text(
                    "حذف سردخانه",
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFamily
                )
            },
            text = {
                Text(
                    "آیا از حذف «${facilityToDelete!!.name}» اطمینان دارید؟",
                    fontFamily = VazirmatnFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteFacility(facilityToDelete!!.id)
                    facilityToDelete = null
                }) {
                    Text("حذف", color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { facilityToDelete = null }) {
                    Text("انصراف", color = TextSecondary, fontFamily = VazirmatnFamily)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مدیریت و انتخاب سردخانه",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = VazirmatnFamily
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "سردخانه فعال را انتخاب کنید یا سردخانه جدید اضافه نمایید:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // گزینه ۱: همه سردخانه‌ها
                    item {
                        val isAllSelected = activeFacilityId == null || activeFacilityId == "ALL"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAllSelected) EmeraldContainer.copy(alpha = 0.4f) else SurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isAllSelected) EmeraldPrimary else BorderColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectFacility("ALL")
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(EmeraldPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "همه سردخانه‌ها (گزارش تجمیعی)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontFamily = VazirmatnFamily
                                        )
                                        Text(
                                            text = "نمایش آمار و بارهای تمامی واحدهای فعال",
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            fontFamily = VazirmatnFamily
                                        )
                                    }
                                }
                                if (isAllSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // لیست سردخانه‌های ثبت شده
                    items(facilities) { fac ->
                        val isSelected = activeFacilityId == fac.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) fac.composeColor.copy(alpha = 0.12f) else SurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) fac.composeColor else BorderColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onSelectFacility(fac.id)
                                    onDismiss()
                                }
                                .padding(12.dp)
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
                                            .size(14.dp)
                                            .background(fac.composeColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fac.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            fontFamily = VazirmatnFamily,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val subtitle = buildString {
                                            if (fac.managerName.isNotBlank()) append("مسئول: ${fac.managerName} • ")
                                            if (fac.capacityBoxes > 0) append("ظرفیت: ${NumberFormatHelper.formatBoxes(fac.capacityBoxes, true)}")
                                        }
                                        if (subtitle.isNotBlank()) {
                                            Text(
                                                text = subtitle,
                                                fontSize = 10.sp,
                                                color = TextSecondary,
                                                fontFamily = VazirmatnFamily,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "فعال",
                                            tint = fac.composeColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    IconButton(
                                        onClick = { onEditFacility(fac) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (facilities.size > 1) {
                                        IconButton(
                                            onClick = { facilityToDelete = fac },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = Color.Red.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddNewFacility,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "افزودن سردخانه جدید",
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFamily
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بستن", fontFamily = VazirmatnFamily)
            }
        }
    )
}

/**
 * دیالوگ افزودن یا ویرایش اطلاعات سردخانه به همراه انتخاب رنگ لیبل
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditFacilityDialog(
    initialFacility: ColdStorageFacility? = null,
    onSave: (name: String, colorHex: String, managerName: String, capacity: Int, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialFacility?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialFacility?.labelColorHex ?: "#10B981") }
    var managerName by remember { mutableStateOf(initialFacility?.managerName ?: "") }
    var capacityStr by remember { mutableStateOf(if ((initialFacility?.capacityBoxes ?: 0) > 0) initialFacility?.capacityBoxes.toString() else "100000") }
    var phone by remember { mutableStateOf(initialFacility?.phone ?: "") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialFacility == null) "افزودن سردخانه جدید" else "ویرایش سردخانه",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = VazirmatnFamily
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // نام سردخانه (اجباری)
                Text(
                    text = "نام سردخانه *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    isError = nameError,
                    placeholder = { Text("مانند: سردخانه آریا ۱", fontSize = 12.sp, color = TextMuted, fontFamily = VazirmatnFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderColor
                    )
                )
                if (nameError) {
                    Text("لطفاً نام سردخانه را وارد کنید", color = Color.Red, fontSize = 10.sp, fontFamily = VazirmatnFamily)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // انتخاب رنگ لیبل سردخانه
                Text(
                    text = "رنگ برچسب سردخانه (جهت تفکیک بصری):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColdStorageFacility.PRESET_COLORS.forEach { preset ->
                        val isColorSelected = selectedColorHex.equals(preset.hex, ignoreCase = true)
                        val color = try {
                            Color(0xFF000000 or preset.hex.removePrefix("#").toLong(16))
                        } catch (_: Exception) {
                            EmeraldPrimary
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    2.dp,
                                    if (isColorSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = preset.hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isColorSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = preset.titleFa,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // نام مدیر یا مسئول
                Text(
                    text = "نام مدیر / مسئول (اختیاری):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    placeholder = { Text("مثال: مهندس حسینی", fontSize = 12.sp, color = TextMuted, fontFamily = VazirmatnFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderColor
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ظرفیت اسمی به جعبه
                Text(
                    text = "ظرفیت اسمی (تعداد جعبه):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    fontFamily = VazirmatnFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("مثال: ۱۰۰,۰۰۰", fontSize = 12.sp, color = TextMuted, fontFamily = VazirmatnFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = SurfaceVariant,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        val cap = capacityStr.filter { it.isDigit() }.toIntOrNull() ?: 100000
                        onSave(name.trim(), selectedColorHex, managerName.trim(), cap, phone.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ذخیره سردخانه", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondary, fontFamily = VazirmatnFamily)
            }
        }
    )
}
