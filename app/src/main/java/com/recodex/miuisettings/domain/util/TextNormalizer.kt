package com.recodex.miuisettings.domain.util

import java.text.Normalizer
import java.util.Locale

object TextNormalizer {
    fun normalize(input: String): String {
        if (input.isBlank()) return ""
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
    }
}
