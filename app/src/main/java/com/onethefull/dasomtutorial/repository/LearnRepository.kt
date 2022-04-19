package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.ApiService
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.model.ConnectedUser
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.CheckExtractResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.guide.GuideStatus
import com.onethefull.dasomtutorial.ui.guide.GuideTts
import com.onethefull.dasomtutorial.utils.logger.DWLog
import java.lang.reflect.Type

/**
 * Created by sjw on 2021/11/10
 */
class LearnRepository private constructor(
    private val context: Context,
) {
    fun getPracticeEmergencyList(key: String): List<InnerTtsV2> {
        return convertJson(DasomProviderHelper.getPracticeEmergencyValue(context, key))
    }

    fun getGeniePracticeEmergencyList(key: String): List<InnerTtsV2> {
        var list = ArrayList<InnerTtsV2>()
        when (key) {
            DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE -> {
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 위급할 때 구조 요청을 빠르게 하는 것은 정말 중요해요! " +
                            "댜솜이가 멀리 있을 때 또는 전화기가 손이 닿지 않는 곳에 있을 때, 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요. " +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 위급 상황이 발생했을 때 신속한 조치가 가장 중요해요!" +
                            "갑자기 어지럽거나, 심한 두통에 시달리는 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 갑작스런 응급상황이 발생했을 때 효과적으로 대응할 수 있는 방법이 있어요!\n" +
                            "갑자기 몸을 움직일 수 없고, 눈을 뜰 수가 없는 위급상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 어느날 갑자기 몸을 가누기 힘들 때 제일 먼저 다솜이를 찾아주세요!\n" +
                            "갑작스러운 고통에 정신을 잃을지 모른다는 판단이 든다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 갑작스런 응급한 상황에 대비해 대처법을 미리 알아둔다면 위험한 상황을 막을 수 있어요!\n" +
                            "갑자기 다치거나 지병으로 몸을 움직일 수 없는 위급한 상황이 발생한다면, \"다솜아, 도와줘\"라고 말해보세요.\n" +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
            }
        }

        return list
    }

    private fun convertJson(jsonString: String): List<InnerTtsV2> {
        var list = ArrayList<InnerTtsV2>()
        if (jsonString == "") {
            list.add(InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0))
            return list as ArrayList<InnerTtsV2>
        }
        val type: Type = object : TypeToken<List<InnerTtsV2?>?>() {}.type
        return Gson().fromJson(jsonString, type) as ArrayList<InnerTtsV2>
    }

    suspend fun logPracticeSos(): Status {
        return apiHelper.practiceSos(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context)
        )
    }

    suspend fun getDementiaQuizList(limit: String): DementiaQuizListResponse {
        return apiHelper.getDementiaQuizList(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            limit
        )
    }

    suspend fun insertDementiaQuizLog(solvedQuizList: ArrayList<DementiaQAReqDetail>): Status {
        return apiHelper.insertDementiaQuizLog(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            DementiaQAReq(solvedQuizList)
        )
    }

    suspend fun logCheckExtract(): CheckExtractResponse {
        return apiHelper.logCheckExtract(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context)
        )
    }

    suspend fun logCheckChatBotData(checkChatBotDataRequest: CheckChatBotDataRequest): CheckChatBotDataResponse {
        return apiHelper.logCheckChatBotData(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            checkChatBotDataRequest
        )
    }

    suspend fun logGetMessageList(mealCategory: String): GetMessageListResponse {
        return apiHelper.logGetMessageList(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            mealCategory
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: LearnRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: LearnRepository(context).also { instance = it }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}