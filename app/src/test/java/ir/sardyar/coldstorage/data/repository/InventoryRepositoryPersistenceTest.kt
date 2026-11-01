package ir.sardyar.coldstorage.data.repository

import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.BoxType
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.ColdStorageFacility
import ir.sardyar.coldstorage.domain.model.RecordType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InventoryRepositoryPersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testPersistenceAcrossAppRestarts() = runBlocking {
        val storageDir = tempFolder.newFolder("sardyar_storage")

        // مرحله ۱: راه‌اندازی اول برنامه (بار اول)
        val repo1 = InventoryRepositoryImpl(context = null, baseDir = storageDir)
        val initialFacilities = repo1.getFacilities().first()
        assertTrue("باید سردخانه‌های اولیه وجود داشته باشند", initialFacilities.isNotEmpty())

        // مرحله ۲: افزودن سردخانه جدید توسط کاربر
        val customFacility = ColdStorageFacility(
            id = "FAC-TEST-99",
            name = "سردخانه نوین ارومیه",
            labelColorHex = "#6366F1",
            managerName = "کریمی",
            capacityBoxes = 95000,
            phone = "09141234567"
        )
        repo1.addFacility(customFacility)
        repo1.setActiveFacility(customFacility.id)

        // افزودن بار جدید در این سردخانه
        val customRecord = ColdStorageEntry(
            id = "CS-TEST-999",
            cargoCode = "5.1",
            facilityId = customFacility.id,
            operationType = RecordType.ENTRY,
            farmerName = "باغدار نمونه",
            dateShamsi = "1403/08/10",
            dateWeekday = "پنج‌شنبه",
            timeStr = "14:30",
            appleCategory = AppleCategory.GOLDEN_PREMIUM,
            boxType = BoxType.TWO_ROW,
            boxCount = 350
        )
        repo1.addRecord(customRecord)

        // مرحله ۳: شبیه‌سازی بستن برنامه و راه‌اندازی مجدد (Instance 2 با همان پوشه)
        val repo2 = InventoryRepositoryImpl(context = null, baseDir = storageDir)

        val loadedFacilities = repo2.getFacilities().first()
        val foundFacility = loadedFacilities.firstOrNull { it.id == customFacility.id }
        assertNotNull("سردخانه اضافه شده باید پس از راه‌اندازی مجدد وجود داشته باشد", foundFacility)
        assertEquals("سردخانه نوین ارومیه", foundFacility?.name)
        assertEquals(customFacility.id, repo2.getActiveFacilityId().first())

        val loadedRecords = repo2.getRecords().first()
        val foundRecord = loadedRecords.firstOrNull { it.id == customRecord.id }
        assertNotNull("رکورد اضافه شده باید پس از راه‌اندازی مجدد وجود داشته باشد", foundRecord)
        assertEquals(350, foundRecord?.boxCount)
        assertEquals("5.1", foundRecord?.cargoCode)
    }
}
