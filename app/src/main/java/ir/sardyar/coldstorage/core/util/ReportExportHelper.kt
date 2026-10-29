package ir.sardyar.coldstorage.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import ir.sardyar.coldstorage.R
import ir.sardyar.coldstorage.domain.model.ColdStorageEntry
import ir.sardyar.coldstorage.domain.model.RecordType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportHelper {

    /**
     * استخراج فایل اکسل (CSV استاندارد UTF-8 با امضای BOM جهت باز شدن بی‌نقص در اکسل ویندوز و موبایل)
     */
    fun exportToExcelCsv(
        context: Context,
        records: List<ColdStorageEntry>,
        reportTitle: String,
        dateRangeStr: String
    ): File {
        val today = JalaliDateHelper.getTodayJalali()
        val todayStr = "${today.year}_${today.month.toString().padStart(2, '0')}_${today.day.toString().padStart(2, '0')}"
        val fileName = "SardYar_Report_${todayStr}_${System.currentTimeMillis().toString().takeLast(4)}.csv"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        // افزودن UTF-8 Byte Order Mark (BOM) برای شناسایی زبان فارسی در مایکروسافت اکسل
        sb.append('\uFEFF')

        // عنوان گزارش و بازه زمانی در بالای فایل
        sb.append("\"سامانه مدیریت سردخانه سیب سردیار\"\n")
        sb.append("\"عنوان گزارش:\",\"$reportTitle\"\n")
        sb.append("\"بازه زمانی گزارش:\",\"$dateRangeStr\"\n")
        sb.append("\"تاریخ استخراج:\",\"${JalaliDateHelper.formatFullWithDay(today, toPersianDigits = false)}\"\n\n")

        // سرستون‌های جدول اکسل
        val headers = listOf(
            "ردیف",
            "شماره قبض",
            "نوع عملیات",
            "تاریخ شمسی",
            "ساعت",
            "نام باغدار / صاحب بار",
            "شماره تماس",
            "کد بار (اتاق.پارت)",
            "نوع سیب",
            "مدل سبد",
            "تعداد جعبه",
            "وزن ناخالص (کیلوگرم)",
            "تارا / سبد (کیلوگرم)",
            "وزن خالص (کیلوگرم)",
            "نوع وسیله نقلیه",
            "پلاک خودرو",
            "نام راننده",
            "نحوه محاسبه کرایه",
            "نرخ واحد (تومان)",
            "کل هزینه تخلیه/بارگیری (تومان)",
            "تعداد کارگران",
            "اسامی کارگران و نقش",
            "مجموع دستمزد توافقی کارگران (تومان)",
            "مجموع پرداختی به کارگران (تومان)",
            "مانده طلب کارگران (تومان)",
            "وضعیت تسویه کارگران",
            "یادداشت‌ها و توضیحات"
        )
        sb.append(headers.joinToString(",") { "\"$it\"" }).append("\n")

        // ردیف‌های داده‌ها
        records.forEachIndexed { index, r ->
            val workersSummary = r.workers.joinToString(" | ") { "${it.name} (${it.role.titleFa}): پرداختی ${it.paidAmount.toLong()}" }
            val isEntry = r.operationType == RecordType.ENTRY
            val row = listOf(
                (index + 1).toString(),
                r.id,
                if (isEntry) "ورود به سردخانه" else "خروج از سردخانه",
                r.dateShamsi,
                r.timeStr,
                r.farmerName,
                r.farmerPhone,
                "${r.cargoCode} (${r.displayCargoDetails})",
                r.displayAppleName,
                r.boxType.titleFa,
                r.boxCount.toString(),
                r.grossWeightKg.toLong().toString(),
                r.tareWeightKg.toLong().toString(),
                r.netWeightKg.toLong().toString(),
                r.vehicleType.titleFa,
                r.plateNumber,
                r.driverName,
                r.feeCalculationType.titleFa,
                r.unitRate.toLong().toString(),
                r.totalHandlingFee.toLong().toString(),
                r.workers.size.toString(),
                workersSummary,
                r.totalWorkersWage.toLong().toString(),
                r.totalWorkersPaid.toLong().toString(),
                r.totalWorkersRemaining.toLong().toString(),
                if (r.isWorkersFullySettled) "تسویه کامل" else "دارای مانده طلب",
                r.notes.replace("\n", " ")
            )
            sb.append(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }).append("\n")
        }

        // ردیف جمع‌کل در انتهای شیت اکسل
        val totalInBoxes = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
        val totalOutBoxes = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
        val totalNetWeight = records.sumOf { it.netWeightKg }
        val totalFees = records.sumOf { it.totalHandlingFee }
        val totalPaidWorkers = records.sumOf { it.totalWorkersPaid }

        sb.append("\n")
        sb.append("\"جمع‌کل ورودی (جعبه)\",\"$totalInBoxes\"\n")
        sb.append("\"جمع‌کل خروجی (جعبه)\",\"$totalOutBoxes\"\n")
        sb.append("\"موجودی باقی‌مانده (جعبه)\",\"${totalInBoxes - totalOutBoxes}\"\n")
        sb.append("\"مجموع وزن خالص گردش (کیلو)\",\"${totalNetWeight.toLong()}\"\n")
        sb.append("\"مجموع کرایه/هزینه ماشین‌ها (تومان)\",\"${totalFees.toLong()}\"\n")
        sb.append("\"مجموع پرداختی به کارگران (تومان)\",\"${totalPaidWorkers.toLong()}\"\n")

        file.writeText(sb.toString(), Charsets.UTF_8)
        copyToPublicDownloads(file)
        return file
    }

    /**
     * تولید فایل رسمی PDF قطع A4 به همراه فونت وزیر متن و ساختار جدولی مدرن
     */
    fun generateA4Pdf(
        context: Context,
        records: List<ColdStorageEntry>,
        reportTitle: String,
        dateRangeStr: String
    ): File {
        val regularTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val boldTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val today = JalaliDateHelper.getTodayJalali()
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val pdfDoc = PdfDocument()
        val pageWidth = 842 // استاندارد عرض A4 افقی (Landscape) به پوینت
        val pageHeight = 595 // استاندارد ارتفاع A4 افقی (Landscape) به پوینت

        // قلم‌ها
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = regularTypeface
            textSize = 8.5f
            color = Color.parseColor("#1E293B")
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = 9.5f
            color = Color.parseColor("#0F172A")
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = boldTypeface
            textSize = 15f
            color = Color.parseColor("#1B4D3E") // Emerald Primary
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = regularTypeface
            textSize = 9.5f
            color = Color.parseColor("#475569")
        }

        val bgPaint = Paint().apply { style = Paint.Style.FILL }
        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.parseColor("#CBD5E1")
        }

        val recordsPerPage = 14
        val chunks = if (records.isEmpty()) listOf(emptyList()) else records.chunked(recordsPerPage)

        chunks.forEachIndexed { pageIndex, pageRecords ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // پس‌زمینه سفید کل صفحه
            canvas.drawColor(Color.WHITE)

            // نوار کادر سربرگ با رنگ سبز زمردی
            bgPaint.color = Color.parseColor("#1B4D3E")
            canvas.drawRect(RectF(24f, 18f, (pageWidth - 24).toFloat(), 23f), bgPaint)

            // سربرگ رسمی (Header)
            canvas.drawText("سردخانه سیب سردیار", (pageWidth - 30).toFloat() - titlePaint.measureText("سردخانه سیب سردیار"), 40f, titlePaint)
            canvas.drawText("گزارش رسمی: $reportTitle", (pageWidth - 30).toFloat() - subtitlePaint.measureText("گزارش رسمی: $reportTitle"), 54f, subtitlePaint)
            canvas.drawText("بازه زمانی: $dateRangeStr", (pageWidth - 30).toFloat() - subtitlePaint.measureText("بازه زمانی: $dateRangeStr"), 67f, subtitlePaint)

            // تاریخ صدور در سمت چپ هدر
            val dateIssued = "تاریخ صدور: ${JalaliDateHelper.formatShort(today, toPersianDigits = true)}  ساعت: $timeNow"
            canvas.drawText(dateIssued, 30f, 44f, subtitlePaint)
            canvas.drawText("صفحه ${pageIndex + 1} از ${chunks.size}", 30f, 59f, subtitlePaint)

            // در صفحه اول: کارت‌های خلاصه شاخص‌ها
            var startTableY = 78f
            if (pageIndex == 0) {
                val cardWidth = (pageWidth - 48 - 18) / 4f
                val totalInBoxes = records.filter { it.operationType == RecordType.ENTRY }.sumOf { it.boxCount }
                val totalOutBoxes = records.filter { it.operationType == RecordType.EXIT }.sumOf { it.boxCount }
                val currentStock = totalInBoxes - totalOutBoxes
                val totalPaidWorkers = records.sumOf { it.totalWorkersPaid }

                val summaryCards = listOf(
                    Triple("کل ورودی", "${NumberFormatHelper.format(totalInBoxes, true)} جعبه", "#ECFDF5"),
                    Triple("کل خروجی", "${NumberFormatHelper.format(totalOutBoxes, true)} جعبه", "#FFF7ED"),
                    Triple("موجودی مانده", "${NumberFormatHelper.format(currentStock, true)} جعبه", "#EFF6FF"),
                    Triple("پرداختی کارگران", "${NumberFormatHelper.format(totalPaidWorkers.toLong(), true)} ت", "#F8FAFC")
                )

                summaryCards.forEachIndexed { i, (t, v, bgHex) ->
                    val x = 24f + i * (cardWidth + 6)
                    bgPaint.color = Color.parseColor(bgHex)
                    val r = RectF(x, 78f, x + cardWidth, 114f)
                    canvas.drawRoundRect(r, 6f, 6f, bgPaint)
                    canvas.drawRoundRect(r, 6f, 6f, strokePaint)

                    val tPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = regularTypeface
                        textSize = 8.5f
                        color = Color.parseColor("#475569")
                    }
                    val vPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        typeface = boldTypeface
                        textSize = 9.5f
                        color = Color.parseColor("#0F172A")
                    }
                    canvas.drawText(t, x + (cardWidth - tPaint.measureText(t)) / 2f, 92f, tPaint)
                    canvas.drawText(v, x + (cardWidth - vPaint.measureText(v)) / 2f, 106f, vPaint)
                }
                startTableY = 124f
            }

            // رسم جدول داده‌ها در صفحه افقی (Landscape)
            val tableLeft = 24f
            val tableRight = (pageWidth - 24).toFloat()
            val rowHeight = 21f

            // سرستون‌های جدول در قطع افقی (RTL: راست به چپ)
            val columns = listOf(
                Pair("ردیف", 28f),
                Pair("قبض", 48f),
                Pair("تاریخ", 56f),
                Pair("نوع", 38f),
                Pair("صاحب بار / باغدار", 115f),
                Pair("رقم سیب", 75f),
                Pair("کد بار", 45f),
                Pair("جعبه", 42f),
                Pair("ناخالص", 46f),
                Pair("خالص (kg)", 52f),
                Pair("خودرو / پلاک", 75f),
                Pair("نام راننده", 68f),
                Pair("کرایه (ت)", 54f),
                Pair("پرداختی کارگر", 52f)
            )

            // رسم سطر هدر جدول
            bgPaint.color = Color.parseColor("#1B4D3E")
            canvas.drawRect(RectF(tableLeft, startTableY, tableRight, startTableY + rowHeight), bgPaint)

            var currentHeaderX = tableRight
            val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = boldTypeface
                textSize = 8f
                color = Color.WHITE
            }

            columns.forEach { (colTitle, width) ->
                val xText = currentHeaderX - width + (width - headerTextPaint.measureText(colTitle)) / 2f
                canvas.drawText(colTitle, xText, startTableY + 14f, headerTextPaint)
                currentHeaderX -= width
            }

            // رسم ردیف‌های داده‌ها با Zebra Striping (یک در میان سفید و طوسی ملایم)
            var currentY = startTableY + rowHeight
            pageRecords.forEachIndexed { rowIdx, r ->
                val isEven = rowIdx % 2 == 0
                bgPaint.color = if (isEven) Color.WHITE else Color.parseColor("#F8FAFC")
                canvas.drawRect(RectF(tableLeft, currentY, tableRight, currentY + rowHeight), bgPaint)
                canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, strokePaint)

                var cellX = tableRight
                val rowCells = listOf(
                    Pair(JalaliDateHelper.toPersianDigits((pageIndex * recordsPerPage + rowIdx + 1).toString()), columns[0].second),
                    Pair(r.id, columns[1].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.dateShamsi), columns[2].second),
                    Pair(if (r.operationType == RecordType.ENTRY) "ورود" else "خروج", columns[3].second),
                    Pair(r.farmerName.take(18), columns[4].second),
                    Pair(r.displayAppleName.take(13), columns[5].second),
                    Pair(r.roomPartCode, columns[6].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.boxCount.toString()), columns[7].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.grossWeightKg.toLong().toString()), columns[8].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.netWeightKg.toLong().toString()), columns[9].second),
                    Pair("${r.vehicleType.titleFa} ${r.plateNumber.take(8)}", columns[10].second),
                    Pair(r.driverName.ifBlank { "-" }.take(10), columns[11].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.totalHandlingFee.toLong().toString()), columns[12].second),
                    Pair(JalaliDateHelper.toPersianDigits(r.totalWorkersPaid.toLong().toString()), columns[13].second)
                )

                rowCells.forEachIndexed { cIdx, (cellText, width) ->
                    val p = if (cIdx == 0 || cIdx == 1 || cIdx == 7) boldPaint else textPaint
                    if (cIdx == 3) {
                        p.color = if (r.operationType == RecordType.ENTRY) Color.parseColor("#15803D") else Color.parseColor("#C2410C")
                    } else {
                        p.color = Color.parseColor("#1E293B")
                    }
                    val x = cellX - width + (width - p.measureText(cellText)) / 2f
                    canvas.drawText(cellText, x, currentY + 14f, p)
                    cellX -= width
                }

                currentY += rowHeight
            }

            // اگر آخرین صفحه است، ردیف جمع کل و محل امضاها را رسم کن
            if (pageIndex == chunks.size - 1) {
                // سطر جمع کل جدول
                bgPaint.color = Color.parseColor("#E2E8F0")
                canvas.drawRect(RectF(tableLeft, currentY, tableRight, currentY + rowHeight + 2), bgPaint)

                val sumTitle = "جمع‌کل ردیف‌های گزارش عملکرد سردیار:"
                canvas.drawText(sumTitle, tableRight - 280f, currentY + 15f, boldPaint)

                val totalBoxes = records.sumOf { it.boxCount }
                val totalKg = records.sumOf { it.netWeightKg }
                val totalFees = records.sumOf { it.totalHandlingFee }
                val totalWorkersPaidAll = records.sumOf { it.totalWorkersPaid }
                canvas.drawText(
                    "${NumberFormatHelper.format(totalBoxes, true)} جعبه  |  ${NumberFormatHelper.format(totalKg.toLong(), true)} کیلو خالص  |  کرایه: ${NumberFormatHelper.format(totalFees.toLong(), true)} ت  |  کارگران: ${NumberFormatHelper.format(totalWorkersPaidAll.toLong(), true)} ت",
                    tableLeft + 20f,
                    currentY + 15f,
                    boldPaint
                )

                currentY += rowHeight + 25f

                // بخش امضا و مهرها
                val signY = (pageHeight - 45).toFloat()
                canvas.drawLine(tableLeft, signY - 14f, tableRight, signY - 14f, strokePaint)

                val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = regularTypeface
                    textSize = 8.5f
                    color = Color.parseColor("#475569")
                }
                canvas.drawText("امضاء و اثر انگشت راننده / تحویل‌دهنده", tableRight - 200f, signY, signPaint)
                canvas.drawText("امضاء متصدی باسکول و انباردار", tableRight - 480f, signY, signPaint)
                canvas.drawText("مهر و تاییدیه رسمی مدیریت سردخانه سردیار", tableLeft + 20f, signY, signPaint)
            }

            // انتهای فوتر هر صفحه
            canvas.drawText(
                "سامانه هوشمند سردیار • خروجی رسمی افقی (Landscape) • صدور ۱۰۰٪ آفلاین",
                (pageWidth - textPaint.measureText("سامانه هوشمند سردیار • خروجی رسمی افقی (Landscape) • صدور ۱۰۰٪ آفلاین")) / 2f,
                (pageHeight - 10).toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = regularTypeface
                    textSize = 7.5f
                    color = Color.parseColor("#94A3B8")
                }
            )

            pdfDoc.finishPage(page)
        }

        val pdfFile = File(context.cacheDir, "SardYar_Report_${today.year}_${today.month}_${today.day}_${System.currentTimeMillis().toString().takeLast(4)}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        copyToPublicDownloads(pdfFile)
        return pdfFile
    }

    /**
     * ارسال دستور چاپ و پیش‌نمایش به PrintManager اندروید
     */
    fun printPdf(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }

    /**
     * اشتراک‌گذاری فایل (PDF یا اکسل) با نرم‌افزارهای پیام‌رسان و سیستم
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    /**
     * کپی فایل در پوشه عمومی Downloads گوشی
     */
    private fun copyToPublicDownloads(file: File) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                val targetFile = File(downloadsDir, file.name)
                file.copyTo(targetFile, overwrite = true)
            }
        } catch (_: Exception) {
            // در اندروید ۱۱ به بالا در صورت محدودیت Scoped Storage فایل کش معتبر است
        }
    }
}
