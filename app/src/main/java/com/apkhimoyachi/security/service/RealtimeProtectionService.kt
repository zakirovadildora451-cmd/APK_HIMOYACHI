package com.apkhimoyachi.security.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apkhimoyachi.security.ApkHimoyachiApp
import com.apkhimoyachi.security.MainActivity
import com.apkhimoyachi.security.R
import com.apkhimoyachi.security.analyzer.ApkFileScanner
import com.apkhimoyachi.security.model.ApkFileInfo
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.ui.CallAlertActivity
import java.io.File

class RealtimeProtectionService : Service() {

    private val observers = mutableListOf<FileObserver>()
    private lateinit var apkScanner: ApkFileScanner

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, RealtimeProtectionService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RealtimeProtectionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        apkScanner = ApkFileScanner(this)
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startMultiFolderWatchers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        observers.forEach { it.stopWatching() }
        observers.clear()
    }

    private fun createForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ApkHimoyachiApp.CHANNEL_ID)
            .setContentTitle("APK Himoyachi faol")
            .setContentText("Telegram va yuklanayotgan APK fayllar real vaqtda nazoratda")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Telegram va Downloads jildlarini bir vaqtning o'zida real vaqtda kuzatish
     */
    private fun startMultiFolderWatchers() {
        val storageRoot = Environment.getExternalStorageDirectory() ?: return
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val targetFolders = listOfNotNull(
            downloadsDir,
            File(storageRoot, "Telegram/Telegram Documents"),
            File(storageRoot, "Download/Telegram"),
            File(storageRoot, "Android/media/org.telegram.messenger/Telegram/Telegram Documents")
        )

        val mask = FileObserver.CLOSE_WRITE or FileObserver.MOVED_TO

        for (dir in targetFolders) {
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val observer = object : FileObserver(dir.absolutePath, mask) {
                override fun onEvent(event: Int, fileName: String?) {
                    if (fileName != null && fileName.endsWith(".apk", ignoreCase = true)) {
                        val apkFile = File(dir, fileName)
                        if (apkFile.exists() && apkFile.canRead()) {
                            val isFromTelegram = dir.absolutePath.contains("Telegram", ignoreCase = true)
                            handleNewApkDetected(apkFile, isFromTelegram)
                        }
                    }
                }
            }
            observer.startWatching()
            observers.add(observer)
        }
    }

    private fun handleNewApkDetected(apkFile: File, isFromTelegram: Boolean) {
        val info = apkScanner.analyzeApkFile(apkFile)

        // Agar fayl xavfli bo'lsa (Kritik, Yuqori xavf yoki Telegramdan kelgan shubhali fayl)
        if (info.riskLevel == RiskLevel.CRITICAL || info.riskLevel == RiskLevel.HIGH || (isFromTelegram && info.riskLevel != RiskLevel.SAFE)) {
            triggerCallAlert(apkFile, info, isFromTelegram)
        } else if (info.riskLevel != RiskLevel.SAFE) {
            triggerStandardNotification(apkFile, info)
        }
    }

    /**
     * Foydalanuvchiga xuddi kiruvchi telefon qo'ng'irog'i kabi to'liq ekranda ovozli signal berish
     */
    private fun triggerCallAlert(apkFile: File, info: ApkFileInfo, isFromTelegram: Boolean) {
        val reasonText = info.reasons.firstOrNull()?.title ?: "Xavfli ruxsatnomalar va troyan andozasi topildi"

        val callIntent = Intent(this, CallAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_FILE_PATH", apkFile.absolutePath)
            putExtra("EXTRA_FILE_NAME", apkFile.name)
            putExtra("EXTRA_REASON", reasonText)
            putExtra("EXTRA_IS_TELEGRAM", isFromTelegram)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            apkFile.hashCode(),
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Yuqori ustuvorlikdagi Qo'ng'iroq / Alarm bildirishnomasi
        val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val builder = NotificationCompat.Builder(this, ApkHimoyachiApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 QO'NG'IROQ: Telegramdan xavfli fayl keldi!")
            .setContentText("\"${apkFile.name}\" faylida xavf aniqlandi. Darhol oching!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setSound(alertUri)
            .setVibrate(longArrayOf(0, 800, 400, 800))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(this).notify(apkFile.name.hashCode(), builder.build())
            // Qo'ng'iroq oynasini bevosita ochish
            startActivity(callIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerStandardNotification(apkFile: File, info: ApkFileInfo) {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "apks")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            apkFile.name.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, ApkHimoyachiApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Yangi yuklangan APK shubhali!")
            .setContentText("\"${apkFile.name}\" faylida xavf aniqlandi (${info.riskLevel.labelUz}).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(apkFile.name.hashCode(), alertNotification)
        } catch (e: SecurityException) {
            // Ruxsat yo'q bo'lsa
        }
    }
}
