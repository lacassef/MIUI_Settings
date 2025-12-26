package com.recodex.miuisettings.domain.model

data class CatalogPayload(
    val version: String,
    val settings: List<HiddenSetting>
)
