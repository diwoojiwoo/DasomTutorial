package com.onethefull.dasomtutorial.ui.learn

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.*
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuiz
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.learn.localhelp.LocalDasomFilterTask
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
                            arrayListOf("‘지니야’ 라고 말하고, 마이크가 켜지면 '살려줘'하고 말해보세요. \n"),
                            "practice_emergency_start",
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
        RxBus.publish(RxEvent.Event(RxEvent.AppDestroy, 90 * 1000L, "AppDestroy"))
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
        RxBus.publish(RxEvent.Event(RxEvent.AppDestoryUpdate, 60 * 1000L, "AppDestroy"))

        /* 긴급상황 튜토리얼 */
        if (_currentLearnStatus.value == LearnStatus.CALL_DASOM) {
            uiScope.launch {
                if (BuildConfig.PRODUCT_TYPE == "KT") {
                    if (LocalDasomFilterTask.checkGenie(text)) {
                        _currentLearnStatus.value = LearnStatus.CALL_SOS
                        _question.value = "\"살려줘\" 또는 \"도와줘\" 라고 말해보세요."
                    } else {
                        DWLog.e("지니야 이외의 단어를 이야기한 경우 ")
                    }
                } else {
                    if (LocalDasomFilterTask.checkDasom(text)) {
                        _currentLearnStatus.value = LearnStatus.CALL_SOS
                        _question.value = "\"살려줘\" 또는 \"도와줘\" 라고 말해보세요."
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
                if (EmergencyFlowTask.isFirst(context)) {
                    getPracticeEmergencyComment(LearnStatus.RETRY)
                } else
                    getPracticeEmergencyComment(LearnStatus.COMPLETE)
            }
        } else if (_currentLearnStatus.value == LearnStatus.RETRY) {
            uiScope.launch {
                if (LocalDasomFilterTask.checkPosWord(text)) {
                    EmergencyFlowTask.insert(context, 1)
                    getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
                } else
                    getPracticeEmergencyComment(LearnStatus.HALF)
            }
        }

        /* 치매예방퀴즈 */
        else if (_currentLearnStatus.value == LearnStatus.QUIZ_START) {
            currentDementiaQuiz.response = text
            mSolvedDementiaQuestionList.add(currentDementiaQuiz)
            getDementiaQuizList(LearnStatus.QUIZ_START, null)
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
    private val _dementiaQuizList = MutableLiveData<DementiaQuizListResponse>()
    fun dementiaQuizList(): LiveData<DementiaQuizListResponse> {
        return _dementiaQuizList
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
//                        var speechText = when (mDementiaQuestionList.size) {
//                            5 -> "두뇌운동퀴즈 시간입니다."
//                            4, 3, 2 -> "다음 문제 입니다. "
//                            1 -> "마지막 문제 입니다. "
//                            else -> ""
//                        }
//                        speechText += quiz.question
                        synchronized(this) {
                            _question.value = quiz.question
                            GCTextToSpeech.getInstance()?.speech(quiz.question)
                        }
                        mDementiaQuestionList.remove(quiz)
                    }
                }
                else -> {

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
            /* 긴급상황 튜토리얼 */
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
                RxBus.publish(RxEvent.Event(RxEvent.AppDestoryUpdate, 30 * 1000L, "AppDestroy"))
            }

            LearnStatus.RETRY -> {
                val delay = 20 * 1000L
                Handler(Looper.getMainLooper()).postDelayed({
                    if (noResponse) {
                        getPracticeEmergencyComment(LearnStatus.HALF)
                    } else {
                        RxBus.publish(RxEvent.Event(RxEvent.AppDestoryUpdate, 15 * 1000L, "AppDestroy"))
                    }
                }, delay)
            }

            LearnStatus.HALF, LearnStatus.COMPLETE -> {
                getPracticeSosResult()
                RxBus.publish(RxEvent.Event(RxEvent.AppDestoryUpdate, 30 * 1000L, "AppDestroy"))
            }

            LearnStatus.END -> {
                RxBus.publish(RxEvent.destroyApp)
            }

            /* 치매예방퀴즈 */
            LearnStatus.QUIZ_SHOW -> {
                getDementiaQuizList(LearnStatus.QUIZ_START, null)
            }

            LearnStatus.QUIZ_START -> {
                DWLog.d("30초동안 응답없음 종료")
                RxBus.publish(RxEvent.Event(RxEvent.AppDestoryUpdate, 30 * 1000L, "AppDestroy"))
            }

            LearnStatus.QUIZ_ERROR -> {
                DWLog.d("더이상 풀 문제 없음")
                RxBus.publish(RxEvent.destroyApp)
            }

            else -> {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}