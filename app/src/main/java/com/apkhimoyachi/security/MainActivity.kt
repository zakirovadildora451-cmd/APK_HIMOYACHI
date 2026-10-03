package com.apkhimoyachi.security

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.apkhimoyachi.security.ui.SecurityViewModel
import com.apkhimoyachi.security.ui.screens.*
import com.apkhimoyachi.security.ui.theme.ApkHimoyachiTheme
import com.apkhimoyachi.security.ui.theme.DarkBackground
import com.apkhimoyachi.security.ui.theme.DarkSurface
import com.apkhimoyachi.security.ui.theme.PrimaryBlue

enum class Screen(val title: String, val icon: ImageVector) {
    DASHBOARD("Bosh sahifa", Icons.Default.Shield),
    APPS("Ilovalar", Icons.Default.Apps),
    APKS("APK fayllar", Icons.Default.Folder),
    SETTINGS("Sozlamalar", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: SecurityViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Natijani qabul qilish */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
        handleIncomingIntent(intent)

        setContent {
            ApkHimoyachiTheme {
                MainScreenContent(
                    viewModel = viewModel,
                    onCheckStoragePermission = { requestManageStoragePermission() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val targetPackage = intent?.getStringExtra("EXTRA_PACKAGE_NAME")
        if (targetPackage != null) {
            viewModel.selectAppByPackageName(targetPackage)
        }
    }

    private fun checkPermissions() {
        // Android 13+ bildirishnomalar ruxsati
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    viewModel: SecurityViewModel,
    onCheckStoragePermission: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    val context = LocalContext.current

    // Agar ilova tafsiloti tanlangan bo'lsa, uni to'liq ekranda ko'rsatamiz
    state.selectedApp?.let { app ->
        AppDetailScreen(
            app = app,
            onBack = { viewModel.selectApp(null) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "APK Himoyachi",
                            fontSize = 19.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface
            ) {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = androidx.compose.ui.graphics.Color.White,
                            selectedTextColor = androidx.compose.ui.graphics.Color.White,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                            unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    state = state,
                    onStartScan = { isFull ->
                        if (isFull) onCheckStoragePermission()
                        viewModel.startScan(isFull)
                    },
                    onNavigateToApps = { currentScreen = Screen.APPS },
                    onNavigateToApks = {
                        onCheckStoragePermission()
                        currentScreen = Screen.APKS
                    }
                )
                Screen.APPS -> AppsScanScreen(
                    apps = state.installedApps,
                    onAppClick = { app -> viewModel.selectApp(app) }
                )
                Screen.APKS -> ApkFilesScreen(
                    apkFiles = state.apkFiles,
                    onDeleteApk = { apk -> viewModel.deleteApkFile(apk) },
                    onRefreshScan = {
                        onCheckStoragePermission()
                        viewModel.startScan(isFullScan = true)
                    }
                )
                Screen.SETTINGS -> SettingsScreen(
                    state = state,
                    onToggleRealtime = { enabled -> viewModel.toggleRealtimeProtection(enabled, context) },
                    onToggleSystemApps = { enabled -> viewModel.setIncludeSystemApps(enabled) }
                )
            }
        }
    }
}
