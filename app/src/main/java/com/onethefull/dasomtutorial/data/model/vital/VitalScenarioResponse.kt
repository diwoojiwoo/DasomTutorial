package com.onethefull.dasomtutorial.data.model.vital

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.onethefull.dasomtutorial.data.model.check.Body

/**
 * {
 * "message": "질문 호출 완료"
 * }
 */

data class VitalScenarioResponse(
    @SerializedName("message") @Expose var message: String?
)
