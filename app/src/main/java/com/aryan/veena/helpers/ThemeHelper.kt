package com.aryan.veena.helpers

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import com.aryan.veena.R
import com.aryan.veena.database.DataStoreHelper
import com.aryan.veena.database.DataStoreHelper.ACCENT_KEY
import com.aryan.veena.database.DataStoreHelper.ACCENT_PINK
import com.aryan.veena.database.DataStoreHelper.ACCENT_YELLOW
import com.aryan.veena.database.DataStoreHelper.DARK_THEME
import com.aryan.veena.database.DataStoreHelper.LIGHT_THEME
import com.aryan.veena.database.DataStoreHelper.SYSTEM_THEME
import com.aryan.veena.database.DataStoreHelper.THEME_KEY
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors

object ThemeHelper {

    suspend fun applyThemeAndAccent(activity: AppCompatActivity) {
        val theme = DataStoreHelper.getPreferenceFlow(activity, THEME_KEY, SYSTEM_THEME)
        applyTheme(theme)
        Log.d("MainAct", "Setting theme: $theme")

        val accent = DataStoreHelper.getPreferenceFlow(activity, ACCENT_KEY, ACCENT_PINK)
        applyAccent(accent, activity)
        Log.d("MainAct", "Applying accent: $accent")
    }

    private fun applyAccent(accent: String, activity: AppCompatActivity) {

        if (accent == DataStoreHelper.ACCENT_MONET) {
            // Apply dynamic colors if available
            DynamicColors.applyToActivityIfAvailable(activity)
        } else {
            val themeResId = when (accent) {
                ACCENT_PINK -> R.style.BaseTheme
                ACCENT_YELLOW -> R.style.YellowTheme
                else -> R.style.BaseTheme
            }
            activity.setTheme(themeResId)
        }
    }

    private fun applyTheme(theme: String) {
        val nightMode = when (theme) {
            LIGHT_THEME -> MODE_NIGHT_NO
            DARK_THEME -> MODE_NIGHT_YES
            SYSTEM_THEME -> MODE_NIGHT_FOLLOW_SYSTEM
            else -> MODE_NIGHT_FOLLOW_SYSTEM
        }

        setDefaultNightMode(nightMode)
    }

    /** Set the color of app name in main toolbar **/
    fun setAppName(context: Context): Spanned {
        val colorPrimary = MaterialColors.getColor(
            context,
            androidx.appcompat.R.attr.colorPrimaryDark,
            Color.TRANSPARENT
        )
        val hexColor = "#%06X".format(0xFFFFFF and colorPrimary)
        return "<span style='color:$hexColor';>Ｖｅｅｎａ</span>"
            .parseAsHtml(HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}
