package com.recodex.miuisettings.presentation.model

import com.recodex.miuisettings.domain.model.LaunchResult

data class LaunchState(
    val isLaunching: Boolean = false,
    val lastResult: LaunchResult? = null
)
