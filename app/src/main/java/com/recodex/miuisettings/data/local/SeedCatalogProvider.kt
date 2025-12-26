package com.recodex.miuisettings.data.local

import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.model.SettingTarget
import com.recodex.miuisettings.domain.util.SettingCategories
import javax.inject.Inject

class SeedCatalogProvider @Inject constructor() {
    fun provide(): CatalogPayload {
        val settings = listOf(
            HiddenSetting(
                id = "notification_log",
                title = "Log de notificacoes",
                category = SettingCategories.SYSTEM,
                searchKeywords = listOf("notificacoes", "log", "historico", "notification"),
                targets = listOf(
                    SettingTarget(
                        id = "notification_log_aosp",
                        settingId = "notification_log",
                        packageName = "com.android.settings",
                        className = "com.android.settings.Settings\$NotificationStationActivity",
                        priority = 100,
                        requiresExported = true,
                        notes = "AOSP"
                    )
                )
            ),
            HiddenSetting(
                id = "autostart_management",
                title = "Gerenciar Autostart",
                category = SettingCategories.SECURITY,
                searchKeywords = listOf("autostart", "inicio", "automatico", "permissoes"),
                targets = listOf(
                    SettingTarget(
                        id = "autostart_miui",
                        settingId = "autostart_management",
                        packageName = "com.miui.securitycenter",
                        className = "com.miui.permcenter.autostart.AutoStartManagementActivity",
                        priority = 100,
                        requiresExported = true,
                        notes = "MIUI/HyperOS"
                    )
                )
            ),
            HiddenSetting(
                id = "private_dns",
                title = "DNS privado",
                category = SettingCategories.NETWORK,
                searchKeywords = listOf("dns", "privado", "rede", "adblock"),
                targets = listOf(
                    SettingTarget(
                        id = "private_dns_action",
                        settingId = "private_dns",
                        packageName = "",
                        action = "android.settings.PRIVATE_DNS_SETTINGS",
                        priority = 100,
                        requiresExported = false,
                        notes = "Intent generica"
                    )
                )
            ),
            HiddenSetting(
                id = "battery_optimization",
                title = "Otimizacao de bateria",
                category = SettingCategories.BATTERY,
                searchKeywords = listOf("bateria", "otimizacao", "doze", "economia"),
                targets = listOf(
                    SettingTarget(
                        id = "battery_optimization_action",
                        settingId = "battery_optimization",
                        packageName = "",
                        action = "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS",
                        priority = 90,
                        requiresExported = false,
                        notes = "Intent generica"
                    )
                )
            ),
            HiddenSetting(
                id = "trusted_credentials",
                title = "Certificados confiaveis",
                category = SettingCategories.SECURITY,
                searchKeywords = listOf("certificados", "credenciais", "seguranca", "ca"),
                targets = listOf(
                    SettingTarget(
                        id = "trusted_credentials_aosp",
                        settingId = "trusted_credentials",
                        packageName = "com.android.settings",
                        className = "com.android.settings.Settings\$TrustedCredentialsSettingsActivity",
                        priority = 80,
                        requiresExported = true,
                        notes = "AOSP"
                    )
                )
            )
        )
        return CatalogPayload(
            version = "seed-1",
            settings = settings
        )
    }
}
