package com.onethefull.dasomtutorial.data.api.vital

import com.onethefull.dasomtutorial.data.model.*
import com.onethefull.dasomtutorial.data.model.check.*
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioRequest
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioResponse
import retrofit2.http.*
import retrofit2.http.Body

/**
 * Created by sjw on 2023/08/09
 */
interface ChatApiService {
    /**
     * 에어리퀴드 시나리오 질문 호출
     */
    @Headers("Content-Type: application/json")
    @POST("nlp/vital/scenario")
    suspend fun requestVitalScenario(
        @Body body: VitalScenarioRequest,
    ): VitalScenarioResponse
}