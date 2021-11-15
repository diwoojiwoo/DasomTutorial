package com.onethefull.dasomtutorial.ui.learn

import android.animation.ObjectAnimator
import android.animation.RectEvaluator
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.adapter.OptionsAdapter
import com.onethefull.dasomtutorial.databinding.FragmentLearnBinding
import com.onethefull.dasomtutorial.utils.InjectorUtils
import com.onethefull.dasomtutorial.utils.Status
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import kotlinx.android.synthetic.main.fragment_learn.*


/**
 * Created by sjw on 2021/11/10
 */

class LearnFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentLearnBinding
    lateinit var optionsAdapter: OptionsAdapter
    var selectedAnswer: String = ""

    private val viewModel: LearnViewModel by viewModels {
        InjectorUtils.provideLearnViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        DWLog.d("onCreateView")
        viewDataBinding = FragmentLearnBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("onViewCreated")
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        Handler(Looper.getMainLooper()).postDelayed({
            setUpText()
            setUpSpeech()
            setRecyclerView()
            viewModel.listOptions.observe(
                viewLifecycleOwner, Observer {
                    val list = it
                    list?.let { value ->
                        run {
                            optionsAdapter.setChoiceist(value)
                        }
                    }
                }
            )

        }, 1300)
    }

    private fun setUpText() {
        viewModel.getPracticeComments()
        viewModel.practicesComments().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let { result -> }
                    }
                    Status.LOADING -> {
                        Log.d(App.TAG, "LOADING")
                    }
                    Status.ERROR -> {
                        //Handle Error
                        Log.e(App.TAG, it.message.toString())
                    }
                }
            }
        )
    }

    private fun setUpSpeech() {
        viewModel.speechStatus.observe(
            viewLifecycleOwner, {
                when (it) {
                    SpeechStatus.WAITING -> {
                        colorizePurple()
                    }
                    SpeechStatus.SPEECH -> {
                        colorizeGreen()
                    }
                }
            }
        )
    }

    fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context);
        optionsAdapter = OptionsAdapter()
        viewDataBinding.choiceRecyclerView.adapter = optionsAdapter
        viewDataBinding.choiceRecyclerView.layoutManager = linearLayoutManager
        optionsAdapter.onItemClick = {
            selectedAnswer = it
        }
    }


    /**
     * 음성입력 애니메이션
     * */
    private fun colorizePurple() {
        layout.setBackgroundColor(resources.getColor(R.color.design_default_color_primary_dark))
    }

    /**
     * 음성출력 애니메이션
     * */
    private fun colorizeGreen() {
        layout.setBackgroundColor(resources.getColor(R.color.design_default_color_secondary))
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}