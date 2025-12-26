package com.recodex.miuisettings.domain.usecase

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.recodex.miuisettings.domain.model.LaunchResult
import com.recodex.miuisettings.domain.model.ResolveResult
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.infra.DeviceUtils
import com.recodex.miuisettings.infra.IntentResolver
import com.recodex.miuisettings.infra.TelemetryLogger
import javax.inject.Inject

class LaunchSettingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val intentResolver: IntentResolver,
    private val telemetryLogger: TelemetryLogger,
    private val deviceUtils: DeviceUtils
) {
    suspend operator fun invoke(context: Context, settingId: String): LaunchResult {
        settingsRepository.ensureSeeded()
        val setting = settingsRepository.getSettingById(settingId) ?: return LaunchResult.NotFound
        val profile = deviceUtils.getDeviceProfile()
        return when (val resolveResult = intentResolver.resolve(setting)) {
            is ResolveResult.Supported -> {
                val intent = resolveResult.intent
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    telemetryLogger.logEvent(
                        "launch_setting_success",
                        mapOf(
                            "settingId" to settingId,
                            "targetId" to resolveResult.target.id,
                            "miuiVersion" to (profile.miuiVersion ?: "unknown"),
                            "hyperOs" to profile.isHyperOs.toString()
                        )
                    )
                    LaunchResult.Launched(resolveResult.target)
                } catch (error: ActivityNotFoundException) {
                    telemetryLogger.logError(
                        "launch_setting_activity_not_found",
                        error,
                        mapOf(
                            "settingId" to settingId,
                            "miuiVersion" to (profile.miuiVersion ?: "unknown"),
                            "hyperOs" to profile.isHyperOs.toString()
                        )
                    )
                    LaunchResult.Failed(error)
                } catch (error: SecurityException) {
                    telemetryLogger.logError(
                        "launch_setting_security_exception",
                        error,
                        mapOf(
                            "settingId" to settingId,
                            "miuiVersion" to (profile.miuiVersion ?: "unknown"),
                            "hyperOs" to profile.isHyperOs.toString()
                        )
                    )
                    LaunchResult.Failed(error)
                }
            }
            is ResolveResult.Unsupported -> {
                telemetryLogger.logEvent(
                    "launch_setting_unsupported",
                    mapOf(
                        "settingId" to settingId,
                        "reason" to (resolveResult.reason ?: "unknown"),
                        "miuiVersion" to (profile.miuiVersion ?: "unknown"),
                        "hyperOs" to profile.isHyperOs.toString()
                    )
                )
                LaunchResult.Unsupported(resolveResult.reason)
            }
        }
    }
}
