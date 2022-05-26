package com.onethefull.dasomtutorial.data.model.check

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.annotations.Nullable

/**
 * Created by sjw on 2021/11/23
 */
data class GetMessageListResponse(
    @SerializedName("status_code") @Expose var status_code: Int?,
    @SerializedName("status") @Expose var status: String?,
    @SerializedName("body") @Expose val body: Body,
)
