package com.onethefull.dasomtutorial.ui.meal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * Created by jaeseok on 2022/02/22
 */

@Parcelize
data class MealTts(
    var status: MealStatus,
    var text: String
) : Parcelable