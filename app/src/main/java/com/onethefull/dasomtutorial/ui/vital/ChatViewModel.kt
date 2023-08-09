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