package com.recodex.miuisettings.domain.model

sealed class SyncResult {
    data class Updated(val version: String) : SyncResult()
    data class Noop(val currentVersion: String?) : SyncResult()
    data class Failed(val reason: String) : SyncResult()
}
