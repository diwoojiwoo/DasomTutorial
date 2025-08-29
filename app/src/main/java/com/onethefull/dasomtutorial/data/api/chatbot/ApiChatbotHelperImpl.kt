package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialResponseData

/**
 * Created by sjw on 2025. 8. 29.
 */
class ApiChatbotHelperImpl(private val service: ChatbotApiService) : ApiChatbotHelper {
    override suspend fun getMealTutorial(requestData: MealTutorialRequestData): MealTutorialResponseData = service.getMealTutorial(
        requestData
    )
}