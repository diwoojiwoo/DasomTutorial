package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialResponseData

/**
 * Created by sjw on 2025. 8. 29.
 */
interface ApiChatbotHelper {
    suspend fun getMealTutorial(
        requestData: MealTutorialRequestData
    ) : MealTutorialResponseData
}