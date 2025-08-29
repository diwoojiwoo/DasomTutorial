package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialResponseData
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by sjw on 2025. 8. 29.
 */
interface ChatbotApiService {
    @POST("/nlp/meal/tutorials")
    suspend fun getMealTutorial(
        @Body body: MealTutorialRequestData,
    ): MealTutorialResponseData
}