package com.example.gotracker.screens.enterCode

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.gotracker.MainActivity
import com.example.gotracker.RegisterActivity
import com.example.gotracker.utils.*
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_enter_code.*

class EnterCodeViewModel(application: Application) : AndroidViewModel(application) {
    fun enterCode(code: String, id: String, phoneNumber: String, onComplete: () -> Unit) {
        val credential = PhoneAuthProvider.getCredential(id, code)


        AUTH.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = AUTH.currentUser?.uid.toString()
                val dateMap = mutableMapOf<String, Any>()
                dateMap[CHILD_ID] = uid
                dateMap[CHILD_PHONE] = phoneNumber
                dateMap[CHILD_USERNAME] = uid
                REF_DATABASE_ROOT.child(NODE_USERS).child(uid).updateChildren(dateMap)
                    .addOnCompleteListener { it ->
                        if (it.isSuccessful) {

                            onComplete()


                        } else
                            Toast.makeText(
                                getApplication(),
                                it.exception?.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()

                    }


            } else
                Toast.makeText(
                    getApplication(),
                    it.exception?.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()

        }

    }
}