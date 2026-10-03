package com.apkhimoyachi.security.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkhimoyachi.security.ui.SecurityUiState
import com.apkhimoyachi.security.ui.theme.*

@Composable
fun SettingsScreen(
    state: SecurityUiState,
    onToggleRealtime: (Boolean) -> Unit,
    onToggleSystemApps: (Boolean) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Xavfsizlik sozlamalari",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Real vaqt himoyasi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Real vaqt rejimida himoya",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yangi ilovalar o'rnatilganda va Downloads papkasiga yangi APK fayllar yuklanganda avtomatik tahlil qilish.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = state.isRealtimeProtectionEnabled,
                        onCheckedChange = { onToggleRealtime(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = PrimaryBlue
                        )
                    )
                }
            }
        }

        // 2. Tizim ilovalarini tekshirish
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tizim ilovalarini tahlil qilish",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zavoddan o'rnatilgan tizim ilovalarini ham xavfsizlik tekshiruviga qo'shish.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = state.includeSystemApps,
                        onCheckedChange = { onToggleSystemApps(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = PrimaryBlue
                        )
                    )
                }
            }
        }

        // 3. Maxfiylik va Ma'lumotlarni saqlash deklaratsiyasi
        item {
            Text(
                text = "Maxfiylik va Xavfsizlik Tamoyili",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RiskSafeColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nol-Telemetriya (Zero Data Collection)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Ushbu ilova shaxsiy ma'lumotlarni yig'maydi va tashqi serverlarga bog'lanmaydi.\n\n" +
                                "2. Barcha xavfsizlik tahlillari, fayllarni skanerlash va ruxsatlarni tekshirish qurilmangizning o'zida amalga oshiriladi.\n\n" +
                                "3. Foydalanuvchining shaxsiy ma'lumotlari, kontaktlari, fotosuratlari yoki xabarlari hech qachon yozib olinmaydi, saqlanmaydi va uzatilmaydi.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 4. Versiya va Dastur haqida
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "APK Himoyachi v1.0.0",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Android uchun xavfsizlik va maxfiylik qalqoni",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
