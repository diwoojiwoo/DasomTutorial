package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.Status

/**
 * Created by sjw on 2021/11/10
 */
interface ApiHelper {
    suspend fun practiceSos(
        customerCode: String,
        deviceCode: String
    ): Status
}