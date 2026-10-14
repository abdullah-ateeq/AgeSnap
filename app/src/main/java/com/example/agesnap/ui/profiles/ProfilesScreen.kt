package com.example.agesnap.ui.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agesnap.data.model.AgeProfile
import com.example.agesnap.ui.profiles.components.ProfileBottomSheet
import com.example.agesnap.ui.profiles.components.ProfileCard
import com.example.agesnap.viewmodel.AgeSnapUiState
import com.example.agesnap.viewmodel.AgeSnapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    uiState: AgeSnapUiState,
    viewModel: AgeSnapViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<AgeProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<AgeProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Profiles",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${uiState.profiles.size} / 20",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (uiState.profiles.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (uiState.profiles.size >= 20) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile limit reached (maximum 20 profiles).")
                            }
                        } else {
                            editingProfile = null
                            showBottomSheet = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.semantics { contentDescription = "Add new profile" }
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                }
            }
        },
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
                if (uiState.profiles.isEmpty()) {
                    EmptyProfilesState(
                        onAddClicked = {
                            editingProfile = null
                            showBottomSheet = true
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.profiles, key = { it.id }) { profile ->
                            ProfileCard(
                                profile = profile,
                                onClick = {
                                    if (uiState.settings.hapticFeedback) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    viewModel.selectProfile(profile)
                                    onNavigateToHome()
                                },
                                onEditClick = {
                                    editingProfile = profile
                                    showBottomSheet = true
                                },
                                onDeleteClick = {
                                    if (uiState.settings.confirmProfileDelete) {
                                        profileToDelete = profile
                                    } else {
                                        viewModel.deleteProfile(profile.id)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Profile \"${profile.name}\" deleted.")
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add / Edit
    if (showBottomSheet) {
        ProfileBottomSheet(
            existingProfile = editingProfile,
            onDismiss = {
                showBottomSheet = false
                editingProfile = null
            },
            onSave = { name, birthDate ->
                showBottomSheet = false
                if (editingProfile != null) {
                    val success = viewModel.updateProfile(editingProfile!!.id, name, birthDate)
                    coroutineScope.launch {
                        if (success) snackbarHostState.showSnackbar("Profile updated.")
                    }
                } else {
                    val success = viewModel.saveProfile(name, birthDate)
                    coroutineScope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Profile \"$name\" saved.")
                        } else {
                            snackbarHostState.showSnackbar("Profile limit reached (maximum 20 profiles).")
                        }
                    }
                }
                editingProfile = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (profileToDelete != null) {
        val target = profileToDelete!!
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete this profile?") },
            text = { Text("Are you sure you want to delete \"${target.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(target.id)
                        profileToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Profile \"${target.name}\" deleted.")
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyProfilesState(
    onAddClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No saved profiles",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save a birth date to access it quickly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddClicked,
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ADD PROFILE")
        }
    }
}
