package com.splunk.android.sr.testapp.ui.compose

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle

class LeakActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() $this")

        setContent {
            Content()
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed() $this")
        super.onBackPressed()
    }

    @Composable
    private fun Content() {
        BackHandler {
            Log.d(TAG, "BackHandler.onBack() $this")
            finishAfterTransition()
        }

        val isDarkTheme = isSystemInDarkTheme()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                modifier = Modifier
                    .clickable { toggleTheme(isDarkTheme = isDarkTheme) },
                text = "Change theme!",
                style = TextStyle(
                    color = Color.Red
                )
            )
        }
    }

    @Composable
    private fun isSystemInDarkTheme(): Boolean {
        return LocalConfiguration.current.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    private fun toggleTheme(isDarkTheme: Boolean) {
        setDefaultNightMode(if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES)
    }

    private companion object {
        const val TAG = "LeakActivity"
    }
}
