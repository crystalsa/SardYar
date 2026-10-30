package ir.sardyar.coldstorage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.CargoCodeStockBalance
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryUiState(
    val records: List<ColdStorageEntry> = emptyList(),
    val filteredRecords: List<ColdStorageEntry> = emptyList(),
    val facilities: List<ColdStorageFacility> = emptyList(),
    val activeFacilityId: String? = "FAC-01",
    val searchQuery: String = "",
    val selectedTypeFilter: RecordType? = null,
    val selectedCategoryFilter: AppleCategory? = null,
    val totalCapacityKg: Double = 3000000.0 // 3,000 tons
) {
    val activeFacility: ColdStorageFacility?
        get() = facilities.firstOrNull { it.id == activeFacilityId }

    val totalEntryWeightKg: Double
        get() = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.netWeightKg }

    val totalExitWeightKg: Double
        get() = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.netWeightKg }

    val currentStockWeightKg: Double
        get() = (totalEntryWeightKg - totalExitWeightKg).coerceAtLeast(0.0)

    val occupancyPercentage: Float
        get() = if (totalCapacityKg > 0) ((currentStockWeightKg / totalCapacityKg) * 100).toFloat().coerceIn(0f, 100f) else 0f

    val totalEntryBoxes: Int
        get() = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }

    val totalExitBoxes: Int
        get() = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }

    val currentStockBoxes: Int
        get() = (totalEntryBoxes - totalExitBoxes).coerceAtLeast(0)

    val hasUnweighedRecords: Boolean
        get() = records.any { !it.hasWeightRecorded }

    val goldenPremiumStockKg: Double
        get() = getCategoryStock(AppleCategory.GOLDEN_PREMIUM)

    val redGradeOneStockKg: Double
        get() = getCategoryStock(AppleCategory.RED_GRADE_ONE)

    val exportGradeStockKg: Double
        get() = getCategoryStock(AppleCategory.EXPORT_GRADE)

    val fallenStockKg: Double
        get() = getCategoryStock(AppleCategory.FALLEN_OR_INDUSTRIAL)

    private fun getCategoryStock(cat: AppleCategory): Double {
        val entry = records.filter { it.operationType == RecordType.ENTRY && it.appleCategory == cat }.sumOf { it.netWeightKg }
        val exit = records.filter { it.operationType == RecordType.EXIT && it.appleCategory == cat }.sumOf { it.netWeightKg }
        return (entry - exit).coerceAtLeast(0.0)
    }

    val totalHandlingFeesCalculated: Double
        get() = records.sumOf { it.totalHandlingFee }

    val totalWorkersWagesPaid: Double
        get() = records.sumOf { it.totalWorkersPaid }

    val totalWorkersWagesRemaining: Double
        get() = records.sumOf { it.totalWorkersRemaining }

    val lotBalances: List<CargoCodeStockBalance>
        get() {
            val grouped = records.groupBy { it.cargoCode }
            return grouped.map { (code, entries) ->
                val totalIn = entries.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
                val totalOut = entries.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
                val firstEntry = entries.firstOrNull()
                val netIn = entries.filter { it.operationType == RecordType.ENTRY }.sumOf { it.netWeightKg }
                val netOut = entries.filter { it.operationType == RecordType.EXIT }.sumOf { it.netWeightKg }
                CargoCodeStockBalance(
                    cargoCode = code,
                    ownerName = firstEntry?.farmerName ?: "نامشخص",
                    appleName = firstEntry?.displayAppleName ?: "",
                    totalInBoxes = totalIn,
                    totalOutBoxes = totalOut,
                    remainingBoxes = (totalIn - totalOut).coerceAtLeast(0),
                    totalNetWeightKg = (netIn - netOut).coerceAtLeast(0.0)
                )
            }.sortedByDescending { it.remainingBoxes }
        }
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<RecordType?>(null)
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<AppleCategory?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        combine(
            repository.getRecords(),
            repository.getFacilities(),
            repository.getActiveFacilityId()
        ) { records, facilities, activeFacId ->
            Triple(records, facilities, activeFacId)
        },
        _searchQuery,
        _selectedTypeFilter,
        _selectedCategoryFilter
    ) { (allRecords, facilities, activeFacId), query, typeFilter, catFilter ->
        val facilityRecords = if (activeFacId == null || activeFacId == "ALL") {
            allRecords
        } else {
            allRecords.filter { it.facilityId == activeFacId }
        }

        val filtered = facilityRecords.filter { r ->
            if (typeFilter != null && r.operationType != typeFilter) return@filter false
            if (catFilter != null && r.appleCategory != catFilter) return@filter false
            if (query.isNotBlank()) {
                val q = query.lowercase()
                val match = r.cargoCode.lowercase().contains(q) ||
                        r.farmerName.lowercase().contains(q) ||
                        r.farmerPhone.contains(q) ||
                        r.driverName.lowercase().contains(q) ||
                        r.plateNumber.contains(q) ||
                        r.displayAppleName.lowercase().contains(q) ||
                        r.workers.any { it.name.lowercase().contains(q) }
                if (!match) return@filter false
            }
            true
        }

        val activeFac = facilities.firstOrNull { it.id == activeFacId }
        val capacityKg = if (activeFac != null && activeFac.capacityBoxes > 0) {
            activeFac.capacityBoxes * 18.0
        } else {
            3000000.0
        }

        InventoryUiState(
            records = facilityRecords,
            filteredRecords = filtered,
            facilities = facilities,
            activeFacilityId = activeFacId,
            searchQuery = query,
            selectedTypeFilter = typeFilter,
            selectedCategoryFilter = catFilter,
            totalCapacityKg = capacityKg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: RecordType?) {
        _selectedTypeFilter.value = type
    }

    fun setCategoryFilter(category: AppleCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setActiveFacility(facilityId: String?) {
        viewModelScope.launch {
            repository.setActiveFacility(facilityId)
        }
    }

    fun addFacility(
        name: String,
        labelColorHex: String = "#10B981",
        managerName: String = "",
        capacityBoxes: Int = 100000,
        phone: String = ""
    ) {
        val newFacility = ColdStorageFacility(
            id = "FAC-${System.currentTimeMillis().toString().takeLast(4)}",
            name = name.trim(),
            labelColorHex = labelColorHex,
            managerName = managerName.trim(),
            capacityBoxes = capacityBoxes,
            phone = phone.trim()
        )
        viewModelScope.launch {
            repository.addFacility(newFacility)
            repository.setActiveFacility(newFacility.id)
        }
    }

    fun updateFacility(facility: ColdStorageFacility) {
        viewModelScope.launch {
            repository.updateFacility(facility)
        }
    }

    fun deleteFacility(facilityId: String) {
        viewModelScope.launch {
            repository.deleteFacility(facilityId)
        }
    }

    fun addRecord(record: ColdStorageEntry) {
        viewModelScope.launch {
            repository.addRecord(record)
        }
    }

    fun updateRecord(record: ColdStorageEntry) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun restoreRecords(records: List<ColdStorageEntry>) {
        viewModelScope.launch {
            repository.restoreRecords(records)
        }
    }
}
