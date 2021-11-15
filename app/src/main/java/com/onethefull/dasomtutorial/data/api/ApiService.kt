package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.ConnectedUser
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by sjw on 2021/11/10
 */
interface ApiService {
    @POST("{CUSTOMER_CODE}/{DEVICE_CODE}/pudding/all_connected_user")
    suspend fun getAllConnectedUser(
        @Path("CUSTOMER_CODE") customerCode: String,
        @Path("DEVICE_CODE") deviceCode: String,
        @Body body: Map<String, String>
    ): ConnectedUser
}