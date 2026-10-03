package com.apkhimoyachi.security.analyzer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.apkhimoyachi.security.model.AppSecurityInfo
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.model.SuspiciousReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRiskAnalyzer(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    private val trustedInstallers = setOf(
        "com.android.vending", // Google Play Store
        "com.google.android.feedback",
        "com.sec.android.app.samsungapps", // Samsung Galaxy Store
        "com.huawei.appmarket", // Huawei AppGallery
        "com.xiaomi.mipicks", // Xiaomi GetApps
        "com.oppo.market",
        "com.heytap.market"
    )

    /**
     * O'rnatilgan barcha ilovalarni xavfsizlik tahlilidan o'tkazish
     */
    suspend fun analyzeInstalledApps(includeSystemApps: Boolean = false): List<AppSecurityInfo> =
        withContext(Dispatchers.Default) {
            val flags = PackageManager.GET_PERMISSIONS
            val packages = packageManager.getInstalledPackages(flags)
            val result = mutableListOf<AppSecurityInfo>()

            for (pkg in packages) {
                // Tizim ilovalarini foydalanuvchi xohishiga ko'ra filtrlaymiz
                val isSystem = (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!includeSystemApps && isSystem) {
                    continue
                }

                // O'zimizning ilovani hisobga olmaymiz
                if (pkg.packageName == context.packageName) {
                    continue
                }

                val appInfo = evaluatePackage(pkg, isSystem)
                result.add(appInfo)
            }

            // Xavflilik darajasi bo'yicha kamayish tartibida saralaymiz
            result.sortedByDescending { it.riskScore }
        }

    /**
     * Aniq bir ilovani tahlil qilish (masalan, yangi o'rnatilganda)
     */
    fun analyzeSinglePackage(packageName: String): AppSecurityInfo? {
        return try {
            val flags = PackageManager.GET_PERMISSIONS
            val pkg = packageManager.getPackageInfo(packageName, flags)
            val isSystem = (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            evaluatePackage(pkg, isSystem)
        } catch (e: Exception) {
            null
        }
    }

    private fun evaluatePackage(pkg: PackageInfo, isSystemApp: Boolean): AppSecurityInfo {
        val appName = pkg.applicationInfo.loadLabel(packageManager).toString()
        val permissions = pkg.requestedPermissions?.toList() ?: emptyList()
        val dangerousPermissions = permissions.filter { PermissionClassifier.isDangerous(it) }

        val installer = getInstallerPackage(pkg.packageName)
        val isSideloaded = !isSystemApp && (installer == null || !trustedInstallers.contains(installer))

        val reasons = mutableListOf<SuspiciousReason>()
        var score = 0

        // 0. 100% Oflayn zararli dasturlar bazasidan tekshirish
        val offlineThreat = OfflineMalwareEngine.checkPackageAgainstOfflineBase(pkg.packageName)
        if (offlineThreat != null) {
            score += 50
            reasons.add(0, offlineThreat)
        }

        // 1. Noma'lum manba tekshiruvi (Sideloading)
        if (isSideloaded) {
            score += 15
            reasons.add(
                SuspiciousReason(
                    title = "Noma'lum manbadan o'rnatilgan",
                    description = "Ilova rasmiy Google Play do'konidan emas, boshqa manba (masalan, brauzer, fayl menejeri yoki messenjer) orqali o'rnatilgan.",
                    severity = RiskLevel.MEDIUM
                )
            )
        }

        // 2. Xavfli ruxsatlar bo'yicha ball hisoblash
        for (perm in dangerousPermissions) {
            val details = PermissionClassifier.getDetails(perm)
            if (details != null) {
                score += details.scoreWeight
                reasons.add(
                    SuspiciousReason(
                        title = details.uzbekName,
                        description = details.uzbekDescription,
                        severity = details.riskLevel,
                        permissionName = perm
                    )
                )
            }
        }

        // 3. Shubhali va zararli kombinatsiyalarni aniqlash (Heuristic Pattern Detection)
        val hasAccessibility = permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
        val hasOverlay = permissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
        val hasSmsReceive = permissions.contains("android.permission.RECEIVE_SMS")
        val hasSmsRead = permissions.contains("android.permission.READ_SMS")
        val hasBootCompleted = permissions.contains("android.permission.RECEIVE_BOOT_COMPLETED")
        val hasInstallPackages = permissions.contains("android.permission.REQUEST_INSTALL_PACKAGES")
        val hasAudio = permissions.contains("android.permission.RECORD_AUDIO")
        val hasCamera = permissions.contains("android.permission.CAMERA")
        val hasBgLocation = permissions.contains("android.permission.ACCESS_BACKGROUND_LOCATION")

        // A. Bank troyani kombinatsiyasi: Accessibility + Overlay
        if (hasAccessibility && hasOverlay) {
            score += 30
            reasons.add(
                0,
                SuspiciousReason(
                    title = "KRITIK XAVF: Bank troyani xususiyati",
                    description = "Ilova 'Maxsus imkoniyatlar' va 'Ekran ustiga chiqish' ruxsatlariga ega. Bu kombinatsiya bank parollari va kartalarini o'g'irlashda qo'llaniladi.",
                    severity = RiskLevel.CRITICAL
                )
            )
        }

        // B. Noma'lum manbadan yuklangan va Accessibility so'ragan ilova
        if (isSideloaded && hasAccessibility) {
            score += 25
            reasons.add(
                0,
                SuspiciousReason(
                    title = "YUQORI XAVF: Shubhali tizim boshqaruvi",
                    description = "Noma'lum manbadan o'rnatilgan ushbu dastur qurilmaning to'liq boshqaruvini qo'lga kiritishga urinmoqda.",
                    severity = RiskLevel.CRITICAL
                )
            )
        }

        // C. SMS troyani kombinatsiyasi: SMS qabul qilish/o'qish + Avtomatik ishga tushish
        if ((hasSmsReceive || hasSmsRead) && hasBootCompleted) {
            score += 25
            reasons.add(
                SuspiciousReason(
                    title = "YUQORI XAVF: Yashirin SMS nazorati",
                    description = "Ilova fon rejimida doimiy ishlab, kiruvchi bank SMS kodlarini ushlab qolishi mumkin.",
                    severity = RiskLevel.HIGH
                )
            )
        }

        // D. Dropper / Yuklovchi virus kombinatsiyasi
        if (hasInstallPackages && hasBootCompleted) {
            score += 20
            reasons.add(
                SuspiciousReason(
                    title = "Dropper (Zararli dastur yuklovchi) belgisi",
                    description = "Ilova orqa fonda avtomatik ishga tushib, boshqa noma'lum dasturlarni o'rnatish huquqiga ega.",
                    severity = RiskLevel.HIGH
                )
            )
        }

        // E. Josuslik dasturi (Stalkerware / Spyware)
        if (hasAudio && (hasCamera || hasBgLocation)) {
            score += 20
            reasons.add(
                SuspiciousReason(
                    title = "Josuslik (Spyware) faolligi ehtimoli",
                    description = "Ilova bir vaqtning o'zida mikrofon, kamera yoki orqa fondagi joylashuvga kirish huquqiga ega.",
                    severity = RiskLevel.HIGH
                )
            )
        }

        // Ball 0 dan 100 oralig'ida cheklanadi
        val finalScore = score.coerceIn(0, 100)

        val riskLevel = when {
            finalScore >= 75 -> RiskLevel.CRITICAL
            finalScore >= 55 -> RiskLevel.HIGH
            finalScore >= 30 -> RiskLevel.MEDIUM
            finalScore >= 10 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        return AppSecurityInfo(
            packageName = pkg.packageName,
            appName = appName,
            versionName = pkg.versionName ?: "1.0",
            isSystemApp = isSystemApp,
            installerPackage = installer,
            isSideloaded = isSideloaded,
            riskScore = finalScore,
            riskLevel = riskLevel,
            dangerousPermissions = dangerousPermissions,
            requestedPermissions = permissions,
            reasons = reasons,
            apkPath = pkg.applicationInfo.sourceDir ?: "",
            lastUpdateTime = pkg.lastUpdateTime
        )
    }

    private fun getInstallerPackage(packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val info = packageManager.getInstallSourceInfo(packageName)
                info.installingPackageName ?: info.initiatingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
}
