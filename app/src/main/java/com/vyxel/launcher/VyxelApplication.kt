package com.vyxel.launcher

import android.app.Application

class VyxelApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: VyxelApplication
            private set
    }
}
