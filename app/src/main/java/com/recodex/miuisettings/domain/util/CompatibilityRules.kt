package com.recodex.miuisettings.domain.util

import com.recodex.miuisettings.domain.model.DeviceProfile
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.model.SettingTarget
import com.recodex.miuisettings.domain.model.CompatibilityStatus

object CompatibilityRules {
    fun isSettingCompatible(setting: HiddenSetting, profile: DeviceProfile): Boolean {
        return getSettingStatus(setting, profile) == CompatibilityStatus.COMPATIBLE
    }

    fun isTargetCompatible(target: SettingTarget, profile: DeviceProfile): Boolean {
        return getTargetStatus(target, profile) == CompatibilityStatus.COMPATIBLE
    }

    fun getSettingStatus(setting: HiddenSetting, profile: DeviceProfile): CompatibilityStatus {
        if (setting.targets.isEmpty()) return CompatibilityStatus.NO_TARGETS
        if (!profile.isXiaomiFamily) return CompatibilityStatus.UNSUPPORTED_MANUFACTURER
        val sdkStatus = getSdkStatus(setting.minSdkVersion, setting.maxSdkVersion, profile.sdkInt)
        if (sdkStatus != null) return sdkStatus
        if (setting.isLegacyOnly && profile.isHyperOs) return CompatibilityStatus.LEGACY_ONLY
        if (!isMiuiCompatible(setting.requiredMiuiVersion, profile.miuiVersion)) {
            return CompatibilityStatus.MIUI_TOO_LOW
        }
        return CompatibilityStatus.COMPATIBLE
    }

    fun getTargetStatus(target: SettingTarget, profile: DeviceProfile): CompatibilityStatus {
        val sdkStatus = getSdkStatus(target.minSdkVersion, target.maxSdkVersion, profile.sdkInt)
        if (sdkStatus != null) return sdkStatus
        if (!isMiuiCompatible(target.requiredMiuiVersion, profile.miuiVersion)) {
            return CompatibilityStatus.MIUI_TOO_LOW
        }
        return CompatibilityStatus.COMPATIBLE
    }

    private fun getSdkStatus(minSdk: Int?, maxSdk: Int?, sdkInt: Int): CompatibilityStatus? {
        if (minSdk != null && sdkInt < minSdk) return CompatibilityStatus.SDK_TOO_LOW
        if (maxSdk != null && maxSdk > 0 && sdkInt > maxSdk) return CompatibilityStatus.SDK_TOO_HIGH
        return null
    }

    private fun isMiuiCompatible(required: String?, current: String?): Boolean {
        val requiredVersion = required?.trim().orEmpty()
        if (requiredVersion.isEmpty()) return true
        if (current.isNullOrBlank()) return false
        return VersionComparator.compare(current, requiredVersion) >= 0
    }
}
