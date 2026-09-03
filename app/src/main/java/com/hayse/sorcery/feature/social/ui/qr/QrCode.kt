package com.hayse.sorcery.feature.social.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private const val BLACK = 0xFF000000.toInt()
private const val WHITE = 0xFFFFFFFF.toInt()

/** Affiche [content] sous forme de QR carré (aucune donnée d'échange : seulement le token de session). */
@Composable
fun QrCodeImage(content: String, modifier: Modifier = Modifier, size: Int = 512) {
    val bitmap = remember(content, size) { qrImageBitmap(content, size) }
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
}

private fun qrImageBitmap(content: String, size: Int): ImageBitmap {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val offset = y * size
        for (x in 0 until size) {
            pixels[offset + x] = if (matrix[x, y]) BLACK else WHITE
        }
    }
    return createBitmap(size, size).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }.asImageBitmap()
}
