package com.example.gotracker.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

import com.example.gotracker.R
import com.example.gotracker.ui.activities.RegisterActivity
import com.example.gotracker.utils.AUTH
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {
        exit_view.setOnClickListener(this)
        super.onStart()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            exit_view.id -> {
                AUTH.signOut()
                val intent = Intent(activity, RegisterActivity::class.java)
                activity?.finish()
                startActivity(intent)
            }
        }
    }


}
