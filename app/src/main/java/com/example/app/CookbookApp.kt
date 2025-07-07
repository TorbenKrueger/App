package com.example.app

import android.app.Application

class CookbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
