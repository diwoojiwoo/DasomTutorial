package com.onethefull.dasomtutorial.di

import android.util.Log
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.ApiService
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.ui.learn.LearnViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

val appModule = module {


    single<ApiService>{
        val apiLogger = HttpLoggingInterceptor.Logger { message -> Log.d(App.TAG, message) }
        val interceptor = HttpLoggingInterceptor(apiLogger)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(interceptor).build()

        Retrofit.Builder()
            .baseUrl("https://channel.dasomi.ai/API/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }
    single<ApiHelper> { ApiHelperImpl(get()) }

    single { LearnRepository(androidApplication(),get()) }

    viewModel { LearnViewModel(androidApplication(),get()) }
    viewModel { BaseViewModel() }
}