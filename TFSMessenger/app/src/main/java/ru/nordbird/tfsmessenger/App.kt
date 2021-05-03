package ru.nordbird.tfsmessenger

import android.app.Application
import android.content.Context
import ru.nordbird.tfsmessenger.di.GlobalDI

class App : Application() {

    companion object {
        private var instance: App? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
        GlobalDI.init(this)
    }

}