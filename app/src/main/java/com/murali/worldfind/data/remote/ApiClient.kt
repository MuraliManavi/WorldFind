package com.murali.worldfind.data.remote

import android.os.Build
import com.murali.worldfind.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val DEFAULT_EMULATOR_HOST = "10.0.2.2"
    private const val PHYSICAL_DEVICE_HOST = "10.185.112.210"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private class DynamicHostInterceptor : Interceptor {
        @Volatile
        private var activeHost: String? = null

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()

            // In production/release build or when target URL is HTTPS, proceed directly
            if (!BuildConfig.DEBUG || request.url.isHttps) {
                return chain.proceed(request)
            }

            val isEmulator = isEmulatorDevice()
            val targetHost = if (!isEmulator) {
                activeHost ?: PHYSICAL_DEVICE_HOST
            } else {
                DEFAULT_EMULATOR_HOST
            }

            val originalUrl = request.url
            if (originalUrl.host == DEFAULT_EMULATOR_HOST && originalUrl.host != targetHost) {
                val newUrl = originalUrl.newBuilder()
                    .host(targetHost)
                    .build()
                request = request.newBuilder().url(newUrl).build()
            }

            return try {
                chain.proceed(request)
            } catch (e: IOException) {
                if (!isEmulator) {
                    val fallbackHost = if (targetHost == PHYSICAL_DEVICE_HOST) "127.0.0.1" else PHYSICAL_DEVICE_HOST
                    val retryUrl = originalUrl.newBuilder().host(fallbackHost).build()
                    val retryRequest = request.newBuilder().url(retryUrl).build()
                    try {
                        val response = chain.proceed(retryRequest)
                        activeHost = fallbackHost
                        return response
                    } catch (_: Exception) {
                        throw e
                    }
                }
                throw e
            }
        }

        private fun isEmulatorDevice(): Boolean {
            return (Build.FINGERPRINT.startsWith("generic") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu"))
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(DynamicHostInterceptor())
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: WorldFindApi by lazy {
        val rawUrl = BuildConfig.WORLD_FIND_API_BASE_URL
        val baseUrl = if (rawUrl.endsWith("/")) rawUrl else "$rawUrl/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WorldFindApi::class.java)
    }
}
