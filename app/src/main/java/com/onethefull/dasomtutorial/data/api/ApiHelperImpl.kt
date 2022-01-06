package com.onethefull.dasomtutorial.data.api

import android.os.Build
import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.utils.ParamGeneratorUtils
import com.onethefull.dasomtutorial.utils.logger.DWLog

/**
 * Created by sjw on 2021/11/10
 */
class ApiHelperImpl(private val apiService: ApiService) : ApiHelper {
    override suspend fun practiceSos(
        customerCode: String,
        deviceCode: String,
    ): Status {
        return apiService.practiceSos(
            customerCode,
            deviceCode,
            ParamGeneratorUtils.getDeviceId(Build.SERIAL))
    }

    override suspend fun getConnectedUsers(
        customerCode: String,
        deviceCode: String,
    ): ConnectedUser = apiService.getAllConnectedUser(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getDeviceId(Build.SERIAL)
    )

    override suspend fun getElderlyInfo(
        customerCode: String,
        deviceCode: String,
    ): ElderlyList = apiService.getElderlyInfo(
        customerCode,
        deviceCode,
        ParamGeneratorUtils.getDeviceId(Build.SERIAL)
    )

    override suspend fun addGuide(customerCode: String, deviceCode: String, sort: String): AddGuide {
        TODO("Not yet implemented")
    }

    override suspend fun getGuide(customerCode: String, deviceCode: String): GetGuide {
        TODO("Not yet implemented")
    }
}