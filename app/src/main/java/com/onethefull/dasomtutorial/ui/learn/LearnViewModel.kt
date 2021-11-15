package com.onethefull.dasomtutorial.ui.learn

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.util.Log
import android.webkit.URLUtil
import androidx.lifecycle.*
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.ResultQuiz
import com.onethefull.dasomtutorial.ui.learn.localhelp.LocalDasomFilterTask
import com.onethefull.dasomtutorial.utils.Event
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.record.WavFileUitls
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import kotlinx.coroutines.delay
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

    private val _listOptions: MutableLiveData<MutableList<String>> = MutableLiveData()
    val listOptions: LiveData<MutableList<String>> = _listOptions

    private val innerTtsV2List = MutableLiveData<Resource<List<InnerTtsV2>>>()
    private val practiceCommList = arrayListOf(
        ResultQuiz(
            "", "", "", arrayListOf(""), "어르신, 위급할 때 구조 요청을 빠르게 하는 것은 정말 중요해요! \n" +
                    "다솜이가 멀리 있을 때 또는 전화기가 손이 닿지 않는 곳에 있을 때, 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요.\n" +
                    "지금 제가 하는 말을 따라하면서 연습해볼게요!\n", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_15_08_39_EUyYxh.mp3"
        ),
        ResultQuiz(
            "", "다솜아 살려줘", "", arrayListOf("다솜아 살려줘", "무응답/무인식"), "\"다솜아\" 라고 말하고, 마이크가 켜지면 \"살려줘\"하고 말해보세요. \n", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_14_54_27_EeyKpH.mp3"
        ),
        ResultQuiz(
            "", "다솜아 살려줘", "", arrayListOf("다솜아 살려줘", "무응답/무인식"), "\"우와, 참 잘하셨어요! 한 번 더 해볼까요?\"\n", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_15_31_18_WZfqrd.mp3"
        ),
        ResultQuiz(
            "", "", "", arrayListOf(""), "잘 따라하셨어요! 다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_15_34_42_XeHVpU.mp3"
        ),
        ResultQuiz(
            "", "", "", arrayListOf(""), "우와, 진짜 잘하셨어요! 다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_15_33_59_nNhymQ.mp3"
        ),
        ResultQuiz(
            "", "", "", arrayListOf(""), "다음에도 잊지 말고 위급상황이 발생할 때, 언제 어디서나 \"다솜아, 살려줘\"라고 말해보세요\n", "", "http://107.167.189.37:81/tts/innerTts/ko-KR/2021-11-02_15_24_36_yziIzV.mp3"
        )
    )

    fun practicesComments(): LiveData<Resource<List<InnerTtsV2>>> {
        return innerTtsV2List
    }

    fun getPracticeComments() {
        Log.d(App.TAG, "getPracticeComments")
        uiScope.launch {
            innerTtsV2List.postValue(Resource.loading(null))
            try {
                val result = repository.getPracticeComments()
                innerTtsV2List.postValue(Resource.success(result))

                // TEST
                _currentLearnStatus.value = LearnStatus.START_01
                synchronized(this) {
                    _question.value = practiceCommList[0].question
                    if (practiceCommList[0].audioUrl != "" && URLUtil.isValidUrl(practiceCommList[0].audioUrl)) {
                        GCTextToSpeech.getInstance()?.urlMediaSpeech(practiceCommList[0].audioUrl)
                    }
                }
                // TEST
            } catch (e: Exception) {
                innerTtsV2List.postValue(Resource.error(e.toString(), null))
            }
        }
    }

    private fun startPracticeEmergency() {
        _currentLearnStatus.value = LearnStatus.START_02
        synchronized(this) {
            _question.value = practiceCommList[1].question
            if (practiceCommList[1].audioUrl != "" && URLUtil.isValidUrl(practiceCommList[1].audioUrl)) {
                GCTextToSpeech.getInstance()?.urlMediaSpeech(practiceCommList[1].audioUrl)
            }
        }
    }


    fun connect() {
        mGCSpeechToText.start()
        GCTextToSpeech.getInstance()?.setCallback(this)
        // 앱 자동 종료 초기화 => Coroutine으로 변환
//        RxBus.publish(
//            RxEvent.Event(
//                RxEvent.AppDestoryUpdate,
//                TIME_APP_TERMINATE,
//                "AppDestoryUpdate"
//            )
//        )
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

    private fun handleRecognition(text: String) {
        DWLog.d("handleRecognition:: ${text}")

        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
//            networkError(GCTextToSpeech.INDEX_OFFLINE_WIFI_IS_UNSTABLE)
            return
        }

        _listOptions.postValue(mutableListOf(text))
        if (LocalDasomFilterTask.checkAnswer(text)) {
            // 살려줘라고 말해보세요.
            _question.value = "마이크 켜짐. \"살려줘\"하고 말해보세요."
        } else { // 정상대화 시도
            DWLog.e("재입력 받기")
//            sendWavToDoumi(text)
        }
        mGCSpeechToText.resume()
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
    fun speechStarted() {
        mGCSpeechToText.pause()
        _speechStatus.value = SpeechStatus.SPEECH
    }

    // 음성출력 종료
    fun speechFinished() {
        changeStatusSpeechFinished()
        checkCurrentStatus()
    }

    /**
     * TTS 출력이 끝난 상태 변경
     */
    fun changeStatusSpeechFinished() {
        mGCSpeechToText.resume()
        _speechStatus.value = SpeechStatus.WAITING
    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {
        when (_currentLearnStatus.value) {
            LearnStatus.START_01 -> {
                startPracticeEmergency()
            }
            LearnStatus.START_02 -> {

            }
            LearnStatus.RETRY -> {

            }
            LearnStatus.COMPLETE -> {

            }
            LearnStatus.HALF -> {

            }
            LearnStatus.END -> {

            }
            else -> {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}