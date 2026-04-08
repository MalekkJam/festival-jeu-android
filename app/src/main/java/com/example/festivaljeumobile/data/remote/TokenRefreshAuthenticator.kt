package com.example.festivaljeumobile.data.remote

import okhttp3.Authenticator
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

class TokenRefreshAuthenticator(
    private val baseUrl: String,
    private val cookieJar: CookieJar,
) : Authenticator {

    private val refreshLock = Any()

    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .callTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path == "/api/auth/refresh" || path == "/api/auth/login" || path == "/api/auth/logout") {
            return null
        }

        if (response.responseCount >= MAX_AUTH_ATTEMPTS) {
            return null
        }

        return synchronized(refreshLock) {
            if (refreshAccessToken()) {
                response.request.newBuilder().build()
            } else {
                null
            }
        }
    }

    private fun refreshAccessToken(): Boolean {
        val request = Request.Builder()
            .url("${baseUrl}api/auth/refresh")
            .post(ByteArray(0).toRequestBody())
            .build()

        return refreshClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }

    private val Response.responseCount: Int
        get() {
            var count = 1
            var priorResponse = this.priorResponse
            while (priorResponse != null) {
                count++
                priorResponse = priorResponse.priorResponse
            }
            return count
        }

    private companion object {
        private const val MAX_AUTH_ATTEMPTS = 2
        private const val REFRESH_TIMEOUT_SECONDS = 10L
    }
}
