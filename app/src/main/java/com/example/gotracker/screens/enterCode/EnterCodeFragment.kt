package com.example.gotracker.screens.enterCode

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.example.gotracker.R
import com.example.gotracker.RegisterActivity
import com.example.gotracker.utils.*
import kotlinx.android.synthetic.main.fragment_enter_code.*

/**
 * A simple [Fragment] subclass.
 */
class EnterCodeFragment() :
    Fragment(R.layout.fragment_enter_code) {

    lateinit var mPhoneNumber: String
    lateinit var mViewModel: EnterCodeViewModel
    lateinit var mId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mPhoneNumber = arguments?.getString(PHONE_NUMBER_KEY).toString()
        mId = arguments?.getString(ID_KEY).toString()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        init()

    }

//    private fun enterCode() {
//        val code = enter_input_code.text.toString()
//        val credential = PhoneAuthProvider.getCredential(id, code)
//        AUTH.signInWithCredential(credential).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val uid = AUTH.currentUser?.uid.toString()
//                val dateMap = mutableMapOf<String, Any>()
//                dateMap[CHILD_ID] = uid
//                dateMap[CHILD_PHONE] = phoneNumber
//                dateMap[CHILD_USERNAME] = uid
//                REF_DATABASE_ROOT.child(NODE_USERS).child(uid).updateChildren(dateMap)
//                    .addOnCompleteListener { it ->
//                        if (it.isSuccessful) {
//                            showToast("Добро пожаловать")
//                            (activity as RegisterActivity).replaceActivity(MainActivity())
//                        } else showToast(it.exception?.message.toString())
//                    }
//
//
//            } else showToast(it.exception?.message.toString())
//        }
//
//    }

    private fun init() {
        mViewModel = ViewModelProvider(this).get(EnterCodeViewModel::class.java)
        phone_number.text = mPhoneNumber
        enter_input_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val code = enter_input_code.text.toString()
                if (code.length == 6) {
//                    enterCode()
                    mViewModel.enterCode(code, mId, mPhoneNumber) {
                        showToast("Добро пожаловать")
                        (activity as RegisterActivity).mNavigation.navigate(R.id.action_enterCodeFragment_to_mainActivity)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }
}
