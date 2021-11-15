package com.onethefull.dasomtutorial.base

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.base.touch.PuddingTouchManager
import com.roobo.core.sceneManager.SceneManager
import com.roobo.focusinterface.FocusManager
import java.lang.Exception

/**
 * Created by Douner on 2019. 4. 12..
 */
open class BaseActivity : AppCompatActivity() {
    private val FOCUS_AUDIO_RECORDER = "audio_recorder"
    private val FOCUS_ASR_OFFLINE_FOCUS = "asr_offline_focus"
    private val FOCUS_CAMERA = "camera"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.instance.currentActivity = this
    }


    protected fun onPauseRoobo(context: Context) {
        try {
//            FocusManager.getInstance(context).releaseFocus(FOCUS_ASR_OFFLINE_FOCUS)
            FocusManager.getInstance(context).releaseFocus(FOCUS_AUDIO_RECORDER)
            FocusManager.getInstance(context).releaseFocus(FOCUS_CAMERA)
            sleep()
        } catch (e: Exception) {
            e.printStackTrace()
            DWLog.w("error setStatus::${e.message}")
        }
        PuddingTouchManager.getInstance(this).unregisterTouchEvent()
    }


    private var isFinishWithSleep = true

    protected fun onResumeRoobo(context: Context) {
//        FocusManager.getInstance(context).requestFocus(FOCUS_ASR_OFFLINE_FOCUS)
        FocusManager.getInstance(context).requestFocus(FOCUS_AUDIO_RECORDER)
        FocusManager.getInstance(context).requestFocus(FOCUS_CAMERA)
    }
    private val sleep: () -> Unit = {
        DWLog.d("sleep")
        if (isFinishWithSleep)
            com.roobo.core.power.RooboPowerManager.getInstance(this).changeState("sleep")
    }
}