package com.onethefull.dasomtutorial.ui.learn

import android.animation.ValueAnimator
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.adapter.OptionsAdapter
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.FragmentLearnBinding
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.utils.CustomToastView
import com.onethefull.dasomtutorial.utils.InjectorUtils
import com.onethefull.dasomtutorial.utils.Status
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.onethefull.wonderfulrobotmodule.scene.SceneHelper
import org.koin.android.ext.android.inject

/**
 * Created by sjw on 2021/11/10
 */

class LearnFragment : Fragment() {
    private lateinit var binding: FragmentLearnBinding
    lateinit var optionsAdapter: OptionsAdapter
    private var selectedAnswer: String = ""
    private var currentStatus: LearnStatus = LearnStatus.START
    private var limit: String = ""
    private var mealCategory: Array<String>? = null
    private var content: String = ""
//    private val viewModel: LearnViewModel by viewModels {
//        InjectorUtils.provideLearnViewModelFactory(requireContext())
//    }
    private val viewModel: LearnViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            DWLog.d("LearnFragment type :: ${LearnFragmentArgs.fromBundle(it).type}, count :: ${LearnFragmentArgs.fromBundle(it).limit}")
            currentStatus = when (LearnFragmentArgs.fromBundle(it).type) {
                OnethefullBase.PRACTICE_EMERGENCY -> LearnStatus.START
                OnethefullBase.QUIZ_TYPE_SHOW -> LearnStatus.QUIZ_SHOW
                OnethefullBase.MEAL_TYPE_SHOW -> LearnStatus.EXTRACT_CATEGORY
                OnethefullBase.KEBBI_TUTORIAL_SHOW -> LearnStatus.START_TUTORIAL_1_1
//                OnethefullBase.KEBBI_TUTORIAL_SHOW -> LearnStatus.END_TUTORIAL_1_4
                else -> LearnStatus.START
            }
            limit = LearnFragmentArgs.fromBundle(it).limit
            mealCategory = LearnFragmentArgs.fromBundle(it).category
            content = LearnFragmentArgs.fromBundle(it).content
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        DWLog.d("onCreateView")
        binding = FragmentLearnBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("onViewCreated")
        binding.lifecycleOwner = this.viewLifecycleOwner.apply {
            binding.btnExit.setOnClickListener {
                RxBus.publish(RxEvent.destroyApp)
            }
        }

        val manCount = DasomProviderHelper.getFaceDetectId(context)
        Handler(Looper.getMainLooper()).postDelayed({
            when (currentStatus) {
                LearnStatus.QUIZ_SHOW -> {
                    if (manCount == "0") {
                        if (BuildConfig.TARGET_DEVICE == App.DEVICE_BEANQ) {
                            (activity as MainActivity).finish()
                        } else if (BuildConfig.TARGET_DEVICE == App.DEVICE_CLOI) {
                            (activity as MainActivity).finishAffinity()
                        }
                    } else
                        setUpDementia()
                }
                LearnStatus.EXTRACT_CATEGORY -> {
                    setUpCheckMeal()
                }
                LearnStatus.START_TUTORIAL_1, LearnStatus.START_TUTORIAL_1_1 -> {
                    setUpTutorial()
                }
                else -> {
                    if (BuildConfig.PRODUCT_TYPE == "KT") {
                        setUpGenieText()
                    } else {
                        setUpText()
                    }
//                    if (manCount == "0") {
//                        if (BuildConfig.TARGET_DEVICE == App.DEVICE_BEANQ) {
//                            (activity as MainActivity).finish()
//                        } else if (BuildConfig.TARGET_DEVICE == App.DEVICE_CLOI) {
//                            (activity as MainActivity).finishAffinity()
//                        }
//                    } else {
//                        if (BuildConfig.PRODUCT_TYPE == "KT") {
//                            setUpGenieText()
//                        } else {
//                            setUpText()
//                        }
//                    }
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
            viewModel.practiceSos().observe(viewLifecycleOwner) {
                RxBus.publish(RxEvent.destroyApp)
            }
        }, 900)
    }

    /**
     * WONDERFUL
     * 긴급상황 튜토리얼 설정
     * */
    private fun setUpText() {
        viewModel.getPracticeEmergencyComment(LearnStatus.START)
        viewModel.practiceComment().observe(
            viewLifecycleOwner
        ) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.contentPb.visibility = View.GONE
                    it.data?.let { result ->
                        if (result.key == "practice_emergency_retry") {
                            optionsAdapter.setChoiceist(mutableListOf("좋아요!"))
                        } else {
                            optionsAdapter.setChoiceist(mutableListOf())
                            val textSize = when (result.text[0].length) {
                                in 100..130 -> 30.toFloat()
                                in 131..150 -> 29.toFloat()
                                in 151..170 -> 25.toFloat()
                                else -> 42.7.toFloat()
                            }
                            binding.questionText.setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                textSize
                            )
                        }
                    }
                }

                Status.LOADING -> {
                    binding.contentPb.visibility = View.VISIBLE
                }

                Status.ERROR -> {
                    //Handle Error
                    DWLog.e(it.message.toString())
                    binding.contentPb.visibility = View.GONE
                }
            }
        }
    }

    /**
     * KT 다솜아
     * 긴급상황 튜토리얼 설정
     * */
    private fun setUpGenieText() {
        viewModel.getGeniePracticeEmergencyComment(LearnStatus.START)
        viewModel.practiceComment().observe(
            viewLifecycleOwner
        ) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.contentPb.visibility = View.GONE
                    it.data?.let { result ->
                        DWLog.d("setUpGenieText result.key ${result.key}, ${result.text[0].length}")
                        val textSize = when (result.text[0].length) {
                            in 100..130 -> 30.toFloat()
                            in 131..150 -> 29.toFloat()
                            else -> 42.7.toFloat()
                        }
                        binding.questionText.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            textSize
                        )
                    }
                }

                Status.LOADING -> {
//                        DWLog.d("LOADING")
                    binding.contentPb.visibility = View.VISIBLE
                }

                Status.ERROR -> {
                    //Handle Error
//                        DWLog.e(it.message.toString())
                    binding.contentPb.visibility = View.GONE
                }
            }
        }
    }

    /**
     * 치매예방퀴즈 설정
     * */
    private fun setUpDementia() {
        binding.contentPb.visibility = View.GONE
        viewModel.getDementiaQuizList(currentStatus, limit)
        viewModel.dementiaQuiz().observe(
            viewLifecycleOwner
        ) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { result ->
                        DWLog.d("setUpDementia speechText ${result.question},  ${result.question.length}")
                        when (BuildConfig.TARGET_DEVICE) {
                            App.DEVICE_BEANQ -> {
                                val textSize = when (result.question.length) {
                                    in 100..130 -> 30.toFloat()
                                    in 131..150 -> 29.toFloat()
                                    else -> 42.7.toFloat()
                                }
                                binding.questionText.setTextSize(
                                    TypedValue.COMPLEX_UNIT_SP,
                                    textSize
                                )
                            }

                            else -> {
                                val textSize = when (result.question.length) {
                                    in 50..99 -> 38.toFloat()
                                    in 100..130 -> 37.toFloat()
                                    in 131..150 -> 36.toFloat()
                                    in 151..170 -> 33.toFloat()
                                    else -> 49.7.toFloat()
                                }
                                binding.questionText.setTextSize(
                                    TypedValue.COMPLEX_UNIT_SP,
                                    textSize
                                )
                            }
                        }
                    }
                }

                else -> {
                }
            }
        }
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
        for (category in mealCategory!!) {
            DWLog.d("setUpCheckMeal mealCategory:: $category")
        }
        binding.contentPb.visibility = View.GONE
        viewModel.checkExtractMeal(currentStatus, mealCategory)
        viewModel.mealComment().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { result ->
                        DWLog.d("setUpCheckMeal speechText $result result.length ${result.length}")
                        when (BuildConfig.TARGET_DEVICE) {
                            App.DEVICE_BEANQ -> {
                                val textSize = when (result.length) {
                                    in 100..130 -> 30.toFloat()
                                    in 131..150 -> 29.toFloat()
                                    else -> 42.7.toFloat()
                                }
                                binding.questionText.setTextSize(
                                    TypedValue.COMPLEX_UNIT_SP,
                                    textSize
                                )
                            }
                            else -> {
                                val textSize = when (result.length) {
                                    in 100..130 -> 32.toFloat()
                                    in 131..150 -> 30.toFloat()
                                    else -> 44.7.toFloat()
                                }
                                binding.questionText.setTextSize(
                                    TypedValue.COMPLEX_UNIT_SP,
                                    textSize
                                )
                            }
                        }
                    }
                }
                else -> {
                    (App.instance.currentActivity as MainActivity).finish()
                }
            }
        }
    }

    /**
     * 5분 데모기능
     * */
    var playCount = 3
    private fun setUpTutorial() {
        DWLog.d("setUpTutorial content :: $content")
        when (content) {
            OnethefullBase.CONTENT_DASOMTALK -> currentStatus = LearnStatus.START_DASOMTALK_TUTORIAL_2
            OnethefullBase.CONTENT_VIDEO -> currentStatus = LearnStatus.START_VIDEOCALL_TUTORIAL_2
            OnethefullBase.CONTENT_SOS -> currentStatus = LearnStatus.START_SOS_TUTORIAL_2
            OnethefullBase.CONTENT_MEDICATION -> currentStatus = LearnStatus.START_MEDICATION_TUTORIAL_2
            OnethefullBase.CONTENT_RADIO -> currentStatus = LearnStatus.START_RADIO_TUTORIAL_2
            OnethefullBase.CONTENT_MV -> currentStatus = LearnStatus.START_TUTORIAL_MV
        }
        viewModel.checkTutorialStatus(currentStatus)
        viewModel.tutorialComment().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { result ->
                        DWLog.d("result $result")
                        DWLog.d("currentLearnStatus ${viewModel.currentLearnStatus.value} result.size ${result.length}")
                        if (result.contains("_offline")) {
                            binding.layoutVideo.visibility = View.VISIBLE
                            binding.layoutText.visibility = View.VISIBLE
                            val uri: Uri? = when (viewModel.currentLearnStatus.value) {
                                LearnStatus.START_DASOMTALK_VIDEO -> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_dasomtalk")
                                }
                                LearnStatus.START_VIDEOCALL_VIDEO -> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_dasomtalk")
                                }
                                LearnStatus.START_RADIO_VIDEO -> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_radio")
                                }
                                LearnStatus.START_SOS_VIDEO -> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_sos")
                                }
                                LearnStatus.START_MEDICATION_VIDEO -> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_medication")
                                }
                                LearnStatus.START_TUTORIAL_MV-> {
                                    Uri.parse("android.resource://" + App.instance.packageName.toString() + "/raw/tutorial_mv")
                                }
                                else -> null
                            }
                            uri?.let {
                                binding.screenVideoView.setVideoURI(uri)
                                binding.screenVideoView.setOnPreparedListener{
                                    binding.screenVideoView.start()
                                }
                                binding.screenVideoView.setOnCompletionListener {
                                    DWLog.e("오프라인 동영상 재생 종료 ${viewModel.currentLearnStatus.value}")
                                    when (viewModel.currentLearnStatus.value) {
                                        LearnStatus.START_DASOMTALK_VIDEO -> {
                                            currentStatus = LearnStatus.START_DASOMTALK_TUTORIAL_2
                                            viewModel.checkTutorialStatus(currentStatus)
                                        }
                                        LearnStatus.START_VIDEOCALL_VIDEO -> {
                                            currentStatus = LearnStatus.START_VIDEOCALL_TUTORIAL_2
                                            viewModel.checkTutorialStatus(currentStatus)
                                        }
                                        LearnStatus.START_RADIO_VIDEO -> {
                                            currentStatus = LearnStatus.START_RADIO_TUTORIAL_2
                                            viewModel.checkTutorialStatus(currentStatus)
                                        }
                                        LearnStatus.START_SOS_VIDEO -> {
                                            currentStatus = LearnStatus.START_SOS_TUTORIAL_2
                                            viewModel.checkTutorialStatus(currentStatus)
                                        }
                                        LearnStatus.START_MEDICATION_VIDEO -> {
                                            currentStatus = LearnStatus.START_MEDICATION_TUTORIAL_2
                                            viewModel.checkTutorialStatus(currentStatus)
                                        }
                                        LearnStatus.START_TUTORIAL_MV-> {
                                            playCount--
                                            DWLog.e("playCount ---> $playCount")
                                            if(playCount == 0) {
                                                playCount = 3
                                                currentStatus = LearnStatus.START_TUTORIAL_1_1
                                                viewModel.checkTutorialStatus(currentStatus)
                                            } else {
                                                currentStatus = LearnStatus.START_TUTORIAL_MV
                                                viewModel.checkTutorialStatus(currentStatus)
                                            }
                                        }
                                        else -> null
                                    }
                                }
                            }
                        } else if (result.contains("_en_offline")) {

                        } else {
                            binding.layoutText.visibility = View.VISIBLE
                            binding.layoutVideo.visibility = View.GONE
                            when (viewModel.currentLearnStatus.value) {
                                LearnStatus.START_DASOMTALK_VIDEO -> {
                                    SceneHelper.startScene(OnethefullBase.MODULE_NAME_YOUTUBE, OnethefullBase.ACTION_YOUTUBE_PLAY_DEMO,
                                        Bundle().apply {
                                            putString(OnethefullBase.PARAM_URL, result)
                                            putString(OnethefullBase.PARAM_TITLE, resources.getString(R.string.title_dasomtalk))
                                            putString(OnethefullBase.PARAM_NEXT_CONTENT, OnethefullBase.CONTENT_DASOMTALK)
                                        }, 0)
                                }
                                LearnStatus.START_VIDEOCALL_VIDEO -> {
                                    SceneHelper.startScene(OnethefullBase.MODULE_NAME_YOUTUBE, OnethefullBase.ACTION_YOUTUBE_PLAY_DEMO, Bundle().apply {
                                        putString(OnethefullBase.PARAM_URL, result)
                                        putString(OnethefullBase.PARAM_TITLE, resources.getString(R.string.title_videocall))
                                        putString(OnethefullBase.PARAM_NEXT_CONTENT, OnethefullBase.CONTENT_VIDEO)
                                    }, 0)
                                }
                                LearnStatus.START_RADIO_VIDEO -> {
                                    SceneHelper.startScene(OnethefullBase.MODULE_NAME_YOUTUBE, OnethefullBase.ACTION_YOUTUBE_PLAY_DEMO, Bundle().apply {
                                        putString(OnethefullBase.PARAM_URL, result)
                                        putString(OnethefullBase.PARAM_TITLE, resources.getString(R.string.title_radio))
                                        putString(OnethefullBase.PARAM_NEXT_CONTENT, OnethefullBase.CONTENT_RADIO)
                                    }, 0)
                                }
                                LearnStatus.START_SOS_VIDEO -> {
                                    SceneHelper.startScene(OnethefullBase.MODULE_NAME_YOUTUBE, OnethefullBase.ACTION_YOUTUBE_PLAY_DEMO, Bundle().apply {
                                        putString(OnethefullBase.PARAM_URL, result)
                                        putString(OnethefullBase.PARAM_TITLE, resources.getString(R.string.title_sos))
                                        putString(OnethefullBase.PARAM_NEXT_CONTENT, OnethefullBase.CONTENT_SOS)
                                    }, 0)
                                }
                                LearnStatus.START_MEDICATION_VIDEO -> {
                                    SceneHelper.startScene(OnethefullBase.MODULE_NAME_YOUTUBE, OnethefullBase.ACTION_YOUTUBE_PLAY_DEMO, Bundle().apply {
                                        putString(OnethefullBase.PARAM_URL, result)
                                        putString(OnethefullBase.PARAM_TITLE, resources.getString(R.string.title_medication))
                                        putString(OnethefullBase.PARAM_NEXT_CONTENT, OnethefullBase.CONTENT_MEDICATION)
                                    }, 0)
                                }
                            }
                        }

                        val textSize = when (viewModel.currentLearnStatus.value) {
                            LearnStatus.START_TUTORIAL_1_3, LearnStatus.START_TUTORIAL_3_1, LearnStatus.START_TUTORIAL_3_2, LearnStatus.START_TUTORIAL_3_3, LearnStatus.START_TUTORIAL_3_4,
                            LearnStatus.START_TUTORIAL_4_2, LearnStatus.END_TUTORIAL_1_2_1,
                            -> 42.toFloat()
                            LearnStatus.START_DASOMTALK_TUTORIAL_2,
                            LearnStatus.END_TUTORIAL_1_3, LearnStatus.START_DASOMTALK_TUTORIAL_2_2,
                            -> 40.7.toFloat()
                            LearnStatus.START_DASOMTALK_TUTORIAL_1_2, LearnStatus.START_DASOMTALK_VIDEO,
                            LearnStatus.START_MEDICATION_TUTORIAL_1, LearnStatus.START_MEDICATION_VIDEO, LearnStatus.START_MEDICATION_TUTORIAL_2,
                            LearnStatus.START_VIDEOCALL_TUTORIAL_1, LearnStatus.START_VIDEOCALL_VIDEO, LearnStatus.START_VIDEOCALL_TUTORIAL_2,
                            LearnStatus.END_TUTORIAL_1_2_2,
                            -> 35.toFloat()
                            LearnStatus.START_SOS_TUTORIAL_1, LearnStatus.START_SOS_VIDEO -> 32.toFloat()
                            else -> 50.7.toFloat()
                        }
                        binding.questionText.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            textSize
                        )
                        binding.questionText.setTextColor(Color.parseColor("#333333"))
                    }
                }
            }
        }
    }


    private fun setUpSpeech() {
        viewModel.speechStatus.observe(viewLifecycleOwner) {
            changeStatus(it)
        }
    }

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        optionsAdapter = OptionsAdapter()
        binding.choiceRecyclerView.adapter = optionsAdapter
        binding.choiceRecyclerView.layoutManager = linearLayoutManager
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
                binding.layoutText.setBackgroundColor(resources.getColor(R.color.colorUserBackground))
                binding.questionHolder.setBackgroundColor(resources.getColor(R.color.colorUserBackground))
                binding.bgBackMic.visibility = View.VISIBLE
                binding.questionText.setTextColor(Color.WHITE)
            }
            SpeechStatus.SPEECH -> {
                when (BuildConfig.TARGET_DEVICE) {
                    App.DEVICE_BEANQ -> binding.layoutText.setBackgroundColor(resources.getColor(R.color.colorBeanQBackground))
                    else -> binding.layoutText.setBackgroundColor(resources.getColor(R.color.colorKebbiBackground))
                }
                binding.questionHolder.setBackgroundResource(R.drawable.holder)
                binding.bgBackMic.visibility = View.GONE
                binding.questionText.setTextColor(Color.BLACK)
            }
        }

        var id = getAnimationIdForStatus(status)
        activity?.runOnUiThread {
            try {
                binding.lottieAnimation.repeatCount = ValueAnimator.INFINITE
                binding.lottieAnimation.apply { setAnimation(id) }.run {
                    DWLog.i("lottie_animation:${binding.lottieAnimation.repeatCount}")
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
            SpeechStatus.SPEECH -> {
                DWLog.d("BuildConfig.TARGET_DEVICE ${BuildConfig.TARGET_DEVICE}")
                when (BuildConfig.TARGET_DEVICE) {
                    App.DEVICE_BEANQ -> R.raw.speech_robot
                    else -> R.raw.dasomk
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.finishAction()
        viewModel.disconnect()
    }

    companion object {

    }
}