package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CostMatrix
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.network.DocumentAnalyzer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PrintFlow", appName)
    }

    @Test
    fun `verify cost matrix calculation`() {
        val matrix = CostMatrix(
            currencySymbol = "$",
            monochromeLowCoveragePrice = 0.05,
            monochromeHighCoveragePrice = 0.10,
            colorLowCoveragePrice = 0.15,
            colorMediumCoveragePrice = 0.25,
            colorHighCoveragePrice = 0.45,
            paperCostA4 = 0.02,
            duplexDiscountPercent = 10.0
        )

        // B&W low coverage on A4: 0.05 + 0.02 = 0.07
        val monoCost = matrix.calculatePageCost(
            isColor = false,
            inkCoveragePercent = 5.0,
            paperSize = "A4",
            isDuplex = false
        )
        assertEquals(0.07, monoCost, 0.001)

        // Color medium coverage on A4: 0.25 + 0.02 = 0.27
        val colorCost = matrix.calculatePageCost(
            isColor = true,
            inkCoveragePercent = 20.0,
            paperSize = "A4",
            isDuplex = false
        )
        assertEquals(0.27, colorCost, 0.001)

        // Duplex 10% discount: 0.27 * 0.9 = 0.243
        val duplexCost = matrix.calculatePageCost(
            isColor = true,
            inkCoveragePercent = 20.0,
            paperSize = "A4",
            isDuplex = true
        )
        assertEquals(0.243, duplexCost, 0.001)
    }

    @Test
    fun `verify no attachment detection`() = runBlocking {
        val result = DocumentAnalyzer.analyzeDocument(
            filename = null,
            mimeType = null,
            attachmentBytes = null
        )
        assertFalse(result.hasAttachment)
        assertNotNull(result.rejectionReason)
    }

    @Test
    fun `verify photograph of document rejection`() = runBlocking {
        val result = DocumentAnalyzer.analyzeDocument(
            filename = "IMG_20260925_contract_photo.jpg",
            mimeType = "image/jpeg",
            attachmentBytes = ByteArray(1024)
        )
        assertTrue(result.hasAttachment)
        assertTrue(result.isPhotographOfDocument)
        assertNotNull(result.rejectionReason)
    }

    @Test
    fun `verify valid PDF document ink calculation`() = runBlocking {
        val result = DocumentAnalyzer.analyzeDocument(
            filename = "Company_Handout.pdf",
            mimeType = "application/pdf",
            attachmentBytes = ByteArray(2048)
        )
        assertTrue(result.hasAttachment)
        assertFalse(result.isPhotographOfDocument)
        assertTrue(result.pageCount >= 1)
        assertTrue(result.pages.isNotEmpty())
        assertTrue(result.averageCoverage > 0.0)
    }

    @Test
    fun `verify job status constants`() {
        assertEquals("PENDING_APPROVAL", JobStatus.PENDING_APPROVAL)
        assertEquals("APPROVED_PRINTED", JobStatus.APPROVED_PRINTED)
        assertEquals("REJECTED_NO_ATTACHMENT", JobStatus.REJECTED_NO_ATTACHMENT)
        assertEquals("REJECTED_PHOTO_OF_DOC", JobStatus.REJECTED_PHOTO_OF_DOC)
    }

    @Test
    fun `verify default monitored email is paprint088`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val gmailService = com.example.network.GmailService(context)
        assertEquals("paprint088@gmail.com", gmailService.userEmailAddress)
    }
}
