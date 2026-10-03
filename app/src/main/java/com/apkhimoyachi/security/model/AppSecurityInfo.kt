package com.apkhimoyachi.security.model

import android.graphics.drawable.Drawable

/**
 * O'rnatilgan ilova haqida xavfsizlik tahlili natijasi
 */
data class AppSecurityInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val installerPackage: String?,
    val isSideloaded: Boolean,
    val riskScore: Int, // 0 - 100
    val riskLevel: RiskLevel,
    val dangerousPermissions: List<String>,
    val requestedPermissions: List<String>,
    val reasons: List<SuspiciousReason>,
    val apkPath: String,
    val lastUpdateTime: Long
)
