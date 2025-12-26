package com.recodex.miuisettings.presentation.model

data class SettingSummary(
    val id: String,
    val title: String,
    val category: String,
    val targetCount: Int,
    val isLegacyOnly: Boolean
)
