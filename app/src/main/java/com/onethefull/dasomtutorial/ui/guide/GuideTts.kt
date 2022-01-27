package com.onethefull.dasomtutorial.ui.guide

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021/12/22
 */

@Parcelize
data class GuideTts(
    var status: GuideStatus,
    var actionName: String,
    var text: String
) : Parcelable