package com.onethefull.dasomtutorial.data.model.check

import com.google.gson.annotations.SerializedName
import io.reactivex.annotations.Nullable

/**
 * Created by sjw on 2022/04/05
 */
data class Body(
    @SerializedName("category") @Nullable val category: MutableList<String>?,
    @SerializedName("status_type") @Nullable val status_type: String,
    @SerializedName("msg") @Nullable var msg: String = "",
    @SerializedName("file") @Nullable var file: String = "",
)