package com.onethefull.dasomtutorial.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by sjw on 2021/11/10
 */

data class UserInfo(
    @SerializedName("USER_TEL") @Expose var user_tel: String,
    @SerializedName("USER_UUID") @Expose var user_uuid: String,
    @SerializedName("USER_IMG") @Expose var user_img: String,
    @SerializedName("MASTER_UUID") @Expose var master_uuid: String,
    @SerializedName("PUDDING_SERIALNUM") @Expose var pudding_serialnum: String,
    @SerializedName("USER_NAME") @Expose var user_name: String,
    @SerializedName("USER_ROLE") @Expose var user_role: String,
    @SerializedName("REGISTRATION_DATE") @Expose var registration_date: String
)