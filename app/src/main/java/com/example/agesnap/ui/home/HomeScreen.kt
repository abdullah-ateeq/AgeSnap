package com.example.agesnap.ui.home

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agesnap.domain.AgeShareFormatter
import com.example.agesnap.ui.components.DatePickerModal
import com.example.agesnap.ui.home.components.AgeResultCard
import com.example.agesnap.ui.home.components.CalculationActionButtons
import com.example.agesnap.ui.home.components.DateSelectionCard
import com.example.agesnap.ui.home.components.NextBirthdayCard
import com.example.agesnap.ui.home.components.QuickProfilesRow
import com.example.agesnap.ui.home.components.RecentCalculationsCard
import com.example.agesnap.ui.home.components.SaveProfileDialog
import com.example.agesnap.ui.home.components.StatisticsCards
import com.example.agesnap.viewmodel.AgeSnapUiState
import com.example.agesnap.viewmodel.AgeSnapViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: AgeSnapUiState,
    viewModel: AgeSnapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showAsOfDatePicker by remember { mutableStateOf(false) }
    var showSaveProfileDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLandscape && maxWidth > 600.dp) {
                    // Landscape Two-Column Layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column: Inputs & Controls
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeaderSection()

                            DateSelectionCard(
                                birthDate = uiState.birthDate,
                                asOfDate = uiState.asOfDate,
                                onSelectBirthDateClicked = { showBirthDatePicker = true },
                                onSelectAsOfDateClicked = { showAsOfDatePicker = true },
                                onResetToTodayClicked = {
                                    if (uiState.settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.resetAsOfDateToToday()
                                }
                            )

                            if (uiState.errorMessage != null) {
                                ErrorBanner(message = uiState.errorMessage)
                            }

                            CalculateButton(
                                onClick = {
                                    if (uiState.settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.calculateAge()
                                }
                            )

                            QuickProfilesRow(
                                profiles = uiState.profiles,
                                selectedProfileId = uiState.selectedProfileId,
                                onProfileSelected = { profile ->
                                    if (uiState.settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.selectProfile(profile)
                                }
                            )
                        }

                        // Right Column: Result & Breakdown
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.ageResult != null && uiState.birthDate != null) {
                                AgeResultCard(
                                    birthDate = uiState.birthDate,
                                    asOfDate = uiState.asOfDate,
                                    result = uiState.ageResult,
                                    animationsEnabled = uiState.settings.animationsEnabled
                                )

                                CalculationActionButtons(
                                    onSaveProfileClicked = { showSaveProfileDialog = true },
                                    onCopyClicked = {
                                        val text = AgeShareFormatter.formatDetailedText(
                                            uiState.birthDate,
                                            uiState.asOfDate,
                                            uiState.ageResult
                                        )
                                        clipboardManager.setText(AnnotatedString(text))
                                        if (uiState.settings.hapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Age calculation copied to clipboard.")
                                        }
                                    },
                                    onShareClicked = {
                                        val text = AgeShareFormatter.formatDetailedText(
                                            uiState.birthDate,
                                            uiState.asOfDate,
                                            uiState.ageResult
                                        )
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, text)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Age Calculation"))
                                    },
                                    onResetClicked = {
                                        if (uiState.settings.hapticFeedback) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        viewModel.resetHome()
                                    }
                                )

                                NextBirthdayCard(result = uiState.ageResult)
                                StatisticsCards(result = uiState.ageResult)
                            } else {
                                EmptyStateCard()
                            }

                            RecentCalculationsCard(
                                history = uiState.history,
                                onHistoryItemClicked = { history ->
                                    if (uiState.settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.loadFromHistory(history)
                                }
                            )
                        }
                    }
                } else {
                    // Portrait Single-Column Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeaderSection()

                        DateSelectionCard(
                            birthDate = uiState.birthDate,
                            asOfDate = uiState.asOfDate,
                            onSelectBirthDateClicked = { showBirthDatePicker = true },
                            onSelectAsOfDateClicked = { showAsOfDatePicker = true },
                            onResetToTodayClicked = {
                                if (uiState.settings.hapticFeedback) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.resetAsOfDateToToday()
                            }
                        )

                        if (uiState.errorMessage != null) {
                            ErrorBanner(message = uiState.errorMessage)
                        }

                        CalculateButton(
                            onClick = {
                                if (uiState.settings.hapticFeedback) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.calculateAge()
                            }
                        )

                        // Result Section
                        AnimatedVisibility(
                            visible = uiState.ageResult != null && uiState.birthDate != null,
                            enter = if (uiState.settings.animationsEnabled) {
                                fadeIn() + slideInVertically { it / 3 }
                            } else {
                                fadeIn()
                            }
                        ) {
                            if (uiState.ageResult != null && uiState.birthDate != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    AgeResultCard(
                                        birthDate = uiState.birthDate,
                                        asOfDate = uiState.asOfDate,
                                        result = uiState.ageResult,
                                        animationsEnabled = uiState.settings.animationsEnabled
                                    )

                                    CalculationActionButtons(
                                        onSaveProfileClicked = { showSaveProfileDialog = true },
                                        onCopyClicked = {
                                            val text = AgeShareFormatter.formatDetailedText(
                                                uiState.birthDate,
                                                uiState.asOfDate,
                                                uiState.ageResult
                                            )
                                            clipboardManager.setText(AnnotatedString(text))
                                            if (uiState.settings.hapticFeedback) {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Age calculation copied to clipboard.")
                                            }
                                        },
                                        onShareClicked = {
                                            val text = AgeShareFormatter.formatDetailedText(
                                                uiState.birthDate,
                                                uiState.asOfDate,
                                                uiState.ageResult
                                            )
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, text)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Age Calculation"))
                                        },
                                        onResetClicked = {
                                            if (uiState.settings.hapticFeedback) {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                            viewModel.resetHome()
                                        }
                                    )

                                    NextBirthdayCard(result = uiState.ageResult)
                                    StatisticsCards(result = uiState.ageResult)
                                }
                            }
                        }

                        if (uiState.ageResult == null) {
                            EmptyStateCard()
                        }

                        QuickProfilesRow(
                            profiles = uiState.profiles,
                            selectedProfileId = uiState.selectedProfileId,
                            onProfileSelected = { profile ->
                                if (uiState.settings.hapticFeedback) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.selectProfile(profile)
                            }
                        )

                        RecentCalculationsCard(
                            history = uiState.history,
                            onHistoryItemClicked = { history ->
                                if (uiState.settings.hapticFeedback) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                viewModel.loadFromHistory(history)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Date Pickers Modals
    if (showBirthDatePicker) {
        DatePickerModal(
            initialDate = uiState.birthDate ?: LocalDate.of(2000, 1, 1),
            title = "Select Date of Birth",
            allowFutureDates = false,
            onDateSelected = { date ->
                viewModel.setBirthDate(date)
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    if (showAsOfDatePicker) {
        DatePickerModal(
            initialDate = uiState.asOfDate,
            title = "Calculate Age As Of",
            allowFutureDates = true,
            onDateSelected = { date ->
                viewModel.setAsOfDate(date)
            },
            onDismiss = { showAsOfDatePicker = false }
        )
    }

    // Save Profile Dialog
    if (showSaveProfileDialog && uiState.birthDate != null) {
        SaveProfileDialog(
            birthDate = uiState.birthDate,
            initialName = uiState.profileName,
            onDismiss = { showSaveProfileDialog = false },
            onSave = { name ->
                showSaveProfileDialog = false
                val success = viewModel.saveProfile(name, uiState.birthDate)
                coroutineScope.launch {
                    if (success) {
                        snackbarHostState.showSnackbar("Profile \"$name\" saved.")
                    } else {
                        snackbarHostState.showSnackbar("Profile limit reached (maximum 20 profiles).")
                    }
                }
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "AgeSnap",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Know your exact age.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalculateButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .semantics { contentDescription = "Calculate age" },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Calculate,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "CALCULATE AGE",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "CALCULATE YOUR AGE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a birth date to see your exact age.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
