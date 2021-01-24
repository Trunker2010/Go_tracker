package com.example.gotracker.screens.enterCode

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions

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


    private fun init() {
        mViewModel = ViewModelProvider(this).get(EnterCodeViewModel::class.java)
        phone_number.text = mPhoneNumber
        enter_input_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val code = enter_input_code.text.toString()
                if (code.length == 6) {
                    mViewModel.enterCode(code, mId, mPhoneNumber) {
                        showToast("Добро пожаловать")


                        val extras = ActivityNavigator.Extras.Builder()
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .build()

                        val action =
                            EnterCodeFragmentDirections.actionEnterCodeFragmentToMainActivity()


                        (activity as RegisterActivity).mNavigation.navigate(
                            action,
                            extras


                        )

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
