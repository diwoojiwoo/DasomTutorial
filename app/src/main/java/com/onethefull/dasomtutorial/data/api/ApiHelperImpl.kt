package com.onethefull.dasomtutorial.data.api

import android.annotation.SuppressLint
import android.os.Build
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.utils.ParamGeneratorUtils
import com.onethefull.dasomtutorial.utils.logger.DWLog
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by sjw on 2021/11/10
 */
class ApiHelperImpl(private val apiService: ApiService) : ApiHelper {
    @SuppressLint("HardwareIds")
    override suspend fun practiceSos(
        customerCode: String,
        deviceCode: String,
    ): Status {
        return apiService.practiceSos(
            customerCode,
            deviceCode,
            ParamGeneratorUtils.getDeviceId(Build.SERIAL)
        )
    }

    @SuppressLint("HardwareIds")
    override suspend fun getConnectedUsers(
        customerCode: String,
        deviceCode: String,
    ): ConnectedUser = apiService.getAllConnectedUser(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getDeviceId(Build.SERIAL)
    )

    @SuppressLint("HardwareIds")
    override suspend fun getElderlyInfo(
        customerCode: String,
        deviceCode: String,
    ): ElderlyList = apiService.getElderlyInfo(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getDeviceId(Build.SERIAL)
    )

    override suspend fun addGuide(
        customerCode: String,
        deviceCode: String,
        sort: String
    ): AddGuide {
        TODO("Not yet implemented")
    }

    override suspend fun getGuide(customerCode: String, deviceCode: String): GetGuide {
        TODO("Not yet implemented")
    }

    @SuppressLint("HardwareIds")
    override suspend fun getDementiaQuizList(
        customerCode: String,
        deviceCode: String,
        limit: String,
    ): DementiaQuizListResponse {
        return apiService.getDementiaQuizList(
            customerCode,
            deviceCode,
            ParamGeneratorUtils.getDementiaQuizListReq(Build.SERIAL, limit)
        )
    }

    override suspend fun insertDementiaQuizLog(
        customerCode: String,
        deviceCode: String,
        dementiaQAReq: DementiaQAReq,
    ): Status = apiService.insertDementiaQuizLog(
        customerCode,
        deviceCode,
        dementiaQAReq
    )

    override suspend fun logCheckChatBotData(
        customerCode: String,
        deviceCode: String,
        checkChatBotDataRequest: CheckChatBotDataRequest,
    ): CheckChatBotDataResponse = apiService.logCheckChatBotData(
        customerCode,
        deviceCode,
        checkChatBotDataRequest
    )

    @SuppressLint("HardwareIds")
    override suspend fun logGetMessageList(
        customerCode: String,
        deviceCode: String,
        category: String
    ): GetMessageListResponse = apiService.logGetMessageList(
        when (App.instance.getLocale()) {
            Locale.US -> "en-US"
            else -> "ko-KR"
        },
//        when (BuildConfig.LANGUAGE_TYPE) { // ko-KR(default), en-US
//            "EN" -> "en-US "
//            else -> "ko-KR"
//        },
        when (BuildConfig.PRODUCT_TYPE) { // Dasom, Secretary
            "WONDERFUL" -> "Dasom"
            else -> "Dasom"
        },
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getCategory(category)
    )

    override suspend fun check204(): Boolean {
        val cc = CheckConnection("http://clients3.google.com/generate_204")
        return try {
            cc.start()
            cc.join()
            cc.isSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    internal inner class CheckConnection(private val host: String) : Thread() {
        var isSuccess = false
        override fun run() {
            var urlConnection: HttpURLConnection? = null
            try {
                sleep(500)
                urlConnection = URL(host).openConnection() as HttpURLConnection
                urlConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"))
                urlConnection.connectTimeout = 1000
                urlConnection.connect()
                val responseCode = urlConnection.responseCode
                if (responseCode == 204) {
                    isSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            urlConnection?.disconnect()
        }
    }
}