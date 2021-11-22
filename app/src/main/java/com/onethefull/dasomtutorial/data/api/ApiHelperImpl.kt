package com.onethefull.dasomtutorial.data.api

import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.utils.ParamGeneratorUtils

/**
 * Created by sjw on 2021/11/10
 */
class ApiHelperImpl(private val apiService: ApiService) : ApiHelper {
    override suspend fun practiceSos(
        customerCode: String,
        deviceCode: String
    ): Status = apiService.practiceSos(customerCode, deviceCode, ParamGeneratorUtils.getDeviceId())
}