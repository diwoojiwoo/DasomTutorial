package com.onethefull.dasomtutorial.data.api

import android.util.Log
import com.onethefull.dasomtutorial.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Created by sjw on 2021/11/10
 */
object RetrofitBuilder {
    private const val BASE_URL = "https://channel.dasomi.ai/API/"
//        private const val BASE_URL = "https://dev.dasomi.ai/API/"
    var ApiLogger = HttpLoggingInterceptor.Logger { message -> Log.d(App.TAG, message) }

    private fun getRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor(ApiLogger)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val trustManager = getUnsafeTrustManager()
        val sslSocketFactory = getNukeSSLSocketFactory(trustManager)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun getUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun getNukeSSLSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        return sslContext.socketFactory
    }

    val apiService: ApiService = getRetrofit().create(ApiService::class.java)
}