package com.recodex.miuisettings.infra

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogcatTelemetryLogger @Inject constructor() : TelemetryLogger {
    override fun logEvent(name: String, attributes: Map<String, String>) {
        if (attributes.isEmpty()) {
            Log.i(TAG, name)
        } else {
            Log.i(TAG, "$name ${attributes.formatAttributes()}")
        }
    }

    override fun logError(name: String, throwable: Throwable, attributes: Map<String, String>) {
        val message = if (attributes.isEmpty()) name else "$name ${attributes.formatAttributes()}"
        Log.e(TAG, message, throwable)
    }

    private fun Map<String, String>.formatAttributes(): String =
        entries.joinToString(prefix = "[", postfix = "]") { "${it.key}=${it.value}" }

    private companion object {
        private const val TAG = "EclipseTelemetry"
    }
}
