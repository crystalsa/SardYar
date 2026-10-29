package ir.sardyar.coldstorage.domain.repository

import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getRecords(): Flow<List<ColdStorageEntry>>
    suspend fun addRecord(record: ColdStorageEntry)
    suspend fun updateRecord(record: ColdStorageEntry)
    suspend fun deleteRecord(id: String)
    suspend fun restoreRecords(records: List<ColdStorageEntry>)

    // مدیریت چند سردخانه
    fun getFacilities(): Flow<List<ColdStorageFacility>>
    fun getActiveFacilityId(): Flow<String?>
    suspend fun setActiveFacility(facilityId: String?)
    suspend fun addFacility(facility: ColdStorageFacility)
    suspend fun updateFacility(facility: ColdStorageFacility)
    suspend fun deleteFacility(facilityId: String)
}


