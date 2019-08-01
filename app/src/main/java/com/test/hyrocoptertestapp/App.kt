package com.test.hyrocoptertestapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(com.test.hyrocoptertestapp.di.modules)
            androidContext(applicationContext)
        }
        Timber.plant(Timber.DebugTree())
    }
}