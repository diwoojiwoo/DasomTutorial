package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun practiceSos(
        customerCode: String,
        deviceCode: String,
    ): Status

    suspend fun getConnectedUsers(
        customerCode: String,
        deviceCode: String,
    ): ConnectedUser

    suspend fun getElderlyInfo(
        customerCode: String,
        deviceCode: String,
    ): ElderlyList


    suspend fun getDementiaQuizList(
        customerCode: String,
        deviceCode: String,
        limit: String,
    ): DementiaQuizListResponse

    suspend fun insertDementiaQuizLog(
        customerCode: String,
        deviceCode: String,
        dementiaQAReq: DementiaQAReq,
    ): Status

    suspend fun logCheckChatBotData(
        customerCode: String,
        deviceCode: String,
        checkChatBotDataRequest: CheckChatBotDataRequest,
    ): CheckChatBotDataResponse

    suspend fun logGetMessageList(
        customerCode: String,
        deviceCode: String,
        category: String
    ): GetMessageListResponse

    suspend fun check204(): Boolean
}