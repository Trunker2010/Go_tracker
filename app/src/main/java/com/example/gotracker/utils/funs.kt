package com.example.gotracker.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gotracker.R
import android.content.Intent

fun AppCompatActivity.ReplaceFragment(fragment: Fragment) {
    if (fragment.isAdded) {
        return
    } else {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mine_fragment, fragment)

            .commit()
    }

}
fun AppCompatActivity.replaceActivity(activity: AppCompatActivity){
    val intent = Intent(this, activity::class.java)
    startActivity(intent)
    this.finish()
}
fun Fragment.replaceRegFragment(fragment: Fragment){
    this.fragmentManager?.beginTransaction()
        ?.addToBackStack(null)
        ?.replace(R.id.register_data_container,
            fragment
        )?.commit()
}

fun Fragment.showToast(text: String) {
    Toast.makeText(
        this.context,
        text,
        Toast.LENGTH_SHORT
    ).show()

}