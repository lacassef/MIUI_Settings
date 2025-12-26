package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.domain.util.CompatibilityRules
import com.recodex.miuisettings.domain.util.TextNormalizer
import com.recodex.miuisettings.domain.util.maxTargetPriority
import javax.inject.Inject

class GetAvailableSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getDeviceProfileUseCase: GetDeviceProfileUseCase
) {
    suspend operator fun invoke(categoryFilter: String? = null): List<HiddenSetting> {
        settingsRepository.ensureSeeded()
        val profile = getDeviceProfileUseCase()
        val normalizedCategory = categoryFilter?.let { TextNormalizer.normalize(it) }
        return settingsRepository.getSettings()
            .filter { CompatibilityRules.isSettingCompatible(it, profile) }
            .map { setting ->
                setting.copy(
                    targets = setting.targets
                        .filter { CompatibilityRules.isTargetCompatible(it, profile) }
                        .sortedByDescending { it.priority }
                )
            }
            .filter { it.targets.isNotEmpty() }
            .filter { setting ->
                if (normalizedCategory.isNullOrBlank()) {
                    true
                } else {
                    TextNormalizer.normalize(setting.category) == normalizedCategory
                }
            }
            .sortedWith(
                compareByDescending<HiddenSetting> { it.maxTargetPriority() }
                    .thenBy { it.title }
            )
    }
}
