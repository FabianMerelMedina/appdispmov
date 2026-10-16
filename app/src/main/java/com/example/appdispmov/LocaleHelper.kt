package com.example.appdispmov

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context): Context {

        val preferences = context.getSharedPreferences(
            "MyChatPreferences",
            Context.MODE_PRIVATE
        )

        val language = preferences.getString(
            "language",
            "es"
        ) ?: "es"

        val locale = Locale(language)

        Locale.setDefault(locale)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}