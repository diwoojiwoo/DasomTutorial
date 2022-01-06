package com.onethefull.dasomtutorial.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.annotations.Nullable


/**
 * Created by sjw on 02,April,2021
 */
data class ElderlyList(
    @SerializedName("status_code")
    @Expose var statusCode: Int,

    @SerializedName("status")
    @Expose var status: String? = "",

    @SerializedName("elderlyList")
    @Expose val list: MutableList<Elderly>
)

data class Elderly(
    @SerializedName("ELDERLY_NAME")
    @Expose var name: String? = "",

    @SerializedName("ELDERLY_TEL")
    @Expose var tel: String? = "",

    @SerializedName("ELDERLY_BLOODTYPE")
    @Expose var bloodType: String? = "",

    @SerializedName("ELDERLY_GENDER")
    @Expose var gender: String? = "",

    @SerializedName("ELDERLY_UUID")
    @Expose
    var uuid: String? = "",

    @SerializedName("ELDERLY_RELIGION")
    @Expose
    var religion: String? = "",

    @SerializedName("ELDERLY_NO")
    @Expose
    var no: String? = "",

    @SerializedName("ELDERLY_ADDRESS")
    @Expose
    var address: String? = "",

    @SerializedName("ELDERLY_ADDRESS_DTL")
    var addressDtl: String? = "",

    @SerializedName("ELDERLY_BIRTH")
    @Expose
    var birth: String? = ""
)