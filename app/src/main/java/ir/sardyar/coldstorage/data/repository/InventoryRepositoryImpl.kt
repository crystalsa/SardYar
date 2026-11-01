package ir.sardyar.coldstorage.data.repository

import android.content.Context
import ir.sardyar.coldstorage.core.util.BackupRestoreHelper
import ir.sardyar.coldstorage.core.util.JalaliDateHelper
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.BoxType
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import ir.sardyar.coldstorage.domain.model.FeeCalculationType
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.domain.model.VehicleType
import ir.sardyar.coldstorage.domain.model.WorkerEntry
import ir.sardyar.coldstorage.domain.model.WorkerRole
import ir.sardyar.coldstorage.domain.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class InventoryRepositoryImpl(
    private val context: Context? = null,
    private val baseDir: File? = context?.filesDir
) : InventoryRepository {

    private val _entries = MutableStateFlow<List<ColdStorageEntry>>(emptyList())
    private val _facilities = MutableStateFlow<List<ColdStorageFacility>>(emptyList())
    private val _activeFacilityId = MutableStateFlow<String?>("FAC-01")

    private val facilitiesFile: File?
        get() = baseDir?.let { File(it, "sardyar_facilities.json") }

    private val recordsFile: File?
        get() = baseDir?.let { File(it, "sardyar_records.json") }

    private val prefsFile: File?
        get() = baseDir?.let { File(it, "sardyar_prefs.json") }

    private val fileLock = Any()

    init {
        loadFromDiskOrSeed()
    }

    private fun loadFromDiskOrSeed() {
        val facFile = facilitiesFile
        val recFile = recordsFile
        val prfFile = prefsFile

        // ۱. بارگذاری یا ایجاد اولیه سردخانه‌ها
        if (facFile != null && facFile.exists()) {
            try {
                val content = facFile.readText(Charsets.UTF_8).trim()
                val parsed = BackupRestoreHelper.parseFacilitiesJson(content)
                if (parsed.isNotEmpty()) {
                    _facilities.value = parsed
                } else {
                    _facilities.value = getInitialFacilities()
                    saveFacilitiesSync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _facilities.value = getInitialFacilities()
                saveFacilitiesSync()
            }
        } else {
            _facilities.value = getInitialFacilities()
            saveFacilitiesSync()
        }

        // ۲. بارگذاری یا ایجاد اولیه رکوردها
        if (recFile != null && recFile.exists()) {
            try {
                val content = recFile.readText(Charsets.UTF_8).trim()
                val parsed = BackupRestoreHelper.parseBackupJson(content)
                _entries.value = parsed ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _entries.value = getInitialData()
                saveRecordsSync()
            }
        } else {
            _entries.value = getInitialData()
            saveRecordsSync()
        }

        // ۳. بارگذاری شناسه سردخانه فعال
        if (prfFile != null && prfFile.exists()) {
            try {
                val savedId = prfFile.readText(Charsets.UTF_8).trim()
                if (savedId == "ALL" || _facilities.value.any { it.id == savedId }) {
                    _activeFacilityId.value = savedId
                } else {
                    _activeFacilityId.value = _facilities.value.firstOrNull()?.id ?: "FAC-01"
                    saveActiveFacilitySync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _activeFacilityId.value = _facilities.value.firstOrNull()?.id ?: "FAC-01"
            }
        } else {
            _activeFacilityId.value = _facilities.value.firstOrNull()?.id ?: "FAC-01"
            saveActiveFacilitySync()
        }
    }

    private fun writeAtomic(targetFile: File, content: String) {
        synchronized(fileLock) {
            try {
                val parent = targetFile.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                val tempFile = File(parent, "${targetFile.name}.tmp")
                tempFile.writeText(content, Charsets.UTF_8)
                if (tempFile.exists()) {
                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    tempFile.renameTo(targetFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveFacilitiesSync() {
        facilitiesFile?.let { file ->
            writeAtomic(file, BackupRestoreHelper.facilitiesToJson(_facilities.value))
        }
    }

    private fun saveRecordsSync() {
        recordsFile?.let { file ->
            writeAtomic(file, BackupRestoreHelper.generateBackupJsonString(_entries.value, _facilities.value))
        }
    }

    private fun saveActiveFacilitySync() {
        prefsFile?.let { file ->
            writeAtomic(file, _activeFacilityId.value ?: "")
        }
    }

    override fun getRecords(): Flow<List<ColdStorageEntry>> = _entries.asStateFlow()

    override suspend fun addRecord(record: ColdStorageEntry) {
        val current = _entries.value.toMutableList()
        current.add(0, record)
        _entries.value = current
        withContext(Dispatchers.IO) {
            saveRecordsSync()
        }
    }

    override suspend fun updateRecord(record: ColdStorageEntry) {
        val current = _entries.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index != -1) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        _entries.value = current
        withContext(Dispatchers.IO) {
            saveRecordsSync()
        }
    }

    override suspend fun deleteRecord(id: String) {
        val current = _entries.value.toMutableList()
        current.removeAll { it.id == id }
        _entries.value = current
        withContext(Dispatchers.IO) {
            saveRecordsSync()
        }
    }

    override suspend fun restoreRecords(records: List<ColdStorageEntry>) {
        _entries.value = records
        withContext(Dispatchers.IO) {
            saveRecordsSync()
        }
    }

    override fun getFacilities(): Flow<List<ColdStorageFacility>> = _facilities.asStateFlow()

    override fun getActiveFacilityId(): Flow<String?> = _activeFacilityId.asStateFlow()

    override suspend fun setActiveFacility(facilityId: String?) {
        _activeFacilityId.value = facilityId
        withContext(Dispatchers.IO) {
            saveActiveFacilitySync()
        }
    }

    override suspend fun addFacility(facility: ColdStorageFacility) {
        val current = _facilities.value.toMutableList()
        current.add(facility)
        _facilities.value = current
        if (_activeFacilityId.value == null) {
            _activeFacilityId.value = facility.id
        }
        withContext(Dispatchers.IO) {
            saveFacilitiesSync()
            saveActiveFacilitySync()
        }
    }

    override suspend fun updateFacility(facility: ColdStorageFacility) {
        val current = _facilities.value.toMutableList()
        val index = current.indexOfFirst { it.id == facility.id }
        if (index != -1) {
            current[index] = facility
            _facilities.value = current
            withContext(Dispatchers.IO) {
                saveFacilitiesSync()
            }
        }
    }

    override suspend fun deleteFacility(facilityId: String) {
        val current = _facilities.value.toMutableList()
        current.removeAll { it.id == facilityId }
        _facilities.value = current

        // پاک کردن رکوردهای متعلق به سردخانه حذف شده
        val currentEntries = _entries.value.toMutableList()
        val entriesChanged = currentEntries.removeAll { it.facilityId == facilityId }
        if (entriesChanged) {
            _entries.value = currentEntries
        }

        if (_activeFacilityId.value == facilityId) {
            _activeFacilityId.value = current.firstOrNull()?.id
        }
        withContext(Dispatchers.IO) {
            saveFacilitiesSync()
            if (entriesChanged) {
                saveRecordsSync()
            }
            saveActiveFacilitySync()
        }
    }

    override suspend fun restoreFacilities(facilities: List<ColdStorageFacility>) {
        _facilities.value = facilities
        withContext(Dispatchers.IO) {
            saveFacilitiesSync()
        }
    }

    private fun getInitialFacilities(): List<ColdStorageFacility> {
        return listOf(
            ColdStorageFacility(
                id = "FAC-01",
                name = "سردخانه مرکزی بهارستان",
                labelColorHex = "#10B981",
                managerName = "مهندس اکبری",
                capacityBoxes = 120000,
                phone = "09121112233"
            ),
            ColdStorageFacility(
                id = "FAC-02",
                name = "سردخانه شماره ۲ دماوند",
                labelColorHex = "#3B82F6",
                managerName = "حاج رضایی",
                capacityBoxes = 85000,
                phone = "09124445566"
            )
        )
    }

    private fun getInitialData(): List<ColdStorageEntry> {
        val today = JalaliDateHelper.getTodayJalali()
        val todayShamsi = JalaliDateHelper.formatShort(today, toPersianDigits = false)
        val todayDay = today.dayOfWeek

        return listOf(
            ColdStorageEntry(
                id = "CS-201",
                cargoCode = "3.8",
                facilityId = "FAC-01",
                operationType = RecordType.ENTRY,
                farmerName = "مهندس رستمی (کشت و صنعت مراغه)",
                farmerPhone = "09149876543",
                createdAt = System.currentTimeMillis() - 5 * 3600 * 1000,
                dateShamsi = todayShamsi,
                dateWeekday = todayDay,
                timeStr = "۰۸:۳۰",
                appleCategory = AppleCategory.YELLOW,
                isGradeTwo = false,
                boxType = BoxType.TWO_ROW,
                boxCount = 420,
                estimatedTonnage = 0.0,
                grossWeightKg = 0.0,
                tareWeightKg = 0.0,
                netWeightKg = 0.0,
                vehicleType = VehicleType.NISSAN,
                plateNumber = "۴۳-ج-۵۱۲ ایران ۷۱",
                driverName = "رسول پناهی",
                feeCalculationType = FeeCalculationType.PER_BOX,
                unitRate = 3500.0,
                workers = listOf(
                    WorkerEntry(
                        id = "W-01",
                        name = "احمد مرادی",
                        role = WorkerRole.UNLOADER,
                        agreedWage = 735000.0,
                        paidAmount = 735000.0
                    ),
                    WorkerEntry(
                        id = "W-02",
                        name = "بهزاد کریمی",
                        role = WorkerRole.STACKER,
                        agreedWage = 735000.0,
                        paidAmount = 500000.0
                    )
                ),
                notes = "سیب زرد صادراتی ممتاز، کد بار مرجع: ۳.۸ (اتاق ۳ پارت ۸)"
            ),
            ColdStorageEntry(
                id = "CS-202",
                cargoCode = "3.9",
                facilityId = "FAC-01",
                operationType = RecordType.ENTRY,
                farmerName = "مهندس رستمی (کشت و صنعت مراغه)",
                farmerPhone = "09149876543",
                createdAt = System.currentTimeMillis() - 3 * 3600 * 1000,
                dateShamsi = todayShamsi,
                dateWeekday = todayDay,
                timeStr = "۱۰:۴۵",
                appleCategory = AppleCategory.RED,
                isGradeTwo = false,
                boxType = BoxType.THREE_ROW,
                boxCount = 750,
                estimatedTonnage = 0.0,
                grossWeightKg = 0.0,
                tareWeightKg = 0.0,
                netWeightKg = 0.0,
                vehicleType = VehicleType.KHAVAR,
                plateNumber = "۲۱-ع-۸۹۰ ایران ۱۵",
                driverName = "محسن بهرامی",
                feeCalculationType = FeeCalculationType.PER_VEHICLE,
                unitRate = 2500000.0,
                workers = listOf(
                    WorkerEntry(
                        id = "W-03",
                        name = "قادر اکبری",
                        role = WorkerRole.UNLOADER,
                        agreedWage = 1250000.0,
                        paidAmount = 1250000.0
                    ),
                    WorkerEntry(
                        id = "W-04",
                        name = "حسین سلیمانی",
                        role = WorkerRole.FORKLIFT_OPERATOR,
                        agreedWage = 1250000.0,
                        paidAmount = 1250000.0
                    )
                ),
                notes = "سیب قرمز درجه یک استخوانی، کد بار مرجع: ۳.۹ (اتاق ۳ پارت ۹)"
            ),
            ColdStorageEntry(
                id = "CS-203",
                cargoCode = "1.4",
                facilityId = "FAC-01",
                operationType = RecordType.ENTRY,
                farmerName = "حاج اسدالله نوری (باغ سمیرم)",
                farmerPhone = "09131234567",
                createdAt = System.currentTimeMillis() - 2 * 3600 * 1000,
                dateShamsi = todayShamsi,
                dateWeekday = todayDay,
                timeStr = "۱۱:۳۰",
                appleCategory = AppleCategory.RED,
                isGradeTwo = false,
                boxType = BoxType.SINGLE_ROW,
                boxCount = 500,
                estimatedTonnage = 0.0,
                grossWeightKg = 0.0,
                tareWeightKg = 0.0,
                netWeightKg = 0.0,
                vehicleType = VehicleType.NISSAN,
                plateNumber = "۵۵-ب-۳۲۱ ایران ۲۳",
                driverName = "علی صابری",
                feeCalculationType = FeeCalculationType.PER_BOX,
                unitRate = 3500.0,
                workers = listOf(
                    WorkerEntry(
                        id = "W-05",
                        name = "احمد مرادی",
                        role = WorkerRole.UNLOADER,
                        agreedWage = 875000.0,
                        paidAmount = 875000.0
                    )
                ),
                notes = "سیب صادراتی سوپر لوکس، کد بار مرجع: ۱.۴ (اتاق ۱ پارت ۴)"
            ),
            ColdStorageEntry(
                id = "CS-204",
                cargoCode = "3.8",
                facilityId = "FAC-01",
                operationType = RecordType.EXIT,
                farmerName = "مهندس رستمی (کشت و صنعت مراغه)",
                farmerPhone = "09149876543",
                createdAt = System.currentTimeMillis() - 1 * 3600 * 1000,
                dateShamsi = todayShamsi,
                dateWeekday = todayDay,
                timeStr = "۱۲:۱۵",
                appleCategory = AppleCategory.YELLOW,
                isGradeTwo = false,
                boxType = BoxType.TWO_ROW,
                boxCount = 120,
                estimatedTonnage = 0.0,
                grossWeightKg = 0.0,
                tareWeightKg = 0.0,
                netWeightKg = 0.0,
                vehicleType = VehicleType.NISSAN,
                plateNumber = "۷۸-ج-۹۸۷ ایران ۱۱",
                driverName = "رضا کاظمی",
                feeCalculationType = FeeCalculationType.PER_BOX,
                unitRate = 3000.0,
                workers = listOf(
                    WorkerEntry(
                        id = "W-06",
                        name = "بهزاد کریمی",
                        role = WorkerRole.LOADER,
                        agreedWage = 360000.0,
                        paidAmount = 360000.0
                    )
                ),
                notes = "خروج ۱۲۰ جعبه از کد ۳.۸ جهت سورتینگ (مانده موجودی کد ۳.۸: ۳۰۰ جعبه)"
            )
        )
    }
}
