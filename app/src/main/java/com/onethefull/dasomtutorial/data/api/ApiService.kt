package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.data.model.check.*
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import io.reactivex.Observable
import retrofit2.http.*
import retrofit2.http.Body

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    /**
     * SOS 연습
     */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/practice_sos")
    suspend fun practiceSos(
        @Path("CUSTOMER_CODE") deviceCode: String,
        @Path("DEVICE_CODE") customerCode: String,
        @QueryMap params: Map<String, String>,
    ): Status

    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/all_connected_user")
    suspend fun getAllConnectedUser(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>,
    ): ConnectedUser

    /**
     * 어르신 정보 가져오기
     */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/get_info")
    fun getElderlyInfo(
        @Path("CUSTOMER_CODE") deviceCode: String,
        @Path("DEVICE_CODE") businessCode: String,
        @Body body: Map<String, String>,
    ): ElderlyList

    /**
     * 5대 기능 호출 횟수 + 1
     * */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/addGuide/{sort}")
    suspend fun addGuide(
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>,
        @Path("sort") sort: String,
    ): GetGuide

    /**
     * 5대 기능 호출 횟수 불러오기
     * */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/getGuide")
    suspend fun getGuide(
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>,
    ): GetGuide

    /**
     * 치매예방 질문 리스트 요청
     */
    @Headers("Content-Type: application/json")
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/dementia/question_list")
    suspend fun getDementiaQuizList(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @QueryMap params: Map<String, String>,
    ): DementiaQuizListResponse

    /**
     * 치매예방 정답 저장
     */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/dementia/log_insert")
    suspend fun insertDementiaQuizLog(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: DementiaQAReq,
    ): Status

    /**
     * 챗봇을 통한 데이터 체크
     */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/log/checkChatBotData")
    suspend fun logCheckChatBotData(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: CheckChatBotDataRequest,
    ): CheckChatBotDataResponse


    /**
     * 카테고리별 메세지 랜덤 추출
     */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/log/getMessageList")
    suspend fun logGetMessageList(
        @Header("languageCode") languageCode: String,
        @Header("characterCode") serviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>,
    ): GetMessageListResponse
}