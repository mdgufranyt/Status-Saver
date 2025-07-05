package com.mg.statussaver.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * LanguagePreferences class to manage language selection persistence
 * Saves and retrieves user's selected language using SharedPreferences
 */
class LanguagePreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "language_preferences"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_IS_LANGUAGE_SELECTED = "is_language_selected"
    }

    /**
     * Save the selected language code
     * @param languageCode: Language code (e.g., "en", "hi", "ur")
     */
    fun saveSelectedLanguage(languageCode: String) {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_LANGUAGE, languageCode)
            .putBoolean(KEY_IS_LANGUAGE_SELECTED, true)
            .apply()
    }

    /**
     * Get the saved language code
     * @return: Language code or null if not set
     */
    fun getSelectedLanguage(): String? {
        return sharedPreferences.getString(KEY_SELECTED_LANGUAGE, null)
    }

    /**
     * Check if user has already selected a language
     * @return: true if language is selected, false otherwise
     */
    fun isLanguageSelected(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LANGUAGE_SELECTED, false)
    }

    /**
     * Clear language preferences (for testing or reset functionality)
     */
    fun clearLanguagePreferences() {
        sharedPreferences.edit()
            .remove(KEY_SELECTED_LANGUAGE)
            .remove(KEY_IS_LANGUAGE_SELECTED)
            .apply()
    }
}
