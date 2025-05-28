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

/**
 * Created by sjw on 2021/11/10
 */
object RetrofitBuilder {
    private const val BASE_URL = "https://channel.dasomi.ai/API/"
//    private const val BASE_URL = "https://dev.dasomi.ai/API/"

    private var ApiLogger = HttpLoggingInterceptor.Logger { message -> DWLog.d(message) }

    // Logging Interceptor
    val loggingInterceptor = HttpLoggingInterceptor(ApiLogger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Header Interceptor
    val headerInterceptor = okhttp3.Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("lang", App.instance.getLocale()?.dasomLangValue() ?: "ko")
            .build()
        chain.proceed(newRequest)
    }

    private fun getRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}