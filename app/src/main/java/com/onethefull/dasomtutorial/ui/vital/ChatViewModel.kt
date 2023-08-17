package com.onethefull.dasomtutorial.ui.vital

import android.app.Activity
import android.os.Build
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioRequest
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.repository.vital.ChatRepository
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2023/08/09
 *
 * scenario_id의 종류
    1. SC_PRESCRIPTION_SCENARIOS - 처방전 만료일 60일 전 ~ 처방전 만료일 30일 까지
    2. SC_AFTER_EQUIPMENT_INSTALLATION_SCENARIO - 설치 다음날
    3. SC_1WEEK_AFTER_EQUIPMENT_INSTALLATION_SCENARIO - 설치 1주일 후
 */
class ChatViewModel(
    private val context: Activity,
    private val repository: ChatRepository,
) : BaseViewModel() {
    fun requestVitalScenario(): String {
        var response = ""
        uiScope.launch {
            try {
                response = repository.requestVitalScenario(
                    VitalScenarioRequest(
                        Build.SERIAL,
                        "SC_AFTER_EQUIPMENT_INSTALLATION_SCENARIO",
                        DasomProviderHelper.getCustomerCode(context),
                        DasomProviderHelper.getDeviceCode(context)
                    )
                ).message ?: ""
            } catch (e: Exception) {
                response = ""
            }
        }
        return response
    }

    override fun onCleared() {
        super.onCleared()
    }

}