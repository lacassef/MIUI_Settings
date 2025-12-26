package com.recodex.miuisettings.infra

interface TelemetryLogger {
    fun logEvent(name: String, attributes: Map<String, String> = emptyMap())
    fun logError(name: String, throwable: Throwable, attributes: Map<String, String> = emptyMap())
}
