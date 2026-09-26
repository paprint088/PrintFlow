package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.NotificationHelper
import com.example.ui.PrintViewModel
import com.example.ui.components.GoogleAuthDialog
import com.example.ui.components.JobApprovalDialog
import com.example.ui.components.SimulateRequestDialog
import com.example.ui.screens.CostMatrixScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomeDashboardScreen
import com.example.ui.screens.JobsLogScreen
import com.example.ui.screens.PrintersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald
import kotlinx.coroutines.delay

enum class AppTab(val title: String, val testTag: String) {
    HOME("Monitor", "tab_home"),
    LOGS("Audit Log", "tab_logs"),
    COST_MATRIX("Cost Matrix", "tab_matrix"),
    PRINTERS("Printers", "tab_printers"),
    INCOME("Income", "tab_income")
}

class MainActivity : ComponentActivity() {
    private var mainViewModel: PrintViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val vm: PrintViewModel = viewModel()
            mainViewModel = vm
            MyApplicationTheme {
                PrintFlowApp(viewModel = vm)
            }
        }
        handleOAuthRedirect(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthRedirect(intent)
    }

    private fun handleOAuthRedirect(incomingIntent: Intent?) {
        val uri = incomingIntent?.data ?: return
        val uriStr = uri.toString()
        if (uriStr.contains("access_token=")) {
            val regex = Regex("access_token=([^&]+)")
            val match = regex.find(uriStr)
            val token = match?.groupValues?.get(1)
            if (!token.isNullOrBlank()) {
                mainViewModel?.updateOAuthToken(token)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintFlowApp(viewModel: PrintViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showSimulateDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }

    // State collections
    val pendingJobs by viewModel.pendingJobs.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val costMatrix by viewModel.costMatrixState.collectAsStateWithLifecycle()
    val printers by viewModel.printersState.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncomeState.collectAsStateWithLifecycle()
    val isMonitoring by viewModel.isMonitoring.collectAsStateWithLifecycle()
    val monitoredEmail by viewModel.monitoredEmail.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isCheckingGmail by viewModel.isCheckingGmail.collectAsStateWithLifecycle()
    val activeJobForModal by viewModel.activeJobForModal.collectAsStateWithLifecycle()
    val bannerAlert by viewModel.bannerAlert.collectAsStateWithLifecycle()

    // Handle back button on sub-tabs
    if (currentTab != AppTab.HOME) {
        BackHandler {
            currentTab = AppTab.HOME
        }
    }

    // Auto-dismiss banner after 4.5 seconds
    LaunchedEffect(bannerAlert) {
        if (bannerAlert != null) {
            delay(4500)
            viewModel.setBanner(null)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PrintFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isMonitoring) PrintEmerald.copy(alpha = 0.2f) else PrintAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isMonitoring) "ONLINE" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMonitoring) PrintEmerald else PrintAmber
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSimulateDialog = true },
                        modifier = Modifier.testTag("topbar_sim_button")
                    ) {
                        Icon(Icons.Default.Science, contentDescription = "Test Scenarios")
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("topbar_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Gmail Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Monitor") },
                    label = { Text("Monitor") },
                    modifier = Modifier.testTag(AppTab.HOME.testTag)
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.LOGS,
                    onClick = { currentTab = AppTab.LOGS },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Audit Log") },
                    label = { Text("Log (${allJobs.size})") },
                    modifier = Modifier.testTag(AppTab.LOGS.testTag)
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.COST_MATRIX,
                    onClick = { currentTab = AppTab.COST_MATRIX },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Cost Matrix") },
                    label = { Text("Matrix") },
                    modifier = Modifier.testTag(AppTab.COST_MATRIX.testTag)
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.PRINTERS,
                    onClick = { currentTab = AppTab.PRINTERS },
                    icon = { Icon(Icons.Default.Print, contentDescription = "Printers") },
                    label = { Text("Printers") },
                    modifier = Modifier.testTag(AppTab.PRINTERS.testTag)
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.INCOME,
                    onClick = { currentTab = AppTab.INCOME },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Income") },
                    label = { Text("Income") },
                    modifier = Modifier.testTag(AppTab.INCOME.testTag)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main tab content
            when (currentTab) {
                AppTab.HOME -> HomeScreen(
                    isMonitoring = isMonitoring,
                    isCheckingGmail = isCheckingGmail,
                    monitoredEmail = monitoredEmail,
                    connectionStatus = connectionStatus,
                    pendingJobs = pendingJobs,
                    recentJobs = allJobs,
                    costMatrix = costMatrix,
                    activePrinters = printers,
                    totalIncome = totalIncome ?: 0.0,
                    onToggleMonitoring = { viewModel.toggleMonitoring(it) },
                    onCheckGmailNow = { viewModel.checkGmailNow() },
                    onConnectAccount = { showGoogleAuthDialog = true },
                    onOpenSimulation = { showSimulateDialog = true },
                    onReviewJob = { viewModel.openJobModal(it) },
                    onNavigateToLogs = { currentTab = AppTab.LOGS }
                )

                AppTab.LOGS -> JobsLogScreen(
                    jobs = allJobs,
                    costMatrix = costMatrix,
                    onDeleteJob = { viewModel.deleteJob(it) },
                    onClearAllLogs = { viewModel.clearAllLogs() },
                    onReviewPendingJob = { viewModel.openJobModal(it) }
                )

                AppTab.COST_MATRIX -> CostMatrixScreen(
                    currentMatrix = costMatrix,
                    onSaveMatrix = { viewModel.saveCostMatrix(it) }
                )

                AppTab.PRINTERS -> PrintersScreen(
                    printers = printers,
                    onAddPrinter = { viewModel.addPrinter(it) },
                    onUpdatePrinter = { viewModel.updatePrinter(it) },
                    onDeletePrinter = { viewModel.deletePrinter(it) }
                )

                AppTab.INCOME -> IncomeDashboardScreen(
                    jobs = allJobs,
                    costMatrix = costMatrix
                )
            }

            // In-app alert banner floating overlay
            AnimatedVisibility(
                visible = bannerAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bannerAlert ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.setBanner(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Review & Approval Dialog (Crucial workflow requirement)
    activeJobForModal?.let { job ->
        JobApprovalDialog(
            job = job,
            printers = printers,
            costMatrix = costMatrix,
            onDismiss = { viewModel.closeJobModal() },
            onUpdateSettings = { printerId, printerName, paperSize, colorMode, copies, isDuplex, customCost ->
                viewModel.updateJobSettings(
                    jobId = job.id,
                    printerId = printerId,
                    printerName = printerName,
                    paperSize = paperSize,
                    colorMode = colorMode,
                    copies = copies,
                    isDuplex = isDuplex,
                    customCost = customCost
                )
            },
            onApprove = { approvedJob ->
                viewModel.approveAndDispatchJob(approvedJob, context)
            }
        )
    }

    // Simulation Trigger Sheet
    if (showSimulateDialog) {
        SimulateRequestDialog(
            onDismiss = { showSimulateDialog = false },
            onSelectScenario = { scenario ->
                viewModel.simulateTestIncomingEmail(scenario)
            }
        )
    }

    // Settings & Account Dialog
    if (showSettingsDialog) {
        var emailInput by remember { mutableStateOf(viewModel.gmailService.userEmailAddress) }
        var tokenInput by remember { mutableStateOf(viewModel.gmailService.oauthToken ?: "") }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Gmail Monitor Settings") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Monitored Gmail Account",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "OAuth Access Token (Optional Manual Sync)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Bearer Access Token") },
                        placeholder = { Text("ya29....") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Text(
                        "Leave blank to run in autonomous simulation mode with real print dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            showGoogleAuthDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Google ($emailInput)")
                    }

                    OutlinedButton(
                        onClick = { viewModel.verifyConnection() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection Status")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMonitoredEmail(emailInput.trim())
                        if (tokenInput.isNotBlank()) {
                            viewModel.updateOAuthToken(tokenInput.trim())
                        }
                        showSettingsDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Sign-In WebView Dialog
    if (showGoogleAuthDialog) {
        GoogleAuthDialog(
            authUrl = viewModel.gmailService.buildOAuthUrl(),
            targetEmail = monitoredEmail,
            onDismiss = { showGoogleAuthDialog = false },
            onTokenReceived = { token ->
                viewModel.updateOAuthToken(token)
                showGoogleAuthDialog = false
            }
        )
    }
}
