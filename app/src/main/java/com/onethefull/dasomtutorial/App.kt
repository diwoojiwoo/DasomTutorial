package com.onethefull.dasomtutorial

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.utils.VolumeManager
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.core.scene.SceneEventListener
import com.roobo.core.scene.SceneHelper
import java.io.Serializable

/**
 * Created by sjw on 2021/11/10
 */
class App : MultiDexApplication() {
    var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        initSceneHelper()
    }

    /**
     * init SceneHelper and add SceneEvent Listener
     */
    private fun initSceneHelper() {
        when (BuildConfig.TARGET_DEVICE) {
            DEVICE_BEANQ -> initBeanQ()
            DEVICE_CLOI -> initCloi()
        }
    }

    /**
     * 빈큐 초기화
     * */
    private fun initBeanQ() {
        SceneHelper.initialize(this)
        SceneHelper.setEventListener(object : SceneEventListener() {
            override fun onSwitchOut() {
                super.onSwitchOut()
            }

            override fun onSwitchIn(flags: Int) {
                super.onSwitchIn(flags)
                currentActivity?.finish()
            }

            override fun onCommand(action: String?, params: Bundle?, suggestion: Serializable?) {
                super.onCommand(action, params, suggestion)
                this@App.onCommand(action, params, suggestion)
            }
        })
    }

    /**
     * 클로이 초기화
     * */
    private fun initCloi() {

    }

    /**
     * Scene onCommand 공통 동작
     */
    private fun onCommand(action: String?, params: Bundle?, suggestion: Serializable?) {
        DWLog.d("App onCommand action name :: $action ")

        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
            if (VolumeManager[this@App] == 1) {
                VolumeManager.setLevel(this@App, 1)
            } else if (VolumeManager[this@App] == 2) {
                VolumeManager.setLevel(this@App, 2)
            }
        }

        val send = Intent(instance, MainActivity::class.java)
        send.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        when (action) {
            OnethefullBase.PRACTICE_EMERGENCY, OnethefullBase.QUIZ_TYPE_SHOW, OnethefullBase.MEAL_TYPE_SHOW -> {
                send.putExtra(OnethefullBase.PARAM_PRAC_TYPE, action)
                send.putExtra(
                    OnethefullBase.PARAM_LIMIT,
                    params?.getString(OnethefullBase.PARAM_LIMIT) ?: ""
                )
            }
            OnethefullBase.GUIDE_WAKEUP, OnethefullBase.GUIDE_VISION, OnethefullBase.GUIDE_MEDICATION -> {
                send.putExtra(OnethefullBase.GUIDE_TYPE_PARAM, action)
            }
        }
        startActionActivity(send)
    }

    private fun startActionActivity(send: Intent) {
        if (currentActivity != null && currentActivity is MainActivity) {
            (currentActivity as MainActivity).apply {
                intent = send
            }.run {
                startFragment()
            }
        } else {
            send.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(send)
        }
    }

    companion object {
        lateinit var instance: App
            private set

        const val TAG = "DasomTutorialDebug"
        const val DEVICE_BEANQ = "BEANQ"
        const val DEVICE_CLOI = "CLOI"
    }
}