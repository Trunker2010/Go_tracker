package com.example.gotracker.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gotracker.R

fun AppCompatActivity.ReplaceFragment(fragment: Fragment) {
   supportFragmentManager.beginTransaction()
        .replace(R.id.mine_fragment, fragment)
        .commit()
}