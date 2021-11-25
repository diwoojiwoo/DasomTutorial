package com.onethefull.dasomtutorial.data.api

import android.os.Build
import com.onethefull.dasomtutorial.data.model.ConnectedUser
import com.onethefull.dasomtutorial.data.model.Status
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
        return apiService.practiceSos(customerCode, deviceCode, ParamGeneratorUtils.getDeviceId(Build.SERIAL))
    }

    override suspend fun getConnectedUsers(
        customerCode: String,
        deviceCode: String,
    ): ConnectedUser = apiService.getAllConnectedUser(customerCode, deviceCode, ParamGeneratorUtils.getDeviceId(Build.SERIAL))
}