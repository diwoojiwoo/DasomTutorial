package com.onethefull.dasomtutorial.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.core.scene.SceneHelper

/**
 * Created by sjw on 16,February,2021
 */
class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.let {
            if (intent.action != null) {
                DWLog.d("action:${intent.action}")
                when (intent.action) {
                    "com.onethefull.dasomtutial.SHOW_MEAL" -> {
                        val mealCategory  = intent.getStringExtra("category")
                        val data = Bundle().apply {
                            putString("category", mealCategory)
                        }
                        SceneHelper.startScene(
                            "DASOM_TUTORIAL",
                            "Meal_show",
                            data,
                            SceneHelper.SCENE_ATTR_NO_ANIMATION
                        )
                    }
                }
            }
        }
    }
}