package com.recodex.miuisettings.domain.model

enum class CompatibilityStatus {
    COMPATIBLE,
    UNSUPPORTED_MANUFACTURER,
    SDK_TOO_LOW,
    SDK_TOO_HIGH,
    MIUI_TOO_LOW,
    LEGACY_ONLY,
    NO_TARGETS,
    NO_COMPATIBLE_TARGETS
}
