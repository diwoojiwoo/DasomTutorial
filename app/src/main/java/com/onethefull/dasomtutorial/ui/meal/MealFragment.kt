package com.onethefull.dasomtutorial.ui.meal

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.FragmentMealBinding
import com.onethefull.dasomtutorial.utils.InjectorUtils
import com.onethefull.dasomtutorial.utils.Status
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.roobo.focusinterface.FocusManager
import kotlinx.android.synthetic.main.fragment_guide.*

/**
 * Created by jeaseok on 2022/02/22
 */
class MealFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentMealBinding

    private val viewModel: MealViewModel by viewModels {
        InjectorUtils.provideMealViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = FragmentMealBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        val requestCamera: Boolean = FocusManager.getInstance(App.instance).requestFocus(OnethefullBase.CAMERA_FOCUS_NAME)
        Handler(Looper.getMainLooper()).postDelayed({
            setUpText()
            setUpSpeech()
        }, 900)
    }

    // 시작점
    private fun setUpText() {
        viewModel.getComment(MealStatus.MEAL_INIT)
        viewModel.mealComment().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let {
                        DWLog.d(TAG, "Success input")
                    }
                }
                Status.ERROR -> {}
            }
        }
    }

    private fun setUpSpeech() {
        viewModel.speechStatus.observe(viewLifecycleOwner) {
            when (it) {
                SpeechStatus.WAITING -> {
                    // 기다리는중
                    layout.setBackgroundColor(resources.getColor(R.color.design_default_color_primary_dark))
                }
                SpeechStatus.SPEECH -> {
                    // 발화중
                    layout.setBackgroundColor(resources.getColor(R.color.design_default_color_secondary))
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        DWLog.e(TAG, "[Meal] onPause")
        viewModel.disconnect()
    }

    companion object {
        const val TAG = "MealFragment"
    }
}