package io.ethanblake4.exponentcore

import android.content.Context
import android.os.Build
import android.support.annotation.Keep
import android.util.Log
import io.ethanblake4.exponentcore.model.LogLevel
import io.ethanblake4.exponentcore.util.IdentifiersUtil
import okhttp3.OkHttpClient

object Exponent {

    private const val TAG = "Exponent"

    lateinit var client: OkHttpClient

    /**
     * Default logger that prints log messages to the Android log.
     */
    private var defaultLog =  { message: String, level: LogLevel, err: Throwable? ->
        when {
            level.ordinal < logLevel.ordinal -> -1
            err == null -> when(level) {
                LogLevel.INFO -> Log.i(TAG, message)
                LogLevel.DEBUG -> Log.d(TAG, message)
                LogLevel.WARN -> Log.w(TAG, message)
                LogLevel.ERROR -> Log.e(TAG, message)
            }
            else -> when(level) {
                LogLevel.INFO -> Log.i(TAG, message, err)
                LogLevel.DEBUG -> Log.d(TAG, message, err)
                LogLevel.WARN -> Log.w(TAG, message, err)
                LogLevel.ERROR -> Log.e(TAG, message, err)
            }
        }
    }

    var logger: (String, LogLevel, Throwable?) -> Int = defaultLog
    var logLevel = LogLevel.DEBUG
    var loginSDKVersion = Build.VERSION.SDK_INT
    var loginUserAgent = IdentifiersUtil.makeUserAgent(
            "GoogleLoginService/", "1.3", Build.PRODUCT, Build.ID, false)
    lateinit var androidID: String
    lateinit var gsfID: String
    var deviceCountryCode = IdentifiersUtil.getDeviceCountryCode()
    var deviceLanguage = IdentifiersUtil.getDeviceLanguage()
    lateinit var operatorCountryCode: String

    /**
     * Initializes properties that need to be set with a [Context]
     */
    @Keep
    fun init(context: Context) {
        androidID = IdentifiersUtil.getAndroidID(context)
        client = OkHttpClient.Builder().build()
        operatorCountryCode = IdentifiersUtil.getOperatorCountryCode(context)
        gsfID = IdentifiersUtil.getGservicesID(context, true)
    }


}