package com.recodex.miuisettings.domain.model

data class CompatibilityReport(
    val totalSettings: Int,
    val compatibleSettings: Int,
    val incompatibleSettings: Map<CompatibilityStatus, Int>,
    val totalTargets: Int,
    val compatibleTargets: Int,
    val incompatibleTargets: Map<CompatibilityStatus, Int>
)
