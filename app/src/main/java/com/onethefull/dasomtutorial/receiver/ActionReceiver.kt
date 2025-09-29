package com.onethefull.dasomtutorial.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by sjw on 16,February,2021
 */
class ActionReceiver : BroadcastReceiver() {
    companion object {
        private val ACTION_SHOW_MEAL = "com.onethefull.dasomtutial.SHOW_MEAL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.let {
            if (intent.action != null) {
                DWLog.d("DASOM_TUTORIAL action:${intent.action}")
                when (intent.action) {
                    Intent.ACTION_LOCALE_CHANGED -> {
                        App.instance.updateLocale()
                    }

                    ACTION_SHOW_MEAL -> {
                        val mealCategory = intent.getStringArrayListExtra(OnethefullBase.PARAM_CATEGORY)
                        val nextScene = intent.getStringExtra(OnethefullBase.PARAM_NEXT_SCENE_NAME)
                        val nextAction = intent.getStringExtra(OnethefullBase.PARAM_NEXT_SCENE_ACTION)
                        val controlType = intent.getStringExtra(OnethefullBase.PARAM_CONTROL_TYPE) ?: ""

                        // mealCategory가 없으면 그냥 종료
                        if (mealCategory.isNullOrEmpty()) {
                            DWLog.d("mealCategory 비어있음 -> 앱 종료")
                            Handler(Looper.getMainLooper()).post {
                                RxBus.publish(RxEvent.destroyApp)
                            }
                            return
                        }

                        // 1. "Time" 접미사 제거 + 중복 제거
                        val uniqueCategories = mealCategory
                            .map { it.removeSuffix("Time") }
                            .distinct()

                        var shouldRun = true

                        // 2. 오늘 이미 실행된 카테고리 있는지 체크
                        for (category in uniqueCategories) {
                            if (!shouldRunTodayByCategory(context, category)) {
                                DWLog.d("오늘 [$category] 이미 실행되어 스킵")
                                shouldRun = false
                                break
                            }
                        }

                        if (shouldRun) {
                            DWLog.e("nextScene $nextScene, nextAction $nextAction  , controlType $controlType")
                            val data = Bundle().apply {
                                putString(
                                    OnethefullBase.PARAM_CATEGORY,
                                    mealCategory.joinToString(separator = ":")
                                )
                                putString(OnethefullBase.PARAM_NEXT_SCENE_NAME, nextScene ?: "")
                                putString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, nextAction ?: "")
                                putString(OnethefullBase.PARAM_CONTROL_TYPE, controlType)
                            }

                            // Activity 실행
                            App.instance.onCommand(OnethefullBase.MEAL_TYPE_SHOW, data, null)
                        } else {
                            // 3. 흰 화면 방지를 위해 안전하게 종료 예약
                            Handler(Looper.getMainLooper()).post {
                                RxBus.publish(RxEvent.destroyApp)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun shouldRunTodayByCategory(context: Context, category: String): Boolean {
        val prefs = context.getSharedPreferences("action_prefs", Context.MODE_PRIVATE)
        val key = "last_run_date_$category" // 카테고리별로 다른 키 사용
        val lastRunDate = prefs.getString(key, null)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return if (lastRunDate == todayDate) {
            false
        } else {
            prefs.edit().putString(key, todayDate).apply()
            true
        }
    }
}