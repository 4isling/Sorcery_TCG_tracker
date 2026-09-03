package com.hayse.sorcery.feature.social.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Permissions runtime requises par Nearby Connections, variables selon le niveau d'API :
 * - localisation fine jusqu'à Android 12L (API 32) inclus (dérivée du scan Bluetooth) ;
 * - permissions Bluetooth granulaires à partir d'Android 12 (API 31) ;
 * - `NEARBY_WIFI_DEVICES` à partir d'Android 13 (API 33).
 */
object NearbyPermissions {

    fun required(): List<String> = buildList {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    fun allGranted(context: Context): Boolean = required().all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
