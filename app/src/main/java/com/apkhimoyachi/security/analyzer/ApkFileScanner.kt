package com.apkhimoyachi.security.analyzer

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import com.apkhimoyachi.security.model.ApkFileInfo
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.model.SuspiciousReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ApkFileScanner(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Tizim yoki mashhur ilovalarni soxtalashtirish (Spoofing) ro'yxati
    private val criticalSystemPackages = setOf(
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox",
        "com.android.vending",
        "com.android.settings",
        "com.android.systemui"
    )

    /**
     * Qurilma xotirasidagi .apk fayllarni qidirish va to'liq tahlil qilish
     */
    suspend fun scanStorageForApks(): List<ApkFileInfo> = withContext(Dispatchers.IO) {
        val apkList = mutableListOf<File>()

        // 1. Asosiy Downloads papkasi
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && downloadsDir.exists()) {
            findApksRecursively(downloadsDir, apkList, maxDepth = 4)
        }

        // 2. Tashqi xotira ildizi (Download, Telegram, WhatsApp, Documents)
        val storageRoot = Environment.getExternalStorageDirectory()
        if (storageRoot != null && storageRoot.exists()) {
            val commonFolders = listOf("Telegram", "WhatsApp", "Documents", "Bluetooth", "Download")
            for (folderName in commonFolders) {
                val folder = File(storageRoot, folderName)
                if (folder.exists() && folder != downloadsDir) {
                    findApksRecursively(folder, apkList, maxDepth = 3)
                }
            }
        }

        // 3. Har bir topilgan APK faylni chuqur xavfsizlik tahlilidan o'tkazamiz
        val results = mutableListOf<ApkFileInfo>()
        for (file in apkList.distinctBy { it.absolutePath }) {
            val analysis = analyzeApkFile(file)
            results.add(analysis)
        }

        // Xavflilik darajasi bo'yicha kamayish tartibida saralaymiz
        results.sortedByDescending { it.riskScore }
    }

    private fun findApksRecursively(dir: File, resultList: MutableList<File>, maxDepth: Int) {
        if (maxDepth <= 0 || !dir.canRead()) return

        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                // Yashirin yoki xavfsiz tizim papkalarini chetlab o'tamiz
                if (!file.name.startsWith(".")) {
                    findApksRecursively(file, resultList, maxDepth - 1)
                }
            } else if (file.isFile && file.name.endsWith(".apk", ignoreCase = true)) {
                resultList.add(file)
            }
        }
    }

    /**
     * Bitta .apk faylni tahlil qilish
     */
    fun analyzeApkFile(apkFile: File): ApkFileInfo {
        val flags = PackageManager.GET_PERMISSIONS
        val packageInfo: PackageInfo? = try {
            packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags)
        } catch (e: Exception) {
            null
        }

        val reasons = mutableListOf<SuspiciousReason>()
        var score = 0

        // Agar APK fayl o'qilmasa - u buzilgan yoki zararli yuklama bo'lishi mumkin
        if (packageInfo == null) {
            return ApkFileInfo(
                fileName = apkFile.name,
                filePath = apkFile.absolutePath,
                fileSizeBytes = apkFile.length(),
                packageName = null,
                appName = "Noma'lum / Shikastlangan APK",
                versionName = null,
                riskScore = 80,
                riskLevel = RiskLevel.HIGH,
                isCorruptedOrFake = true,
                requestedPermissions = emptyList(),
                reasons = listOf(
                    SuspiciousReason(
                        title = "Shikastlangan yoki soxta paket",
                        description = "Android tizimi ushbu faylning raqamli tuzilishini o'qiy olmadi. Bu ko'pincha to'liq yuklanmagan yoki himoya tizimini chalg'itishga uringan fayllarda uchraydi.",
                        severity = RiskLevel.HIGH
                    )
                )
            )
        }

        val packageName = packageInfo.packageName
        val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
        val dangerousPermissions = permissions.filter { PermissionClassifier.isDangerous(it) }

        // Ilova nomini olishga urinish
        var appName: String? = null
        try {
            packageInfo.applicationInfo.sourceDir = apkFile.absolutePath
            packageInfo.applicationInfo.publicSourceDir = apkFile.absolutePath
            appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            appName = apkFile.nameWithoutExtension
        }

        // Tahlil 0: 100% Oflayn troyanlar bazasidan tekshirish
        val offlineThreat = OfflineMalwareEngine.checkPackageAgainstOfflineBase(packageName)
        if (offlineThreat != null) {
            score += 55
            reasons.add(0, offlineThreat)
        }

        // Tahlil 1: Mashhur tizim paketlarini soxtalashtirish (Spoofing)
        if (criticalSystemPackages.contains(packageName)) {
            score += 45
            reasons.add(
                SuspiciousReason(
                    title = "Soxta tizim ilovasi (Spoofing)",
                    description = "Fayl Google yoki Android tizim paketi nomidan foydalanmoqda, ammo tashqi APK sifatida saqlangan. Bu jiddiy fishing yoki troyan belgisi bo'lishi mumkin.",
                    severity = RiskLevel.CRITICAL
                )
            )
        }

        // Tahlil 2: Xavfli ruxsatlar
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

        // Tahlil 3: Maxsus xavfli kombinatsiyalar
        val hasAccessibility = permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")
        val hasOverlay = permissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
        val hasSms = permissions.contains("android.permission.RECEIVE_SMS") || permissions.contains("android.permission.READ_SMS")

        if (hasAccessibility && hasOverlay) {
            score += 35
            reasons.add(
                0,
                SuspiciousReason(
                    title = "KRITIK XAVF: Bank troyani andozasi",
                    description = "Ushbu APK o'rnatilgach, maxsus imkoniyatlar va ekran ustiga soxta oynalar chiqarish orqali hisoblarni o'g'irlashi mumkin.",
                    severity = RiskLevel.CRITICAL
                )
            )
        }

        if (hasSms) {
            score += 20
            reasons.add(
                SuspiciousReason(
                    title = "SMS xabarlarni tutib qolish xavfi",
                    description = "Ushbu fayl SMS xabarlarni, jumladan bir martalik bank tasdiqlash kodlarini o'qish imkoniyatiga ega.",
                    severity = RiskLevel.HIGH
                )
            )
        }

        val finalScore = score.coerceIn(0, 100)
        val riskLevel = when {
            finalScore >= 75 -> RiskLevel.CRITICAL
            finalScore >= 55 -> RiskLevel.HIGH
            finalScore >= 30 -> RiskLevel.MEDIUM
            finalScore >= 10 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        return ApkFileInfo(
            fileName = apkFile.name,
            filePath = apkFile.absolutePath,
            fileSizeBytes = apkFile.length(),
            packageName = packageName,
            appName = appName,
            versionName = packageInfo.versionName ?: "1.0",
            riskScore = finalScore,
            riskLevel = riskLevel,
            isCorruptedOrFake = false,
            requestedPermissions = permissions,
            reasons = reasons
        )
    }

    /**
     * Xavfli APK faylni xotiradan xavfsiz o'chirish
     */
    fun deleteApk(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
