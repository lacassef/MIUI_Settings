package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.domain.util.CompatibilityRules
import com.recodex.miuisettings.domain.util.TextNormalizer
import com.recodex.miuisettings.domain.util.maxTargetPriority
import com.recodex.miuisettings.di.IoDispatcher
import com.recodex.miuisettings.infra.TargetAvailabilityChecker
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class GetAvailableSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getDeviceProfileUseCase: GetDeviceProfileUseCase,
    private val availabilityChecker: TargetAvailabilityChecker,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(categoryFilter: String? = null): List<HiddenSetting> = withContext(ioDispatcher) {
        settingsRepository.ensureSeeded()
        val profile = getDeviceProfileUseCase()
        val normalizedCategory = categoryFilter?.let { TextNormalizer.normalize(it) }
        settingsRepository.getSettings()
            .asSequence()
            .filter { CompatibilityRules.isSettingCompatible(it, profile) }
            .map { setting ->
                val launchableTargets = setting.targets
                    .filter { CompatibilityRules.isTargetCompatible(it, profile) }
                    .filter { availabilityChecker.isLaunchable(it) }
                    .sortedByDescending { it.priority }

                setting.copy(targets = launchableTargets)
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
            .toList()
    }
}
