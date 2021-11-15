package com.onethefull.dasomtutorial.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021/11/10
 */
@Parcelize
data class ResultQuiz(
    var category: String = "",
    var correct_answer: String = "",
    var difficulty: String = "",
    var incorrect_answers: MutableList<String>,
    var question: String = "",
    var type: String = "",
    var audioUrl: String = ""
) : Parcelable