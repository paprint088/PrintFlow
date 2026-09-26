package com.example.service

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.model.PrintJob
import java.io.FileOutputStream

object PrinterDispatcher {

    /**
     * Dispatches a print job to the Android print framework spooler.
     * Uses Android PrintManager to send the document to the connected printer.
     */
    fun sendToPrinter(
        context: Context,
        job: PrintJob,
        onComplete: (Boolean) -> Unit
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            onComplete(false)
            return
        }

        val jobName = "PrintFlow - ${job.attachmentName ?: job.emailSubject}"

        val adapter = object : PrintDocumentAdapter() {
            private var totalPages = job.pageCount.coerceAtLeast(1)

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

                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                val pdfDocument = PdfDocument()
                val paint = Paint().apply {
                    textSize = 14f
                    color = Color.BLACK
                    isAntiAlias = true
                }
                val headerPaint = Paint().apply {
                    textSize = 20f
                    color = Color.DKGRAY
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                for (i in 0 until totalPages) {
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, i + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    // Render document content header
                    canvas.drawText("PrintFlow Auto-Dispatch Document", 40f, 60f, headerPaint)
                    canvas.drawText("Sender: ${job.senderName} (${job.senderEmail})", 40f, 100f, paint)
                    canvas.drawText("Subject: ${job.emailSubject}", 40f, 125f, paint)
                    canvas.drawText("Document: ${job.attachmentName ?: "PrintJob_${job.id}.pdf"}", 40f, 150f, paint)
                    canvas.drawText("Assigned Printer: ${job.assignedPrinterName}", 40f, 175f, paint)
                    canvas.drawText("Settings: ${job.paperSize} | ${job.printColorMode} | Copies: ${job.copies}", 40f, 200f, paint)
                    canvas.drawText("Page ${i + 1} of $totalPages", 40f, 240f, paint)

                    // Draw decorative divider & simulated content block
                    paint.color = Color.LTGRAY
                    canvas.drawRect(40f, 260f, 555f, 262f, paint)
                    canvas.drawRect(40f, 290f, 555f, 750f, paint)

                    paint.color = Color.GRAY
                    paint.textSize = 12f
                    canvas.drawText("[Document Content - Dispatched to ${job.assignedPrinterName}]", 140f, 520f, paint)

                    pdfDocument.finishPage(page)
                }

                try {
                    val out = FileOutputStream(destination?.fileDescriptor)
                    pdfDocument.writeTo(out)
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    onComplete(true)
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                    onComplete(false)
                } finally {
                    pdfDocument.close()
                }
            }
        }

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(
                when (job.paperSize.uppercase()) {
                    "LETTER" -> PrintAttributes.MediaSize.NA_LETTER
                    "LEGAL" -> PrintAttributes.MediaSize.NA_LEGAL
                    else -> PrintAttributes.MediaSize.ISO_A4
                }
            )
            .setColorMode(
                if (job.printColorMode == "COLOR") PrintAttributes.COLOR_MODE_COLOR
                else PrintAttributes.COLOR_MODE_MONOCHROME
            )
            .build()

        printManager.print(jobName, adapter, printAttributes)
    }
}
