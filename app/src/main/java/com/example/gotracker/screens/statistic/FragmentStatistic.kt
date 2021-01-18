package com.example.gotracker.screens.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gotracker.R

/**
 * A simple [Fragment] subclass.
 */
class FragmentStatistic : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }
    companion object{
        fun newInstance () : FragmentStatistic {
            return FragmentStatistic()
        }
    }

}
