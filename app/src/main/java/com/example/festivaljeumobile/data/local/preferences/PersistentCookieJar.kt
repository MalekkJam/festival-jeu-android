package com.example.festivaljeumobile.data.local.preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(
    private val cookieDataStore: CookieDataStore,
) : CookieJar {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cookiesByHost = ConcurrentHashMap<String, List<Cookie>>()

    fun initialize() {
        val persistedCookies = runBlocking(Dispatchers.IO) {
            cookieDataStore.readCookies()
        }
        cookiesByHost.clear()
        cookiesByHost.putAll(persistedCookies)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) {
            return
        }

        val currentCookies = cookiesByHost[url.host].orEmpty()
        val incomingCookies = cookies.filterNot { it.expiresAt <= System.currentTimeMillis() }
        val incomingKeys = cookies.map { it.cookieKey() }.toSet()
        val keptCookies = currentCookies.filterNot { it.cookieKey() in incomingKeys }

        cookiesByHost[url.host] = keptCookies + incomingCookies
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val validCookies = cookiesByHost[url.host]
            .orEmpty()
            .filterNot { it.expiresAt <= System.currentTimeMillis() }

        if (validCookies.size != cookiesByHost[url.host].orEmpty().size) {
            cookiesByHost[url.host] = validCookies
            persist()
        }

        return validCookies
    }

    suspend fun clear() {
        cookiesByHost.clear()
        cookieDataStore.clearCookies()
    }

    private fun persist() {
        val snapshot = cookiesByHost
            .mapValues { (_, cookies) -> cookies.filterNot { it.expiresAt <= System.currentTimeMillis() } }
            .filterValues { it.isNotEmpty() }

        scope.launch {
            cookieDataStore.writeCookies(snapshot)
        }
    }

    private fun Cookie.cookieKey(): String = "$name|$domain|$path"
}
