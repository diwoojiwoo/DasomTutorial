package com.onethefull.dasomtutorial.data.model.chatbot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.onethefull.dasomtutorial.data.model.check.Body

/**
 * Created by sjw on 2025. 8. 29.
 */
//{
//    "category" : "food",
//    "entity": "김치찌개",
//    "entity_time" : "23:00:00", # 사용자 답변 시간
//    "extract_time" : "13:00:12", # 로직 수행 시간
//    "porn": "p", # p 긍정, n 부정
//    "answer_or_not" : "y", # y 답변 일치, n 답변 불일치
//    "q_a" : "A-1", # 질문에 대한 답변에 대한 질문 번호
//}
data class MealTutorialResponseData(
    @SerializedName("category") @Expose var category: String,
    @SerializedName("entity") @Expose var entity: String,
    @SerializedName("entity_time") @Expose val entityTime: String,
    @SerializedName("extract_time") @Expose val extractTime: String,
    @SerializedName("porn") @Expose val porn: String,
    @SerializedName("answer_or_not") @Expose val answerOrNot: String,
    @SerializedName("q_a") @Expose val qA: String
)
