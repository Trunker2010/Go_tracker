package com.example.gotracker.screens.enterPhone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider

import com.example.gotracker.R
import com.example.gotracker.MainActivity
import com.example.gotracker.RegisterActivity
import com.example.gotracker.utils.*
import kotlinx.android.synthetic.main.fragment_enter_phone.*

/**
 * A simple [Fragment] subclass.
 */
class EnterPhoneFragment : Fragment(R.layout.fragment_enter_phone) {

    private lateinit var mPhoneNumber: String
    private lateinit var mViewModel: EnterPhoneViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_enter_phone, container, false)

    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init() {

        mViewModel = ViewModelProvider(this).get(EnterPhoneViewModel::class.java)
        register_btn_next.setOnClickListener {
            val bundle = Bundle()

            mPhoneNumber = register_input_phone_number.text.toString()

            mViewModel.run(fun EnterPhoneViewModel.() = sentSMS(
                { phoneAuthCredential ->
                    AUTH.signInWithCredential(phoneAuthCredential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showToast("Введите номер")
                            (activity as RegisterActivity).replaceActivity(MainActivity())
                        } else showToast(it.exception?.message.toString())

                    }

                },
                { showToast(it.message.toString()) },

                { id, _ ->
                    bundle.putString(PHONE_NUMBER_KEY, mPhoneNumber)
                    bundle.putString(ID_KEY, id)
                    (requireActivity() as RegisterActivity).mNavigation.navigate(
                        R.id.action_enterPhoneFragment_to_enterCodeFragment,
                        bundle
                    )
                },
                mPhoneNumber


            ))
        }

    }

}
