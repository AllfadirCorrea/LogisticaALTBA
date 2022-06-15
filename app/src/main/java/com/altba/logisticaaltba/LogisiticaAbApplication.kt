package com.altba.logisticaaltba

import android.app.Application

class LogisiticaAbApplication : Application() {
    companion object{
        lateinit var pref: Pref
    }

    override fun onCreate() {
        super.onCreate()
        pref = Pref(applicationContext)
    }
}