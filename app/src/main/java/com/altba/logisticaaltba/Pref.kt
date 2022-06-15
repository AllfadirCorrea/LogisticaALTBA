package com.altba.logisticaaltba

import android.content.Context


class Pref(val context: Context){

    val SHARED_NAME = "myDB"
    val SHARED_USERNAME = "usuario"
    val SHARED_PASS = "password"
    val storage = context.getSharedPreferences(SHARED_NAME, 0)

    fun saveUser(user:String){
        storage.edit().putString(SHARED_USERNAME, user).apply()
    }

    fun savePass(pass: String){
        storage.edit().putString(SHARED_PASS, pass).apply()
    }

    fun getUser():String{
        return storage.getString(SHARED_USERNAME, "")!!
    }

    fun getPass():String{
        return storage.getString(SHARED_PASS, "")!!
    }

    fun wipe(){
        storage.edit().clear().apply()
    }
}