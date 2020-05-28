package com.example.gotracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.gotracker.R
import com.example.gotracker.ui.activities.MainActivity
import com.example.gotracker.ui.activities.RegisterActivity
import com.example.gotracker.utils.AUTH
import com.example.gotracker.utils.replaceActivity
import com.example.gotracker.utils.replaceRegFragment
import com.example.gotracker.utils.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_enter_phone.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class EnterPhoneFragment : Fragment(R.layout.fragment_enter_phone) {

    private lateinit var phoneNumber: String
    private lateinit var callBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_phone, container, false)
    }

    override fun onStart() {
        super.onStart()
        callBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                AUTH.signInWithCredential(credential).addOnCompleteListener{
                    if (it.isSuccessful){
                         showToast("Добро пожаловать")
                        (activity as RegisterActivity).replaceActivity(MainActivity())
                    }else showToast(it.exception?.message.toString())

                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                showToast(p0.message.toString())
            }

            override fun onCodeSent(id: String, tolen: PhoneAuthProvider.ForceResendingToken) {
                replaceRegFragment(EnterCodeFragment(phoneNumber, id))
            }
        }
        register_btn_next.setOnClickListener { sentSMS() }
    }

    private fun sentSMS() {
        if (register_input_phone_number.text.toString().isEmpty()) {
            showToast("введите номер")
        } else {

//            fragmentManager?.beginTransaction()
//                ?.replace(R.id.register_data_container, EnterCodeFragment())
//                ?.addToBackStack(null)
//                ?.commit()
            authUser()

        }

    }

    private fun authUser() {
        phoneNumber = register_input_phone_number.text.toString()
        PhoneAuthProvider.getInstance()
            .verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                activity as RegisterActivity,
                callBack

            )
    }

}
