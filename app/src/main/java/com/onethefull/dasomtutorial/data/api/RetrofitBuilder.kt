package com.onethefull.dasomtutorial.data.api

import android.util.Log
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.data.api.chatbot.ChatbotApiService
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.ext.dasomLangValue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

/**
 * Created by sjw on 2021/11/10
 */
object RetrofitBuilder {
    private const val BASE_URL = "https://channel.dasomi.ai/API/"
//    private const val BASE_URL = "https://dev.dasomi.ai/API/"

    private const val CHATBOT_BASE_URL = "https://nlp-chat-272203.appspot.com/"

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

    private fun getChatbotRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)   // 연결 시도 10초 제한
            .readTimeout(10, TimeUnit.SECONDS)      // 응답 읽기 10초 제한
            .writeTimeout(10, TimeUnit.SECONDS)     // 요청 쓰기 10초 제한
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor).build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val apiService: ApiService = getRetrofit(BASE_URL).create(ApiService::class.java)
    val chatbotApiService: ChatbotApiService = getChatbotRetrofit(CHATBOT_BASE_URL).create(ChatbotApiService::class.java)
}