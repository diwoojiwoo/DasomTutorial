package com.onethefull.dasomtutorial.data.api.vital

import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioRequest
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioResponse

/**
 * Created by sjw on 2023/08/09
 */
class ChatApiHelperImpl(private val chatApiService: ChatApiService) : ChatApiHelper {
    override suspend fun requestVitalScenario(
        request: VitalScenarioRequest
    ): VitalScenarioResponse = chatApiService.requestVitalScenario(request)
}