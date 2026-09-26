package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printers")
data class PrinterModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val location: String = "Main Station",
    val isColor: Boolean = true,
    val isDuplexSupported: Boolean = true,
    val blackInkLevelPercent: Int = 88,
    val colorInkLevelPercent: Int = 74,
    val status: String = "Online & Ready", // Online, Busy, Low Ink, Offline
    val isDefault: Boolean = false
)
