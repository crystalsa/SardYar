package ir.sardyar.coldstorage.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.sardyar.coldstorage.core.theme.EmeraldContainer
import ir.sardyar.coldstorage.core.theme.EmeraldPrimary
import ir.sardyar.coldstorage.core.theme.Surface
import ir.sardyar.coldstorage.core.theme.TextMuted
import ir.sardyar.coldstorage.core.theme.VazirmatnFamily
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.presentation.InventoryViewModel
import ir.sardyar.coldstorage.presentation.components.AddOrEditFacilityDialog
import ir.sardyar.coldstorage.presentation.components.FacilityManagementDialog

@Composable
fun MainScreen(viewModel: InventoryViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isNewRecordOpen by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<ColdStorageEntry?>(null) }
    var newRecordInitialType by remember { mutableStateOf(RecordType.ENTRY) }

    // وضعیت مدیریت چند سردخانه
    var isFacilityManagerOpen by remember { mutableStateOf(false) }
    var isAddOrEditFacilityOpen by remember { mutableStateOf(false) }
    var editingFacility by remember { mutableStateOf<ColdStorageFacility?>(null) }

    // اگر هیچ سردخانه‌ای ثبت نشده باشد، دیالوگ ثبت اولیه به صورت خودکار باز می‌شود
    val shouldShowInitialDialog = state.facilities.isEmpty()

    // مدیریت دکمه عقب گوشی: فقط در ۴ تب اصلی خارج می‌شود؛ در صفحات فرعی مثل ثبت بار برمی‌گردد
    BackHandler(enabled = isNewRecordOpen) {
        editingRecord = null
        isNewRecordOpen = false
    }

    BackHandler(enabled = isFacilityManagerOpen) {
        isFacilityManagerOpen = false
    }

    BackHandler(enabled = isAddOrEditFacilityOpen && !shouldShowInitialDialog) {
        editingFacility = null
        isAddOrEditFacilityOpen = false
    }

    if (isFacilityManagerOpen) {
        FacilityManagementDialog(
            facilities = state.facilities,
            activeFacilityId = state.activeFacilityId,
            onSelectFacility = { facId ->
                viewModel.setActiveFacility(facId)
            },
            onAddNewFacility = {
                editingFacility = null
                isFacilityManagerOpen = false
                isAddOrEditFacilityOpen = true
            },
            onEditFacility = { fac ->
                editingFacility = fac
                isFacilityManagerOpen = false
                isAddOrEditFacilityOpen = true
            },
            onDeleteFacility = { facId ->
                viewModel.deleteFacility(facId)
            },
            onDismiss = { isFacilityManagerOpen = false }
        )
    }

    if (isAddOrEditFacilityOpen || shouldShowInitialDialog) {
        AddOrEditFacilityDialog(
            initialFacility = editingFacility,
            onSave = { name, colorHex, managerName, capacity, phone ->
                if (editingFacility != null) {
                    viewModel.updateFacility(
                        editingFacility!!.copy(
                            name = name,
                            labelColorHex = colorHex,
                            managerName = managerName,
                            capacityBoxes = capacity,
                            phone = phone
                        )
                    )
                } else {
                    viewModel.addFacility(
                        name = name,
                        labelColorHex = colorHex,
                        managerName = managerName,
                        capacityBoxes = capacity,
                        phone = phone
                    )
                }
                editingFacility = null
                isAddOrEditFacilityOpen = false
            },
            onDismiss = {
                // اگر هیچ سردخانه‌ای وجود ندارد کاربر باید حداقل یکی بسازد
                if (state.facilities.isNotEmpty()) {
                    editingFacility = null
                    isAddOrEditFacilityOpen = false
                }
            }
        )
    }

    AnimatedContent(
        targetState = isNewRecordOpen,
        transitionSpec = {
            (fadeIn() + slideInHorizontally { width -> if (targetState) width else -width })
                .togetherWith(fadeOut() + slideOutHorizontally { width -> if (targetState) -width else width })
        },
        label = "NewRecordScreenTransition"
    ) { isOpen ->
        if (isOpen) {
            key(editingRecord?.id ?: "NEW_ENTRY") {
                NewRecordScreen(
                    initialType = newRecordInitialType,
                    editingRecord = editingRecord,
                    facilities = state.facilities,
                    defaultFacilityId = state.activeFacilityId,
                    lotBalances = state.lotBalances,
                    allRecords = state.records,
                    onSave = { record ->
                        if (editingRecord != null) {
                            viewModel.updateRecord(record)
                        } else {
                            viewModel.addRecord(record)
                        }
                        editingRecord = null
                        isNewRecordOpen = false
                    },
                    onBack = {
                        editingRecord = null
                        isNewRecordOpen = false
                    }
                )
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Surface,
                        tonalElevation = 6.dp
                    ) {
                        val tabs = listOf(
                            Triple(0, "داشبورد", Icons.Default.Dashboard),
                            Triple(1, "وضعیت روز", Icons.Default.CalendarMonth),
                            Triple(2, "ثبت بار", Icons.Default.SwapHoriz),
                            Triple(3, "گزارشات", Icons.Default.Description)
                        )
                        tabs.forEach { (index, title, icon) ->
                            val isSelected = selectedTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                icon = { Icon(icon, contentDescription = title) },
                                label = {
                                    Text(
                                        title,
                                        fontFamily = VazirmatnFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldPrimary,
                                    selectedTextColor = EmeraldPrimary,
                                    indicatorColor = EmeraldContainer,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { width -> if (targetState > initialState) width / 4 else -width / 4 })
                            .togetherWith(fadeOut() + slideOutHorizontally { width -> if (targetState > initialState) -width / 4 else width / 4 })
                    },
                    label = "TabContentTransition"
                ) { currentTab ->
                    when (currentTab) {
                        0 -> DashboardScreen(
                            state = state,
                            onNavigateToNewRecord = { type ->
                                newRecordInitialType = type
                                editingRecord = null
                                isNewRecordOpen = true
                            },
                            onNavigateToRecords = { selectedTab = 2 },
                            onOpenFacilityManager = { isFacilityManagerOpen = true },
                            modifier = Modifier.padding(padding)
                        )
                        1 -> DailyStatusScreen(
                            state = state,
                            onNavigateToNewRecord = { type ->
                                newRecordInitialType = type
                                editingRecord = null
                                isNewRecordOpen = true
                            },
                            onEditRecord = { record ->
                                editingRecord = record
                                newRecordInitialType = record.operationType
                                isNewRecordOpen = true
                            },
                            onDeleteRecord = { id ->
                                viewModel.deleteRecord(id)
                            },
                            onOpenFacilityManager = { isFacilityManagerOpen = true },
                            modifier = Modifier.padding(padding)
                        )
                        2 -> InventoryRecordsScreen(
                            state = state,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onTypeFilterChange = { viewModel.setTypeFilter(it) },
                            onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                            onDeleteRecord = { viewModel.deleteRecord(it) },
                            onEditRecord = { record ->
                                editingRecord = record
                                newRecordInitialType = record.operationType
                                isNewRecordOpen = true
                            },
                            onNavigateToNewRecord = {
                                newRecordInitialType = RecordType.ENTRY
                                editingRecord = null
                                isNewRecordOpen = true
                            },
                            modifier = Modifier.padding(padding)
                        )
                        3 -> ReportsScreen(
                            state = state,
                            onRestoreRecords = { records ->
                                viewModel.restoreRecords(records)
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}
