package com.onethefull.dasomtutorial.data.model.quiz

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DementiaQuizListResponse(
    @SerializedName("status_code") @Expose val status_code: Int,
    @SerializedName("status") @Expose val status: String,
    @SerializedName("dementiaQuestionList") @Expose val dementiaQuestionList: ArrayList<DementiaQuiz>
)


data class DementiaQuiz(
    @SerializedName("SORT") @Expose val sort: String,
    @SerializedName("IDX") @Expose val idx: String,
    @SerializedName("ETC1") @Expose val etc1: String,
    @SerializedName("QUESTION") @Expose var question: String,
    @SerializedName("ANSWER") @Expose var answer: String
) : Serializable