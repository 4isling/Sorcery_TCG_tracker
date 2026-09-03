package com.hayse.sorcery.feature.social.ui.nfc

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.hayse.sorcery.feature.social.data.p2p.nfc.NfcApdu
import java.io.IOException

private const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

/**
 * Active le reader mode NFC tant que ce composant est en composition (invité). À la détection d'un
 * tag HCE, lit le token via SELECT AID et le remonte brut via [onToken]. No-op si l'appareil n'a
 * pas de NFC. Le décodage/validation du token est laissé à l'appelant.
 */
@Composable
fun NfcReader(onToken: (String) -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val adapter = remember(context) { NfcAdapter.getDefaultAdapter(context) }
    val currentOnToken by rememberUpdatedState(onToken)

    DisposableEffect(adapter, activity) {
        if (adapter == null || activity == null) {
            onDispose { }
        } else {
            val callback = NfcAdapter.ReaderCallback { tag -> readToken(tag)?.let(currentOnToken) }
            adapter.enableReaderMode(activity, callback, READER_FLAGS, null)
            onDispose { adapter.disableReaderMode(activity) }
        }
    }
}

private fun readToken(tag: Tag): String? {
    val isoDep = IsoDep.get(tag) ?: return null
    return try {
        isoDep.connect()
        NfcApdu.parseTokenResponse(isoDep.transceive(NfcApdu.selectAidCommand()))
    } catch (e: IOException) {
        null
    } finally {
        runCatching { isoDep.close() }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
