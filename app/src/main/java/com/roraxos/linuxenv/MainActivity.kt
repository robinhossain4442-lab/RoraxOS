package com.roraxos.linuxenv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.roraxos.linuxenv.core.AppPreferences
import com.roraxos.linuxenv.data.Tool
import com.roraxos.linuxenv.ui.dashboard.AppDrawerContent
import com.roraxos.linuxenv.ui.dashboard.DashboardScreen
import com.roraxos.linuxenv.ui.dashboard.DashboardViewModel
import com.roraxos.linuxenv.ui.onboarding.*
import com.roraxos.linuxenv.ui.settings.SettingsScreen
import com.roraxos.linuxenv.ui.terminal.TerminalScreen
import com.roraxos.linuxenv.ui.terminal.TerminalViewModel
import com.roraxos.linuxenv.ui.theme.RoraxOSTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoraxOSTheme {
                RoraxNavHost()
            }
        }
    }
}

private object Routes {
    const val WELCOME = "welcome"
    const val ENGINE = "engine"
    const val ROOTFS_URL = "rootfs_url"
    const val INSTALL = "install"
    const val COMPLETE = "complete"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val TERMINAL = "terminal/{toolId}"
    fun terminal(toolId: String) = "terminal/$toolId"
}

@Composable
private fun RoraxNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as RoraxApp
    val preferences = remember { app.preferences }

    val startDestination = if (preferences.onboardingComplete) Routes.DASHBOARD else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.WELCOME) {
            WelcomeStep(
                features = listOf(
                    "Local, non-root Linux sandbox",
                    "Configurable tool dashboard",
                    "Live terminal output for any command you add"
                ),
                onContinue = { navController.navigate(Routes.ENGINE) }
            )
        }

        composable(Routes.ENGINE) {
            var selected by remember {
                mutableStateOf(
                    if (preferences.engine == "CHROOT_ROOT") Engine.CHROOT_ROOT else Engine.PROOT_ROOTLESS
                )
            }
            EngineStep(
                selected = selected,
                onSelect = { selected = it },
                onContinue = {
                    preferences.engine = selected.name
                    navController.navigate(Routes.ROOTFS_URL)
                }
            )
        }

        composable(Routes.ROOTFS_URL) {
            RootfsUrlStep(
                initialUrl = preferences.rootfsUrl ?: "",
                onContinue = { url ->
                    preferences.rootfsUrl = url
                    navController.navigate(Routes.INSTALL)
                }
            )
        }

        composable(Routes.INSTALL) {
            val viewModel: InstallViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val finished by viewModel.finished.collectAsState()

            LaunchedEffect(Unit) { viewModel.startInstall() }
            LaunchedEffect(finished) {
                if (finished) navController.navigate(Routes.COMPLETE)
            }

            InstallStep(
                currentStage = uiState.stage,
                percent = uiState.percent,
                logLines = uiState.logLines,
                error = uiState.error
            )
        }

        composable(Routes.COMPLETE) {
            CompleteStep(onLaunch = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel()
            val vmStatus by viewModel.vmStatus.collectAsState()
            val ramUsage by viewModel.ramUsageMb.collectAsState()
            val recentActivity by viewModel.recentActivity.collectAsState()
            val toolsByCategory = remember { app.toolRepository.loadToolsByCategory() }

            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AppDrawerContent(
                        toolsByCategory = toolsByCategory,
                        onToolClick = { tool: Tool ->
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.terminal(tool.id))
                        },
                        onSettingsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.SETTINGS)
                        }
                    )
                }
            ) {
                DashboardScreen(
                    vmStatus = vmStatus,
                    ramUsageMb = ramUsage,
                    tools = viewModel.tools,
                    recentActivity = recentActivity,
                    onStartVm = { viewModel.startVm() },
                    onStopVm = { viewModel.stopVm() },
                    onToolClick = { tool -> navController.navigate(Routes.terminal(tool.id)) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onReinstall = {
                    navController.navigate(Routes.INSTALL) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.TERMINAL,
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId")
            val tool = remember(toolId) { app.toolRepository.loadTools().firstOrNull { it.id == toolId } }
            val viewModel: TerminalViewModel = viewModel()
            val lines by viewModel.lines.collectAsState()
            val isRunning by viewModel.isRunning.collectAsState()

            LaunchedEffect(tool?.id) {
                tool?.let { viewModel.run(it) }
            }

            TerminalScreen(
                toolName = tool?.name ?: "Terminal",
                lines = lines,
                isRunning = isRunning,
                onBack = { navController.popBackStack() },
                onStop = { viewModel.stop() }
            )
        }
    }
}
