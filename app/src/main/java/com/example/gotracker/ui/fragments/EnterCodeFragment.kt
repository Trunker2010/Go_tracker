package com.example.gotracker.ui.fragments

import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment

import com.example.gotracker.R
import com.example.gotracker.ui.activities.MainActivity
import com.example.gotracker.ui.activities.RegisterActivity
import com.example.gotracker.utils.AUTH
import com.example.gotracker.utils.replaceActivity
import com.example.gotracker.utils.showToast
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_enter_code.*

/**
 * A simple [Fragment] subclass.
 */
class EnterCodeFragment(val phoneNumber: String, val id: String) :
    Fragment(R.layout.fragment_enter_code) {
    override fun onStart() {
        super.onStart()
        phone_number.text = phoneNumber
        enter_input_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val code = enter_input_code.text.toString()
                if (code.length == 6) {
                    enterCode()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun enterCode() {
        val code = enter_input_code.text.toString()
        val credential = PhoneAuthProvider.getCredential(id, code)
        AUTH.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                showToast("Добро пожаловать")
                (activity as RegisterActivity).replaceActivity(MainActivity())
            } else showToast(it.exception?.message.toString())
        }

    }
}
