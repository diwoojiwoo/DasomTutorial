package com.onethefull.dasomtutorial.repository.vital

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.api.vital.ChatApiHelper
import com.onethefull.dasomtutorial.data.api.vital.ChatApiHelperImpl
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioRequest
import com.onethefull.dasomtutorial.data.model.vital.VitalScenarioResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper


/**
 * Created by sjw on 2023/08/09
 */
class ChatRepository private constructor(
    private val context : Context
) {
    suspend fun requestVitalScenario(request : VitalScenarioRequest) : VitalScenarioResponse{
        return apiHelper.requestVitalScenario(request)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: ChatRepository(context).also { instance = it }
            }

        private val apiHelper: ChatApiHelper = ChatApiHelperImpl(RetrofitBuilder.vitalAirService)
    }
}