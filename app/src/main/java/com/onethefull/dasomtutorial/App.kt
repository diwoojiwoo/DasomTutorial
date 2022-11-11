package com.onethefull.dasomtutorial

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.multidex.MultiDexApplication
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.provider.SettingProviderHelper
import com.onethefull.dasomtutorial.utils.VolumeManager
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.core.power.RooboPowerManager
import com.roobo.core.scene.SceneEventListener
import com.roobo.core.scene.SceneHelper
import kotlinx.coroutines.Job
import java.io.Serializable
import java.util.*

/**
 * Created by sjw on 2021/11/10
 */

typealias CloiSceneHelper = com.onethefull.wonderfulrobotmodule.scene.SceneHelper
typealias CloiSceneEventListener = com.onethefull.wonderfulrobotmodule.scene.SceneEventListener

class App : MultiDexApplication() {
    var currentActivity: Activity? = null
    private var mRooboWakeLock: RooboPowerManager.RooboWakeLock? = null
    private var mWakeLock: PowerManager.WakeLock? = null

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
            override fun onSwitchIn(flags: Int) {
                super.onSwitchIn(flags)
                mRooboWakeLock = RooboPowerManager.getInstance(this@App).newWakeLock("active")
                mRooboWakeLock?.acquire()
            }

            override fun onSwitchOut() {
                super.onSwitchOut()
                currentActivity?.finish()
                currentActivity = null
                mRooboWakeLock?.release()
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
        DWLog.e("initCloi")
        CloiSceneHelper.initialize(this)
        CloiSceneHelper.setSceneEventListener(object : CloiSceneEventListener() {
            override fun onSwitchIn(flags: Int) {
                super.onSwitchIn(flags)
                DWLog.d("onSwitchIn")
            }

            override fun onSwitchOut() {
                DWLog.d("onSwitchOut")
                super.onSwitchOut()
                mWakeLock?.release()
            }

            override fun onCommand(
                action: String?,
                params: Bundle?,
                suggestion: Serializable?,
            ) {
                super.onCommand(action, params, suggestion)
//                DWLog.e("App onCommand action name :: $action ")

                mWakeLock =
                    (instance.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                        .newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK or
                                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                    PowerManager.ON_AFTER_RELEASE,
                            "Tag:TutorialPower"
                        )
                mWakeLock?.acquire()
                this@App.onCommand(action, params, suggestion)
                return
            }
        })
    }

    /**
     * Scene onCommand 공통 동작
     */
    fun onCommand(action: String?, params: Bundle?, suggestion: Serializable?) {
        SettingProviderHelper.insert(
            SettingProviderHelper.ProviderInsertData(
                SettingProviderHelper.KEY_TOP_SCENE,
                "DEMO_DASOM_TUTORIAL", // DASOM_TUTORIAL
                SettingProviderHelper.VALUE_EMPTY,
            )
        )

        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
            if (VolumeManager[this@App] == 1) {
                VolumeManager.setLevel(this@App, 1)
            } else if (VolumeManager[this@App] == 2) {
                VolumeManager.setLevel(this@App, 2)
            }
        }

        val send = Intent(instance, MainActivity::class.java)
        send.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        DWLog.w("onCommand action:$action")

        when (action) {
            OnethefullBase.PRACTICE_EMERGENCY, OnethefullBase.QUIZ_TYPE_SHOW, OnethefullBase.MEAL_TYPE_SHOW, OnethefullBase.KEBBI_TUTORIAL_SHOW -> {
                send.putExtra(OnethefullBase.PARAM_PRAC_TYPE, action)
                send.putExtra(
                    OnethefullBase.PARAM_LIMIT,
                    params?.getString(OnethefullBase.PARAM_LIMIT) ?: ""
                )
                send.putExtra(
                    OnethefullBase.PARAM_CATEGORY,
                    params?.getString(OnethefullBase.PARAM_CATEGORY) ?: ""
                )
                send.putExtra(
                    OnethefullBase.PARAM_CONTENT,
                    params?.getString(OnethefullBase.PARAM_CONTENT) ?: ""
                )
            }
            OnethefullBase.GUIDE_WAKEUP, OnethefullBase.GUIDE_VISION, OnethefullBase.GUIDE_MEDICATION -> {
                send.putExtra(OnethefullBase.GUIDE_TYPE_PARAM, action)
            }
        }
        startActionActivity(send)
    }


    fun getLocale(): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            instance?.resources?.configuration?.locales?.get(0)
        } else {
            instance?.resources?.configuration?.locale
        }
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

    val jobList = HashMap<String, Job>()

    fun releaseJob() {
        jobList.forEach {
            it.value.cancel()
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