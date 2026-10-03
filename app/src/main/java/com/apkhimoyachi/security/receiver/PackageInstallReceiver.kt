package com.apkhimoyachi.security.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apkhimoyachi.security.ApkHimoyachiApp
import com.apkhimoyachi.security.MainActivity
import com.apkhimoyachi.security.R
import com.apkhimoyachi.security.analyzer.AppRiskAnalyzer
import com.apkhimoyachi.security.model.RiskLevel

class PackageInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REPLACED) {
            val packageName = intent.data?.schemeSpecificPart ?: return

            // Yangi o'rnatilgan ilovani darhol tahlil qilamiz
            val analyzer = AppRiskAnalyzer(context)
            val appInfo = analyzer.analyzeSinglePackage(packageName) ?: return

            // Agar ilova xavfli yoki shubhali bo'lsa (MEDIUM, HIGH, CRITICAL)
            if (appInfo.riskLevel != RiskLevel.SAFE) {
                showRiskNotification(context, appInfo.appName, appInfo.riskLevel, packageName)
            }
        }
    }

    private fun showRiskNotification(
        context: Context,
        appName: String,
        riskLevel: RiskLevel,
        packageName: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_PACKAGE_NAME", packageName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            packageName.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "Diqqat: Yangi o'rnatilgan ilovada xavf aniqlandi!"
        val notificationText = "\"$appName\" ilovasi ${riskLevel.labelUz.lowercase()} darajasiga ega. Tahlilni ko'rish uchun bosing."

        val builder = NotificationCompat.Builder(context, ApkHimoyachiApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(packageName.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Android 13+ da POST_NOTIFICATIONS ruxsati berilmagan bo'lsa
        }
    }
}
