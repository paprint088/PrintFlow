package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CostMatrix
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs ORDER BY receivedAt DESC")
    fun getAllJobs(): Flow<List<PrintJob>>

    @Query("SELECT * FROM print_jobs WHERE status = 'PENDING_APPROVAL' ORDER BY receivedAt DESC")
    fun getPendingApprovalJobs(): Flow<List<PrintJob>>

    @Query("SELECT * FROM print_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): PrintJob?

    @Query("SELECT * FROM print_jobs WHERE gmailMessageId = :msgId LIMIT 1")
    suspend fun getJobByGmailId(msgId: String): PrintJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: PrintJob): Long

    @Update
    suspend fun updateJob(job: PrintJob)

    @Query("DELETE FROM print_jobs WHERE id = :id")
    suspend fun deleteJobById(id: Long)

    @Query("DELETE FROM print_jobs")
    suspend fun clearAllJobs()

    @Query("SELECT SUM(incomeEarned) FROM print_jobs WHERE status = 'APPROVED_PRINTED'")
    fun getTotalIncomeFlow(): Flow<Double?>
}

@Dao
interface CostMatrixDao {
    @Query("SELECT * FROM cost_matrix WHERE id = 1 LIMIT 1")
    fun getCostMatrixFlow(): Flow<CostMatrix?>

    @Query("SELECT * FROM cost_matrix WHERE id = 1 LIMIT 1")
    suspend fun getCostMatrix(): CostMatrix?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(matrix: CostMatrix)
}

@Dao
interface PrinterDao {
    @Query("SELECT * FROM printers ORDER BY isDefault DESC, name ASC")
    fun getAllPrinters(): Flow<List<PrinterModel>>

    @Query("SELECT * FROM printers WHERE id = :id LIMIT 1")
    suspend fun getPrinterById(id: Int): PrinterModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrinter(printer: PrinterModel): Long

    @Update
    suspend fun updatePrinter(printer: PrinterModel)

    @Delete
    suspend fun deletePrinter(printer: PrinterModel)
}
