package com.onethefull.dasomtutorial.ui.learn

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.*
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuiz
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.learn.localhelp.LocalDasomFilterTask
import com.onethefull.dasomtutorial.utils.ParamGeneratorUtils
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.record.WavFileUitls
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.onethefull.dasomtutorial.utils.task.EmergencyFlowTask
import com.onethefull.dasomtutorial.utils.task.noResponseFlowTask
import com.roobo.core.scene.SceneHelper
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2021/11/10
 */
class LearnViewModel(
    private val context: Activity,
    private val repository: LearnRepository,
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback {
    private var mGCSpeechToText: GCSpeechToText =
        GCSpeechToTextImpl(context as MainActivity)

    private var wavUtils = WavFileUitls()
    private var isSuccessRecog = false

    init {
        connect()
        mGCSpeechToText.setCallback(this)
        mGCSpeechToText.setWavUtils(wavUtils)
        EmergencyFlowTask.insert(context, 0)
        noResponseFlowTask.insert(context, 0)
        LocalDasomFilterTask.setCommand(LocalDasomFilterTask.Command.EMPTY)
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
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE).random()
                    LearnStatus.CALL_DASOM -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_START_VALUE).random()
                    }
                    LearnStatus.RETRY -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_RETRY_VALUE).random()
                    }
                    LearnStatus.HALF -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_HALF_VALUE).random()
                    }
                    LearnStatus.COMPLETE -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_COMPLETE_VALUE).random()
                    }
                    LearnStatus.END -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_END_VALUE).random()
                    }
                    else -> InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0)
                }

//                noResponse = true // 응답 초기화
                _currentLearnStatus.value = status
                synchronized(this) {
                    _question.value = result.text[0]
                    if (result.audioUrl[0] != "" && URLUtil.isValidUrl(result.audioUrl[0])) {
                        GCTextToSpeech.getInstance()?.urlMediaSpeech(result.audioUrl[0])
                    }
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
                        repository.getGeniePracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE).random()
                    LearnStatus.CALL_GEINIE -> {
                        InnerTtsV2(arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("‘다솜아’ 라고 말하고, 마이크가 켜지면 '살려줘'하고 말해보세요."),
                            "",
                            1)
                    }
                    LearnStatus.RETRY -> {
                        //“우와, 참 잘하셨어요! 한 번 더 해볼까요?”
                        InnerTtsV2(arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("우와, 참 잘하셨어요! 한 번 더 해볼까요? 원하시면 '그래' 라고 말씀해주세요."),
                            "",
                            1)
                    }
                    LearnStatus.HALF -> {
                        InnerTtsV2(arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("잘 따라하셨어요! 다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요"),
                            "",
                            1)
                    }
                    LearnStatus.COMPLETE -> {
                        InnerTtsV2(arrayListOf(),
                            arrayListOf(),
                            arrayListOf(),
                            "",
                            "Dasom,Avadin",
                            arrayListOf("다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요"),
                            "",
                            1)
                    }
                    else -> InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0)
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
    }

    fun disconnect() {
        mGCSpeechToText.release()
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
                /***
                 * KT "다솜아"
                 * */
                if (BuildConfig.PRODUCT_TYPE == "KT") {
//                    if (LocalDasomFilterTask.checkGenie(text)) {
                    if (LocalDasomFilterTask.checkDasom(text)) {
                        _currentLearnStatus.value = LearnStatus.CALL_SOS
                        _question.value = context.getString(R.string.tv_call_sos)
                    } else {
                        DWLog.e("다솜아 이외의 단어를 이야기한 경우 ")
                    }
                }
                /***
                 * 원더풀 "다솜아"
                 * */
                else {
                    if (LocalDasomFilterTask.checkDasom(text)) {
                        _currentLearnStatus.value = LearnStatus.CALL_SOS
                        _question.value = context.getString(R.string.tv_call_sos)
                    } else {
                        DWLog.e("다솜아 이외의 단어를 이야기한 경우 ")
                    }
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
                val lang = when (BuildConfig.LANGUAGE_TYPE) {
                    "KO" -> "ko"
                    "en" -> "en"
                    else -> "ko"
                }

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
                val lang = when (BuildConfig.LANGUAGE_TYPE) {
                    "KO" -> "ko"
                    "en" -> "en"
                    else -> "ko"
                }

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

    override fun onGenieSTTResult(result: String) {}


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
            mGCSpeechToText.resume()
            _speechStatus.value = SpeechStatus.WAITING
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
                                quiz.answer)
                            answerDementiaQuizList.add(answer)
                        }
                        val insertLogApi: Status = repository.insertDementiaQuizLog(answerDementiaQuizList)
                        insertLogApi.let {
                            RxBus.publish(RxEvent.destroyApp)
                        }
                    } else { // 문제 풀기
                        val quiz = mDementiaQuestionList[0]
                        currentDementiaQuiz = quiz
                        var speechText = when (mDementiaQuestionList.size) {
                            5 -> "두뇌운동퀴즈 시간입니다.\n"
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
        val category = when (_currentLearnStatus.value) {
            LearnStatus.EXTRACT_CATEGORY -> {
                if(_mealCategory!!.size > 1)
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

        uiScope.launch {
            val response: GetMessageListResponse = repository.logGetMessageList(category)
            when (response.status_code) {
                0 -> {
                    response.body?.let { it ->
                        if (it.msg != "") {
                            synchronized(this) {
                                _question.value = it.msg
                                GCTextToSpeech.getInstance()?.speech(it.msg)
                            }
                            _mealComment.postValue(Resource.success(it.msg))
                        }
                    } ?: run {
                        _mealComment.postValue(Resource.error("status code == -1", null))
                    }
                }
                else -> {
                    _mealComment.postValue(Resource.error("status code == -1", null))
                }
            }
        }
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
                DWLog.d("30초동안 응답없음 종료")
                RxBus.publish(RxEvent.destroyAppUpdate)
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

            else -> {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}