package com.onethefull.dasomtutorial.ui.learn

import android.media.MediaPlayer
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
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.adapter.OptionsAdapter
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.FragmentLearnBinding
import com.onethefull.dasomtutorial.utils.CustomToastView
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
                RxBus.publish(RxEvent.destroyApp)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            setUpText()
            setUpSpeech()
            setRecyclerView()
            viewModel.listOptions.observe(
                viewLifecycleOwner, Observer {
                    val list = it
                    list[0].let { value ->
                        run {
                            MediaPlayer.create(App.instance.currentActivity, R.raw.d54).apply {
                                setOnPreparedListener {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        it.start()
                                    }, 100)
                                }
                                setOnCompletionListener { it.release() }
                            }
                            CustomToastView.makeInfoToast(activity as MainActivity, value, View.GONE).show()
//                            optionsAdapter.setChoiceist(value)
                        }
                    }
                }
            )
            viewModel.practiceSos().observe(
                viewLifecycleOwner, {
                    RxBus.publish(RxEvent.destroyApp)
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
                            if (result.key == "practice_emergency_retry") {
                                optionsAdapter.setChoiceist(mutableListOf("좋아요!"))
                            } else {
                                optionsAdapter.setChoiceist(mutableListOf())
                            }
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
            optionsAdapter.setChoiceist(mutableListOf())
            viewModel.handleRecognition("좋아요")
        }
    }

    /**
     * 음성입력 애니메이션
     * */
    private fun colorizePurple() {
        synchronized(this) {
            viewDataBinding.imgSpeaker.visibility = View.VISIBLE
            viewDataBinding.imgSosDasom.visibility = View.GONE
            layout.setBackgroundColor(resources.getColor(R.color.design_default_color_primary_dark))
        }
    }

    /**
     * 음성출력 애니메이션
     * */
    private fun colorizeGreen() {
        synchronized(this) {
            viewDataBinding.imgSpeaker.visibility = View.GONE
            viewDataBinding.imgSosDasom.visibility = View.VISIBLE
            layout.setBackgroundColor(resources.getColor(R.color.design_default_color_secondary))
        }
    }


    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}