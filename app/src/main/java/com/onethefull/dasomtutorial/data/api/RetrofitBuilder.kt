package com.onethefull.dasomtutorial.data.api

import android.util.Log
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.ext.dasomLangValue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitBuilder {
    // 기본 URL (Provider 없을 경우 fallback)
    private const val DEFAULT_BASE_URL = "https://channel.dasomi.ai/API/"

    private var apiLogger = HttpLoggingInterceptor.Logger { message -> DWLog.d(message) }

    // Logging Interceptor
    val loggingInterceptor = HttpLoggingInterceptor(apiLogger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Header Interceptor
    val headerInterceptor = okhttp3.Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("lang", App.instance.getLocale()?.dasomLangValue() ?: "ko")
            .build()
        chain.proceed(newRequest)
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl) // 반드시 끝에 /
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // var로 선언해서 changeHost() 호출 시 재할당 가능
    var apiService: ApiService = getRetrofit(DEFAULT_BASE_URL).create(ApiService::class.java)

    /**
     * Provider에서 host를 가져와 apiService 변경
     */
    fun changeHost() {
        val host = App.instance.provider.getHostUrl()
        val normalizedHost = if (host.isNotEmpty()) host.trimEnd('/') + "/API/" else DEFAULT_BASE_URL
        DWLog.d("RetrofitBuilder changeHost -> $normalizedHost")
        apiService = getRetrofit(normalizedHost).create(ApiService::class.java)
    }

    /**
     * 초기화 시점에 provider host 적용
     */
    init {
        changeHost()
    }
}