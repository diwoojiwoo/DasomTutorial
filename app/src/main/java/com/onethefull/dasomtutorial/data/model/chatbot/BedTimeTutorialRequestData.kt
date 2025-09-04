package com.onethefull.dasomtutorial.data.model.chatbot

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

/**
 * Created by sjw on 2025. 8. 29.
 */
//{
//    "clientId" : "jin3137",
//    "customerCode": "alpha",
//    "languageCode": "ko",
//    "utcInfo": "+09:00"
//    "bedTime":"23:00",
//    "q": "A-2",
//    "query": "음 틀렸어"
//}
data class BedTimeTutorialRequestData (
    @SerializedName("clientId") @Expose val clientId: String,
    @SerializedName("customerCode") @Expose val customerCode : String,
    @SerializedName("languageCode") @Expose val languageCode : String,
    @SerializedName("q") @Expose val q : String,
    @SerializedName("query") @Expose val query: String?,
    @SerializedName("bedTime") @Expose val bedTime : String?,
    @SerializedName("utcInfo") @Expose val utcInfo: String?
)
