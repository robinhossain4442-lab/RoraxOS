package com.roraxos.linuxenv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roraxos.linuxenv.core.InstallStage
import com.roraxos.linuxenv.data.Tool
import com.roraxos.linuxenv.data.ToolRepository
import com.roraxos.linuxenv.ui.dashboard.DashboardScreen
import com.roraxos.linuxenv.ui.dashboard.VmStatus
import com.roraxos.linuxenv.ui.onboarding.*
import com.roraxos.linuxenv.ui.theme.RoraxOSTheme

/**
 * Wires together the onboarding flow, dashboard, and terminal screens.
 * This is a minimal nav graph intended as a starting point — hook up a
 * ViewModel per screen as the app grows instead of the local state used
 * here for demonstration.
 */
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
    const val INSTALL = "install"
    const val COMPLETE = "complete"
    const val DASHBOARD = "dashboard"
}

@Composable
private fun RoraxNavHost() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val toolRepository = remember { ToolRepository(context) }

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
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
            var selected by remember { mutableStateOf(Engine.PROOT_ROOTLESS) }
            EngineStep(
                selected = selected,
                onSelect = { selected = it },
                onContinue = { navController.navigate(Routes.INSTALL) }
            )
        }

        composable(Routes.INSTALL) {
            // In a full implementation, collect RootfsInstaller.install() here
            // via a ViewModel and drive these values from real progress events.
            InstallStep(
                currentStage = InstallStage.DONE,
                percent = 100,
                logLines = listOf("[ready] install pipeline wired up — connect RootfsInstaller")
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
            val tools = remember { toolRepository.loadTools() }
            var vmStatus by remember { mutableStateOf(VmStatus.STOPPED) }

            DashboardScreen(
                vmStatus = vmStatus,
                ramUsageMb = 0,
                tools = tools,
                recentActivity = emptyList(),
                onStartVm = { vmStatus = VmStatus.RUNNING },
                onStopVm = { vmStatus = VmStatus.STOPPED },
                onToolClick = { /* navigate to terminal screen with tool.command */ },
                onSettingsClick = { },
                onMenuClick = { }
            )
        }
    }
}
