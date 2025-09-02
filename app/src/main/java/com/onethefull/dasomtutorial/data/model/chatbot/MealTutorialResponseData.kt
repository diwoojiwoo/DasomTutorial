package com.onethefull.dasomtutorial.data.model.chatbot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.onethefull.dasomtutorial.data.model.check.Body

/**
 * Created by sjw on 2025. 8. 29.
 */
//{
//    "datas":{},
//    "hint": "",
//    "q" : "A-1", # 질문에 대한 답변에 대한 질문 번호
//    "status_code": 200 # 성공, 에러: 500
//}
data class MealTutorialResponseData(
    @SerializedName("datas") @Expose val datas: Any?,
    @SerializedName("hint") @Expose var hint: String?,
    @SerializedName("q") @Expose val q: String?,
    @SerializedName("status_code") @Expose val status_code: Int?
)
