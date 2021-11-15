package com.onethefull.dasomtutorial.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by sjw on 2021/11/09
 */
data class ConnectedUser(
    @SerializedName("status_code") @Expose var status_code: Int,
    @SerializedName("status") @Expose var status: String,
    @SerializedName("connectedUserlist") @Expose var connectedUserlist: List<UserInfo>
)
