package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CostMatrix
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PrintJob::class, CostMatrix::class, PrinterModel::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun printJobDao(): PrintJobDao
    abstract fun costMatrixDao(): CostMatrixDao
    abstract fun printerDao(): PrinterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "printflow_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default printers and matrix
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.costMatrixDao().insertOrUpdate(CostMatrix())
                                database.printerDao().insertPrinter(
                                    PrinterModel(
                                        name = "Epson EcoTank L3210 (Main Desk)",
                                        location = "Front Counter",
                                        isColor = true,
                                        isDuplexSupported = true,
                                        blackInkLevelPercent = 92,
                                        colorInkLevelPercent = 84,
                                        isDefault = true
                                    )
                                )
                                database.printerDao().insertPrinter(
                                    PrinterModel(
                                        name = "HP LaserJet Pro MFP 4103 (High Speed)",
                                        location = "Back Office",
                                        isColor = false,
                                        isDuplexSupported = true,
                                        blackInkLevelPercent = 75,
                                        colorInkLevelPercent = 0,
                                        isDefault = false
                                    )
                                )
                                database.printerDao().insertPrinter(
                                    PrinterModel(
                                        name = "Canon PIXMA G3020 (Photo & Color)",
                                        location = "Station 2",
                                        isColor = true,
                                        isDuplexSupported = false,
                                        blackInkLevelPercent = 68,
                                        colorInkLevelPercent = 90,
                                        isDefault = false
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
