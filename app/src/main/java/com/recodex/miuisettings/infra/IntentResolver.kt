package com.recodex.miuisettings.infra

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.model.ResolveResult
import com.recodex.miuisettings.domain.model.SettingTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.util.Locale

class IntentResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageVisibilityHelper: PackageVisibilityHelper,
    private val telemetryLogger: TelemetryLogger,
    private val deviceUtils: DeviceUtils,
    private val resolutionMetrics: IntentResolutionMetrics
) {
    fun resolve(setting: HiddenSetting): ResolveResult {
        val profile = deviceUtils.getDeviceProfile()
        val baseAttributes = mutableMapOf(
            "settingId" to setting.id,
            "manufacturer" to profile.manufacturer,
            "miuiVersion" to (profile.miuiVersion ?: "unknown"),
            "hyperOs" to profile.isHyperOs.toString()
        )
        if (setting.targets.isEmpty()) {
            val reason = "Sem alvos configurados"
            val snapshot = resolutionMetrics.recordResult(false)
            telemetryLogger.logEvent(
                "intent_resolver_unsupported",
                baseAttributes + mapOf(
                    "reason" to reason,
                    "successRate" to formatRate(snapshot),
                    "totalAttempts" to snapshot.totalAttempts.toString(),
                    "totalSuccesses" to snapshot.totalSuccesses.toString()
                )
            )
            return ResolveResult.Unsupported(reason)
        }
        val packageManager = context.packageManager
        for (target in setting.targets.sortedByDescending { it.priority }) {
            val intent = buildIntent(target) ?: continue
            if (target.packageName.isNotBlank() && !packageVisibilityHelper.isPackageVisible(target.packageName)) {
                telemetryLogger.logEvent(
                    "intent_resolver_package_not_visible",
                    baseAttributes + mapOf("package" to target.packageName, "targetId" to target.id)
                )
                continue
            }
            val resolveInfo = resolveActivity(packageManager, intent) ?: continue
            val exported = resolveInfo.activityInfo.exported
            if (target.requiresExported && !exported) {
                telemetryLogger.logEvent(
                    "intent_resolver_not_exported",
                    baseAttributes + mapOf("targetId" to target.id)
                )
                continue
            }
            val snapshot = resolutionMetrics.recordResult(true)
            telemetryLogger.logEvent(
                "intent_resolver_resolved",
                baseAttributes + mapOf(
                    "targetId" to target.id,
                    "successRate" to formatRate(snapshot),
                    "totalAttempts" to snapshot.totalAttempts.toString(),
                    "totalSuccesses" to snapshot.totalSuccesses.toString()
                )
            )
            return ResolveResult.Supported(target, intent)
        }
        val reason = "Nenhum alvo compativel encontrado"
        val snapshot = resolutionMetrics.recordResult(false)
        telemetryLogger.logEvent(
            "intent_resolver_unsupported",
            baseAttributes + mapOf(
                "reason" to reason,
                "successRate" to formatRate(snapshot),
                "totalAttempts" to snapshot.totalAttempts.toString(),
                "totalSuccesses" to snapshot.totalSuccesses.toString()
            )
        )
        return ResolveResult.Unsupported(reason)
    }

    private fun buildIntent(target: SettingTarget): Intent? {
        return when {
            !target.className.isNullOrBlank() -> {
                Intent().setClassName(target.packageName, target.className)
            }
            !target.action.isNullOrBlank() -> {
                Intent(target.action)
            }
            else -> null
        }
    }

    private fun resolveActivity(packageManager: PackageManager, intent: Intent) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, 0)
        }

    private fun formatRate(snapshot: IntentResolutionMetrics.ResolutionSnapshot): String {
        return String.format(Locale.ROOT, "%.2f", snapshot.successRate)
    }
}
