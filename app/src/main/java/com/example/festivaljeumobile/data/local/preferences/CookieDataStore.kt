package com.example.festivaljeumobile.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.festivaljeumobile.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Cookie

private val Context.cookieDataStore by preferencesDataStore(name = "cookies")

class CookieDataStore(
    private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun readCookies(): Map<String, List<Cookie>> {
        val preferences = context.cookieDataStore.data.first()
        return preferences.asMap()
            .mapNotNull { (key, value) ->
                if (!key.name.startsWith(COOKIE_PREFIX)) {
                    return@mapNotNull null
                }
                val host = key.name.removePrefix(COOKIE_PREFIX)
                val serialized = value as? String ?: return@mapNotNull null
                host to deserializeCookies(serialized)
            }
            .filter { (host, _) -> host.isNotBlank() }
            .toMap()
    }

    suspend fun hasValidCookies(): Boolean =
        readCookies().values.any { cookies -> cookies.isNotEmpty() }

    suspend fun readUserRole(): UserRole? {
        val preferences = context.cookieDataStore.data.first()
        val roleValue = preferences[USER_ROLE_KEY] ?: return null
        return runCatching { UserRole.valueOf(roleValue) }.getOrNull()
    }

    suspend fun writeUserRole(role: UserRole?) {
        context.cookieDataStore.edit { preferences ->
            if (role == null) {
                preferences.remove(USER_ROLE_KEY)
            } else {
                preferences[USER_ROLE_KEY] = role.name
            }
        }
    }

    suspend fun writeCookies(cookiesByHost: Map<String, List<Cookie>>) {
        context.cookieDataStore.edit { preferences ->
            clearCookiePreferences(preferences)
            cookiesByHost.forEach { (host, cookies) ->
                preferences[stringPreferencesKey("$COOKIE_PREFIX$host")] = serializeCookies(cookies)
            }
        }
    }

    suspend fun clearCookies() {
        context.cookieDataStore.edit { preferences ->
            clearCookiePreferences(preferences)
            preferences.remove(USER_ROLE_KEY)
        }
    }

    private fun clearCookiePreferences(preferences: MutablePreferences) {
        val keysToRemove = preferences.asMap().keys.filter { it.name.startsWith(COOKIE_PREFIX) }
        keysToRemove.forEach { key ->
            preferences.remove(key)
        }
    }

    private fun serializeCookies(cookies: List<Cookie>): String =
        json.encodeToString(cookies.map { it.toPersistedCookie() })

    private fun deserializeCookies(serialized: String): List<Cookie> =
        json.decodeFromString<List<PersistedCookie>>(serialized)
            .mapNotNull { it.toCookieOrNull() }

    @Serializable
    private data class PersistedCookie(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean,
    )

    private fun Cookie.toPersistedCookie(): PersistedCookie =
        PersistedCookie(
            name = name,
            value = value,
            expiresAt = expiresAt,
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
            hostOnly = hostOnly
        )

    private fun PersistedCookie.toCookieOrNull(): Cookie? {
        if (expiresAt <= System.currentTimeMillis()) {
            return null
        }

        return try {
            Cookie.Builder()
                .name(name)
                .value(value)
                .apply {
                    if (hostOnly) {
                        hostOnlyDomain(domain)
                    } else {
                        domain(domain)
                    }
                    path(path)
                    expiresAt(expiresAt)
                    if (secure) secure()
                    if (httpOnly) httpOnly()
                }
                .build()
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    companion object {
        private const val COOKIE_PREFIX = "cookie_"
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }
}
