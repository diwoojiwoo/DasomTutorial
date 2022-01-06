package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.ConnectedUser
import com.onethefull.dasomtutorial.data.model.ElderlyList
import com.onethefull.dasomtutorial.data.model.GetGuide
import com.onethefull.dasomtutorial.data.model.Status
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    /**
     * SOS 연습
     */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/practice_sos")
    suspend fun practiceSos(
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>
    ): Status

    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/all_connected_user")
    suspend fun getAllConnectedUser(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>
    ): ConnectedUser

    /**
     * 어르신 정보 가져오기
     */
    @Headers("Content-Type: application/json")
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/get_info")
    fun getElderlyInfo(
        @Path("CUSTOMER_CODE") deviceCode: String,
        @Path("DEVICE_CODE") businessCode: String,
        @Body body: Map<String, String>
    ): ElderlyList

    /**
     * 5대 기능 호출 횟수 + 1
     * */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/addGuide/{sort}")
    suspend fun addGuide (
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>,
        @Path("sort") sort: String,
    ) : GetGuide

    /**
     * 5대 기능 호출 횟수 불러오기
     * */
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/elderly/getGuide")
    suspend fun getGuide (
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>
    ) : GetGuide
}