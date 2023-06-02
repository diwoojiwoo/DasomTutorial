package com.onethefull.dasomtutorial

import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.onethefull.dasomtutorial.base.BaseActivity
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.ActivityMainBinding
import com.onethefull.dasomtutorial.utils.CustomToastView
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.network.NetworkUtils
import com.roobo.core.scene.SceneHelper

/**
 * Created by sjw on 2021/11/10
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var resId: Int? = null
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host)
        setupViewModel()
        startFragment()
    }

    override fun onResume() {
        super.onResume()
        when (BuildConfig.TARGET_DEVICE) {
            App.DEVICE_BEANQ -> super.onResumeRoobo(this@MainActivity)
        }
        GCTextToSpeech.getInstance()?.start(this)
        viewModel.start()
    }

    /**
     * Fragment 분리
     */
    fun startFragment() {
        DWLog.d("MainActivity - startFragment")
        when {
            intent.hasExtra(OnethefullBase.PARAM_PRAC_TYPE) -> {
                val practiceType = intent.getStringExtra(OnethefullBase.PARAM_PRAC_TYPE)
                DWLog.d("MainActivity - startFragment type:$practiceType")
                DWLog.d("practiceType $practiceType")
                if (practiceType.equals(OnethefullBase.KEBBI_TUTORIAL_SHOW) || practiceType.equals(OnethefullBase.MEAL_TYPE_SHOW)) {
                    startTutorialService()
                } else {
                    if (NetworkUtils.isConnected(this))
                        startTutorialService()
                    else {
                        DWLog.e("네트워크 연결을 확인해주세요.!")
                        CustomToastView.makeInfoToast(this@MainActivity, "네트워크 연결을 확인해주세요.", View.VISIBLE).show()
                        when (BuildConfig.TARGET_DEVICE) {
                            App.DEVICE_BEANQ -> {
                                SceneHelper.switchOut()
                                App.instance.currentActivity?.finish()
                                Process.killProcess(Process.myPid())
                            }
                            else -> {
                                com.onethefull.wonderfulrobotmodule.scene.SceneHelper.switchOut()
                                App.instance.currentActivity?.finishAffinity()
                                Process.killProcess(Process.myPid())
                            }
                        }
                    }
                }
            }
            else -> resId = R.id.action_main_fragment_to_learn_fragment
        }
        resId?.let { navigateFragment(it) }
    }

    /**
     * 긴급상황 튜토리얼, 치매예방퀴즈
     * 22/4/7 식사체크
     */
    private fun startTutorialService() {
        if (navController.currentDestination?.id == R.id.main_fragment) {
            val data = intent.getStringExtra(OnethefullBase.PARAM_CATEGORY)
            val categoryList = data?.split(":")?.toTypedArray()

            navController.navigate(MainFragmentDirections.actionMainFragmentToLearnFragment(
                intent.getStringExtra(OnethefullBase.PARAM_PRAC_TYPE).toString(),
                intent.getStringExtra(OnethefullBase.PARAM_LIMIT).toString(),
                categoryList,
                intent.getStringExtra(OnethefullBase.PARAM_CONTENT).toString()))
        }
    }

    /**
     * 대화 서비스 가이드
     */


    /**
     * 식사 확인
     */


    private fun navigateFragment(resId: Int) {
        navController.navigate(resId)
    }

    override fun onPause() {
        super.onPause()
        when (BuildConfig.TARGET_DEVICE) {
            App.DEVICE_BEANQ -> {
                super.onPauseRoobo(this@MainActivity)
            }
        }
        overridePendingTransition(0, 0)
        GCTextToSpeech.getInstance()?.release()
        viewModel.release()
        Process.killProcess(android.os.Process.myPid())
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            MainViewModelFactory()
        ).get(MainViewModel::class.java)
    }

    companion object {
        const val MIN_CLICK_INTERVAL = 10 * 1000L
    }
}