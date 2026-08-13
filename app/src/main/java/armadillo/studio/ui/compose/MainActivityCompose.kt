/*
 * Copyright (c) 2021. Armadillo
 * 承载 Jetpack Compose UI 的 Activity（底部导航 + NavHost）。
 *
 * 注意：本 Activity 仅创建文件，未在 AndroidManifest 中注册。
 * 如需启用，请在 AndroidManifest.xml 中添加对应 <activity> 声明。
 */

package armadillo.studio.ui.compose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import armadillo.studio.common.config.AppConfig
import armadillo.studio.ui.compose.screen.AppItem
import armadillo.studio.ui.compose.screen.HomeScreen
import armadillo.studio.ui.compose.screen.SettingsScreen
import armadillo.studio.ui.compose.screen.SoftwareScreen
import armadillo.studio.ui.compose.screen.TaskItem
import armadillo.studio.ui.compose.screen.TaskStatus
import armadillo.studio.ui.compose.state.UiState
import armadillo.studio.ui.compose.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 底部导航路由常量 */
private object Routes {
    const val HOME = "home"
    const val SOFTWARE = "software"
    const val SETTINGS = "settings"
}

/** GitHub 开源地址（与 SettingsScreen 中保持一致，可按需替换） */
private const val GITHUB_URL = "https://github.com/ArmadilloStudio/ArmPro"

/** 底部导航项 */
private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    NavItem(Routes.HOME, "控制台", Icons.Filled.Home),
    NavItem(Routes.SOFTWARE, "注册机/弹窗管理", Icons.Filled.Apps),
    NavItem(Routes.SETTINGS, "关于", Icons.Filled.Info),
)

class MainActivityCompose : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

/**
 * 主界面：底部导航 + NavHost。各屏幕通过 [collectAsState] / remember 持有
 * [UiState]，这里以本地状态 + 协程模拟刷新流程做演示；实际项目可替换为
 * 各自的 ViewModel（HomeViewModel / SoftwareViewModel）并 collectAsState。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val homeState = remember { mutableStateOf<UiState<List<TaskItem>>>(UiState.Success(sampleTasks())) }
    val softwareState = remember { mutableStateOf<UiState<List<AppItem>>>(UiState.Success(sampleApps())) }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    uiState = homeState.value,
                    onRefresh = {
                        homeState.value = UiState.Loading
                        scope.launch {
                            delay(1200)
                            homeState.value = UiState.Success(sampleTasks())
                        }
                    },
                    onAddTask = {
                        // TODO: 跳转新增任务流程
                    },
                    onRetry = {
                        homeState.value = UiState.Loading
                        scope.launch {
                            delay(1200)
                            homeState.value = UiState.Success(sampleTasks())
                        }
                    },
                )
            }
            composable(Routes.SOFTWARE) {
                SoftwareScreen(
                    uiState = softwareState.value,
                    onRefresh = {
                        softwareState.value = UiState.Loading
                        scope.launch {
                            delay(1200)
                            softwareState.value = UiState.Success(sampleApps())
                        }
                    },
                    onRetry = {
                        softwareState.value = UiState.Loading
                        scope.launch {
                            delay(1200)
                            softwareState.value = UiState.Success(sampleApps())
                        }
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onEmailClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
                        }
                        context.startActivity(Intent.createChooser(intent, "发送邮件"))
                    },
                    onGithubClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
                        context.startActivity(intent)
                    },
                )
            }
        }
    }
}

// ---- 演示数据 ----

private fun sampleTasks(): List<TaskItem> = listOf(
    TaskItem("示例任务一", TaskStatus.SUCCESS, "2026-08-13 10:24", 1f),
    TaskItem("加固检测任务", TaskStatus.RUNNING, "运行中…", 0.65f),
    TaskItem("等待队列任务", TaskStatus.WAITING, "排队中…", 0f),
    TaskItem("失败任务", TaskStatus.FAIL, "2026-08-12 18:02", 0f),
)

private fun sampleApps(): List<AppItem> = listOf(
    AppItem("示例应用", "com.example.demo", "1.0.0", "12.4 MB", packingDetected = true),
    AppItem("测试程序", "armadillo.studio.test", "2.3.1", "28.0 MB", packingDetected = false),
    AppItem("加固样例", "com.sample.pack", "0.9.5", "5.6 MB", packingDetected = true),
)
