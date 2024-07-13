package com.aryan.veena.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.aryan.veena.R
import com.aryan.veena.database.DataStoreHelper.getPreferenceFlow
import com.aryan.veena.database.DataStoreHelper.ACCENT_KEY
import com.aryan.veena.database.DataStoreHelper.ACCENT_MONET
import com.aryan.veena.database.DataStoreHelper.ACCENT_PINK
import com.aryan.veena.database.DataStoreHelper.ACCENT_YELLOW
import com.aryan.veena.database.DataStoreHelper.DARK_THEME
import com.aryan.veena.database.DataStoreHelper.LIGHT_THEME
import com.aryan.veena.database.DataStoreHelper.SYSTEM_THEME
import com.aryan.veena.database.DataStoreHelper.THEME_KEY
import com.aryan.veena.database.DataStoreHelper.setPreference
import com.aryan.veena.utils.CoroutineUtils.mainScope

class AppearanceSettings : PreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*mainScope {
            val currentTheme = getPreferenceFlow(context ?: return@mainScope, THEME_KEY, SYSTEM_THEME)
            val nightMode = when (currentTheme) {
                LIGHT_THEME -> MODE_NIGHT_NO
                DARK_THEME -> MODE_NIGHT_YES
                SYSTEM_THEME -> MODE_NIGHT_FOLLOW_SYSTEM
                else -> MODE_NIGHT_FOLLOW_SYSTEM
            }

            setDefaultNightMode(nightMode)
        }*/
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_appearance, rootKey)
        val ctx = context ?: return
        val accentColor = findPreference<ListPreference>("accent")
        //updateAccentColorValues(accentColor!!)
        accentColor?.setOnPreferenceChangeListener { _, newAccent ->
            when (newAccent) {
                "pink" -> setPreference(ctx, ACCENT_KEY, ACCENT_PINK)
                "yellow" -> setPreference(ctx, ACCENT_KEY, ACCENT_YELLOW)
                "monet" -> setPreference(ctx, ACCENT_KEY, ACCENT_MONET)
            }
            activity?.recreate()
            true
        }

        val themeMode = findPreference<ListPreference>("theme")
        themeMode?.setOnPreferenceChangeListener { _, newTheme ->
            when (newTheme) {
                "light" -> setPreference(ctx, THEME_KEY, LIGHT_THEME)
                "dark" -> setPreference(ctx, THEME_KEY, DARK_THEME)
                "system" -> setPreference(ctx, THEME_KEY, SYSTEM_THEME)
            }

            activity?.recreate()
            true
        }
    }

    private fun updateAccentColorValues(pref: ListPreference) {
        pref.entries = pref.entries.toList().subList(1, pref.entries.size).toTypedArray()
        pref.entryValues =
            pref.entryValues.toList().subList(2, pref.entryValues.size).toTypedArray()
    }
}