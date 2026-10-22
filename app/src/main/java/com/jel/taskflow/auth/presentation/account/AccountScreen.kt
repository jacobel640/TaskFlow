package com.jel.taskflow.auth.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.auth.domain.model.AuthUser
import com.jel.taskflow.core.utils.flatColors
import com.jel.taskflow.core.utils.toRelativeTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: AccountViewModel = hiltViewModel(), navController: NavController) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val context = LocalContext.current
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AccountUiEvent.ShowMessage -> snackBarHostState.showSnackbar(resources.getString(event.messageRes))
            }
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(stringResource(R.string.delete_account_dialog_title)) },
            text = { Text(stringResource(R.string.delete_account_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    viewModel.onUiAction(AccountUiActions.OnDeleteAccount(context))
                }) {
                    Text(
                        text = stringResource(R.string.delete_account),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.flatColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading || uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            val user = uiState.user
            if (user == null) {
                GuestSection(
                    isLoading = uiState.isLoading,
                    onSignIn = { viewModel.onUiAction(AccountUiActions.OnSignInWithGoogle(context)) }
                )
            } else {
                SignedInSection(
                    user = user,
                    uiState = uiState,
                    onSyncNow = { viewModel.onUiAction(AccountUiActions.OnSyncNow) },
                    onSignOut = { viewModel.onUiAction(AccountUiActions.OnSignOut) },
                    onDeleteAccount = { showDeleteAccountDialog = true }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val uriHandler = LocalUriHandler.current
            val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
            TextButton(onClick = { uriHandler.openUri(privacyPolicyUrl) }) {
                Icon(imageVector = Icons.Rounded.Policy, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                Text(text = stringResource(R.string.privacy_policy))
            }
        }
    }
}

@Composable
private fun GuestSection(isLoading: Boolean, onSignIn: () -> Unit) {
    Icon(
        modifier = Modifier.size(72.dp),
        imageVector = Icons.Rounded.PhoneAndroid,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = null
    )
    Text(
        text = stringResource(R.string.guest_mode_title),
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        text = stringResource(R.string.guest_mode_desc),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        onClick = onSignIn
    ) {
        Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = stringResource(R.string.sign_in_for_backup))
    }
}

@Composable
private fun SignedInSection(
    user: AuthUser,
    uiState: AccountUiState,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val name = user.displayName?.takeIf { it.isNotBlank() } ?: user.email.orEmpty()
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    Text(text = name, style = MaterialTheme.typography.titleLarge)
    user.email?.let {
        Text(text = it, style = MaterialTheme.typography.bodyMedium)
    }

    val syncStatus = when {
        uiState.isSyncing -> stringResource(R.string.sync_in_progress)
        uiState.syncFailed -> stringResource(R.string.sync_failed)
        uiState.lastSyncTime != null -> stringResource(
            R.string.last_backup,
            Instant.fromEpochMilliseconds(uiState.lastSyncTime).toRelativeTime()
        )
        else -> stringResource(R.string.sync_pending)
    }
    Text(
        text = syncStatus,
        style = MaterialTheme.typography.bodySmall,
        color = if (uiState.syncFailed && !uiState.isSyncing) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant
    )

    AccountButton(
        icon = Icons.Rounded.CloudSync,
        text = stringResource(R.string.backup_now),
        enabled = !uiState.isLoading && !uiState.isSyncing,
        onClick = onSyncNow
    )
    AccountButton(
        icon = Icons.AutoMirrored.Rounded.Logout,
        text = stringResource(R.string.sign_out),
        enabled = !uiState.isLoading,
        onClick = onSignOut
    )
    Text(
        text = stringResource(R.string.sign_out_desc),
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center
    )
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        onClick = onDeleteAccount
    ) {
        Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = stringResource(R.string.delete_account))
    }
}

@Composable
private fun AccountButton(icon: ImageVector, text: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = text)
    }
}
