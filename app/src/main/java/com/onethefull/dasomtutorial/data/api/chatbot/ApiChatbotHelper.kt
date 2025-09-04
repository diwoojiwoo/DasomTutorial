package com.onethefull.dasomtutorial.data.api.chatbot

import com.onethefull.dasomtutorial.data.model.chatbot.BedTimeTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.CommonTutorialResponseData
import com.onethefull.dasomtutorial.data.model.chatbot.WakeupTutorialRequestData

/**
 * Created by sjw on 2025. 8. 29.
 */
interface ApiChatbotHelper {
    suspend fun getMealTutorial(
        requestData: MealTutorialRequestData
    ) : CommonTutorialResponseData

    suspend fun getWakeupTutorial(
        requestData: WakeupTutorialRequestData
    ) : CommonTutorialResponseData

    suspend fun getBedtimeTutorial(
        requestData: BedTimeTutorialRequestData
    ) : CommonTutorialResponseData
}