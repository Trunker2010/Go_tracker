package com.example.gotracker.screens.enterPhone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gotracker.MainActivity
import com.example.gotracker.R
import com.example.gotracker.RegisterActivity
import com.example.gotracker.databinding.FragmentEnterPhoneBinding
import com.example.gotracker.utils.*


/**
 * A simple [Fragment] subclass.
 */
class EnterPhoneFragment : Fragment(R.layout.fragment_enter_phone) {

    private lateinit var mPhoneNumber: String
    private lateinit var mViewModel: EnterPhoneViewModel
    private var _binding: FragmentEnterPhoneBinding? = null
    private val mBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnterPhoneBinding.inflate(inflater, container, false)


        return mBinding.root

    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init() {

        mViewModel = ViewModelProvider(this).get(EnterPhoneViewModel::class.java)
        mBinding.registerBtnNext.setOnClickListener {
            val bundle = Bundle()

            mPhoneNumber = mBinding.registerInputPhoneNumber.text.toString()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
