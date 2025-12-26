package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.CompatibilityReport
import com.recodex.miuisettings.domain.model.CompatibilityStatus
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.domain.util.CompatibilityRules
import com.recodex.miuisettings.infra.TelemetryLogger
import javax.inject.Inject
import java.util.Locale

class GetCompatibilityReportUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getDeviceProfileUseCase: GetDeviceProfileUseCase,
    private val telemetryLogger: TelemetryLogger
) {
    suspend operator fun invoke(): CompatibilityReport {
        settingsRepository.ensureSeeded()
        val profile = getDeviceProfileUseCase()
        val settings = settingsRepository.getSettings()

        val settingStatusCounts = mutableMapOf<CompatibilityStatus, Int>()
        val targetStatusCounts = mutableMapOf<CompatibilityStatus, Int>()
        var compatibleSettings = 0
        var totalTargets = 0
        var compatibleTargets = 0

        settings.forEach { setting ->
            val baseStatus = CompatibilityRules.getSettingStatus(setting, profile)
            var compatibleTargetsForSetting = 0

            setting.targets.forEach { target ->
                val targetStatus = CompatibilityRules.getTargetStatus(target, profile)
                if (targetStatus == CompatibilityStatus.COMPATIBLE) {
                    compatibleTargetsForSetting += 1
                } else {
                    incrementCount(targetStatusCounts, targetStatus)
                }
            }

            if (baseStatus != CompatibilityStatus.COMPATIBLE) {
                incrementCount(settingStatusCounts, baseStatus)
            } else if (compatibleTargetsForSetting == 0) {
                incrementCount(settingStatusCounts, CompatibilityStatus.NO_COMPATIBLE_TARGETS)
            } else {
                compatibleSettings += 1
            }

            totalTargets += setting.targets.size
            compatibleTargets += compatibleTargetsForSetting
        }

        val report = CompatibilityReport(
            totalSettings = settings.size,
            compatibleSettings = compatibleSettings,
            incompatibleSettings = settingStatusCounts,
            totalTargets = totalTargets,
            compatibleTargets = compatibleTargets,
            incompatibleTargets = targetStatusCounts
        )

        telemetryLogger.logEvent(
            "compatibility_report",
            buildTelemetryAttributes(profile.manufacturer, profile.miuiVersion, profile.isHyperOs, report)
        )
        return report
    }

    private fun incrementCount(counts: MutableMap<CompatibilityStatus, Int>, status: CompatibilityStatus) {
        counts[status] = (counts[status] ?: 0) + 1
    }

    private fun buildTelemetryAttributes(
        manufacturer: String,
        miuiVersion: String?,
        isHyperOs: Boolean,
        report: CompatibilityReport
    ): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        attributes["manufacturer"] = manufacturer
        attributes["miuiVersion"] = miuiVersion ?: "unknown"
        attributes["hyperOs"] = isHyperOs.toString()
        attributes["totalSettings"] = report.totalSettings.toString()
        attributes["compatibleSettings"] = report.compatibleSettings.toString()
        attributes["totalTargets"] = report.totalTargets.toString()
        attributes["compatibleTargets"] = report.compatibleTargets.toString()
        report.incompatibleSettings.forEach { (status, count) ->
            attributes["setting_${status.name.lowercase(Locale.ROOT)}"] = count.toString()
        }
        report.incompatibleTargets.forEach { (status, count) ->
            attributes["target_${status.name.lowercase(Locale.ROOT)}"] = count.toString()
        }
        return attributes
    }
}
