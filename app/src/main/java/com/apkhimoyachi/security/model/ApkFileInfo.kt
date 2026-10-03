package com.apkhimoyachi.security.model

/**
 * Xotirada topilgan .apk fayl haqida tahlil natijasi
 */
data class ApkFileInfo(
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val packageName: String?,
    val appName: String?,
    val versionName: String?,
    val riskScore: Int, // 0 - 100
    val riskLevel: RiskLevel,
    val isCorruptedOrFake: Boolean,
    val requestedPermissions: List<String>,
    val reasons: List<SuspiciousReason>
)
