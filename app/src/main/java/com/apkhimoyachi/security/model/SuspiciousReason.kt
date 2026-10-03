package com.apkhimoyachi.security.model

/**
 * Xavf yoki shubhali faollik sababi tavsifi
 */
data class SuspiciousReason(
    val title: String,
    val description: String,
    val severity: RiskLevel,
    val permissionName: String? = null
)
