package com.centuryprogrammer18thwasentsingleland.manager.Settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.centuryprogrammer18thwasentsingleland.R

///////////////////////////////////////////////////////////////////////
// this is not used


//SharedPreferences PreferencesAcitivity PreferencesFragment
// ref) https://youtu.be/M15PEeHXM64

class SettingsActivity : AppCompatActivity() {
    private val TAG = SettingsActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        }
    }
}