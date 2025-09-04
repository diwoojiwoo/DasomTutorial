package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.BedTimeTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.CommonTutorialResponseData
import com.onethefull.dasomtutorial.data.model.chatbot.WakeupTutorialRequestData

/**
 * Created by sjw on 2025. 8. 29.
 */
class ApiChatbotHelperImpl(private val service: ChatbotApiService) : ApiChatbotHelper {
    override suspend fun getMealTutorial(requestData: MealTutorialRequestData): CommonTutorialResponseData = service.getMealTutorial(
        requestData
    )

    override suspend fun getWakeupTutorial(requestData: WakeupTutorialRequestData): CommonTutorialResponseData = service.getWakeupTutorial(
        requestData
    )

    override suspend fun getBedtimeTutorial(requestData: BedTimeTutorialRequestData): CommonTutorialResponseData = service.getBedTimeTutorial(
        requestData
    )

}