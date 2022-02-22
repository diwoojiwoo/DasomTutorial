package com.onethefull.dasomtutorial.data.model.quiz

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.annotations.Nullable
import java.io.Serializable

/**
 * Created by sjw on 07,July,2020
 */
// PUDDING_SERIALNUM-필수, QUESTION-필수, ANSWER-필수
// {
//    "ANSWER_LIST": [
//    {
//        "PUDDING_SERIALNUM": "TEST",
//        "IDX": "질문 고유번호",
//        "SORT": "질문 종류(등록 질문-I, 공통 질문-C)",
//        "QUESTION": "질문 내용",
//        "RESPONSE": "어르신 답변 여부(1-대답, 2-무응답)",
//        "ETC1": "QUESTION or ANSWER(질문코드)",
//        "UserAnswer": "사용자 발화 정답",
//        "CorrectAnswer": "실제 정답(Ex - 일일구, 일일이, TIME, MONTH, DAY)"
//    }
//    ]
//}
data class DementiaQAReq(
    @SerializedName("ANSWER_LIST")  @Expose val answerList : ArrayList<DementiaQAReqDetail>
)


data class DementiaQAReqDetail(
    @SerializedName("PUDDING_SERIALNUM") @Expose val serializedName:String,
    @SerializedName("RESPONSE") @Expose val response: String,
    @SerializedName("IDX") @Expose val idx: String,
    @SerializedName("SORT") @Expose val sort: String,
    @SerializedName("QUESTION") @Expose val question: String,
    @SerializedName("ETC1") @Expose val etc1: String,
    @SerializedName("UserAnswer") @Expose val userAnswer: String,
    @SerializedName("CorrectAnswer") @Nullable var correctAnswer: String?=null
) : Serializable