package com.onethefull.dasomtutorial.ui.guide

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021-09-01.
 */
@Parcelize
data class GuideTts(
    var actionName: String,
    var text: String
) : Parcelable