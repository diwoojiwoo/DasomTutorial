package com.onethefull.dasomtutorial.utils

import android.app.Application
import android.os.Bundle
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.roobo.core.scene.SceneHelper

/**
 * Created by Douner on 2020/09/04.
 */

typealias CloiSceneHelper = com.onethefull.wonderfulrobotmodule.scene.SceneHelper
typealias CloiSceneEventListener = com.onethefull.wonderfulrobotmodule.scene.SceneEventListener

object DefaultSceneHelper {
    fun startScene(scene: String?, action: String?, data: Bundle?, flag: Int){
        when(BuildConfig.TARGET_DEVICE) {
            App.DEVICE_BEANQ -> SceneHelper.startScene(scene,action,data,flag)
            else -> CloiSceneHelper.startScene(scene, action, data,flag)
        }
    }
}