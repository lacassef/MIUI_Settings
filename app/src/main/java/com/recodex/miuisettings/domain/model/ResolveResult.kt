package com.recodex.miuisettings.domain.model

import android.content.Intent

sealed class ResolveResult {
    data class Supported(
        val target: SettingTarget,
        val intent: Intent
    ) : ResolveResult()

    data class Unsupported(val reason: String? = null) : ResolveResult()
}
