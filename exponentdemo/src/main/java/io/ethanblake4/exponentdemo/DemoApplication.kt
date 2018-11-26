package io.ethanblake4.exponentdemo

import android.app.Application
import android.support.annotation.Keep
import io.ethanblake4.exponentcore.Exponent
import io.ethanblake4.exponentcore.model.internal.LogLevel

@Keep
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Exponent.init(this)
        Exponent.logLevel = LogLevel.INFO
    }
}