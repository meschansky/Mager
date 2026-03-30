package com.example.armoredage.ui

import androidx.annotation.StringRes
import androidx.core.os.LocaleListCompat
import com.example.armoredage.R

enum class AppLanguage(
    @param:StringRes val labelRes: Int,
    val languageTag: String
) {
    SYSTEM_DEFAULT(R.string.language_system_default, ""),
    ENGLISH(R.string.language_english, "en"),
    RUSSIAN(R.string.language_russian, "ru"),
    SPANISH(R.string.language_spanish, "es"),
    SIMPLIFIED_CHINESE(R.string.language_chinese_simplified, "zh-CN");

    fun toLocaleList(): LocaleListCompat =
        if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }

    companion object {
        fun fromLanguageTags(languageTags: String): AppLanguage {
            val primaryTag = languageTags.substringBefore(",")
            return entries.firstOrNull { it.languageTag == primaryTag } ?: SYSTEM_DEFAULT
        }
    }
}
