package com.example.appdispmov

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object SystemBarsHelper {

    fun applyInsets(view: View) {

        ViewCompat.setOnApplyWindowInsetsListener(
            view
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }
}