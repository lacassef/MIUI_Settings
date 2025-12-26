package com.recodex.miuisettings.domain.util

object VersionComparator {
    fun compare(left: String, right: String): Int {
        val leftParts = parseVersion(left)
        val rightParts = parseVersion(right)
        val max = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until max) {
            val l = leftParts.getOrNull(index) ?: 0
            val r = rightParts.getOrNull(index) ?: 0
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    private fun parseVersion(version: String): List<Int> {
        val cleaned = version
            .uppercase()
            .replace("MIUI", "")
            .replace("HYPEROS", "")
            .replace("OS", "")
            .replace("V", "")
        return cleaned
            .split(Regex("[^0-9]+"))
            .filter { it.isNotBlank() }
            .mapNotNull { it.toIntOrNull() }
    }
}
