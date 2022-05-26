package com.onethefull.dasomtutorial.data.model.check

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.annotations.Nullable
import java.io.Serializable


// {"PUDDING_SERIALNUM" : "PUDDING_SERIALNUM" ,"CATEGORY" : "sleepTime" ,"LANGUAGE_CODE": "kr" ,"TEXT" : "어제 11시 반 쯤에 잤어"}
data class CheckChatBotDataRequest(
    @SerializedName("PUDDING_SERIALNUM") @Expose val puddingSerial: String,
    @SerializedName("CATEGORY") @Expose val category: String,
    @SerializedName("LANGUAGE_CODE") @Expose val languageCode: String,
    @SerializedName("TEXT") @Expose val text: String,
)