package com.splunk.android.sr.testapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.splunk.android.common.utils.extensions.contentView
import com.splunk.android.common.utils.extensions.rootView
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.ActivityMainBinding
import com.splunk.android.sr.testapp.extension.setCustomAnimations
import com.splunk.android.sr.testapp.ui.menu.MenuFragment
import com.splunk.android.sr.testapp.util.AppPreferences
import com.splunk.android.sr.testapp.util.FragmentAnimation

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private val preferences = AppPreferences.getInstance()
    private val startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater, contentView, true)

        rootView?.isSensitive = preferences.isContentSensitive

        setSupportActionBar(viewBinding.toolbar)
        viewBinding.toolbar.setNavigationOnClickListener { navigateUp() }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null)
            navigateTo(MenuFragment())
        else
            contentView?.post { updateToolbar() }

        deferContentAfterSplashScreen()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        menu.findItem(R.id.main_sensitivity_false).setOnMenuItemClickListener(onMenuItemClickListener)
        menu.findItem(R.id.main_sensitivity_null).setOnMenuItemClickListener(onMenuItemClickListener)

        return true
    }

    fun navigateTo(fragment: BaseFragment<*>, animation: FragmentAnimation? = null) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(animation)
            .replace(viewBinding.container.id, fragment)
            .addToBackStack(fragment.tag ?: fragment.hashCode().toString())
            .commit()

        contentView?.post { updateToolbar() }
    }

    fun navigateUp() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStackImmediate()
            updateToolbar()
        } else
            finish()
    }

    private fun deferContentAfterSplashScreen() {
        contentView?.viewTreeObserver?.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (System.currentTimeMillis() - startTime > SPLASH_SCREEN_HOLD_TIME) {
                        contentView?.viewTreeObserver?.removeOnPreDrawListener(this)
                        true
                    } else
                        false
                }
            }
        )
    }

    private fun updateToolbar() {
        val isBackButtonVisible = supportFragmentManager.backStackEntryCount > 1
        supportActionBar?.setDisplayHomeAsUpEnabled(isBackButtonVisible)
        supportActionBar?.setDisplayShowHomeEnabled(isBackButtonVisible)

        val fragment = supportFragmentManager.fragments.last() as? BaseFragment<*>
        if (fragment != null) {
            viewBinding.toolbar.setTitle(fragment.titleRes)
            fragment.subtitleRes?.let { viewBinding.toolbar.setSubtitle(it) }
        }
    }

    private val onMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.main_sensitivity_false -> {
                rootView?.isSensitive = false
                preferences.isContentSensitive = false
            }
            R.id.main_sensitivity_null -> {
                rootView?.isSensitive = null
                preferences.isContentSensitive = null
            }
        }

        true
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateUp()
        }
    }

    private companion object {
        const val SPLASH_SCREEN_HOLD_TIME = 1000L
    }
}
