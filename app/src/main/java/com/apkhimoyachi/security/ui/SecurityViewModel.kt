package com.apkhimoyachi.security.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apkhimoyachi.security.analyzer.ApkFileScanner
import com.apkhimoyachi.security.analyzer.AppRiskAnalyzer
import com.apkhimoyachi.security.model.ApkFileInfo
import com.apkhimoyachi.security.model.AppSecurityInfo
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.service.RealtimeProtectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentScanItem: String = "",
    val installedApps: List<AppSecurityInfo> = emptyList(),
    val apkFiles: List<ApkFileInfo> = emptyList(),
    val isRealtimeProtectionEnabled: Boolean = true,
    val includeSystemApps: Boolean = false,
    val selectedApp: AppSecurityInfo? = null,
    val lastScanTime: Long? = null
) {
    val totalApps: Int get() = installedApps.size
    val criticalThreatsCount: Int get() = installedApps.count { it.riskLevel == RiskLevel.CRITICAL } + apkFiles.count { it.riskLevel == RiskLevel.CRITICAL }
    val highRiskCount: Int get() = installedApps.count { it.riskLevel == RiskLevel.HIGH } + apkFiles.count { it.riskLevel == RiskLevel.HIGH }
    val totalRiskyApps: Int get() = installedApps.count { it.riskLevel != RiskLevel.SAFE }
    val sideloadedAppsCount: Int get() = installedApps.count { it.isSideloaded }
    val apkFilesCount: Int get() = apkFiles.size

    val overallRiskStatus: RiskLevel
        get() = when {
            criticalThreatsCount > 0 -> RiskLevel.CRITICAL
            highRiskCount > 0 -> RiskLevel.HIGH
            installedApps.any { it.riskLevel == RiskLevel.MEDIUM } -> RiskLevel.MEDIUM
            installedApps.any { it.riskLevel == RiskLevel.LOW } -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }
}

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val appRiskAnalyzer = AppRiskAnalyzer(application)
    private val apkFileScanner = ApkFileScanner(application)

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        // Ilova ishga tushganda avtomatik tezkor tahlilni boshlash
        startScan(isFullScan = false)
        // Real vaqt himoyasini faollashtirish
        try {
            RealtimeProtectionService.startService(application)
        } catch (e: Exception) {
            // Android 14+ da ruxsat berilmaguncha fon xizmati xatolik bermasligi uchun
        }
    }

    fun startScan(isFullScan: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanProgress = 0.1f, currentScanItem = "O'rnatilgan ilovalar tekshirilmoqda...") }

            // 1. O'rnatilgan ilovalarni tahlil qilish
            val apps = appRiskAnalyzer.analyzeInstalledApps(_uiState.value.includeSystemApps)
            _uiState.update { it.copy(scanProgress = 0.6f, installedApps = apps, currentScanItem = "Xotiradagi APK fayllar qidirilmoqda...") }

            // 2. APK fayllarni skanerlash
            val apks = if (isFullScan) {
                apkFileScanner.scanStorageForApks()
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1.0f,
                    currentScanItem = "Tahlil yakunlandi",
                    apkFiles = apks,
                    lastScanTime = System.currentTimeMillis()
                )
            }
        }
    }

    fun deleteApkFile(apk: ApkFileInfo) {
        val deleted = apkFileScanner.deleteApk(apk.filePath)
        if (deleted) {
            _uiState.update { state ->
                state.copy(apkFiles = state.apkFiles.filter { it.filePath != apk.filePath })
            }
        }
    }

    fun setIncludeSystemApps(include: Boolean) {
        _uiState.update { it.copy(includeSystemApps = include) }
        startScan(isFullScan = false)
    }

    fun toggleRealtimeProtection(enabled: Boolean, context: Context) {
        _uiState.update { it.copy(isRealtimeProtectionEnabled = enabled) }
        if (enabled) {
            try {
                RealtimeProtectionService.startService(context)
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            RealtimeProtectionService.stopService(context)
        }
    }

    fun selectApp(app: AppSecurityInfo?) {
        _uiState.update { it.copy(selectedApp = app) }
    }

    fun selectAppByPackageName(packageName: String) {
        val app = _uiState.value.installedApps.find { it.packageName == packageName }
            ?: appRiskAnalyzer.analyzeSinglePackage(packageName)
        _uiState.update { it.copy(selectedApp = app) }
    }
}
