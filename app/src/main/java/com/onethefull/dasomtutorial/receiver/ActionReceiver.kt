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
                DWLog.d("action:${intent.action}")
                when (intent.action) {
                    ACTION_SHOW_MEAL -> {
                        val mealCategory  = intent.getStringArrayListExtra(OnethefullBase.PARAM_CATEGORY)
                        val data = Bundle().apply {
                            putString(
                                OnethefullBase.PARAM_CATEGORY,
                                mealCategory?.joinToString(
                                    prefix = "",
                                    separator = ":",
                                    postfix = ""
                                )
                            )
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
}