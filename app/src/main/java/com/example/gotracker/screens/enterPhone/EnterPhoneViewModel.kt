package com.example.gotracker.screens.enterPhone

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.gotracker.utils.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class EnterPhoneViewModel(app: Application) : AndroidViewModel(app) {


    fun sentSMS(
        onVerificationCompleted: (phoneAuthCredential: PhoneAuthCredential) -> Unit,
        onVerificationFailed: (firebaseException: FirebaseException) -> Unit,
        onCodeSent: (p0: String, p1: PhoneAuthProvider.ForceResendingToken) -> Unit,
        phoneNumber:String
    ) {
        if (phoneNumber.isEmpty()) {
            Toast.makeText(getApplication(), "Введите номер", Toast.LENGTH_SHORT).show()
        } else {

            authUser(onVerificationCompleted, onVerificationFailed, onCodeSent, phoneNumber)

        }


    }

    private fun authUser(
        onVerificationCompleted: (phoneAuthCredential: PhoneAuthCredential) -> Unit,
        onVerificationFailed: (firebaseException: FirebaseException) -> Unit,
        onCodeSent: (id: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        phoneNumber: String
    ) {

        val options = PhoneAuthOptions.newBuilder(AUTH)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(CURRENT_ACTIVITY)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    onVerificationCompleted(phoneAuthCredential)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    onVerificationFailed(exception)
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    onCodeSent(id,token)
                }

            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}