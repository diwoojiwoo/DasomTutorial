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
//        "foodMain": {
//            "category": "food",
//            "food": "김치 볶음밥",
//            "logic_execution_time": "10:00:00"
//        }
//    }
data class MealTutorialRequestData (
    @SerializedName("clientId") @Expose val clientId: String,
    @SerializedName("customerCode") @Expose val customerCode : String,
    @SerializedName("languageCode") @Expose val languageCode : String,
    @SerializedName("q") @Expose val q : String,
    @SerializedName("query") @Expose val query: String?,
    @SerializedName("dayPart") @Expose val dayPart : String?,
    @SerializedName("utcInfo") @Expose val utcInfo: String?,
    @SerializedName("foodMain") @Expose val foodMain: FoodMain?,
)

data class FoodMain(
    @SerializedName("category") @Expose val category: String,
    @SerializedName("food") @Expose val food: String,
    @SerializedName("logic_execution_time") @Expose val logic_execution_time: String,
)