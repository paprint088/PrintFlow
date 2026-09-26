package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cost_matrix")
data class CostMatrix(
    @PrimaryKey val id: Int = 1,
    val currencySymbol: String = "$",
    val monochromeLowCoveragePrice: Double = 0.05,  // < 10% ink coverage
    val monochromeHighCoveragePrice: Double = 0.10, // >= 10% ink coverage
    val colorLowCoveragePrice: Double = 0.15,       // < 15% color coverage
    val colorMediumCoveragePrice: Double = 0.25,    // 15% - 30% coverage
    val colorHighCoveragePrice: Double = 0.45,      // > 30% coverage
    val paperCostA4: Double = 0.02,
    val paperCostLetter: Double = 0.02,
    val paperCostLegal: Double = 0.04,
    val duplexDiscountPercent: Double = 15.0,        // discount on 2-sided print
    val baseServiceFee: Double = 0.00
) {
    fun calculatePageCost(
        isColor: Boolean,
        inkCoveragePercent: Double,
        paperSize: String,
        isDuplex: Boolean
    ): Double {
        val inkPrice = if (!isColor) {
            if (inkCoveragePercent < 10.0) monochromeLowCoveragePrice else monochromeHighCoveragePrice
        } else {
            when {
                inkCoveragePercent < 15.0 -> colorLowCoveragePrice
                inkCoveragePercent <= 30.0 -> colorMediumCoveragePrice
                else -> colorHighCoveragePrice
            }
        }

        val paperPrice = when (paperSize.uppercase()) {
            "LEGAL" -> paperCostLegal
            "LETTER" -> paperCostLetter
            else -> paperCostA4
        }

        val totalPerPage = inkPrice + paperPrice
        return if (isDuplex) {
            totalPerPage * (1.0 - (duplexDiscountPercent / 100.0))
        } else {
            totalPerPage
        }
    }
}
