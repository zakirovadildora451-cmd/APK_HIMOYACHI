package com.apkhimoyachi.security.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkhimoyachi.security.analyzer.PermissionClassifier
import com.apkhimoyachi.security.model.AppSecurityInfo
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppSecurityInfo,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = app.riskLevel.getColor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.appName, color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Asosiy Xavfsizlik Reytingi Kartasi
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${app.riskScore}",
                                color = statusColor,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Xavf darajasi: ${app.riskLevel.labelUz}",
                            color = statusColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = app.packageName,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // O'rnatish manbasi
                        Surface(
                            color = if (app.isSideloaded) RiskMediumColor.copy(alpha = 0.2f) else RiskSafeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (app.isSideloaded) Icons.Default.Warning else Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = if (app.isSideloaded) RiskMediumColor else RiskSafeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (app.isSideloaded) "Manba: Noma'lum (Sideload)" else "Manba: Ishonchli (Google Play)",
                                    color = if (app.isSideloaded) RiskMediumColor else RiskSafeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tizim sozlamalariga o'tish (Ilovani o'chirish yoki ruxsatlarini olish uchun)
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${app.packageName}")
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (app.riskLevel != RiskLevel.SAFE) RiskCriticalColor else PrimaryBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (app.riskLevel != RiskLevel.SAFE) "Ilovani o'chirish / Ruxsatlarni cheklash" else "Tizim sozlamalarini ko'rish",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 2. Aniqlangan xavflar va ogohlantirishlar
            if (app.reasons.isNotEmpty()) {
                item {
                    Text(
                        text = "Aniqlangan xavfsizlik ogohlantirishlari (${app.reasons.size})",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(app.reasons) { reason ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = when (reason.severity) {
                                    RiskLevel.CRITICAL -> Icons.Default.Dangerous
                                    RiskLevel.HIGH -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = reason.severity.getColor(),
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = reason.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = reason.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Ilova so'ragan barcha ruxsatnomalar
            item {
                Text(
                    text = "So'ralgan ruxsatnomalar (${app.requestedPermissions.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(app.requestedPermissions) { perm ->
                val details = PermissionClassifier.getDetails(perm)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (details != null) DarkSurface else DarkBackground
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = details?.uzbekName ?: perm.substringAfterLast('.'),
                                color = if (details != null) details.riskLevel.getColor() else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (details != null) {
                                Surface(
                                    color = details.riskLevel.getColor().copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = details.riskLevel.labelUz,
                                        color = details.riskLevel.getColor(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = perm,
                            color = TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        if (details != null) {
                            Text(
                                text = details.uzbekDescription,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
