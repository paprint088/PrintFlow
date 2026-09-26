package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CostMatrix
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PrintRepository(private val db: AppDatabase) {
    val allJobs: Flow<List<PrintJob>> = db.printJobDao().getAllJobs()
    val pendingJobs: Flow<List<PrintJob>> = db.printJobDao().getPendingApprovalJobs()
    val totalIncome: Flow<Double?> = db.printJobDao().getTotalIncomeFlow()
    val costMatrixFlow: Flow<CostMatrix?> = db.costMatrixDao().getCostMatrixFlow()
    val allPrinters: Flow<List<PrinterModel>> = db.printerDao().getAllPrinters()

    suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
        val matrix = db.costMatrixDao().getCostMatrix()
        if (matrix == null) {
            db.costMatrixDao().insertOrUpdate(CostMatrix())
        }
    }

    suspend fun getCostMatrix(): CostMatrix = withContext(Dispatchers.IO) {
        db.costMatrixDao().getCostMatrix() ?: CostMatrix()
    }

    suspend fun saveCostMatrix(matrix: CostMatrix) = withContext(Dispatchers.IO) {
        db.costMatrixDao().insertOrUpdate(matrix)
    }

    suspend fun insertJob(job: PrintJob): Long = withContext(Dispatchers.IO) {
        db.printJobDao().insertJob(job)
    }

    suspend fun updateJob(job: PrintJob) = withContext(Dispatchers.IO) {
        db.printJobDao().updateJob(job)
    }

    suspend fun getJobById(id: Long): PrintJob? = withContext(Dispatchers.IO) {
        db.printJobDao().getJobById(id)
    }

    suspend fun getJobByGmailId(msgId: String): PrintJob? = withContext(Dispatchers.IO) {
        db.printJobDao().getJobByGmailId(msgId)
    }

    suspend fun deleteJob(id: Long) = withContext(Dispatchers.IO) {
        db.printJobDao().deleteJobById(id)
    }

    suspend fun clearAllJobs() = withContext(Dispatchers.IO) {
        db.printJobDao().clearAllJobs()
    }

    suspend fun addPrinter(printer: PrinterModel) = withContext(Dispatchers.IO) {
        db.printerDao().insertPrinter(printer)
    }

    suspend fun updatePrinter(printer: PrinterModel) = withContext(Dispatchers.IO) {
        db.printerDao().updatePrinter(printer)
    }

    suspend fun deletePrinter(printer: PrinterModel) = withContext(Dispatchers.IO) {
        db.printerDao().deletePrinter(printer)
    }
}
