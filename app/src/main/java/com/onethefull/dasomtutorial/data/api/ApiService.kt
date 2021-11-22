package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.Status
import retrofit2.http.*

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    @GET("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/practice_sos")
    suspend fun practiceSos(
        @Path("DEVICE_CODE") deviceCode: String,
        @Path("CUSTOMER_CODE") customerCode: String,
        @QueryMap params: Map<String, String>
    ): Status

}