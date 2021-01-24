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
import com.example.gotracker.databinding.FragmentEnterCodeBinding
import com.example.gotracker.utils.*


/**
 * A simple [Fragment] subclass.
 */
class EnterCodeFragment() :
    Fragment() {

    lateinit var mPhoneNumber: String
    lateinit var mViewModel: EnterCodeViewModel
    lateinit var mId: String
    private var _binding: FragmentEnterCodeBinding? = null
    val mBinding: FragmentEnterCodeBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnterCodeBinding.inflate(inflater, container, false)


        mPhoneNumber = arguments?.getString(PHONE_NUMBER_KEY).toString()
        mId = arguments?.getString(ID_KEY).toString()

        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        init()

    }


    private fun init() {
        mViewModel = ViewModelProvider(this).get(EnterCodeViewModel::class.java)
        mBinding.phoneNumber.text = mPhoneNumber
        mBinding.enterInputCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val code = mBinding.enterInputCode.text.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
