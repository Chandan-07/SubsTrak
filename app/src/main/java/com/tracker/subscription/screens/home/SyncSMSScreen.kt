package com.tracker.subscription.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.navigation.NavController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.tracker.subscription.R
import com.tracker.subscription.data.ParsedSubscription
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val manropeRegular = FontFamily(Font(R.font.manrope_regular))
private val manropeMedium = FontFamily(Font(R.font.manrope_medium))
private val manropeSemiBold = FontFamily(Font(R.font.manrope_semi_bold))
private val manropeBold = FontFamily(Font(R.font.manrope_bold))
private val manropeExtraBold = FontFamily(Font(R.font.manrope_extra_bold))

private enum class SubscriptionSyncSource {
    Sms,
    Email
}

@Composable
fun EmptySubscriptionSyncPrompt(
    viewModel: DashboardViewModel,
    navController: NavController,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val smsSuggestions by viewModel.smsSyncState.collectAsState()
    val isLoadingSms by viewModel.isLoadingSMS.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val (isPremium, currentSubCount) = remember(uiState) {
        val successData = (uiState as? DashboardUiState.Success)?.data
        val isPrem = successData?.user?.isPremium == true
        val count = successData?.subscriptions?.size ?: 0
        Pair(isPrem, count)
    }

    var showPermissionPrompt by remember { mutableStateOf(true) }
    var showResultsDialog by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var scanRequested by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            scanRequested = true
            viewModel.scanSms()
        } else {
            permissionDenied = true
            showPermissionPrompt = false
        }
    }

    LaunchedEffect(isLoadingSms, smsSuggestions) {
        if (scanRequested && !isLoadingSms) {
            scanRequested = false
            showResultsDialog = true
        }
    }

    if (showPermissionPrompt) {
        SmsPermissionDialog(
            onDismiss = { showPermissionPrompt = false },
            onAllow = {
                showPermissionPrompt = false
                scanRequested = true
                startSmsSync(
                    context = context,
                    viewModel = viewModel,
                    onPermissionNeeded = {
                        scanRequested = false
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                )
            },
            isDarkTheme = isDarkTheme
        )
    }

    if (permissionDenied) {
        PermissionDeniedDialog(
            onDismiss = { permissionDenied = false },
            isDarkTheme = isDarkTheme
        )
    }

    if (isLoadingSms) {
        SmsSyncLoadingDialog(isDarkTheme = isDarkTheme)
    }

    if (showResultsDialog) {
        SmsSyncResultsDialog(
            suggestions = smsSuggestions,
            isPremium = isPremium,
            currentSubCount = currentSubCount,
            onDismiss = {
                showResultsDialog = false
                viewModel.clearSmsSuggestions()
            },
            onAddSuggestions = { suggestionsToAdd ->
                viewModel.addSmsSuggestionsToSubscriptions(suggestionsToAdd)
            },
            onNavigateToPremium = {
                showResultsDialog = false
                navController.navigate("premium")
            },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun SmsSyncDashboardBanner(
    viewModel: DashboardViewModel,
    navController: NavController,
    isDarkTheme: Boolean,
    externalTriggerSignal: Int = 0,
    onDismissBanner: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val smsSuggestions by viewModel.smsSyncState.collectAsState()
    val isLoadingSms by viewModel.isLoadingSMS.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val (isPremium, currentSubCount) = remember(uiState) {
        val successData = (uiState as? DashboardUiState.Success)?.data
        val isPrem = successData?.user?.isPremium == true
        val count = successData?.subscriptions?.size ?: 0
        Pair(isPrem, count)
    }

    var showResultsDialog by remember { mutableStateOf(false) }
    var scanRequested by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var showPermissionPrompt by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            scanRequested = true
            viewModel.scanSms()
        } else {
            permissionDenied = true
        }
    }

    LaunchedEffect(externalTriggerSignal) {
        if (externalTriggerSignal > 0) {
            isDismissed = false
            if (smsSuggestions.isNotEmpty()) {
                showResultsDialog = true
            } else {
                scanRequested = true
                startSmsSync(
                    context = context,
                    viewModel = viewModel,
                    onPermissionNeeded = {
                        scanRequested = false
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                )
            }
        }
    }

    LaunchedEffect(isLoadingSms, smsSuggestions) {
        if (scanRequested && !isLoadingSms) {
            scanRequested = false
            if (smsSuggestions.isNotEmpty()) {
                showResultsDialog = true
            }
        }
    }

    if (isLoadingSms) {
        SmsSyncLoadingDialog(isDarkTheme = isDarkTheme)
    }

    if (permissionDenied) {
        PermissionDeniedDialog(
            onDismiss = { permissionDenied = false },
            isDarkTheme = isDarkTheme
        )
    }

    if (showPermissionPrompt) {
        SmsPermissionDialog(
            onDismiss = { showPermissionPrompt = false },
            onAllow = {
                showPermissionPrompt = false
                scanRequested = true
                startSmsSync(
                    context = context,
                    viewModel = viewModel,
                    onPermissionNeeded = {
                        scanRequested = false
                        smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                    }
                )
            },
            isDarkTheme = isDarkTheme
        )
    }

    if (showResultsDialog) {
        SmsSyncResultsDialog(
            suggestions = smsSuggestions,
            isPremium = isPremium,
            currentSubCount = currentSubCount,
            onDismiss = {
                showResultsDialog = false
            },
            onAddSuggestions = { suggestionsToAdd ->
                viewModel.addSmsSuggestionsToSubscriptions(suggestionsToAdd)
            },
            onNavigateToPremium = {
                showResultsDialog = false
                navController.navigate("premium")
            },
            onRefreshSync = {
                viewModel.scanSms()
            },
            isDarkTheme = isDarkTheme
        )
    }

    if (isDismissed) return

    val pendingCount = smsSuggestions.size

    Spacer(modifier = Modifier.height(12.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable {
                if (pendingCount > 0) {
                    showResultsDialog = true
                } else {
                    scanRequested = true
                    startSmsSync(
                        context = context,
                        viewModel = viewModel,
                        onPermissionNeeded = {
                            scanRequested = false
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                        }
                    )
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFEBF3FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sync),
                        contentDescription = null,
                        tint = ThemeColors.getPrimaryColor(isDarkTheme),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (pendingCount > 0) "$pendingCount Subs" else "Sync SMS",
                            fontFamily = manropeBold,
                            fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        )
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detected",
                                fontSize = 12.sp,
                                fontFamily = manropeBold,
                                color = ThemeColors.getRedColor(isDarkTheme)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "track subscriptions automatically",
                        fontSize = 12.sp,
                        fontFamily = manropeMedium,
                        color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.72f),
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        if (pendingCount > 0) {
                            showResultsDialog = true
                        } else {
                            scanRequested = true
                            startSmsSync(
                                context = context,
                                viewModel = viewModel,
                                onPermissionNeeded = {
                                    scanRequested = false
                                    smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.getPrimaryColor(isDarkTheme)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (pendingCount > 0) "Add" else "Sync",
                        fontFamily = manropeBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = {
                        onDismissBanner?.invoke()
                        isDismissed = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun startSmsSync(
    context: Context,
    viewModel: DashboardViewModel,
    onPermissionNeeded: () -> Unit
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        viewModel.scanSms()
    } else {
        onPermissionNeeded()
    }
}

@Composable
private fun SmsPermissionDialog(
    onDismiss: () -> Unit,
    onAllow: () -> Unit,
    isDarkTheme: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Find subscriptions automatically?",
                fontFamily = manropeSemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "We can scan recent SMS messages on this device to spot subscription renewals. This stays on your phone and only detected subscriptions are shown for review.",
                    fontFamily = manropeMedium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                Spacer(modifier = Modifier.height(14.dp))
                SyncSourceRow(
                    title = "Sync SMS",
                    subtitle = "Available now",
                    enabled = true,
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.height(8.dp))
                SyncSourceRow(
                    title = "Sync Email",
                    subtitle = "Coming later",
                    enabled = false,
                    isDarkTheme = isDarkTheme
                )
            }
        },
        confirmButton = {
            Button(onClick = onAllow) {
                Text("Allow SMS scan", fontFamily = manropeSemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now", fontFamily = manropeMedium)
            }
        }
    )
}

@Composable
private fun SyncSourceRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    isDarkTheme: Boolean
) {
    val alpha = if (enabled) 1f else 0.52f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getBlueBgColor(isDarkTheme).copy(alpha = alpha)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.sync),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    fontFamily = manropeSemiBold,
                    color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = alpha)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontFamily = manropeMedium,
                    color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = alpha)
                )
            }
        }
    }
}

@Composable
private fun SmsSyncLoadingDialog(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "sms_sync")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sms_sync_pulse"
    )

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(ThemeColors.getBackgroundColor(isDarkTheme))
                .padding(horizontal = 28.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.18f))
                )
                CircularProgressIndicator(color = ThemeColors.getPrimaryColor(isDarkTheme))
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Scanning recent SMS",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = manropeSemiBold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Looking for renewal messages and payment alerts.",
                textAlign = TextAlign.Center,
                fontFamily = manropeMedium,
                color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun SmsSyncResultsDialog(
    suggestions: List<ParsedSubscription>,
    isPremium: Boolean,
    currentSubCount: Int,
    onDismiss: () -> Unit,
    onAddSuggestions: (List<ParsedSubscription>) -> Unit,
    onNavigateToPremium: () -> Unit,
    onRefreshSync: (() -> Unit)? = null,
    isDarkTheme: Boolean
) {
    var reviewSuggestions by remember(suggestions) { mutableStateOf(suggestions) }
    var selectedSuggestions by remember(suggestions) { mutableStateOf<List<ParsedSubscription>>(emptyList()) }
    var pendingSummary by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    if (pendingSummary != null) {
        val (addedCount, pendingCount) = pendingSummary!!
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Subscription Limit Reached",
                    fontFamily = manropeSemiBold
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ThemeColors.getBlueBgColor(isDarkTheme))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$addedCount",
                                fontFamily = manropeExtraBold,
                                fontSize = 22.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Added",
                                fontSize = 12.sp,
                                fontFamily = manropeMedium,
                                color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 32.dp)
                                .background(ThemeColors.getLightGreyColor(isDarkTheme))
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$pendingCount",
                                fontFamily = manropeExtraBold,
                                fontSize = 22.sp,
                                color = ThemeColors.getOrangeColor(isDarkTheme)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pending",
                                fontSize = 12.sp,
                                fontFamily = manropeMedium,
                                color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Free accounts can track up to 5 subscriptions. Upgrade to Premium to add all $pendingCount pending subscriptions!",
                        fontFamily = manropeMedium,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onNavigateToPremium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.getPrimaryColor(isDarkTheme)
                    )
                ) {
                    Text("Unlock Premium", fontFamily = manropeSemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Done", fontFamily = manropeMedium)
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Potential Subscriptions",
                        fontFamily = manropeBold,
                        fontSize = 18.sp,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        modifier = Modifier.weight(1f)
                    )
                    if (onRefreshSync != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.12f))
                                .border(1.dp, ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.25f), CircleShape)
                                .clickable { onRefreshSync() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh sync",
                                tint = ThemeColors.getPrimaryColor(isDarkTheme),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ThemeColors.getBlueBgColor(isDarkTheme))
                            .border(1.dp, ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Found: ",
                                fontSize = 11.sp,
                                fontFamily = manropeMedium,
                                color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${reviewSuggestions.size}",
                                fontSize = 12.sp,
                                fontFamily = manropeExtraBold,
                                color = ThemeColors.getPrimaryColor(isDarkTheme)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedSuggestions.isNotEmpty()) Color(0xFFE8F5E9) else ThemeColors.getBlueBgColor(isDarkTheme)
                            )
                            .border(
                                1.dp,
                                if (selectedSuggestions.isNotEmpty()) Color(0xFF81C784) else ThemeColors.getTextGreyColor(isDarkTheme).copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Selected: ",
                                fontSize = 11.sp,
                                fontFamily = manropeMedium,
                                color = if (selectedSuggestions.isNotEmpty()) Color(0xFF2E7D32) else ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${selectedSuggestions.size}",
                                fontSize = 12.sp,
                                fontFamily = manropeExtraBold,
                                color = if (selectedSuggestions.isNotEmpty()) Color(0xFF2E7D32) else ThemeColors.getTextColor(isDarkTheme)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column {
                if (reviewSuggestions.isEmpty()) {
                    Text(
                        text = if (suggestions.isEmpty()) {
                            "Nothing importable showed up this time."
                        } else {
                            "All suggestions have been handled."
                        },
                        fontFamily = manropeMedium,
                        color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.72f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reviewSuggestions) { suggestion ->
                            SmsSuggestionItem(
                                suggestion = suggestion,
                                isDarkTheme = isDarkTheme,
                                isSelected = selectedSuggestions.contains(suggestion),
                                onRemove = {
                                    reviewSuggestions = reviewSuggestions - suggestion
                                    selectedSuggestions = selectedSuggestions - suggestion
                                },
                                onAdd = {
                                    selectedSuggestions =
                                        if (selectedSuggestions.contains(suggestion)) {
                                            selectedSuggestions - suggestion
                                        } else {
                                            selectedSuggestions + suggestion
                                        }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (reviewSuggestions.isEmpty()) {
                Button(onClick = onDismiss) {
                    Text("Done", fontFamily = manropeSemiBold)
                }
            } else {
                Button(
                    enabled = selectedSuggestions.isNotEmpty(),
                    onClick = {
                        if (isPremium) {
                            onAddSuggestions(selectedSuggestions)
                            onDismiss()
                        } else {
                            val remainingCapacity = maxOf(0, 5 - currentSubCount)
                            if (selectedSuggestions.size <= remainingCapacity) {
                                onAddSuggestions(selectedSuggestions)
                                onDismiss()
                            } else {
                                val added = selectedSuggestions.take(remainingCapacity)
                                val pendingCount = selectedSuggestions.size - remainingCapacity
                                if (added.isNotEmpty()) {
                                    onAddSuggestions(added)
                                }
                                pendingSummary = Pair(added.size, pendingCount)
                            }
                        }
                    }
                ) {
                    Text("Add to subscription list", fontFamily = manropeSemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", fontFamily = manropeMedium)
            }
        }
    )
}

@Composable
private fun SmsSuggestionItem(
    suggestion: ParsedSubscription,
    isDarkTheme: Boolean,
    isSelected: Boolean,
    onRemove: () -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFFE8F5E9)
            } else {
                ThemeColors.getBlueBgColor(isDarkTheme)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.service,
                        fontFamily = manropeBold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spent on ${formatSpentDate(suggestion.date)}",
                        fontSize = 12.sp,
                        fontFamily = manropeMedium,
                        color = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.68f)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove suggestion",
                        tint = ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.62f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${suggestion.currency}${suggestion.amount}",
                    fontFamily = manropeSemiBold,
                    color = ThemeColors.getDarkBlueColor(isDarkTheme),
                    fontSize = 18.sp
                )
                TextButton(onClick = onAdd) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF2E7D32) else Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (isSelected) "Added" else "Add",
                        color = if (isSelected) Color(0xFF2E7D32) else Color.Unspecified,
                        fontFamily = if (isSelected) manropeSemiBold else manropeMedium
                    )
                }
            }
        }
    }
}

private fun formatSpentDate(date: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(date))
}

@Composable
private fun PermissionDeniedDialog(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SMS permission skipped", fontFamily = manropeSemiBold) },
        text = {
            Text(
                text = "No problem. You can still add subscriptions manually whenever you like.",
                fontFamily = manropeMedium,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeColors.getPrimaryColor(isDarkTheme)
                )
            ) {
                Text("Okay", fontFamily = manropeSemiBold)
            }
        }
    )
}
