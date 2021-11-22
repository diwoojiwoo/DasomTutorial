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
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.ResultQuiz
import com.onethefull.dasomtutorial.data.model.Status
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
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2021/11/10
 */
class LearnViewModel(
    private val context: Activity,
    private val repository: LearnRepository,
    private val apiHelper: ApiHelper
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
        LocalDasomFilterTask.setCommand(LocalDasomFilterTask.Command.EMPTY)
        // 앱 자동 종료 초기화
        RxBus.publish(
            RxEvent.Event(
                RxEvent.AppDestoryUpdate,
                TIME_APP_TERMINATE,
                "AppDestoryUpdate"
            )
        )
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
    fun practicesComment(): LiveData<Resource<InnerTtsV2>> {
        return practiceComment
    }

    fun getPracticeEmergencyComment(status: LearnStatus) {
        Log.e(App.TAG, "******** Task.getPracticeEmergencyComment [${status}] ********")
        _currentLearnStatus.value = status
        uiScope.launch {
            practiceComment.postValue(Resource.loading(null))
            _listOptions.postValue(mutableListOf())
            try {
                var result: InnerTtsV2 = when (status) {
                    LearnStatus.START -> {
                        repository.getPracticeEmergencyList(DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE).random()
                    }
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
                    else -> InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0)
                }

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

    fun connect() {
        mGCSpeechToText.start()
        GCTextToSpeech.getInstance()?.setCallback(this)
        // 앱 자동 종료 초기화 => Coroutine 으로 변환
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
        DWLog.d("handleRecognition:: $text ")

        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
//            networkError(GCTextToSpeech.INDEX_OFFLINE_WIFI_IS_UNSTABLE)
            return
        }

        _listOptions.postValue(mutableListOf(text))

        if ((_currentLearnStatus.value == LearnStatus.CALL_DASOM)
            && LocalDasomFilterTask.checkDasom(text)
        ) {
            uiScope.launch {
                _currentLearnStatus.value = LearnStatus.CALL_SOS
                _question.value = "\"살려줘\" 또는 \"도와줘\" 라고 말해보세요."
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
        if (_currentLearnStatus.value != LearnStatus.START) {
            mGCSpeechToText.resume()
            _speechStatus.value = SpeechStatus.WAITING
        }
    }

    private val status = MutableLiveData<Resource<Status>>()
    fun callPracticeSos(): LiveData<Resource<Status>> {
        return status
    }
    fun practiceSos() {
        uiScope.launch {
            status.postValue(Resource.loading(null))
            try {
                val practiceSosApi = apiHelper.practiceSos(
                    DasomProviderHelper.getCustomerCode(context),
                    DasomProviderHelper.getDeviceCode(context)
                )
                status.postValue(Resource.success(practiceSosApi))
            } catch (e: Exception) {
                status.postValue(Resource.error("Something Went Wrong", null))
            }
        }
    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {
        DWLog.e("checkCurrentStatus :: ${_currentLearnStatus.value}")
        when (_currentLearnStatus.value) {
            LearnStatus.START -> {
                getPracticeEmergencyComment(LearnStatus.CALL_DASOM)
            }

            LearnStatus.CALL_DASOM -> {
                // 1분 동안 무응답/미인식 일 때, 앱 자동 종료 세팅
//                RxBus.publish(RxEvent.destroyApp)
            }

            LearnStatus.CALL_SOS, LearnStatus.RETRY -> {

            }

            LearnStatus.HALF, LearnStatus.COMPLETE -> {
                // practiceSos API 호출
                (App.instance.currentActivity as MainActivity).finish()
            }

            LearnStatus.END -> {

            }

            LearnStatus.CALL_DASOM_RETRY -> {
                //무응답
            }
            else -> {

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        private const val TIME_APP_TERMINATE = 90 * 1000L
    }
}