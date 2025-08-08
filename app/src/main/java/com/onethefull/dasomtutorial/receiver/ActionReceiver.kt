package com.onethefull.dasomtutorial.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.onethefull.dasomtutorial.utils.logger.DWLog
import android.content.pm.PackageManager
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.utils.CloiSceneHelper
import com.onethefull.dasomtutorial.utils.DefaultSceneHelper
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Created by sjw on 16,February,2021
 */
class ActionReceiver : BroadcastReceiver() {
    companion object{
        private val ACTION_SHOW_MEAL= "com.onethefull.dasomtutial.SHOW_MEAL"
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
                        val mealCategory  = intent.getStringArrayListExtra(OnethefullBase.PARAM_CATEGORY)
                        val nextScene  = intent.getStringExtra(OnethefullBase.PARAM_NEXT_SCENE_NAME)
                        val nextAction  = intent.getStringExtra(OnethefullBase.PARAM_NEXT_SCENE_ACTION)
                        val controlType = intent.getStringExtra(OnethefullBase.PARAM_CONTROL_TYPE) ?: ""

                        mealCategory?.forEach { categoryRaw ->
                            val category = categoryRaw.removeSuffix("Time")
                            if (!shouldRunTodayByCategory(context, category)) {
                                DWLog.d("오늘 [$category] 이미 실행되어 스킵")
                                RxBus.publish(RxEvent.destroyApp)
                                return
                            }
                        }
                        
                        DWLog.e("nextScene $nextScene, nextAction $nextAction  , controlType $controlType")
                        val data = Bundle().apply {
                            putString(
                                OnethefullBase.PARAM_CATEGORY,
                                mealCategory?.joinToString(
                                    prefix = "",
                                    separator = ":",
                                    postfix = ""
                                )
                            )
                            putString(OnethefullBase.PARAM_NEXT_SCENE_NAME, nextScene ?: "")
                            putString(OnethefullBase.PARAM_NEXT_SCENE_ACTION, nextAction ?: "")
                            putString(OnethefullBase.PARAM_CONTROL_TYPE, controlType ?: "")
                        }

                        // 8/12 적용 Activity 실행으로 변경
                        App.instance.onCommand(OnethefullBase.MEAL_TYPE_SHOW, data, null)
                        /*
                        // 기존 StartScene 방식
                        val ai = context
                            .packageManager
                            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                        val moduleName = ai.metaData.getString("ROOBO_MODULE_NAME")
                        DefaultSceneHelper.startScene(
                            moduleName,
                            OnethefullBase.MEAL_TYPE_SHOW,
                            data,
                            CloiSceneHelper.SCENE_ATTR_NO_ANIMATION
                        )
                        */
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