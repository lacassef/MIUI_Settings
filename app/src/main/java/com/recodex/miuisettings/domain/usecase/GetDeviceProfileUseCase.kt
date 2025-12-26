package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.DeviceProfile
import com.recodex.miuisettings.infra.DeviceUtils
import javax.inject.Inject

class GetDeviceProfileUseCase @Inject constructor(
    private val deviceUtils: DeviceUtils
) {
    operator fun invoke(): DeviceProfile = deviceUtils.getDeviceProfile()
}
