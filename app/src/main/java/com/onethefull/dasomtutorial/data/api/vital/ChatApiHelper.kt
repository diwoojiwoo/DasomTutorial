package com.onethefull.dasomtutorial.data.api.vital

import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioRequest
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioResponse

/**
 * Created by sjw on 2023/08/09
 */
interface ChatApiHelper {
    suspend fun requestVitalScenario(
        request: VitalScenarioRequest,
    ): VitalScenarioResponse
}