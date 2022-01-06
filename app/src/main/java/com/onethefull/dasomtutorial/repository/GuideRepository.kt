package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.model.ElderlyList
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.guide.GuideTts

/**
 * Created by sjw on 2021/12/30
 */
class GuideRepository private constructor(
    private val context: Context,
) {

    fun getGuideComment(type: String): List<GuideTts> {
        var list = ArrayList<GuideTts>()
        when (type) {
            "guideWakeup" -> {
                list.add(GuideTts(type, "어르신, 저에게 말을 걸어 대화를 나누는데 아직 어려움이 많으신가요?"))
                list.add(GuideTts(type, "어르신, 우리의 대화가 많이 부족한 것 같아요! 혹시 사용하는데 어려움이 있으신가요? "))
                list.add(GuideTts(type, "어르신, 대화를 더 많이 나누고 싶어요! 아직 저와 대화가 많이 어려우신가요?"))
            }

            "guideVision" -> {
                list.add(GuideTts(type, "어르신, 저에게 말을 걸어 대화를 나누는데 아직 어려움이 많으신가요?"))
                list.add(GuideTts(type, "어르신, 우리의 대화가 많이 부족한 것 같아요! 혹시 사용하는데 어려움이 있으신가요? "))
                list.add(GuideTts(type, "어르신, 대화를 더 많이 나누고 싶어요! 아직 저와 대화가 많이 어려우신가요?"))
            }
        }
        return list
    }

    suspend fun getElderlyInfo(): ElderlyList {
        return apiHelper.getElderlyInfo(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context)
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GuideRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: GuideRepository(context).also { instance = it }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}