package com.example.gotracker.utils

import android.app.Activity
import com.example.gotracker.dataBase.DataBaseRepository

lateinit var CURRENT_ACTIVITY: Activity
lateinit var CURRENT_PHONE_NUMBER: String
lateinit var REPOSITORY: DataBaseRepository
const val PHONE_NUMBER_KEY = "phone_number"
const val ID_KEY = "id"