package com.onethefull.dasomtutorial.ui.learn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import kotlinx.android.synthetic.main.fragment_learn.*


/**
 * Created by sjw on 2021/11/10
 */

class LearnFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentLearnBinding
    lateinit var optionsAdapter: OptionsAdapter
    private var selectedAnswer: String = ""

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
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner.apply {
            btn_exit.setOnClickListener {
                RxBus.publish(
                    RxEvent.Event(
                        RxEvent.AppDestroy,
                        2 * 1000L,
                        "AppDestroy"
                    )
                )
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setUpText()
            setUpSpeech()
            setRecyclerView()
            viewModel.listOptions.observe(
                viewLifecycleOwner, Observer {
                    val list = it
                    list?.let { value ->
                        run {
                            showToastView(value.toString())
//                            optionsAdapter.setChoiceist(value)
                        }
                    }
                }
            )
            viewModel.practiceSos().observe(
                viewLifecycleOwner, {
                    RxBus.publish(
                        RxEvent.Event(
                            RxEvent.AppDestroy,
                            2 * 1000L,
                            "AppDestroy"
                        )
                    )
                })
        }, 900)
    }

    private fun setUpText() {
        viewModel.getPracticeEmergencyComment(LearnStatus.START)
        viewModel.practiceComment().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let { result ->
//                            if(result.key == "practice_emergency_retry") {
//                                optionsAdapter.setChoiceist(mutableListOf("좋아요"))
//                            }
                        }
                    }
                    Status.LOADING -> {
//                        DWLog.d("LOADING")
                    }
                    Status.ERROR -> {
                        //Handle Error
//                        DWLog.e(it.message.toString())
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

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        optionsAdapter = OptionsAdapter()
        viewDataBinding.choiceRecyclerView.adapter = optionsAdapter
        viewDataBinding.choiceRecyclerView.layoutManager = linearLayoutManager
        optionsAdapter.onItemClick = {
            selectedAnswer = it
//            viewModel.getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
        }
    }

    private fun showToastView(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    /**
     * 음성입력 애니메이션
     * */
    private fun colorizePurple() {
        viewDataBinding.imgSpeaker.visibility = View.VISIBLE
        viewDataBinding.imgSosDasom.visibility = View.GONE
        layout.setBackgroundColor(resources.getColor(R.color.design_default_color_primary_dark))
    }

    /**
     * 음성출력 애니메이션
     * */
    private fun colorizeGreen() {
        viewDataBinding.imgSpeaker.visibility = View.GONE
        viewDataBinding.imgSosDasom.visibility = View.VISIBLE
        layout.setBackgroundColor(resources.getColor(R.color.design_default_color_secondary))
    }


    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}