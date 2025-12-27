package com.recodex.miuisettings.infra

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.recodex.miuisettings.domain.model.SettingTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TargetAvailabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageVisibilityHelper: PackageVisibilityHelper
) {
    fun isLaunchable(target: SettingTarget): Boolean {
        val intent = buildIntent(target) ?: return false

        if (target.packageName.isNotBlank() && !packageVisibilityHelper.isPackageVisible(target.packageName)) {
            return false
        }

        val packageManager = context.packageManager
        val resolveInfo = resolveActivity(packageManager, intent) ?: return false

        if (target.requiresExported && !resolveInfo.activityInfo.exported) {
            return false
        }

        val requiredPermission = resolveInfo.activityInfo.permission
        if (!requiredPermission.isNullOrBlank()) {
            val granted = ContextCompat.checkSelfPermission(context, requiredPermission) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        return true
    }

    private fun buildIntent(target: SettingTarget): Intent? {
        val intent = when {
            !target.className.isNullOrBlank() -> Intent().setClassName(target.packageName, target.className)
            !target.action.isNullOrBlank() -> Intent(target.action)
            else -> null
        }

        if (intent != null && target.extras.isNotEmpty()) {
            target.extras.forEach { (key, value) ->
                if (key.isNotBlank()) {
                    intent.putExtra(key, value)
                }
            }
        }
        return intent
    }

    private fun resolveActivity(packageManager: PackageManager, intent: Intent) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, 0)
        }
}
