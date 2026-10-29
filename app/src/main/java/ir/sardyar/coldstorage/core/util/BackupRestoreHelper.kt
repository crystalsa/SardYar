package ir.sardyar.coldstorage.core.util

import android.content.Context
import android.net.Uri
import ir.sardyar.coldstorage.domain.model.AppleCategory
import ir.sardyar.coldstorage.domain.model.BoxType
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.FeeCalculationType
import ir.sardyar.coldstorage.domain.model.RecordType
import ir.sardyar.coldstorage.domain.model.VehicleType
import ir.sardyar.coldstorage.domain.model.WorkerEntry
import ir.sardyar.coldstorage.domain.model.WorkerRole
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object BackupRestoreHelper {

    /**
     * تولید متن استاندارد JSON از کل دیتابیس
     */
    fun generateBackupJsonString(records: List<ColdStorageEntry>): String {
        val root = JSONObject()
        root.put("app", "SardYar")
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())
        val today = JalaliDateHelper.getTodayJalali()
        root.put("backupDateShamsi", JalaliDateHelper.formatShort(today, toPersianDigits = false))
        root.put("recordCount", records.size)

        val recordsArray = JSONArray()
        records.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("cargoCode", r.cargoCode)
            obj.put("facilityId", r.facilityId)
            obj.put("operationType", r.operationType.name)
            obj.put("farmerName", r.farmerName)
            obj.put("farmerPhone", r.farmerPhone)
            obj.put("createdAt", r.createdAt)
            obj.put("dateShamsi", r.dateShamsi)
            obj.put("dateWeekday", r.dateWeekday)
            obj.put("timeStr", r.timeStr)
            obj.put("appleCategory", r.appleCategory.name)
            obj.put("customAppleName", r.customAppleName)
            obj.put("isGradeTwo", r.isGradeTwo)
            obj.put("boxType", r.boxType.name)
            obj.put("boxCount", r.boxCount)
            obj.put("estimatedTonnage", r.estimatedTonnage)
            obj.put("grossWeightKg", r.grossWeightKg)
            obj.put("tareWeightKg", r.tareWeightKg)
            obj.put("netWeightKg", r.netWeightKg)
            obj.put("vehicleType", r.vehicleType.name)
            obj.put("plateNumber", r.plateNumber)
            obj.put("driverName", r.driverName)
            obj.put("feeCalculationType", r.feeCalculationType.name)
            obj.put("unitRate", r.unitRate)
            obj.put("notes", r.notes)

            val workersArray = JSONArray()
            r.workers.forEach { w ->
                val wObj = JSONObject()
                wObj.put("id", w.id)
                wObj.put("name", w.name)
                wObj.put("role", w.role.name)
                wObj.put("agreedWage", w.agreedWage)
                wObj.put("paidAmount", w.paidAmount)
                wObj.put("isSettled", w.isSettled)
                wObj.put("settlementDate", w.settlementDate ?: "")
                workersArray.put(wObj)
            }
            obj.put("workers", workersArray)
            recordsArray.put(obj)
        }

        root.put("records", recordsArray)
        return root.toString(2)
    }

    /**
     * نام پیش‌فرض فایل پشتیبان بر اساس تاریخ امروز شمسی
     */
    fun getDefaultBackupFileName(): String {
        val today = JalaliDateHelper.getTodayJalali()
        val m = today.month.toString().padStart(2, '0')
        val d = today.day.toString().padStart(2, '0')
        return "sardyar_backup_${today.year}_${m}_${d}.json"
    }

    /**
     * نوشتن مستقیم پشتیبان در Uri انتخاب‌شده توسط کاربر (Storage Access Framework)
     */
    fun writeBackupToUri(context: Context, uri: Uri, records: List<ColdStorageEntry>): Boolean {
        return try {
            val jsonString = generateBackupJsonString(records)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * پشتیبان‌گیری از کل دیتابیس به صورت یک فایل موقت استاندارد JSON (جهت اشتراک‌گذاری)
     */
    fun exportBackupJson(context: Context, records: List<ColdStorageEntry>): File {
        val jsonString = generateBackupJsonString(records)
        val today = JalaliDateHelper.getTodayJalali()
        val fileName = "sardyar_backup_${today.year}_${today.month}_${today.day}_${System.currentTimeMillis().toString().takeLast(4)}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonString, Charsets.UTF_8)
        return file
    }

    /**
     * تجزیه و بارگذاری فایل پشتیبان JSON
     */
    fun parseBackupJson(jsonString: String): List<ColdStorageEntry>? {
        return try {
            val root = JSONObject(jsonString)
            val recordsArray = root.getJSONArray("records")
            val list = mutableListOf<ColdStorageEntry>()

            for (i in 0 until recordsArray.length()) {
                val obj = recordsArray.getJSONObject(i)

                val workersList = mutableListOf<WorkerEntry>()
                if (obj.has("workers")) {
                    val wArray = obj.getJSONArray("workers")
                    for (j in 0 until wArray.length()) {
                        val wObj = wArray.getJSONObject(j)
                        workersList.add(
                            WorkerEntry(
                                id = wObj.optString("id", "w-$j"),
                                name = wObj.optString("name", ""),
                                role = try {
                                    WorkerRole.valueOf(wObj.optString("role", WorkerRole.UNLOADER.name))
                                } catch (_: Exception) {
                                    WorkerRole.UNLOADER
                                },
                                agreedWage = wObj.optDouble("agreedWage", 0.0),
                                paidAmount = wObj.optDouble("paidAmount", 0.0),
                                isSettled = wObj.optBoolean("isSettled", false),
                                settlementDate = if (wObj.has("settlementDate") && wObj.getString("settlementDate").isNotBlank()) wObj.getString("settlementDate") else null
                            )
                        )
                    }
                }

                val entry = ColdStorageEntry(
                    id = obj.getString("id"),
                    cargoCode = obj.optString("cargoCode", obj.optString("roomPartCode", obj.optString("lotCode", "1.1"))),
                    facilityId = obj.optString("facilityId", "FAC-01"),
                    operationType = try {
                        RecordType.valueOf(obj.getString("operationType"))
                    } catch (_: Exception) {
                        RecordType.ENTRY
                    },
                    farmerName = obj.optString("farmerName", ""),
                    farmerPhone = obj.optString("farmerPhone", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    dateShamsi = obj.optString("dateShamsi", ""),
                    dateWeekday = obj.optString("dateWeekday", ""),
                    timeStr = obj.optString("timeStr", "۱۲:۰۰"),
                    appleCategory = try {
                        AppleCategory.valueOf(obj.optString("appleCategory", AppleCategory.GOLDEN_PREMIUM.name))
                    } catch (_: Exception) {
                        AppleCategory.GOLDEN_PREMIUM
                    },
                    customAppleName = obj.optString("customAppleName", ""),
                    isGradeTwo = obj.optBoolean("isGradeTwo", false),
                    boxType = try {
                        BoxType.valueOf(obj.optString("boxType", BoxType.TWO_ROW.name))
                    } catch (_: Exception) {
                        BoxType.TWO_ROW
                    },
                    boxCount = obj.optInt("boxCount", 0),
                    estimatedTonnage = obj.optDouble("estimatedTonnage", 0.0),
                    grossWeightKg = obj.optDouble("grossWeightKg", 0.0),
                    tareWeightKg = obj.optDouble("tareWeightKg", 0.0),
                    netWeightKg = obj.optDouble("netWeightKg", 0.0),
                    vehicleType = try {
                        VehicleType.valueOf(obj.optString("vehicleType", VehicleType.NISSAN.name))
                    } catch (_: Exception) {
                        VehicleType.NISSAN
                    },
                    plateNumber = obj.optString("plateNumber", ""),
                    driverName = obj.optString("driverName", ""),
                    feeCalculationType = try {
                        FeeCalculationType.valueOf(obj.optString("feeCalculationType", FeeCalculationType.PER_BOX.name))
                    } catch (_: Exception) {
                        FeeCalculationType.PER_BOX
                    },
                    unitRate = obj.optDouble("unitRate", 0.0),
                    workers = workersList,
                    notes = obj.optString("notes", "")
                )
                list.add(entry)
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    /**
     * خواندن متن فایل از طریق Content URI (مثلا پس از انتخاب فایل توسط کاربر)
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
