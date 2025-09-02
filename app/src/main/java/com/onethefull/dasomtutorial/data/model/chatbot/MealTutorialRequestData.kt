package com.onethefull.dasomtutorial.data.model.chatbot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

/**
 * Created by sjw on 2025. 8. 29.
 */
//    {
//        "clientId" : "jin3137",
//        "customerCode": "alpha",
//        "languageCode": "ko", # ko, en, ja-JP, zh-TW, zh-CN, ....
//        "q": "A-1",
//        "query": "" # 답변이 있는 데이터
//    }
data class MealTutorialRequestData (
    @SerializedName("clientId") @Expose val clientId: String,
    @SerializedName("customerCode") @Expose val customerCode : String,
    @SerializedName("languageCode") @Expose val languageCode : String,
    @SerializedName("q") @Expose val q : String,
    @SerializedName("query") @Expose val query: String?,
    @SerializedName("dayPart") @Expose val dayPart : String?,
    @SerializedName("utcInfo") @Expose val utcInfo: String?
)
