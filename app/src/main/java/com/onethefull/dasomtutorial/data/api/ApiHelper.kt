package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.ConnectedUser

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun getConnectedUsers(
        customerCode: String,
        deviceCode: String
    ): ConnectedUser
}