package com.onethefull.dasomtutorial.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021-09-01.
 */
@Parcelize
data class InnerTtsV2(
    var audioUrl: ArrayList<String>,
    var delay: ArrayList<String>,
    var action: ArrayList<String>,
    var time: String,
    var deviceCode: String,
    var text: ArrayList<String>,
    var key: String,
    var versionCode: Long
) : Parcelable