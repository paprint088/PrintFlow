package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object JobStatus {
    const val PENDING_APPROVAL = "PENDING_APPROVAL"
    const val APPROVED_PRINTED = "APPROVED_PRINTED"
    const val REJECTED_NO_ATTACHMENT = "REJECTED_NO_ATTACHMENT"
    const val REJECTED_PHOTO_OF_DOC = "REJECTED_PHOTO_OF_DOC"
    const val CANCELLED = "CANCELLED"
}

@Entity(tableName = "print_jobs")
data class PrintJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gmailMessageId: String = "",
    val senderEmail: String,
    val senderName: String,
    val emailSubject: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val hasAttachment: Boolean,
    val isPhotographOfDocument: Boolean = false,
    val attachmentName: String? = null,
    val attachmentType: String? = null,
    val pageCount: Int = 0,
    val avgInkCoverage: Double = 0.0,
    val inkBreakdownJson: String = "", // e.g. "Page 1: 5.4% (Black), Page 2: 14.2% (Color)"
    val assignedPrinterId: Int = 1,
    val assignedPrinterName: String = "Epson EcoTank L3210",
    val paperSize: String = "A4",
    val printColorMode: String = "MONOCHROME", // "MONOCHROME" or "COLOR"
    val copies: Int = 1,
    val isDuplex: Boolean = false,
    val calculatedCost: Double = 0.0,
    val finalApprovedCost: Double = 0.0,
    val status: String = JobStatus.PENDING_APPROVAL,
    val replySent: Boolean = false,
    val replyMessage: String? = null,
    val completedAt: Long? = null,
    val incomeEarned: Double = 0.0
)
