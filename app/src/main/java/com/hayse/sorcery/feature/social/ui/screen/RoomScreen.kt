package com.hayse.sorcery.feature.social.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.feature.social.data.p2p.nfc.HceTokenHolder
import com.hayse.sorcery.feature.social.domain.p2p.PairingRole
import com.hayse.sorcery.feature.social.ui.nfc.NfcReader
import com.hayse.sorcery.feature.social.ui.permission.NearbyPermissions
import com.hayse.sorcery.feature.social.ui.qr.QrCodeImage
import com.hayse.sorcery.feature.social.ui.qr.QrScannerView
import com.hayse.sorcery.feature.social.ui.screen.composable.RoomInterior
import com.hayse.sorcery.feature.social.ui.viewmodel.RoomViewModel
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomFailure
import com.hayse.sorcery.feature.social.ui.viewmodel.state.RoomPhase
import org.koin.androidx.compose.koinViewModel

@Composable
fun RoomScreen(
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.phase) {
        RoomPhase.InRoom -> RoomInterior(
            state = state,
            onCardClick = onCardClick,
            onTabChange = viewModel::setTab,
            onSendChat = viewModel::sendChat,
            onShareCollection = viewModel::shareCollection,
            onShareLists = viewModel::shareLists,
            onShareDeck = viewModel::shareDeck,
            onSavePeerDeck = viewModel::savePeerDeck,
            onPropose = viewModel::proposeTrade,
            onSaveComposed = viewModel::saveComposedForLater,
            onRespondOffer = viewModel::respondOffer,
            onSaveIncoming = viewModel::saveIncomingForLater,
            onLeave = viewModel::leaveRoom,
            modifier = modifier,
        )

        RoomPhase.Connecting -> ConnectingView(onCancel = viewModel::leaveRoom, modifier = modifier)

        RoomPhase.Failed -> FailedView(
            failure = state.failure,
            onRetry = viewModel::startSession,
            onLeave = viewModel::leaveRoom,
            modifier = modifier,
        )

        RoomPhase.Pairing -> PairingView(viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun ConnectingView(onCancel: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxSize().padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.room_connecting), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onCancel) { Text(stringResource(R.string.pairing_cancel)) }
    }
}

@Composable
private fun FailedView(
    failure: RoomFailure?,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val message = when (failure) {
        RoomFailure.INCOMPATIBLE_VERSION -> stringResource(R.string.room_error_version)
        else -> stringResource(R.string.room_error_transport)
    }
    Column(
        modifier = modifier.fillMaxSize().padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Button(onClick = onRetry) { Text(stringResource(R.string.room_retry)) }
            OutlinedButton(onClick = onLeave) { Text(stringResource(R.string.room_leave)) }
        }
    }
}

// --- Phase d'appairage (reprise de l'ancien PairingScreen) --------------------

@Composable
private fun PairingView(viewModel: RoomViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    val nfcAvailable = remember { NfcAdapter.getDefaultAdapter(context) != null }
    val permissions = remember { NearbyPermissions.required().toTypedArray() }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.onPermissionsResult(NearbyPermissions.allGranted(context)) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.openScanner() }

    LaunchedEffect(Unit) { viewModel.onPermissionsResult(NearbyPermissions.allGranted(context)) }

    if (state.scanning) {
        QrScannerOverlay(onScanned = viewModel::onQrScanned, onClose = viewModel::closeScanner)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = stringResource(R.string.room_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoleSelector(role = state.role, onHost = viewModel::chooseHost, onGuest = viewModel::chooseGuest)

        when (state.role) {
            PairingRole.HOST -> {
                HostCodeCard(code = state.hostCode.orEmpty(), qrPayload = state.hostQrPayload)
                if (nfcAvailable) {
                    HostNfcPublisher(payload = state.hostQrPayload)
                    NfcHint()
                }
            }
            PairingRole.GUEST -> {
                GuestCodeInput(
                    code = state.guestCodeInput,
                    onCodeChange = viewModel::onGuestCodeChange,
                    onScanClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) viewModel.openScanner() else cameraLauncher.launch(Manifest.permission.CAMERA)
                    },
                )
                if (nfcAvailable) {
                    NfcReader(onToken = viewModel::onNfcToken)
                    NfcHint()
                }
            }
            null -> Unit
        }

        if (state.role != null && !state.permissionsGranted) {
            PermissionCard(onGrant = { launcher.launch(permissions) })
        }

        Button(
            onClick = viewModel::startSession,
            enabled = state.canStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.room_open))
        }
    }
}

@Composable
private fun RoleSelector(role: PairingRole?, onHost: () -> Unit, onGuest: () -> Unit) {
    val spacing = LocalSpacing.current
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        RoleButton(
            selected = role == PairingRole.HOST,
            label = stringResource(R.string.pairing_host),
            onClick = onHost,
            modifier = Modifier.weight(1f),
        )
        RoleButton(
            selected = role == PairingRole.GUEST,
            label = stringResource(R.string.pairing_guest),
            onClick = onGuest,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RoleButton(selected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

@Composable
private fun HostCodeCard(code: String, qrPayload: String?) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.pairing_your_code),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = code, style = MaterialTheme.typography.displaySmall)
            if (qrPayload != null) {
                Text(
                    text = stringResource(R.string.pairing_show_qr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QrCodeImage(
                    content = qrPayload,
                    modifier = Modifier.fillMaxWidth(fraction = 0.6f).aspectRatio(1f),
                )
            }
        }
    }
}

@Composable
private fun GuestCodeInput(code: String, onCodeChange: (String) -> Unit, onScanClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text(stringResource(R.string.pairing_enter_code)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onScanClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
            Text(
                text = stringResource(R.string.pairing_scan_qr),
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
    }
}

/** Diffuse le token de l'hôte via HCE tant que l'écran hôte est affiché. */
@Composable
private fun HostNfcPublisher(payload: String?) {
    DisposableEffect(payload) {
        HceTokenHolder.token = payload
        onDispose { HceTokenHolder.token = null }
    }
}

@Composable
private fun NfcHint() {
    Text(
        text = stringResource(R.string.pairing_nfc_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun QrScannerOverlay(onScanned: (String) -> Unit, onClose: () -> Unit) {
    val spacing = LocalSpacing.current
    Box(modifier = Modifier.fillMaxSize()) {
        QrScannerView(onScanned = onScanned, modifier = Modifier.fillMaxSize())
        Text(
            text = stringResource(R.string.pairing_scan_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.TopCenter).padding(spacing.md),
        )
        FilledTonalButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.BottomCenter).padding(spacing.lg),
        ) {
            Text(stringResource(R.string.pairing_close))
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.pairing_permissions_rationale),
                style = MaterialTheme.typography.bodyMedium,
            )
            FilledTonalButton(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pairing_grant))
            }
        }
    }
}
