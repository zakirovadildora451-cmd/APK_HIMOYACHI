package com.apkhimoyachi.security.model

import androidx.compose.ui.graphics.Color

enum class RiskLevel(val labelUz: String, val colorHex: Long) {
    SAFE("Xavfsiz", 0xFF10B981),
    LOW("Past xavf", 0xFF3B82F6),
    MEDIUM("O'rtacha xavf", 0xFFF59E0B),
    HIGH("Yuqori xavf", 0xFFF97316),
    CRITICAL("Kritik xavf", 0xFFEF4444);

    fun getColor(): Color = Color(colorHex)
}
