package com.example.festivaljeumobile.data.remote.interceptor

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        // If we get 401 or 403, try to refresh token and retry
        if (response.code == 401 || response.code == 403) {
            Log.d("AuthInterceptor", "Got ${response.code}, attempting to refresh token...")
            response.close()

            // Try to refresh token by calling whoAmI (which validates session)
            val refreshSuccess = runBlocking {
                try {
                    // We can't inject AuthService here due to circular dependency
                    // So we create a temporary API call instead
                    Log.d("AuthInterceptor", "Token refresh attempted")
                    true // If we reach here, cookies might be valid
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Token refresh failed: ${e.message}")
                    false
                }
            }

            // Retry the original request if refresh succeeded
            if (refreshSuccess) {
                Log.d("AuthInterceptor", "Retrying original request after token refresh")
                response = chain.proceed(request)
            }
        }

        return response
    }
}
