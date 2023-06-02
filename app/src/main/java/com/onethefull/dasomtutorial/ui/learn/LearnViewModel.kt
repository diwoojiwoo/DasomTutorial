package com.onethefull.dasomtutorial.ui.learn

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.widget.Toast
import androidx.lifecycle.*
import com.google.gson.Gson
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuiz
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.learn.localhelp.LocalDasomFilterTask
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.WMediaPlayer
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.record.WavFileUitls
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.onethefull.dasomtutorial.utils.task.EmergencyFlowTask
import com.onethefull.dasomtutorial.utils.task.noResponseFlowTask
import com.onethefull.wonderfulrobotmodule.data.LED_CONIFG
import com.onethefull.wonderfulrobotmodule.data.LedData
import com.onethefull.wonderfulrobotmodule.robot.BaseRobotController
import com.onethefull.wonderfulrobotmodule.robot.IMotionCallback
import com.onethefull.wonderfulrobotmodule.robot.KebbiMotion
import com.roobo.core.scene.SceneHelper
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * Created by sjw on 2021/11/10
 */
class LearnViewModel(
    private val context: Context,
    private val repository: LearnRepository,
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback,
    WMediaPlayer.OnMediaPlayerListener {
    private var mGCSpeechToText: GCSpeechToText =
        GCSpeechToTextImpl(context)

    private var wavUtils = WavFileUitls()
    private var isSuccessRecog = false

    init {
        connect()
        mGCSpeechToText.setCallback(this)
        mGCSpeechToText.setWavUtils(wavUtils)
        EmergencyFlowTask.insert(context, 0)
        noResponseFlowTask.insert(context, 0)
        LocalDasomFilterTask.setCommand(LocalDasomFilterTask.Command.EMPTY)
        WMediaPlayer.instance.setListener(this)
        DWLog.d("뷰모델 초기화 ")
    }

    //
    // 음성출력 상태
    //
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    //
    // 튜토리얼 진행 상태
    //
    private val _currentLearnStatus: MutableLiveData<LearnStatus> = MutableLiveData<LearnStatus>()
    val currentLearnStatus: LiveData<LearnStatus> = _currentLearnStatus

    private val _question: MutableLiveData<String> = MutableLiveData<String>()
    val question: LiveData<String> = _question

    private val _progress: MutableLiveData<Int> = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _listOptions: MutableLiveData<MutableList<String>> = MutableLiveData()
    val listOptions: LiveData<MutableList<String>> = _listOptions

    private val practiceComment = MutableLiveData<Resource<InnerTtsV2>>()
    fun practiceComment(): LiveData<Resource<InnerTtsV2>> {
        return practiceComment
    }

    fun getPracticeEmergencyComment(status: LearnStatus) {
        DWLog.e("******** Task.getPracticeEmergencyComment [Dasom][${status}] ********")
        practiceComment.postValue(Resource.loading(null))
        mGCSpeechToText.pause()
        uiScope.launch {
            try {
                var result: InnerTtsV2 = when (status) {
                    LearnStatus.START ->
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE)
                            .random()
                    LearnStatus.CALL_DASOM -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_START_VALUE)
                            .random()
                    }
                    LearnStatus.RETRY -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_RETRY_VALUE)
                            .random()
                    }
                    LearnStatus.HALF -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_HALF_VALUE)
                            .random()
                    }
                    LearnStatus.COMPLETE -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_COMPLETE_VALUE)
                            .random()
                    }
                    LearnStatus.END -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_END_VALUE)
                            .random()
                    }
                    else -> InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "",
                        arrayListOf(),
                        "",
                        0
                    )
                }

//                noResponse = true // 응답 초기화
                _currentLearnStatus.value = status
                synchronized(this) {
                    _question.value = result.text[0]
//                    if (result.audioUrl[0] != "" && URLUtil.isValidUrl(result.audioUrl[0])) {
//                        GCTextToSpeech.getInstance()?.urlMediaSpeech(result.audioUrl[0])
//                    }
                    GCTextToSpeech.getInstance()?.speech(result.text[0])
                }
                practiceComment.postValue(Resource.success(result))
            } catch (e: Exception) {
                practiceComment.postValue(Resource.error(e.toString(), null))
            }
        }
    }

    fun getGeniePracticeEmergencyComment(status: LearnStatus) {
        DWLog.e("******** Task.getGeniePracticeEmergencyComment [${status}] ********")
        mGCSpeechToText.pause()
        uiScope.launch {
            try {
                var result: InnerTtsV2 = when (status) {
                    LearnStatus.START ->
                        repository.getGeniePracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE)
                            .random()
                    LearnStatus.CALL_GEINIE -> {
                        InnerTtsV2(
                            arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("‘다솜아’ 라고 말하고, 마이크가 켜지면 '살려줘'하고 말해보세요."),
                            "",
                            1
                        )
                    }
                    LearnStatus.RETRY -> {
                        //“우와, 참 잘하셨어요! 한 번 더 해볼까요?”
                        InnerTtsV2(
                            arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("우와, 참 잘하셨어요! 한 번 더 해볼까요? 원하시면 '그래' 라고 말씀해주세요."),
                            "",
                            1
                        )
                    }
                    LearnStatus.HALF -> {
                        InnerTtsV2(
                            arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("잘 따라하셨어요! 다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요"),
                            "",
                            1
                        )
                    }
                    LearnStatus.COMPLETE -> {
                        InnerTtsV2(
                            arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요"),
                            "",
                            1
                        )
                    }
                    else -> InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "",
                        arrayListOf(),
                        "",
                        0
                    )
                }

//                noResponse = true // 응답 초기화
                _currentLearnStatus.value = status
                synchronized(this) {
                    _question.value = result.text[0]
                    GCTextToSpeech.getInstance()?.speech(result.text[0])
                }
                practiceComment.postValue(Resource.success(result))
            } catch (e: Exception) {
                practiceComment.postValue(Resource.error(e.toString(), null))
            }
        }
    }

    private fun restart() {
        noResponseFlowTask.insert(context, 1)
        getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
    }

    fun connect() {
        mGCSpeechToText.start()
        GCTextToSpeech.getInstance()?.setCallback(this)
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroyUpdate, 90 * 1000L, "AppDestroyUpdate"))
        BaseRobotController.initialize(App.instance)
        ledJob?.cancel()
        ledJob = null
    }

    fun finishAction() {
        ledJob = setLedOfDevice(-1)
    }

    fun disconnect() {
        mGCSpeechToText.release()
        GCTextToSpeech.getInstance()?.release()
        WMediaPlayer.instance.setListener(null)
        ledJob?.cancel()
        BaseRobotController.robotService?.robotMotor?.motionStop()
        BaseRobotController.robotService?.robotMotor?.reset()
    }

    /***
     * GCSpeechToText
     * */
    override fun onSTTConnected() {}

    override fun onSTTDisconneted() {}

    override fun onVoiceStart() {}

    override fun onVoice(data: ByteArray?, size: Int) {}

    override fun onVoiceEnd() {}

    override fun onVoiceResult(result: String?) {
        isSuccessRecog = true
        result?.let {
            handleRecognition(result)
        }
    }

    fun handleRecognition(text: String) {
        DWLog.d("handleRecognition:: $text currentLearnStatus:: {${_currentLearnStatus.value}}")

        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
//            networkError(GCTfextToSpeech.INDEX_OFFLINE_WIFI_IS_UNSTABLE)
            DWLog.e("GCSpeechToTextImpl.ERROR_OUT_OF_RANGE")
            RxBus.publish(RxEvent.destroyApp)
            return
        }

        noResponse = false
        _listOptions.postValue(mutableListOf(text))
        RxBus.publish(RxEvent.destroyLongTimeUpdate)

        /* 긴급상황 튜토리얼 */
        if (_currentLearnStatus.value == LearnStatus.CALL_DASOM
            || _currentLearnStatus.value == LearnStatus.CALL_GEINIE
        ) {
            uiScope.launch {
                if (LocalDasomFilterTask.checkDasom(text)) {
                    _currentLearnStatus.value = LearnStatus.CALL_SOS
                    _question.value = context.getString(R.string.tv_call_sos)
                } else {
                    DWLog.e("다솜아 이외의 단어를 이야기한 경우")
                }
                changeStatusSpeechFinished()
            }
        } else if ((_currentLearnStatus.value == LearnStatus.CALL_SOS)
            && LocalDasomFilterTask.checkSOS(text)
        ) {
            uiScope.launch {
                /***
                 * KT "다솜아"
                 * */
                if (BuildConfig.PRODUCT_TYPE == "KT") {
                    if (EmergencyFlowTask.isFirst(context)) {
                        getGeniePracticeEmergencyComment(LearnStatus.RETRY)
                    } else
                        getGeniePracticeEmergencyComment(LearnStatus.COMPLETE)
                }
                /***
                 * 원더풀 "다솜아"
                 * */
                else {
                    if (EmergencyFlowTask.isFirst(context)) {
                        getPracticeEmergencyComment(LearnStatus.RETRY)
                    } else
                        getPracticeEmergencyComment(LearnStatus.COMPLETE)
                }
            }
        } else if (_currentLearnStatus.value == LearnStatus.RETRY) {
            uiScope.launch {
                /***
                 * KT "다솜아"
                 * */
                if (BuildConfig.PRODUCT_TYPE == "KT") {
                    if (LocalDasomFilterTask.checkPosWord(text)) {
                        EmergencyFlowTask.insert(context, 1)
                        getGeniePracticeEmergencyComment(LearnStatus.CALL_GEINIE)
                    } else
                        getGeniePracticeEmergencyComment(LearnStatus.HALF)
                }
                /***
                 * 원더풀 "다솜아"
                 * */
                else {
                    if (LocalDasomFilterTask.checkPosWord(text)) {
                        EmergencyFlowTask.insert(context, 1)
                        getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
                    } else
                        getPracticeEmergencyComment(LearnStatus.HALF)
                }
            }
        }

        /* 치매예방퀴즈 */
        else if (_currentLearnStatus.value == LearnStatus.QUIZ_START) {
            currentDementiaQuiz.response = text
            mSolvedDementiaQuestionList.add(currentDementiaQuiz)
            getDementiaQuizList(LearnStatus.QUIZ_START, null)
        }

        /* 식사 시간 확인 */
        else if (_currentLearnStatus.value == LearnStatus.EXTRACT_TIME) {
            uiScope.launch {

                val lang = when (App.instance.getLocale()) {
                    Locale.US -> "en"
                    Locale.KOREA -> "ko"
                    else -> "ko"
                }
//                val lang = when (BuildConfig.LANGUAGE_TYPE) {
//                    "KO" -> "ko"
//                    "EN" -> "en"
//                    else -> "ko"
//                }

                repository.logCheckChatBotData(
                    CheckChatBotDataRequest(
                        Build.SERIAL,
                        _mealCategory!![0],
                        lang,
                        text
                    )
                )?.let {
                    Handler(Looper.getMainLooper()).postDelayed({
                        checkExtractMeal(LearnStatus.EXTRACT_TIME, _mealCategory)
                    }, 500)
                }
            }
        }

        /* 취침/기상/식사 확인 */
        else if (_currentLearnStatus.value == LearnStatus.SHOW) {
            uiScope.launch {
                val lang = when (App.instance.getLocale()) {
                    Locale.US -> "en"
                    Locale.KOREA -> "ko"
                    else -> "ko"
                }
//                val lang = when (BuildConfig.LANGUAGE_TYPE) {
//                    "KO" -> "ko"
//                    "EN" -> "en"
//                    else -> "ko"
//                }

                val mealCategory =
                    if (_mealCategory!!.size == 1) _mealCategory!![0] else _mealCategory!![1]
                repository.logCheckChatBotData(
                    CheckChatBotDataRequest(
                        Build.SERIAL,
                        mealCategory,
                        lang,
                        text
                    )
                )?.let {
                    Handler(Looper.getMainLooper()).postDelayed({
                        when (BuildConfig.TARGET_DEVICE) {
                            App.DEVICE_BEANQ -> {
                                SceneHelper.switchOut()
                                App.instance.currentActivity?.finish()
                                Process.killProcess(Process.myPid())
                            }
                            else -> {
                                com.onethefull.wonderfulrobotmodule.scene.SceneHelper.switchOut()
                                App.instance.currentActivity?.finish()
                                Process.killProcess(Process.myPid())
                            }
                        }
                    }, 500)
                }
            }
        } else {
            DWLog.e("재입력 받기")
            changeStatusSpeechFinished()
        }
    }

    /***
     * GCTextToSpeech
     * */
    // TTS 출력 시작
    override fun onSpeechStart() {
        DWLog.d("onSpeechStart")
        speechStarted()
    }

    // TTS 출력 종료
    override fun onSpeechFinish() {
        DWLog.d("onSpeechFinish")
        speechFinished()
    }

    override fun onGenieSTTResult(result: String) {

    }


    // 음성출력 시작
    private fun speechStarted() {
        mGCSpeechToText.pause()
        _speechStatus.value = SpeechStatus.SPEECH
    }

    // 음성출력 종료
    private fun speechFinished() {
        changeStatusSpeechFinished()
        checkCurrentStatus()
    }

    /**
     * TTS 출력이 끝난 상태 변경
     */
    private fun changeStatusSpeechFinished() {
        if (_currentLearnStatus.value != LearnStatus.START) {
            if (_currentLearnStatus.value.toString().contains("TUTORIAL") ||
                _currentLearnStatus.value.toString().contains("VIDEO")
            ) { // 다솜 튜토리얼 데모모드는 음성입력 안받음.
                mGCSpeechToText.pause()
                _speechStatus.value = SpeechStatus.SPEECH
            } else {
                mGCSpeechToText.resume()
                _speechStatus.value = SpeechStatus.WAITING
            }
        }
    }

    /**
     * SOS 연습 API 호출
     */
    private val _practiceSos = MutableLiveData<Status>()
    fun practiceSos(): LiveData<Status> {
        return _practiceSos
    }

    private fun getPracticeSosResult() {
        uiScope.launch {
            val practiceSosApi = repository.logPracticeSos()
            _practiceSos.postValue(practiceSosApi)
        }
    }

    /**
     * 치매예방퀴즈리스트 API 호출
     */
    private val _dementiaQuiz = MutableLiveData<Resource<DementiaQuiz>>()
    fun dementiaQuiz(): LiveData<Resource<DementiaQuiz>> {
        return _dementiaQuiz
    }

    private var mDementiaQuestionList = ArrayList<DementiaQuiz>()
    private var mSolvedDementiaQuestionList = ArrayList<DementiaQuiz>()
    lateinit var currentDementiaQuiz: DementiaQuiz
    fun getDementiaQuizList(status: LearnStatus, limit: String?) {
        uiScope.launch {
            _currentLearnStatus.value = status
            when (_currentLearnStatus.value) {
                LearnStatus.QUIZ_SHOW -> {
                    val dementiaQuizApi = repository.getDementiaQuizList(limit!!)
                    when (dementiaQuizApi.status_code) {
                        0 -> {
                            if (dementiaQuizApi.dementiaQuestionList.size > 0)
                                mDementiaQuestionList = dementiaQuizApi.dementiaQuestionList
                            else {
                                _currentLearnStatus.value = LearnStatus.QUIZ_ERROR
                            }
                            checkCurrentStatus()
                        }
                        else -> {
                            // 에러 시 종료
                            RxBus.publish(RxEvent.destroyApp)
                        }
                    }
                }
                LearnStatus.QUIZ_START -> {
                    if (mDementiaQuestionList.size == 0) { // 문제 다 풀고 insert log
                        var answerDementiaQuizList = ArrayList<DementiaQAReqDetail>()
                        for (quiz in mSolvedDementiaQuestionList) {
                            val answer = DementiaQAReqDetail(
                                Build.SERIAL.toString(),
                                "1",
                                quiz.idx,
                                quiz.sort,
                                quiz.question,
                                quiz.etc1,
                                quiz.response,
                                quiz.answer
                            )
                            answerDementiaQuizList.add(answer)
                        }
                        val insertLogApi: Status =
                            repository.insertDementiaQuizLog(answerDementiaQuizList)
                        insertLogApi.let {
                            RxBus.publish(RxEvent.destroyApp)
                        }
                    } else { // 문제 풀기
                        val quiz = mDementiaQuestionList[0]
                        currentDementiaQuiz = quiz
                        var speechText = when (mDementiaQuestionList.size) {
                            5 -> context.getString(R.string.text_start_demetia_quiz)
                            else -> ""
                        }
                        speechText += quiz.question
                        synchronized(this) {
                            _question.value = speechText
                            GCTextToSpeech.getInstance()?.speech(speechText)
                        }
                        _dementiaQuiz.postValue(Resource.success(quiz))
                        mDementiaQuestionList.remove(quiz)
                    }
                }
                else -> {

                }
            }

        }
    }


    private val _mealComment = MutableLiveData<Resource<String>>()
    fun mealComment(): LiveData<Resource<String>> {
        return _mealComment
    }

    private var _mealCategory: Array<String>? = null

    /**
     * 취침/기상/식사 정상추출여부 확인 API 호출
     */
    fun checkExtractMeal(status: LearnStatus, mealCategory: Array<String>?) {
        uiScope.launch {
            if (mealCategory == null || mealCategory.isEmpty()) {
                Toast.makeText(
                    context,
                    "식사시간 검토앱에 이상이 발생했습니다.\n문의하실때, 해당 문구를 말씀해주시면 감사하겠습니다!",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            _currentLearnStatus.value = status
            _mealCategory = mealCategory

            when (_currentLearnStatus.value) {
                LearnStatus.EXTRACT_CATEGORY, LearnStatus.EXTRACT_TIME -> {
                    getMessageList()
                }
            }
        }
    }

    /**
     * 취침/기상/식사 정상추출여부 확인 API 호출
     */
    private fun getMessageList() {
        DWLog.d("getMessageList ${_currentLearnStatus.value} mealCategory $_mealCategory")
        uiScope.launch {
            val category = when (_currentLearnStatus.value) {
                LearnStatus.EXTRACT_CATEGORY -> {
                    if (_mealCategory!!.size > 1)
                        _currentLearnStatus.value = LearnStatus.EXTRACT_TIME
                    else
                        _currentLearnStatus.value = LearnStatus.SHOW
                    _mealCategory!![0]
                }
                else -> {
                    _currentLearnStatus.value = LearnStatus.SHOW
                    _mealCategory!![1]
                }
            }

            val check204 = repository.check204() ?: false
            if (check204) {
                DWLog.e("온라인 상태")
                val response: GetMessageListResponse = repository.logGetMessageList(category)
                when (response.status_code) {
                    0 -> {
                        response.body?.let { it ->
                            if (BuildConfig.PRODUCT_TYPE == "WONDERFUL") { // WONDERFUL
                                if (it.msg != "") {
                                    synchronized(this) {
                                        _question.value = it.msg
                                        GCTextToSpeech.getInstance()?.speech(it.msg)
//                                        if (it.file != "" && URLUtil.isValidUrl(it.file)) {
//                                            GCTextToSpeech.getInstance()?.urlMediaSpeech(it.file)
//                                        } else {
//                                            GCTextToSpeech.getInstance()?.speech(it.msg)
//                                        }
                                    }
                                    _mealComment.postValue(Resource.success(it.msg))
                                }
                            } else { // KT
                                if (it.msg != "") {
                                    synchronized(this) {
                                        _question.value = it.msg
                                        GCTextToSpeech.getInstance()?.speech(it.msg)
                                    }
                                    _mealComment.postValue(Resource.success(it.msg))
                                }
                            }
                        } ?: run {
                            _mealComment.postValue(Resource.error("status code == -1", null))
                        }
                    }
                    else -> {
                        _mealComment.postValue(Resource.error("status code == -1", null))
                    }
                }
            } else {
                DWLog.e("오프라인 상태 category $category")
                var text = ""
                var src: Int = -1
                when (category) {
                    OnethefullBase.SLEEP_TIME_NAME -> {
                        text = context.getString(R.string.text_sleep_time_question_1)
                        src = R.raw.sleep_time1
                    }
                    OnethefullBase.WAKEUP_TIME_NAME -> {
                        text = context.getString(R.string.text_wakeup_time_question_2)
                        src = R.raw.wakeup_time2
                    }
                    OnethefullBase.BREAKFAST_NAME -> {
                        text = context.getString(R.string.text_breakfast_question_1)
                        src = R.raw.breakfast1
                    }
                    OnethefullBase.BREAKFAST_TIME_NAME -> {
                        text = context.getString(R.string.text_breakfast_time_question_1)
                        src = R.raw.breakfast_time1
                    }
                    OnethefullBase.LUNCH_NAME -> {
                        text = context.getString(R.string.text_lunch_question_1)
                        src = R.raw.lunch_time1
                    }
                    OnethefullBase.LUNCH_TIME_NAME -> {
                        text = context.getString(R.string.text_lunch_time_question_1)
                        src = R.raw.lunch_time1
                    }
                    OnethefullBase.DINNER_NAME -> {
                        text = context.getString(R.string.text_dinner_question_1)
                        src = R.raw.dinner1
                    }
                    OnethefullBase.DINNER_TIME_NAME -> {
                        text = context.getString(R.string.text_dinner_time_question_1)
                        src = R.raw.dinner_time1
                    }
                    else -> {
                        text = ""
                        src = -1
                    }
                }

                if (text != "" && src != -1) {
                    synchronized(this) {
                        _question.value = text
                        WMediaPlayer.instance.start(src)
                    }
                }
                _mealComment.postValue(Resource.success(text))
            }
        }
    }

    /**
     * 다솜K 튜토리얼
     */
    private val _tutorialComment = MutableLiveData<Resource<String>>()
    fun tutorialComment(): LiveData<Resource<String>> {
        return _tutorialComment
    }

    fun checkTutorialStatus(status: LearnStatus) {
        DWLog.d("checkTutorialStatus $status")
        uiScope.launch {
            _currentLearnStatus.value = status
            when (_currentLearnStatus.value) {
                LearnStatus.START_TUTORIAL_1,

                LearnStatus.START_TUTORIAL_1_1,
                LearnStatus.START_TUTORIAL_1_2,
                LearnStatus.START_TUTORIAL_1_3,

                LearnStatus.START_DASOMTALK_TUTORIAL_2,
                LearnStatus.START_VIDEOCALL_TUTORIAL_2,
                LearnStatus.START_SOS_TUTORIAL_2,
                LearnStatus.START_MEDICATION_TUTORIAL_2,
                LearnStatus.START_RADIO_TUTORIAL_2,
                LearnStatus.START_TUTORIAL_MV,

                LearnStatus.END_TUTORIAL_1_4,
                -> {
                    getTutorialMessage()
                }
                else -> {}
            }
        }
    }

    var ttsSetPlayCount = arrayListOf(3, 4, 5).random()
    private fun getTutorialMessage() {
        DWLog.d("getTutorialMessage ${_currentLearnStatus.value}")
        uiScope.launch {
            val text: String = repository.getIntroduceList(_currentLearnStatus.value)
            if (_currentLearnStatus.value == LearnStatus.START_DASOMTALK_VIDEO ||
                _currentLearnStatus.value == LearnStatus.START_VIDEOCALL_VIDEO ||
                _currentLearnStatus.value == LearnStatus.START_SOS_VIDEO ||
                _currentLearnStatus.value == LearnStatus.START_MEDICATION_VIDEO ||
                _currentLearnStatus.value == LearnStatus.START_RADIO_VIDEO ||
                _currentLearnStatus.value == LearnStatus.START_TUTORIAL_MV
            ) {
                DWLog.d("온라인,오프라인 상태 상관없이 로컬영상 재생")
                synchronized(this) {
                    ledJob = setLedOfDevice(arrayListOf(0, 1, 2).random())
                    BaseRobotController.robotService?.robotMotor?.motionStart(getRandom(), callback)
                }
                delay(1000L)
                _tutorialComment.postValue(Resource.success(text + "_offline"))
//                val check204 = repository.check204() ?: false
//                if (check204) {
//                    DWLog.d("영상 실행(유투브)")
//                    _tutorialComment.postValue(Resource.success(text))
//                } else {
//                    DWLog.d("오프라인 상태")
//                    _tutorialComment.postValue(Resource.success(text + "_offline"))
//                }
            } else {
                val check204 = repository.check204() ?: false
                if (check204) {
                    DWLog.d("온라인 상태")
                    synchronized(this) {
                        _question.value = text
                        ledJob = setLedOfDevice(arrayListOf(0, 1, 2, 3).random())
                        GCTextToSpeech.getInstance()?.speech(text)
                        BaseRobotController.robotService?.robotMotor?.motionStart(getRandom(), callback)
                    }
                    _tutorialComment.postValue(Resource.success(text))
                } else {
                    DWLog.d("오프라인 상태 ${_currentLearnStatus.value}")
                    _question.value = text
//                    if(BuildConfig.LANGUAGE_TYPE == "EN" || DasomProviderHelper.getCustomerCode(context) == "overseas") {
                    if (App.instance.getLocale() == Locale.US) {
                        when (_currentLearnStatus.value) {
                            LearnStatus.START_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1)

                            LearnStatus.START_TUTORIAL_1_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(arrayListOf(0, 1, 2).random())
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1_1)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1_1_avadin_bot)
                                }
                            }
                            LearnStatus.START_TUTORIAL_1_2 -> {
//                                WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1_2)
                                BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.MALBUT, callback)
                                WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1_2_avadin_bot)
                            }
                            LearnStatus.START_TUTORIAL_1_3 ->{
                                WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_1_3)
                            }

                            LearnStatus.START_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_2)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_2_avadin_bot)
                                }
                            }
                            LearnStatus.START_TUTORIAL_3 -> WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3)

                            LearnStatus.START_TUTORIAL_3_1 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.RANDOMCHAT_WAIT, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_1)
                                }
                            }
                            LearnStatus.START_TUTORIAL_3_2 -> WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_2)
                            LearnStatus.START_TUTORIAL_3_3 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.RANDOMCHAT_START, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_3)
                                }
                            }
                            LearnStatus.START_TUTORIAL_3_4 -> {
//                                WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_4)
                                WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_4_avadin_bot)
                            }
                            LearnStatus.START_TUTORIAL_3_5 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_RL, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_3_5)
                                }
                            }
                            LearnStatus.START_TUTORIAL_4 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_4)
                                }
                            }
                            LearnStatus.START_TUTORIAL_4_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(3)
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_4_1)
                                }
                            }
                            LearnStatus.START_TUTORIAL_4_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.RIGHT_ARM_UP, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_tutorial_4_2)
                                }
                            }
                            LearnStatus.START_DASOMTALK_TUTORIAL_1 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.HANDS_UP, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_1)
                                }
                            }
                            LearnStatus.START_DASOMTALK_TUTORIAL_1_1 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.HANDS_UP, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_1_1)
                                }
                            }
                            LearnStatus.START_DASOMTALK_TUTORIAL_1_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.HANDS_UP, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_1_2)
                                }
                            }
                            LearnStatus.START_DASOMTALK_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.RANDOMCHAT_WAIT, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_2_1)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_2_1_avadin_bot)
                                }
                            }

                            LearnStatus.START_DASOMTALK_TUTORIAL_2_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.RANDOMCHAT_WAIT, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_dasomtalk_tutorial_2_2)
                                }
                            }

                            LearnStatus.START_VIDEOCALL_TUTORIAL_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(arrayListOf(0, 1).random())
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_RL, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_videocall_tutorial_1)
                                }
                            }
                            LearnStatus.START_VIDEOCALL_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_videocall_tutorial_2)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_videocall_tutorial_2_avadinbot)
                                }
                            }
                            LearnStatus.START_SOS_TUTORIAL_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(2)
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.MALBUT, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_sos_tutorial_1)
                                }
                            }
                            LearnStatus.START_SOS_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_sos_tutorial_2)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_sos_tutorial_2_avadinbot)
                                }
                            }
                            LearnStatus.START_MEDICATION_TUTORIAL_1 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_medication_tutorial_1)
                                }
                            }
                            LearnStatus.START_MEDICATION_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.ROOSTER, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_medication_tutorial_2)
                                }
                            }
                            LearnStatus.START_RADIO_TUTORIAL_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(3)
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.GUITAR, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_radio_tutorial_1)
                                }
                            }
                            LearnStatus.START_RADIO_TUTORIAL_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_start_radio_tutorial_2)
                                    WMediaPlayer.instance.start(R.raw._c_en_start_radio_tutorial_2_avadin)
                                }
                            }

                            LearnStatus.END_TUTORIAL -> WMediaPlayer.instance.start(R.raw._c_en_end_tutorial)
                            LearnStatus.END_TUTORIAL_1_1 -> {
//                                WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_1)
                                WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_1_avadin)
                            }
                            LearnStatus.END_TUTORIAL_1_2_1 -> {
                                synchronized(this) {
                                    ledJob = setLedOfDevice(arrayListOf(0, 1, 2).random())
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_LR, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_2_1)
                                }
                            }
                            LearnStatus.END_TUTORIAL_1_2_2 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.LOOK_RL, callback)
//                                    WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_2_2)
                                    WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_2_2_avadin)
                                }
                            }

                            LearnStatus.END_TUTORIAL_1_3 -> {
                                ledJob = setLedOfDevice(3)
                                WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_3)
                            }
                            LearnStatus.END_TUTORIAL_1_4 -> {
                                synchronized(this) {
                                    BaseRobotController.robotService?.robotMotor?.motionStart(KebbiMotion.BYE, callback)
                                    WMediaPlayer.instance.start(R.raw._c_en_end_tutorial_1_4)
                                }
                            }
                        }
                    } else {
                        when (_currentLearnStatus.value) {
                            LearnStatus.START_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_start_tutorial_1)
                            LearnStatus.START_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_start_tutorial_2)
                            LearnStatus.START_TUTORIAL_3 -> WMediaPlayer.instance.start(R.raw._c_start_tutorial_3)
                            LearnStatus.START_TUTORIAL_4 -> WMediaPlayer.instance.start(R.raw._c_start_tutorial_4)

                            LearnStatus.START_DASOMTALK_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_start_dasomtalk_tutorial_1)
                            LearnStatus.START_DASOMTALK_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_start_dasomtalk_tutorial_2)
                            LearnStatus.START_VIDEOCALL_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_videocall_tutorial_1)
                            LearnStatus.START_VIDEOCALL_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_videocall_tutorial_2)
                            LearnStatus.START_SOS_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_sos_tutorial_1)
                            LearnStatus.START_SOS_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_sos_tutorial_2)
                            LearnStatus.START_MEDICATION_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_start_medication_tutorial_1)
                            LearnStatus.START_MEDICATION_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_start_medication_tutorial_2)
                            LearnStatus.START_RADIO_TUTORIAL_1 -> WMediaPlayer.instance.start(R.raw._c_start_radio_tutorial_1)
                            LearnStatus.START_RADIO_TUTORIAL_2 -> WMediaPlayer.instance.start(R.raw._c_start_radio_tutorial_2)

                            LearnStatus.END_TUTORIAL -> WMediaPlayer.instance.start(R.raw._c_end_tutorial)
                        }
                    }
                    _tutorialComment.postValue(Resource.success(text))
                }
            }
        }
    }

    var callback: IMotionCallback = object : IMotionCallback.Stub() {
        override fun finishMotion() {
            BaseRobotController.robotService?.robotMotor?.reset()
        }
    }

    private fun getRandom(): String {
        return arrayListOf(
            KebbiMotion.RANDOMCHAT_WAIT, KebbiMotion.RANDOMCHAT_START,
            KebbiMotion.LOOK_RL, KebbiMotion.LOOK_LR,
            KebbiMotion.HANDS_UP,
            KebbiMotion.RANDOMCHAT_FINISH, KebbiMotion.BOTH_ARM_UP
        ).random()
    }

    /**
     * 미디어 재생 시작
     * */
    override fun mediaPlayed() {
        DWLog.d("mediaPlayed")
        speechStarted()
    }

    /**
     * 미디어 재생 중지
     * */
    override fun mediaCompleted() {
        DWLog.d("mediaCompleted")
        speechFinished()
    }

    var noResponse: Boolean = true

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {
        DWLog.e("checkCurrentStatus :: ${_currentLearnStatus.value}")
        when (_currentLearnStatus.value) {
            /**
             *  긴급상황 튜토리얼
             *  */
            LearnStatus.START -> {
                if (BuildConfig.PRODUCT_TYPE == "KT") {
                    getGeniePracticeEmergencyComment(LearnStatus.CALL_GEINIE)
                } else {
                    getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
                }
            }

            LearnStatus.CALL_DASOM -> {
                var delay = 10 * 1000L
                if (noResponse) {
                    delay = if (noResponseFlowTask.get(context)) {
                        DWLog.e("1회 무응답/미인식 => 10초 delay 후 재시작 ")
                        10 * 1000L
                    } else {
                        DWLog.e("2회 무응답/미인식 => 20초 delay 후 앱 종료")
                        20 * 1000L
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (noResponse) {
                        if (noResponseFlowTask.get(context))
                            restart()
                        else
                            getPracticeEmergencyComment(LearnStatus.END) // 두번째 무응답/미인식 일 때, end
                    }
                }, delay)
            }

            LearnStatus.CALL_SOS -> {
                RxBus.publish(RxEvent.destroyAppUpdate)
            }

            LearnStatus.RETRY -> {
                val delay = 20 * 1000L
                Handler(Looper.getMainLooper()).postDelayed({
                    if (noResponse) {
                        getPracticeEmergencyComment(LearnStatus.HALF)
                    } else {
                        RxBus.publish(RxEvent.destroyShortAppUpdate)
                    }
                }, delay)
            }

            LearnStatus.HALF, LearnStatus.COMPLETE -> {
                getPracticeSosResult()
                RxBus.publish(RxEvent.destroyAppUpdate)
            }

            LearnStatus.END -> {
                RxBus.publish(RxEvent.destroyApp)
            }

            /**
             * 치매예방퀴즈
             * */
            LearnStatus.QUIZ_SHOW -> {
                getDementiaQuizList(LearnStatus.QUIZ_START, null)
            }

            LearnStatus.QUIZ_START -> {
                DWLog.d("30초동안 응답없음 종료 coroutine 적용")
                RxBus.publish(RxEvent.destroyAppUpdate)
//                App.instance.jobList.put(RxEvent.AppDestroyUpdate.toString(), createNewTimer(RxEvent.AppDestroyUpdate.toString(), 30 * 1000L))
            }

            LearnStatus.QUIZ_ERROR -> {
                DWLog.d("더이상 풀 문제 없음 즉시종료")
                RxBus.publish(RxEvent.destroyApp)
            }

            /**
             * 취침/기상/식사확인
             * */
            LearnStatus.SHOW -> {
                DWLog.d("30초동안 응답없음 종료")
                RxBus.publish(RxEvent.destroyAppUpdate)
            }
            /**
             * 식사 시간확인
             * */
            LearnStatus.EXTRACT_TIME -> {
                DWLog.d("30초동안 응답없음 종료")
                RxBus.publish(RxEvent.destroyAppUpdate)
            }
            /**
             *  다솜 깨비 튜토리얼
             *  */
            LearnStatus.START_TUTORIAL_1 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_2
                getTutorialMessage()
            }
            //
            LearnStatus.START_TUTORIAL_1_1 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_1_2
                getTutorialMessage()

//                // @@@@@@@@@@@ test !!!!!!!!!!!!!
//                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_4
//                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_1_2 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_1_3
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_1_3 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_2
                getTutorialMessage()
            }

            LearnStatus.START_TUTORIAL_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_3_1
                getTutorialMessage()
            }

            LearnStatus.START_TUTORIAL_3 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_4
                getTutorialMessage()
            }
            //
            LearnStatus.START_TUTORIAL_3_1 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_3_2
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_3_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_3_3
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_3_3 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_3_4
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_3_4 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_3_5
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_3_5 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_4_1
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_4 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_TUTORIAL_1
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_4_1 -> {
                _currentLearnStatus.value = LearnStatus.START_TUTORIAL_4_2
                getTutorialMessage()
            }
            LearnStatus.START_TUTORIAL_4_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_TUTORIAL_1_1
                getTutorialMessage()
            }
            LearnStatus.START_DASOMTALK_TUTORIAL_1 -> {
                DWLog.d("유투브 앱(다솜톡 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_VIDEO
                getTutorialMessage()
            }
            LearnStatus.START_DASOMTALK_TUTORIAL_1_1 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate)
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_TUTORIAL_1_2
                getTutorialMessage()
            }
            LearnStatus.START_DASOMTALK_TUTORIAL_1_2 -> {
                DWLog.d("유투브 앱(다솜톡 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
//                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_VIDEO
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_TUTORIAL_2
                getTutorialMessage()
            }
            //  유투브 앱(다솜톡 동영상) 실행 후 발화 후
            LearnStatus.START_DASOMTALK_TUTORIAL_2 -> {
                _currentLearnStatus.value = LearnStatus.START_DASOMTALK_TUTORIAL_2_2
                getTutorialMessage()
            }
            LearnStatus.START_DASOMTALK_TUTORIAL_2_2 -> {
                _currentLearnStatus.value = LearnStatus.START_VIDEOCALL_TUTORIAL_1
                getTutorialMessage()
            }
            LearnStatus.START_VIDEOCALL_TUTORIAL_1 -> {
                DWLog.d("유투브 앱(영상통화 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
//                _currentLearnStatus.value = LearnStatus.START_VIDEOCALL_VIDEO
                _currentLearnStatus.value = LearnStatus.START_VIDEOCALL_TUTORIAL_2
                getTutorialMessage()
            }

            //  유투브 앱(영상통화 동영상) 실행 후 후
            LearnStatus.START_VIDEOCALL_TUTORIAL_2 -> {
                _currentLearnStatus.value = LearnStatus.START_SOS_TUTORIAL_1
                getTutorialMessage()
            }
            LearnStatus.START_SOS_TUTORIAL_1 -> {
                DWLog.d("유투브 앱(긴급상황 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
//                _currentLearnStatus.value = LearnStatus.START_SOS_VIDEO
                _currentLearnStatus.value = LearnStatus.START_SOS_TUTORIAL_2
                getTutorialMessage()
            }

            //  유투브 앱(긴급상황 동영상) 실행 후 발화 후
            LearnStatus.START_SOS_TUTORIAL_2 -> {
                _currentLearnStatus.value = LearnStatus.START_MEDICATION_TUTORIAL_1
                getTutorialMessage()
            }
            LearnStatus.START_MEDICATION_TUTORIAL_1 -> {
                DWLog.d("유투브 앱(복약 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
//                _currentLearnStatus.value = LearnStatus.START_MEDICATION_VIDEO
                _currentLearnStatus.value = LearnStatus.START_RADIO_TUTORIAL_1
                getTutorialMessage()
            }

            //  유투브 앱(긴급상황 동영상) 실행 후 발화 후
            LearnStatus.START_MEDICATION_TUTORIAL_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                _currentLearnStatus.value = LearnStatus.START_RADIO_TUTORIAL_1
                getTutorialMessage()
            }
            LearnStatus.START_RADIO_TUTORIAL_1 -> {
                DWLog.d("유투브 앱(라디오 동영상)")
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
//                _currentLearnStatus.value = LearnStatus.START_RADIO_VIDEO
                _currentLearnStatus.value = LearnStatus.START_RADIO_TUTORIAL_2
                getTutorialMessage()
            }

            //  유투브 앱(라디오 동영상) 실행 후 발화 후
            LearnStatus.START_RADIO_TUTORIAL_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_1
                getTutorialMessage()
            }
            LearnStatus.END_TUTORIAL -> {
                DWLog.d("END_TUTORIAL")
                RxBus.publish(RxEvent.destroyApp)
            }
            LearnStatus.END_TUTORIAL_1_1 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_2_1
                getTutorialMessage()
            }
            LearnStatus.END_TUTORIAL_1_2_1 -> {
                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_2_2
                getTutorialMessage()
            }
            LearnStatus.END_TUTORIAL_1_2_2 -> {
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_3
                getTutorialMessage()
            }
            LearnStatus.END_TUTORIAL_1_3 -> {
                _currentLearnStatus.value = LearnStatus.END_TUTORIAL_1_4
                getTutorialMessage()
            }
            LearnStatus.END_TUTORIAL_1_4 -> {
//                RxBus.publish(RxEvent.destroyApp)
                RxBus.publish(RxEvent.destroyLongTimeUpdate4)
                ttsSetPlayCount--
                DWLog.e("ttsSetPlayCount ==> $ttsSetPlayCount")
                if (ttsSetPlayCount <= 0) {
                    DWLog.d("END_TUTORIAL_1_4 영상 실행")
                    ttsSetPlayCount = arrayListOf(3, 4, 5).random()
                    _currentLearnStatus.value = LearnStatus.START_TUTORIAL_MV
                    getTutorialMessage()
                } else {
                    DWLog.d("END_TUTORIAL_1_4 앱 재실행")
                    _currentLearnStatus.value = LearnStatus.START_TUTORIAL_1_1
                    getTutorialMessage()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun createNewTimer(key: String, delay: Long): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            delay(delay)
            when (key) {
                RxEvent.AppDestroyUpdate.toString() -> {
                    DWLog.d("앱 종료?????? ")
                    when (BuildConfig.TARGET_DEVICE) {
                        App.DEVICE_BEANQ -> {
                            DWLog.d("CoroutineScope SceneHelper.switchOut() ")
                            SceneHelper.switchOut()
                            App.instance.currentActivity?.finish()
                            Process.killProcess(Process.myPid())
                        }
                        App.DEVICE_CLOI -> {
                            com.onethefull.wonderfulrobotmodule.scene.SceneHelper.switchOut()
                            App.instance.currentActivity?.finishAffinity()
                        }
                    }
                }
            }
            App.instance.jobList.remove(key)
        }
    }

    var ledJob: Job? = null
    var colorValue = 0
    var colorReturn = false

    private fun setLedOfDevice(type: Int): Job? {
        var isRunning = true
        return CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isRunning) {
                    when (type) {
                        -1 -> {
                            Gson().toJson(
                                LedData(
                                    redId = 0,
                                    green = -1,
                                    blue = -1,
                                    red = -1,
                                    period = -1,
                                    count = 0,
                                    type = LED_CONIFG.TURN_OFF,
                                    direction = 0
                                )
                            ).toString().apply {
                                BaseRobotController.robotService?.setLed(this)
                            }
                            isRunning = false
                            cancel()
                        }
                        0 -> {
                            Gson().toJson(
                                LedData(
                                    redId = 0,
                                    green = 255,
                                    blue = 0,
                                    red = 0,
                                    period = 0,
                                    count = 0,
                                    type = -0x00001,
                                    direction = 0
                                )
                            ).toString().apply {
                                BaseRobotController.robotService?.setLed(this)
                            } // 다솜
                            isRunning = false
                            cancel()
                        }
                        1 -> {
                            Gson().toJson(
                                LedData(
                                    redId = -0,
                                    green = 0,
                                    blue = 255,
                                    red = 0,
                                    period = 0,
                                    count = 0,
                                    type = -0x00001,
                                    direction = 0
                                )
                            ).toString().apply {
                                BaseRobotController.robotService?.setLed(this)
                            } // 다솜
                            isRunning = false
                            cancel()
                        }
                        2 -> {
                            Gson().toJson(
                                LedData(
                                    redId = 0,
                                    green = 0,
                                    blue = 0,
                                    red = 255,
                                    period = 0,
                                    count = 0,
                                    type = -0x00001,
                                    direction = 0
                                )
                            ).toString().apply {
                                BaseRobotController.robotService?.setLed(this)
                            } // 다솜
                            isRunning = false
                            cancel()
                        }
                        3 -> {
                            var r = 181 + colorValue
                            var g = 29 + colorValue
                            var b = 234 + colorValue

                            if (r > 255) r = 255
                            if (g > 255) g = 255
                            if (b > 255) b = 255

                            colorValue
                            Gson().toJson(
                                LedData(
                                    redId = 0,
                                    green = abs(g),
                                    blue = abs(b),
                                    red = abs(r),
                                    period = 0,
                                    count = 0,
                                    type = -0x00001,
                                    direction = 0
                                )
                            ).toString().apply {
                                BaseRobotController.robotService?.setLed(this)
                            } // 다솜
                            isRunning = false
                            cancel()
                        }
                    }

                    if (colorReturn) {
                        if (colorValue <= 0)
                            colorReturn = false
                    } else {
                        if (colorValue > 100) {
                            colorReturn = true
                        }
                    }
                    if (colorReturn) {
                        colorValue--
                    } else {
                        colorValue++
                    }
                    delay(250)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                colorValue = 0
            }
        }
    }
}