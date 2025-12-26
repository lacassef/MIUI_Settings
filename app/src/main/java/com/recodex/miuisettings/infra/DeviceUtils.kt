package com.recodex.miuisettings.infra

import android.os.Build
import com.recodex.miuisettings.domain.model.DeviceProfile
import java.util.Locale
import javax.inject.Inject

class DeviceUtils @Inject constructor() {
    fun getDeviceProfile(): DeviceProfile {
        val manufacturer = Build.MANUFACTURER?.lowercase(Locale.ROOT).orEmpty()
        val miuiVersion = getSystemProperty("ro.miui.ui.version.name")?.takeIf { it.isNotBlank() }
        val isHyperOs = miuiVersion?.startsWith("OS", ignoreCase = true) == true
            || miuiVersion?.contains("hyper", ignoreCase = true) == true
        return DeviceProfile(
            manufacturer = manufacturer,
            sdkInt = Build.VERSION.SDK_INT,
            miuiVersion = miuiVersion,
            isHyperOs = isHyperOs
        )
    }

    private fun getSystemProperty(key: String): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, "") as? String
        } catch (_: Throwable) {
            null
        }
    }
}
