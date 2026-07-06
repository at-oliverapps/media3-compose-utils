package io.oliverapps.media3.compose.shared.ui

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

/*

/**
 * Returns whether the app is running on a TV device.
 *
 * @param context Any context.
 * @return Whether the app is running on a TV device.
 */
val Context.isTv: Boolean
    get() {
        val uiModeManager = this.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager?
        return uiModeManager != null && uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

/**
 * Returns whether the app is running on an automotive device.
 *
 * @param context Any context.
 * @return Whether the app is running on an automotive device.
 */
val Context.isAutomotive: Boolean
    get() = this.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

/**
 * Returns whether the app is running on a Wear OS device.
 *
 * @param context Any context.
 * @return Whether the app is running on a Wear OS device.
 */
val Context.isWear: Boolean
    get() = this.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

*/
