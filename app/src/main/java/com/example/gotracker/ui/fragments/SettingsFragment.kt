package com.example.gotracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import com.example.gotracker.R
import com.example.gotracker.ui.activities.MainActivity
import com.example.gotracker.ui.activities.RegisterActivity
import com.example.gotracker.utils.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.math.log

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    lateinit var phone: EditTextPreference
    lateinit var name: EditTextPreference
    lateinit var logOut: Preference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        phone = findPreference<EditTextPreference>(getString(R.string.settings_phone_number))!!
        name = findPreference(getString(R.string.settings_name_key))!!
        logOut = findPreference(getString(R.string.exitButton))!!
        logOut.onPreferenceClickListener = this
        name.title = USER.username

        name.setOnBindEditTextListener {
            Log.d("settings", "name.setOnBindEditTextListener")

        }

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                getString(R.string.settings_name_key) -> {
                    val name_pref =
                        sharedPreferences?.getString(getString(R.string.settings_name_key), "")
                            ?: ""
                    name.title = name_pref
                    val dateMap = mutableMapOf<String, Any>()
                    dateMap[CHILD_USERNAME] = name_pref
                    REF_DATABASE_ROOT.child(NODE_USERS).child(UID).updateChildren(dateMap)
                    USER.username = name_pref

                }
            }
        }



        phone.title = AUTH.currentUser?.phoneNumber
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.exitButton) -> {
                AUTH.signOut()
                val intent = Intent(activity, RegisterActivity::class.java)
                activity?.finish()
                startActivity(intent)
                return true
            }
        }
        return false
    }


}
