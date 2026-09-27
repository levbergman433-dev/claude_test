package com.maxrave.simpmusic.expect

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import org.koin.mp.KoinPlatform.getKoin

actual fun supportsLinkHandlingSettings(): Boolean = true

/**
 * Android 12+ has a page for exactly this ("Open by default" → "Add link"); older versions only
 * have the general app page, where the same switch lives under "Open by default".
 */
actual fun openLinkHandlingSettings() {
    val context: AppCompatActivity = getKoin().get()
    val packageUri = Uri.parse("package:${context.packageName}")
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, packageUri)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
        }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
