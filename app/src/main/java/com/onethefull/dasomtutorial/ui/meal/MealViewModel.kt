package com.onethefull.dasomtutorial.ui.meal

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.repository.MealRepository
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.record.WavFileUitls
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToTextImpl
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import com.onethefull.dasomtutorial.utils.speech.SpeechStatus
import com.onethefull.dasomtutorial.utils.task.EmergencyFlowTask
import com.onethefull.dasomtutorial.utils.task.noResponseFlowTask
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by jeaseok on 2022/02/22
 */
class MealViewModel(
    private val context: Activity,
    private val repository: MealRepository
) : BaseViewModel(), GCSpeechToText.SpeechToTextCallback, GCTextToSpeech.Callback {
    private var mGCSpeechToText: GCSpeechToText = GCSpeechToTextImpl(context as MainActivity)

    private var wavUtils = WavFileUitls()

    /** 음성출력 상태*/
    private val _speechStatus: MutableLiveData<SpeechStatus> = MutableLiveData<SpeechStatus>()
    val speechStatus: LiveData<SpeechStatus> = _speechStatus

    /** 식사관련 상태*/
    private val _mealStatus: MutableLiveData<MealStatus> = MutableLiveData<MealStatus>()
    val mealStatus: LiveData<MealStatus> = _mealStatus

    /** 텍스트 관련 */
    private val mealComment = MutableLiveData<Resource<MealTts>>()
    fun mealComment(): LiveData<Resource<MealTts>> {
        return mealComment
    }

    /** 현재 시간 */
    val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    init {
        connect()
        mGCSpeechToText.setCallback(this)
        mGCSpeechToText.setWavUtils(wavUtils)
        EmergencyFlowTask.insert(context, 0)
        noResponseFlowTask.insert(context, 0)
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
        result?.let {
            handleRecognition(result)
        }
    }

    /** 들어온 음성 응답에 대한 처리 */
    private fun handleRecognition(text: String) {
        DWLog.d(TAG, "handleRecognition input $text")
        if (text == GCSpeechToTextImpl.ERROR_OUT_OF_RANGE) {
            return
        }

        if (_mealStatus.value == MealStatus.MEAL_INIT) {
            uiScope.launch {
                if (true) {
                    // Input Yes <- 애초에 여기서 선택지니까 다른 쪽으로 접근해야함
                    DWLog.d(TAG, "식사 확인 가이드 진행")
                    getComment(MealStatus.MEAL_GUIDE_TIME)
                } else {
                    // Input No 혹은 30초가 지난 경우 -> else 가 아닐수도 있음
                    DWLog.e(TAG, "NO인 경우, 기존 대화 로직으로 진행")
                }
            }
        } else if (_mealStatus.value == MealStatus.MEAL_GUIDE_TIME) {
            uiScope.launch {
                if (true) {
                    // Input 음성 입력
                    DWLog.d(TAG, "식사시간 음성입력 들어옴")
                    getComment(MealStatus.MEAL_GUIDE_FOOD)
                } else {
                    DWLog.e(TAG, "가이드 이해하지 못한 경우")
                    getComment(MealStatus.MEAL_GUIDE_RETRY)
                }
            }
        } else if (_mealStatus.value == MealStatus.MEAL_GUIDE_FOOD) {
            uiScope.launch {
                if (true) {
                    // Input 음성 입력
                    DWLog.d(TAG, "식사음식 음성입력 들어옴\n")
                    getComment(MealStatus.MEAL_GUIDE_FINISH)
                } else {
                    DWLog.e(TAG, "가이드 이해하지 못한 경우\n  ")
                    getComment(MealStatus.MEAL_GUIDE_RETRY)
                }
            }
        }
    }

    /** 발화 및 Ui 출력 관여 */
    fun getComment(status: MealStatus) {
        uiScope.launch {
            val result = repository.getMealComment(status, nowHour).getOrNull(0)
            result?.let {
                _mealStatus.value = status

                synchronized(this) {
                    // 발화 및 TODO: 텍스트 출력
                    GCTextToSpeech.getInstance()?.speech(result.text)
                }

                mealComment.postValue(Resource.success(result)) // Callback
            }
        }
    }

    /***
     * GCTextToSpeech
     * */
    // TTS 출력 시작
    override fun onSpeechStart() {
        DWLog.d(TAG, "onSpeechStart")
        speechStarted()
    }

    // TTS 출력 종료
    override fun onSpeechFinish() {
        DWLog.d(TAG, "onSpeechFinish")
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
        DWLog.d(TAG, "checkCurrentStatus ${mealStatus.value}")
        when(mealStatus.value) {
            MealStatus.MEAL_INIT -> {

            }
        }
    }

    fun btnClickYes() {
        getComment(MealStatus.MEAL_GUIDE_TIME)
    }

    companion object {
        const val TAG = "MealFragment"
    }
}