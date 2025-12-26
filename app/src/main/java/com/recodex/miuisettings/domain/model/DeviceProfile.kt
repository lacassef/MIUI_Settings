package com.recodex.miuisettings.domain.model

data class DeviceProfile(
    val manufacturer: String,
    val sdkInt: Int,
    val miuiVersion: String?,
    val isHyperOs: Boolean
) {
    val isXiaomiFamily: Boolean
        get() = manufacturer.equals("xiaomi", ignoreCase = true)
            || manufacturer.equals("redmi", ignoreCase = true)
            || manufacturer.equals("poco", ignoreCase = true)
}
