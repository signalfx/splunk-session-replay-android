package com.splunk.android.instrumentation.recording.interactions

import android.app.AlertDialog
import android.os.Looper
import android.widget.EditText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class ComposeAlertDialogTest {

    companion object {

        @BeforeClass
        @JvmStatic
        fun setup() {
            Looper.prepare()

            val application = ApplicationProvider.getApplicationContext<android.app.Application>()
            Interactions.attach(application)
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    fun cleanUp() {
        Interactions.interactionsHolder.clear()
    }

    @Test
    fun testLegacyDialog() {
        Interactions.allowedInteractions = setOf(Interaction.Keyboard::class)

        composeTestRule.setContent { LegacyAlertDialog() }

        composeTestRule.onNodeWithText("Open dialog").performClick()

        onView(withText("AndroidDialog"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(isAssignableFrom(EditText::class.java))
            .inRoot(isDialog())
            .perform(typeText("Hello, my dear keyboard"), closeSoftKeyboard())

        val interactions = Interactions.interactionsHolder.getInteractions()

        assert(interactions.size == 2) { "There should be two interactions. Please check virtual keyboard settings." }
        assert((interactions.first() as Interaction.Keyboard).isOpened) { "First interaction is not Keyboard.isOpened" }
        assert((interactions.last() as Interaction.Keyboard).isClosed) { "Last interaction is not Keyboard.isClosed" }

        onView(withText("OK"))
            .inRoot(isDialog())
            .perform(click())
    }

    @Composable
    private fun LegacyAlertDialog() {
        var showDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable { showDialog = true }
            ) {
                BasicText("Open dialog")
            }

            if (showDialog) {
                DisposableEffect(Unit) {
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("AndroidDialog")
                        .setMessage("This is 'android.app.AlertDialog' opened from Jetpack Compose.")
                        .setView(EditText(context))
                        .setPositiveButton("OK") { _, _ ->
                            showDialog = false
                        }
                        .setNegativeButton("Close") { _, _ ->
                            showDialog = false
                        }
                        .setOnDismissListener {
                            showDialog = false
                        }
                        .create()

                    dialog.show()

                    onDispose {
                        dialog.dismiss()
                    }
                }
            }
        }
    }
}
