package com.apkhimoyachi.security.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkhimoyachi.security.model.RiskLevel
import com.apkhimoyachi.security.ui.SecurityUiState
import com.apkhimoyachi.security.ui.theme.*

@Composable
fun DashboardScreen(
    state: SecurityUiState,
    onStartScan: (Boolean) -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToApks: () -> Unit
) {
    val overallStatus = state.overallRiskStatus
    val statusColor = overallStatus.getColor()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // 1. Asosiy Himoya Holati Paneli
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (overallStatus) {
                                RiskLevel.SAFE -> Icons.Default.Shield
                                RiskLevel.LOW -> Icons.Default.Security
                                RiskLevel.MEDIUM -> Icons.Default.Warning
                                else -> Icons.Default.GppBad
                            },
                            contentDescription = "Himoya qalqoni",
                            tint = statusColor,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = when (overallStatus) {
                            RiskLevel.SAFE -> "Qurilmangiz himoyalangan"
                            RiskLevel.LOW -> "Xavfsizlik darajasi yaxshi"
                            RiskLevel.MEDIUM -> "Shubhali ilovalar aniqlandi"
                            RiskLevel.HIGH -> "Yuqori xavfli ilovalar bor!"
                            RiskLevel.CRITICAL -> "KRITIK XAVF ANIQLANDI!"
                        },
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (state.isScanning) {
                            state.currentScanItem
                        } else {
                            "Real vaqt tahlili faol • Shaxsiy ma'lumotlar to'planmaydi"
                        },
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    AnimatedVisibility(visible = state.isScanning) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { state.scanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = PrimaryBlue,
                                trackColor = DarkSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onStartScan(false) },
                            enabled = !state.isScanning,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tezkor tahlil", fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onStartScan(true) },
                            enabled = !state.isScanning,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("To'liq tahlil", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // 2. Statistik Kartalar (2x2 Grid)
        item {
            Text(
                text = "Xavfsizlik umumiy ko'rinishi",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "O'rnatilgan ilovalar",
                    value = state.totalApps.toString(),
                    icon = Icons.Default.Apps,
                    iconTint = PrimaryBlue,
                    onClick = onNavigateToApps
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Xavfli ilovalar",
                    value = state.totalRiskyApps.toString(),
                    icon = Icons.Default.WarningAmber,
                    iconTint = if (state.totalRiskyApps > 0) RiskHighColor else RiskSafeColor,
                    onClick = onNavigateToApps
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Noma'lum manbalar",
                    value = state.sideloadedAppsCount.toString(),
                    icon = Icons.Default.CloudDownload,
                    iconTint = RiskMediumColor,
                    onClick = onNavigateToApps
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "APK fayllar",
                    value = state.apkFilesCount.toString(),
                    icon = Icons.Default.InsertDriveFile,
                    iconTint = AccentCyan,
                    onClick = onNavigateToApks
                )
            }
        }

        // 3. 100% Maxfiylik va Shaxsiy Ma'lumotlarni Himoyalash Kafolati
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RiskSafeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Maxfiylik",
                            tint = RiskSafeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Xavfsizlik va Maxfiylik",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "APK Himoyachi shaxsiy ma'lumotlarni to'plamaydi. Barcha tahlillar xavfsiz holda qurilmangiz ichida bajariladi.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
