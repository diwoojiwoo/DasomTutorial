package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.ui.meal.MealStatus
import com.onethefull.dasomtutorial.ui.meal.MealTts

/**
 * Created by jeaseok on 2022/02/22
 */
class MealRepository private constructor(
    private val context: Context
) {
    fun getMealComment(status: MealStatus, nowHour: Int): List<MealTts> {
        val timeName = when (nowHour) {
            in 8..10 -> { "아침" }
            in 11..13 -> { "점심" }
            in 17..21 -> { "저녁" }
            else -> ""
        }
        if (timeName.isBlank()) return emptyList()

        val list = ArrayList<MealTts>()
        when (status) {
            MealStatus.MEAL_INIT -> {
                list.add(
                    MealTts(status,
                        "어르신 오늘 ${timeName}식사는 하셨나요?\n" +
                                "마이크가 켜지면 예 혹은 아니요라고 말씀해주시거나\n" +
                                "화면에 보이시는 버튼을 눌러주세요."
                    )
                )
            }
            MealStatus.MEAL_GUIDE_TIME -> {
                list.add(
                    MealTts(status,"언제 식사를 하셨나요?\n" +
                            "말씀해주세요.")
                )
            }
            MealStatus.MEAL_GUIDE_FOOD -> {
                list.add(
                    MealTts(status,"그럼 어떤 식사를 드셨나요?\n" +
                            "식사하신 음식을 말씀해주세요.")
                )
            }
            MealStatus.MEAL_GUIDE_RETRY -> {
            }
            else -> {
            }
        }
        return list
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MealRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: MealRepository(context).also { instance = it }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}