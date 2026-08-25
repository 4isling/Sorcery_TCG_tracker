package com.hayse.sorcery

import android.app.Application
import com.hayse.sorcery.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SorceryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SorceryApplication)
            modules(appModules)
        }
    }
}
