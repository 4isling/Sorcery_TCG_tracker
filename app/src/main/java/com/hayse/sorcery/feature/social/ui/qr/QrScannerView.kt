package com.hayse.sorcery.feature.social.ui.qr

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Aperçu caméra plein écran qui scanne les QR (CameraX + ML Kit) et remonte le texte brut via
 * [onScanned]. Aucun décodage métier ici : l'appelant valide le token.
 */
@Composable
fun QrScannerView(onScanned: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    val providerFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            executor.shutdown()
            scanner.close()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply { setAnalyzer(executor, QrAnalyzer(scanner, onScanned)) }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
    )
}

private class QrAnalyzer(
    private val scanner: BarcodeScanner,
    private val onScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val media = image.image
        if (media == null) {
            image.close()
            return
        }
        val input = InputImage.fromMediaImage(media, image.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onScanned)
            }
            .addOnCompleteListener { image.close() }
    }
}
