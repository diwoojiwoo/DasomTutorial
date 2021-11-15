package com.onethefull.dasomtutorial

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.onethefull.dasomtutorial.base.BaseActivity
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.ActivityMainBinding
import com.onethefull.dasomtutorial.utils.logger.DWLog

/**
 * Created by sjw on 2021/11/10
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var resId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host)
    }

    override fun onResume() {
        super.onResume()
        super.onResumeRoobo(this@MainActivity)
//        when (BuildConfig.TARGET_DEVICE) {
//            App.DEVICE_BEANQ -> super.onResumeRoobo(this@MainActivity)
//        }
        GCTextToSpeech.getInstance()?.start(this)
    }

    /**
     * Fragment 분리
     */
    fun startFragment() {
        DWLog.d("MainActivity - startFragment")
        when {
            intent.hasExtra(OnethefullBase.PRAC_TYPE_PARAM) -> { /* 랜덤 채팅 */
                startTutorialService()
            }
            intent.hasExtra(OnethefullBase.QUIZ_TYPE_PARAM) -> {
                startQuizService()
            }
            else -> resId = R.id.action_learnFragment_self
        }
        resId?.let { navigateFragment(it) }
    }

    /**
     * 긴급상황 튜토리얼
     */
    private fun startTutorialService() {
        resId = R.id.action_learnFragment_self
    }

    /**
     * 두뇌운동 퀴즈
     */
    private fun startQuizService() {
//        resId = R.id.
    }

    private fun navigateFragment(resId: Int) {
        navController.navigate(resId)
    }

    override fun onPause() {
        super.onPause()
        super.onPauseRoobo(this@MainActivity)
//        when (BuildConfig.TARGET_DEVICE) {
//            App.DEVICE_BEANQ -> {
//                super.onPauseRoobo(this@MainActivity)
//            }
//        }
        GCTextToSpeech.getInstance()?.release()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}