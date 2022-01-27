package com.onethefull.dasomtutorial.ui.guide

import android.app.Activity
import android.os.Process
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.repository.GuideRepository
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.ui.guide.localhelp.LocalGuideFilterTask
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.record.WavFileUitls
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.roobo.vision.WFaceManager
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Created by sjw on 2021/12/22
 */

class GuideViewModel(
    private val context: Activity,
    private val repository: GuideRepository,
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback,
    WFaceManager.Callback {
    private var mGCSpeechToText: GCSpeechToText =
        GCSpeechToTextImpl(context as MainActivity)
    private var wavUtils = WavFileUitls()
    private var isSuccessRecog = false

    /** 음성출력 상태*/
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    /** 서비스가이드 대화*/
    private val _guideText: MutableLiveData<String> = MutableLiveData<String>()
    val guideText: LiveData<String> = _guideText

    private val guideComment = MutableLiveData<Resource<GuideTts>>()
    fun guideComment(): LiveData<Resource<GuideTts>> {
        return guideComment
    }

    /** 가이드 진행 상태*/
    private val _currentGuideStatus: MutableLiveData<GuideStatus> = MutableLiveData<GuideStatus>()
    val currentGuideStatus: LiveData<GuideStatus> = _currentGuideStatus

    private val _listOptions: MutableLiveData<MutableList<String>> = MutableLiveData()
    val listOptions: LiveData<MutableList<String>> = _listOptions

    init {
        connect()
        mGCSpeechToText.setCallback(this)
        mGCSpeechToText.setWavUtils(wavUtils)
        WFaceManager.instance.setCallback(this)
        LocalGuideFilterTask.setCommand(LocalGuideFilterTask.Command.EMPTY)
    }

    fun connect() {
        mGCSpeechToText.start()
        GCTextToSpeech.getInstance()?.setCallback(this)
        delayDestroyApp() // 매 상태마다 앱 종료 초기화 (1분 30초) TODO Coroutine 변경
    }

    fun disconnect() {
        mGCSpeechToText.release()
        stopFaceCheck()
    }

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
        DWLog.d("handleRecognition :: $text, currentGuideStatus ${_currentGuideStatus.value}")

        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
            return
        }

        delayDestroyApp()
        _listOptions.postValue(mutableListOf(text))

        /** 웨이크업 가이드 **/
        if (_currentGuideStatus.value == GuideStatus.WAKEUP_INIT) {  // "어려움이 많으신가요?"
            uiScope.launch {
                if (LocalGuideFilterTask.checkDifficulty(text)) { // 응 어려워
                    DWLog.d("웨이크업 서비스 가이드 진행")
                    getGuideComment(GuideStatus.WAKEUP_GUIDE_SERVICE)
                } else {
                    DWLog.e("NO인 경우, 기존 대화 로직으로 진행 ==> 대화 앱 ")
                }
            }
        } else if (_currentGuideStatus.value == GuideStatus.WAKEUP_GUIDE_SERVICE) {
            uiScope.launch {
                if (LocalGuideFilterTask.checkDasom(text)) {
                    getGuideComment(GuideStatus.WAKEUP_GUIDE_FINISH)
                } else {
                    DWLog.e("제가 잘 이해하지 못했네요.\n" +
                            "다시 한번 '다솜아'라고 말하고 마이크가 켜진 후 저에게 하고싶은 말을 자유롭게 해보세요\n  ==> 대화 앱 ")
                }
            }
        }

        /** 영상인식 가이드 **/
        if (_currentGuideStatus.value == GuideStatus.VISION_INIT) { // 알고 계세요?
            uiScope.launch {
                if (!LocalGuideFilterTask.checkPosWord(text)) {
                    DWLog.d("영상인식 서비스 가이드 진행")
                    getGuideComment(GuideStatus.VISION_GUIDE_SERVICE)
                } else {
                    DWLog.e("NO인 경우, 기존 대화 로직으로 진행 ==> 대화 앱 ")
                }
            }
        }

        /** 복약알림 가이드 **/
        if (_currentGuideStatus.value == GuideStatus.MEDI_INIT) {
            uiScope.launch {
                if (!LocalGuideFilterTask.checkPosWord(text)) {
                    DWLog.d("복약알림 서비스 가이드 진행")
                    getGuideComment(GuideStatus.MEDI_GUIDE_SERVICE)
                } else {
                    DWLog.e("NO인 경우, 기존 대화 로직으로 진행")
                }
            }
        } else if (_currentGuideStatus.value == GuideStatus.MEDI_GUIDE_SERVICE) {
            uiScope.launch {
                if (LocalGuideFilterTask.checkPosWord(text)) {
                    DWLog.d("가이드 이해한 경우\n")
                    getGuideComment(GuideStatus.MEDI_GUIDE_FINISH)
                } else {
                    DWLog.e("가이드 이해하지 못한 경우\n")
                    getGuideComment(GuideStatus.MEDI_GUIDE_RETRY)
                }
            }
        }

        /** 커뮤니티 질의 가이드 **/
        if (_currentGuideStatus.value == GuideStatus.COMM_INIT) {
            uiScope.launch {
                if (!LocalGuideFilterTask.checkPosWord(text)) {
                    DWLog.d("커뮤니티 질의 서비스 가이드 진행")
                    getGuideComment(GuideStatus.COMM_GUIDE_SERVICE)
                } else {
                    DWLog.e("NO인 경우, 기존 대화 로직으로 진행")
                }
            }
        } else if (_currentGuideStatus.value == GuideStatus.COMM_GUIDE_SERVICE) {
            uiScope.launch {
                if (LocalGuideFilterTask.checkPosWord(text)) {
                    DWLog.d("가이드 이해한 경우\n")
                    getGuideComment(GuideStatus.COMM_GUIDE_FINISH)
                } else {
                    DWLog.e("가이드 이해하지 못한 경우\n  ")
                    getGuideComment(GuideStatus.COMM_GUIDE_FINISH)
                }
            }
        }
    }


    /**
     * 가이드 멘트
     **/
    fun getGuideComment(status: GuideStatus) {
        Log.e(App.TAG, "******** Task.getGuideComment [${status}] ********")
        uiScope.launch {
            try {
                val result: GuideTts = repository.getGuideComment(status).random()
                _currentGuideStatus.value = status

                synchronized(this) {
                    _guideText.value = result.text
                    GCTextToSpeech.getInstance()?.speech(result.text, result.actionName)
                }

                guideComment.postValue(Resource.success(result)) // Callback
            } catch (e: Exception) {
                guideComment.postValue(Resource.error(e.toString(), null))
            }
        }
    }

    // 어르신 정보 가져오기
//    private val _elderlyList = MutableLiveData<ElderlyList>()
//    fun elderlyList(): LiveData<ElderlyList> {
//        return _elderlyList
//    }
//
//    private fun getElderlyListResult() {
//        uiScope.launch {
//            val getElderlyListApi = repository.getElderlyInfo()
//            _elderlyList.postValue(getElderlyListApi)
//        }
//    }

    /***
     * GCTextToSpeech
     * */
    override fun onSpeechStart() {
        speechStarted()
    }

    override fun onSpeechFinish() {
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
        mGCSpeechToText.resume()
        _speechStatus.value = SpeechStatus.WAITING
    }

    /**
     * 현재 상태 확인
     */
    private fun checkCurrentStatus() {
        DWLog.d("checkCurrentStatus :: ${_currentGuideStatus.value}")
        when (_currentGuideStatus.value) {
            GuideStatus.WAKEUP_INIT, GuideStatus.VISION_INIT, GuideStatus.WAKEUP_GUIDE_SERVICE -> {
                DWLog.e("GuideStatus.INIT 상태로 20초 이상 유지 시, 앱 종료")
                delayDestroyApp()
            }
            GuideStatus.VISION_GUIDE_SERVICE -> {
                startFaceCheck()
            }
            GuideStatus.MEDI_GUIDE_RETRY -> {
                getGuideComment(GuideStatus.MEDI_INIT)
            }
            GuideStatus.COMM_GUIDE_RETRY -> {
                getGuideComment(GuideStatus.COMM_INIT)
            }
            GuideStatus.WAKEUP_GUIDE_FINISH, GuideStatus.VISION_GUIDE_SUCCESS, GuideStatus.MEDI_GUIDE_FINISH, GuideStatus.COMM_GUIDE_FINISH -> {
                App.instance.currentActivity?.let {
                    it.finish()
                }
                Process.killProcess(Process.myPid())
            }
        }
    }

    /**
     * 얼굴인식 시작
     * */
    private fun startFaceCheck() {
        DWLog.d("[FaceCheck] start")
        WFaceManager.instance.startTrackFace(context)
    }

    /**
     * 얼굴인식 중지
     * */
    private fun stopFaceCheck() {
        DWLog.d("[FaceCheck] stop")
        WFaceManager.instance.stopTrackFace()
    }

    override fun onFaceDetected() {
        DWLog.d("[FaceCheck] 얼굴감지")
        stopFaceCheck()
        getGuideComment(GuideStatus.VISION_GUIDE_SUCCESS)
    }

    private fun delayDestroyApp() {
        DWLog.e("delayDestroyApp :: ${_currentGuideStatus.value}")
        val event: RxEvent.Event = when (_currentGuideStatus.value) {
            GuideStatus.WAKEUP_INIT, GuideStatus.VISION_INIT -> {
                RxEvent.noResponseLongTime
            }
            else -> {
                RxEvent.destroyAppUpdate
            }
        }

        RxBus.publish(
            event
        )
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object {

    }
}