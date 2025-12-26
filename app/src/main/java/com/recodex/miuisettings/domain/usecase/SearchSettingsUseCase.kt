package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.util.TextNormalizer
import com.recodex.miuisettings.domain.util.maxTargetPriority
import javax.inject.Inject

class SearchSettingsUseCase @Inject constructor(
    private val getAvailableSettingsUseCase: GetAvailableSettingsUseCase
) {
    suspend operator fun invoke(query: String, categoryFilter: String? = null): List<HiddenSetting> {
        val normalizedQuery = TextNormalizer.normalize(query)
        val available = getAvailableSettingsUseCase(categoryFilter)
        if (normalizedQuery.isBlank()) {
            return available
                .sortedWith(
                    compareByDescending<HiddenSetting> { it.maxTargetPriority() }
                        .thenBy { it.title }
                )
        }
        val queryTokens = normalizedQuery.split(" ").filter { it.isNotBlank() }
        return available.mapNotNull { setting ->
            val score = scoreSetting(setting, normalizedQuery, queryTokens)
            if (score == 0) {
                null
            } else {
                setting to score
            }
        }
            .sortedWith(
                compareByDescending<Pair<HiddenSetting, Int>> { it.second }
                    .thenByDescending { it.first.maxTargetPriority() }
                    .thenBy { it.first.title }
            )
            .map { it.first }
    }

    private fun scoreSetting(
        setting: HiddenSetting,
        normalizedQuery: String,
        queryTokens: List<String>
    ): Int {
        val normalizedTitle = TextNormalizer.normalize(setting.title)
        val normalizedCategory = TextNormalizer.normalize(setting.category)
        val normalizedKeywords = setting.searchKeywords
            .map { TextNormalizer.normalize(it) }
            .filter { it.isNotBlank() }
        val normalizedHaystack = buildString {
            append(normalizedTitle)
            if (normalizedCategory.isNotBlank()) {
                append(' ')
                append(normalizedCategory)
            }
            if (normalizedKeywords.isNotEmpty()) {
                append(' ')
                append(normalizedKeywords.joinToString(" "))
            }
        }

        var score = 0
        when {
            normalizedTitle == normalizedQuery -> score += 1000
            normalizedTitle.startsWith(normalizedQuery) -> score += 700
            normalizedTitle.contains(normalizedQuery) -> score += 500
        }
        if (normalizedCategory == normalizedQuery) {
            score += 200
        } else if (normalizedCategory.contains(normalizedQuery)) {
            score += 80
        }

        val keywordExact = normalizedKeywords.any { it == normalizedQuery }
        val keywordPrefix = normalizedKeywords.any { it.startsWith(normalizedQuery) }
        val keywordContains = normalizedKeywords.any { it.contains(normalizedQuery) }
        when {
            keywordExact -> score += 400
            keywordPrefix -> score += 250
            keywordContains -> score += 150
        }

        val tokenMatches = queryTokens.count { normalizedHaystack.contains(it) }
        if (tokenMatches > 0) {
            score += tokenMatches * 40
            if (tokenMatches == queryTokens.size) {
                score += 120
            }
        }

        return score
    }
}
