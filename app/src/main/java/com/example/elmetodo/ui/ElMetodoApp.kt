package com.example.elmetodo.ui

import android.app.Application
import com.example.elmetodo.core.appModule
import com.example.elmetodo.core.dataModule
import com.example.elmetodo.core.viewModelModule
import com.example.elmetodo.data.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ElMetodoApp: Application() {
    companion object {
        lateinit var preferences: Preferences
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ElMetodoApp)
            modules(appModule, dataModule, viewModelModule)
        }

        preferences = Preferences(applicationContext)
    }
}