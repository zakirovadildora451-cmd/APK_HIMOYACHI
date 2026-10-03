package com.apkhimoyachi.security.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkhimoyachi.security.model.ApkFileInfo
import com.apkhimoyachi.security.ui.theme.*

@Composable
fun ApkFilesScreen(
    apkFiles: List<ApkFileInfo>,
    onDeleteApk: (ApkFileInfo) -> Unit,
    onRefreshScan: () -> Unit
) {
    var apkToDelete by remember { mutableStateOf<ApkFileInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Qurilmadagi APK fayllar",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Downloads va saqlash xotirasidan topilgan fayllar",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onRefreshScan) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Qayta tahlil",
                    tint = PrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (apkFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hech qanday APK fayl topilmadi",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Qurilma xotirasida o'rnatilmagan .apk paketlari mavjud emas yoki to'liq skanerlash qilinmagan.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefreshScan,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Xotirani skanerlash")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(apkFiles, key = { it.filePath }) { apk ->
                    ApkItemCard(
                        apk = apk,
                        onDeleteClick = { apkToDelete = apk }
                    )
                }
            }
        }
    }

    // O'chirishni tasdiqlash dialogi
    apkToDelete?.let { apk ->
        AlertDialog(
            onDismissRequest = { apkToDelete = null },
            title = {
                Text("APK faylni o'chirish", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Rostdan ham \"${apk.fileName}\" faylini qurilma xotirasidan butunlay o'chirib tashlamoqchimisiz?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteApk(apk)
                        apkToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCriticalColor)
                ) {
                    Text("O'chirish")
                }
            },
            dismissButton = {
                TextButton(onClick = { apkToDelete = null }) {
                    Text("Bekor qilish", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ApkItemCard(
    apk: ApkFileInfo,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(apk.riskLevel.getColor().copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = apk.riskLevel.getColor(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = apk.appName ?: apk.fileName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = apk.fileName,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Surface(
                    color = apk.riskLevel.getColor().copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = apk.riskLevel.labelUz,
                        color = apk.riskLevel.getColor(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hajmi va Yo'li
            val sizeMb = apk.fileSizeBytes / (1024f * 1024f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f MB", sizeMb),
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Faylni o'chirish",
                        tint = RiskCriticalColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Xavf sabablari ro'yxati
            if (apk.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                apk.reasons.take(2).forEach { reason ->
                    Text(
                        text = "• ${reason.title}: ${reason.description}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
