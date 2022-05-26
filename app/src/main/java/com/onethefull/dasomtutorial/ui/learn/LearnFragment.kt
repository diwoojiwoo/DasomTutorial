package com.onethefull.dasomtutorial.ui.learn

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
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
    private var currentStatus: LearnStatus = LearnStatus.START
    private var limit: String = ""
    private var mealCategory: String = ""
    private val viewModel: LearnViewModel by viewModels {
        InjectorUtils.provideLearnViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            DWLog.d("LearnFragment type :: ${LearnFragmentArgs.fromBundle(it).type}, count :: ${LearnFragmentArgs.fromBundle(it).limit}")
            currentStatus = when (LearnFragmentArgs.fromBundle(it).type) {
                OnethefullBase.PRACTICE_EMERGENCY -> LearnStatus.START
                OnethefullBase.QUIZ_TYPE_SHOW -> LearnStatus.QUIZ_SHOW
                OnethefullBase.MEAL_TYPE_SHOW -> LearnStatus.EXTRACT_CATEGORY
                else -> LearnStatus.START
            }
            limit = LearnFragmentArgs.fromBundle(it).limit
            mealCategory = LearnFragmentArgs.fromBundle(it).category
        }
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
            when (currentStatus) {
                LearnStatus.QUIZ_SHOW -> {
                    setUpDementia()
                }
                LearnStatus.EXTRACT_CATEGORY -> {
                    setUpCheckMeal()
                }
                else -> {
                    if (BuildConfig.PRODUCT_TYPE == "KT") {
                        setUpGenieText()
                    } else {
                        setUpText()
                    }
                }
            }
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

    /**
     * WONDERFUL
     * 긴급상황 튜토리얼 설정
     * */
    private fun setUpText() {
        viewModel.getPracticeEmergencyComment(LearnStatus.START)
        viewModel.practiceComment().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        content_pb.visibility = View.GONE
                        it.data?.let { result ->
                            if (result.key == "practice_emergency_retry") {
                                optionsAdapter.setChoiceist(mutableListOf("좋아요!"))
                            } else {
                                optionsAdapter.setChoiceist(mutableListOf())
                                val textSize = when (result.text[0].length) {
                                    in 100..130 -> 30.toFloat()
                                    in 131..150 -> 29.toFloat()
                                    else -> 42.7.toFloat()
                                }
                                viewDataBinding.questionText.setTextSize(
                                    TypedValue.COMPLEX_UNIT_SP,
                                    textSize
                                )
                            }
                        }
                    }
                    Status.LOADING -> {
                        content_pb.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        //Handle Error
                        DWLog.e(it.message.toString())
                        content_pb.visibility = View.GONE
                    }
                }
            }
        )
    }

    /**
     * KT 다솜아
     * 긴급상황 튜토리얼 설정
     * */
    private fun setUpGenieText() {
        viewModel.getGeniePracticeEmergencyComment(LearnStatus.START)
        viewModel.practiceComment().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        content_pb.visibility = View.GONE
                        it.data?.let { result ->
                            DWLog.d("setUpGenieText result.key ${result.key}, ${result.text[0].length}")
                            val textSize = when (result.text[0].length) {
                                in 100..130 -> 30.toFloat()
                                in 131..150 -> 29.toFloat()
                                else -> 42.7.toFloat()
                            }
                            viewDataBinding.questionText.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                textSize
                            )
                        }
                    }
                    Status.LOADING -> {
//                        DWLog.d("LOADING")
                        content_pb.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        //Handle Error
//                        DWLog.e(it.message.toString())
                        content_pb.visibility = View.GONE
                    }
                }
            }
        )
    }

    /**
     * 치매예방퀴즈 설정
     * */
    private fun setUpDementia() {
        content_pb.visibility = View.GONE
        viewModel.getDementiaQuizList(currentStatus, limit)
        viewModel.dementiaQuiz().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let { result ->
                            DWLog.d("setUpDementia speechText ${result.question},  ${result.question.length}")
                            val textSize = when (result.question.length) {
                                in 100..130 -> 30.toFloat()
                                in 131..150 -> 29.toFloat()
                                else -> 42.7.toFloat()
                            }
                            viewDataBinding.questionText.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                textSize
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        )
    }

    /**
     * 취침, 식사알람 응답 설정
     *
     *"1. 카테고리 별 질문시점에 시작(/log/checkExtract/ 여기 들어가있는 카테고리는 질문에서 제외)"
     * 2. /log/getMessageList/ API 호출 TTS 리턴
     * 3. 리턴 받은 TTS 발화
     * 4. 발화 종료 후 음성입력
     * 5. 음성입력시 카테고리와 함께 /log/checkChatBotData/ API 응답 기록
     * */
    private fun setUpCheckMeal() {
        DWLog.d("setUpCheckMeal mealCategory:: $mealCategory")
        content_pb.visibility = View.GONE
        viewModel.checkExtractMeal(currentStatus, mealCategory)
        viewModel.mealComment().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let { result ->
                            DWLog.d("setUpCheckMeal speechText $result")
                            val textSize = when (result.length) {
                                in 100..130 -> 30.toFloat()
                                in 131..150 -> 29.toFloat()
                                else -> 42.7.toFloat()
                            }
                            viewDataBinding.questionText.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                textSize
                            )
                        }
                    }
                    else -> {
                    }
                }
            }
        )
    }

    private fun setUpSpeech() {
        viewModel.speechStatus.observe(
            viewLifecycleOwner, {
                changeStatus(it)
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

    private fun changeStatus(status: SpeechStatus) {
        DWLog.i("changeStatus animation == [$status]")
        when (status) {
            SpeechStatus.WAITING -> {
                viewDataBinding.layout.setBackgroundColor(resources.getColor(R.color.colorUserBackground))
                viewDataBinding.questionHolder.setBackgroundColor(resources.getColor(R.color.colorUserBackground))
                viewDataBinding.bgBackMic.visibility = View.VISIBLE
                viewDataBinding.questionText.setTextColor(Color.WHITE)
            }
            SpeechStatus.SPEECH -> {
                viewDataBinding.layout.setBackgroundColor(resources.getColor(R.color.colorBeanQBackground))
                viewDataBinding.questionHolder.setBackgroundResource(R.drawable.holder)
                viewDataBinding.bgBackMic.visibility = View.GONE
                viewDataBinding.questionText.setTextColor(Color.BLACK)
            }
        }

        var id = getAnimationIdForStatus(status)
        activity?.runOnUiThread {
            try {
                viewDataBinding.lottieAnimation.repeatCount = ValueAnimator.INFINITE
                viewDataBinding.lottieAnimation.apply { setAnimation(id) }.run {
                    DWLog.i("lottie_animation:${lottie_animation.repeatCount}")
                    if (id == R.raw.speech_robot)
                        imageAssetsFolder = "images"
                    playAnimation()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAnimationIdForStatus(status: SpeechStatus): Int {
        return when (status) {
            SpeechStatus.WAITING -> R.raw.mic_circle
            SpeechStatus.SPEECH -> R.raw.speech_robot
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.disconnect()
    }
}