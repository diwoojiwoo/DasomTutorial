package com.onethefull.dasomtutorial.ui.guide

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.adapter.OptionsAdapter
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.FragmentGuideBinding
import com.onethefull.dasomtutorial.utils.CustomToastView
import com.onethefull.dasomtutorial.utils.InjectorUtils
import com.onethefull.dasomtutorial.utils.Status
import com.onethefull.dasomtutorial.utils.WCameraHelper
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.roobo.focusinterface.FocusManager

class GuideFragment : Fragment(), WCameraHelper.OnWCameraHelperListener {
    private lateinit var viewDataBinding: FragmentGuideBinding
    lateinit var optionsAdapter: OptionsAdapter
    private var selectedAnswer: String = ""
    private var currentGuideStatus: GuideStatus = GuideStatus.EMPTY

    private val viewModel: GuideViewModel by viewModels {
        InjectorUtils.provideGuideViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentGuideStatus = when (GuideFragmentArgs.fromBundle(it).type) {
                OnethefullBase.GUIDE_WAKEUP -> GuideStatus.WAKEUP_INIT
                OnethefullBase.GUIDE_VISION -> GuideStatus.VISION_INIT
                OnethefullBase.GUIDE_MEDICATION -> GuideStatus.MEDI_INIT
                OnethefullBase.GUIDE_COMMUNITY -> GuideStatus.COMM_INIT
                OnethefullBase.GUIDE_MONITORING -> GuideStatus.MONI_INIT
                OnethefullBase.GUIDE_MESSAGE -> GuideStatus.MESSAGE_INIT
                else -> GuideStatus.EMPTY
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        DWLog.d("[Guide] onCreateView")
        viewDataBinding = FragmentGuideBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("[Guide] onViewCreated")
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner.apply {}
        val requestCamera: Boolean = FocusManager.getInstance(App.instance).requestFocus(OnethefullBase.CAMERA_FOCUS_NAME)
        if (requestCamera)
            Handler(Looper.getMainLooper()).postDelayed({
                setUpText()
                setUpSpeech()
                setRecyclerView()
                viewModel.listOptions.observe(
                    viewLifecycleOwner, Observer {
                        val list = it
                        list[0].let { value ->
                            run {
                                CustomToastView.makeInfoToast(activity as MainActivity, value, View.GONE).show()
                            }
                        }
                    })
            }, 900)
    }

    private fun setUpText() {
        DWLog.d("[Guide] setUpText")
        viewModel.getGuideComment(currentGuideStatus)
        viewModel.guideComment().observe(
            viewLifecycleOwner, { it ->
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let {
                            DWLog.d("guideComment ${it.status.name}")
                            if (it.status == GuideStatus.VISION_INIT) {
                                optionsAdapter.setChoiceist(mutableListOf("모르겠어요"))
                            }
                        }
                    }

                    Status.LOADING -> {

                    }
                    Status.ERROR -> {
                        //Handle Error
                        DWLog.e(it.message.toString())
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
            viewModel.handleRecognition("몰라")
        }
    }

    /**
     * 사진 촬영 완료
     * */
    override fun onComplete() {
        TODO("Not yet implemented")
    }

    /**
     * 음성입력 애니메이션
     * */
    private fun colorizePurple() {
        synchronized(this) {
            viewDataBinding.imgSpeaker.visibility = View.VISIBLE
            viewDataBinding.imgSosDasom.visibility = View.GONE
            viewDataBinding.layout.setBackgroundColor(resources.getColor(R.color.design_default_color_primary_dark))
        }
    }

    /**
     * 음성출력 애니메이션`
     * */
    private fun colorizeGreen() {
        synchronized(this) {
            viewDataBinding.imgSpeaker.visibility = View.GONE
            viewDataBinding.imgSosDasom.visibility = View.VISIBLE
            viewDataBinding.layout.setBackgroundColor(resources.getColor(R.color.design_default_color_secondary))
        }
    }

    override fun onPause() {
        super.onPause()
        DWLog.e("[Guide] onPause")
        viewModel.disconnect()
    }

    companion object {

    }
}