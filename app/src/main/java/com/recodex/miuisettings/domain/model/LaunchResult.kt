package com.recodex.miuisettings.domain.model

sealed class LaunchResult {
    data class Launched(val target: SettingTarget) : LaunchResult()
    data class Unsupported(val reason: String? = null) : LaunchResult()
    object NotFound : LaunchResult()
    data class Failed(val throwable: Throwable) : LaunchResult()
}
