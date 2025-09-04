package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.BedTimeTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.CommonTutorialResponseData
import com.onethefull.dasomtutorial.data.model.chatbot.WakeupTutorialRequestData
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by sjw on 2025. 8. 29.
 */
interface ChatbotApiService {
    /**
     * 식사 튜토리얼
     * */
    @POST("/nlp/meal/tutorials")
    suspend fun getMealTutorial(
        @Body body: MealTutorialRequestData,
    ): CommonTutorialResponseData

    /**
     * 기상 튜토리얼
     */
    @POST("/nlp/wakeup/tutorials")
    suspend fun getWakeupTutorial(
        @Body body: WakeupTutorialRequestData,
    ): CommonTutorialResponseData

    /**
     * 취침 튜토리얼
     */
    @POST("/nlp/bedtime/tutorials")
    suspend fun getBedTimeTutorial(
        @Body body: BedTimeTutorialRequestData,
    ): CommonTutorialResponseData
}