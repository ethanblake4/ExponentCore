package io.ethanblake4.exponentcore

import android.content.Context
import android.os.Build
import android.support.annotation.Keep
import android.util.Log
import io.ethanblake4.exponentcore.hl.util.GzipRequestInterceptor
import io.ethanblake4.exponentcore.model.hlapi.SharedPrefsTokenRegistry
import io.ethanblake4.exponentcore.model.hlapi.TokenRegistry
import io.ethanblake4.exponentcore.model.internal.LogLevel
import io.ethanblake4.exponentcore.util.IdentifiersUtil
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor

object Exponent {

    private const val TAG = "Exponent"

    /**
     * Default logger that prints log messages to the Android log.
     */
    private var defaultLog =  { message: String, level: LogLevel, err: Throwable? ->
        Log.d(TAG, message + level.ordinal.toString())
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

    @JvmStatic lateinit var client: OkHttpClient

    @JvmStatic var logger: (String, LogLevel, Throwable?) -> Int = defaultLog
    @JvmStatic var logLevel = LogLevel.DEBUG
    @JvmStatic var loginSDKVersion = 17
    @JvmStatic var loginUserAgent = IdentifiersUtil.makeUserAgent(
            "GoogleLoginService", "1.3", Build.PRODUCT, Build.ID, true)
    @JvmStatic lateinit var androidID: String
    @JvmStatic lateinit var gsfID: String
    @JvmStatic var deviceCountryCode = IdentifiersUtil.getDeviceCountryCode()
    @JvmStatic var deviceLanguage = IdentifiersUtil.getDeviceLanguage()
    @JvmStatic lateinit var operatorCountryCode: String
    @JvmStatic lateinit var tokenRegistry: TokenRegistry

    fun compatibleConnectionSpec(): ConnectionSpec {
        return ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA
                )
                .tlsVersions(TlsVersion.TLS_1_2)
                .supportsTlsExtensions(true)
                .build()
    }

    /**
     * Initializes properties that need to be set with a [Context]
     */
    @Keep
    @JvmStatic fun init(context: Context) {
        androidID = IdentifiersUtil.getAndroidID(context)
        val lic = HttpLoggingInterceptor()
        lic.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
                .connectionSpecs(listOf(compatibleConnectionSpec()))
                .addInterceptor(lic)
                .addInterceptor(GzipRequestInterceptor())
                .build()
        operatorCountryCode = "us"
        gsfID = IdentifiersUtil.getGservicesID(context, true)
        tokenRegistry = SharedPrefsTokenRegistry(
                context.getSharedPreferences("Exponent", Context.MODE_PRIVATE))
    }

}