package com.onethefull.dasomtutorial.data.model.vital

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 *
 * {
 * "clientId": "10390NNNB2212300254",
 * "scenario_id": "SC_PRESCRIPTION_SCENARIOS",
 * "customerCode": "prodbeta",
 * "deviceCode": "Kebbi"
 * }
 *
 *
    SC_PRESCRIPTION_SCENARIOS - 처방전 만료일 60일 전 ~ 처방전 만료일 30일 까지
    SC_AFTER_EQUIPMENT_INSTALLATION_SCENARIO - 설치 다음날
    SC_1WEEK_AFTER_EQUIPMENT_INSTALLATION_SCENARIO - 설치 1주일 후
 */

data class VitalScenarioRequest(
    @SerializedName("clientId") @Expose val clientId: String,
    @SerializedName("scenario_id") @Expose val scenarioId: String,
    @SerializedName("customerCode") @Expose val customerCode: String,
    @SerializedName("deviceCode") @Expose val deviceCode: String,
)