package com.xaiworld.expensetracker.sync

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Runtime permissions Nearby Connections needs, which vary by Android version. */
object NearbyPermissions {

    fun required(): Array<String> {
        val permissions = mutableListOf(
            // Nearby Connections checks for COARSE specifically (not just FINE) before it
            // will start advertising/discovery — see error 8034,
            // MISSING_PERMISSION_ACCESS_COARSE_LOCATION. This holds on every Android version
            // this app supports: NEARBY_WIFI_DEVICES (added below on API 33+) does not
            // replace it in this version of the Nearby Connections library, despite Google's
            // general Android-13+ migration guidance suggesting it would.
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += android.Manifest.permission.BLUETOOTH_ADVERTISE
            permissions += android.Manifest.permission.BLUETOOTH_CONNECT
            permissions += android.Manifest.permission.BLUETOOTH_SCAN
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += android.Manifest.permission.NEARBY_WIFI_DEVICES
        }

        return permissions.toTypedArray()
    }

    fun allGranted(context: Context): Boolean = required().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
