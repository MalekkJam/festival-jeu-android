package com.example.festivaljeumobile.data.remote

import android.content.Context
import com.example.festivaljeumobile.data.local.preferences.CookieDataStore
import com.example.festivaljeumobile.data.local.preferences.PersistentCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:4000/"

    @Volatile
    private var initialized = false

    private lateinit var cookieJar: PersistentCookieJar

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        check(initialized) { "RetrofitInstance.initialize(context) must be called before using retrofit." }
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .callTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .authenticator(
                TokenRefreshAuthenticator(
                    baseUrl = BASE_URL,
                    cookieJar = cookieJar
                )
            )
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun initialize(context: Context) {
        if (initialized) return

        synchronized(this) {
            if (initialized) return
            cookieJar = PersistentCookieJar(
                cookieDataStore = CookieDataStore(context.applicationContext)
            ).also { it.initialize() }
            initialized = true
        }
    }

    suspend fun clearCookies() {
        if (initialized) {
            cookieJar.clear()
        }
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    private const val NETWORK_TIMEOUT_SECONDS = 20L
}
