package com.onethefull.dasomtutorial.ui.guide

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.data.model.ElderlyList
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.repository.GuideRepository
import com.onethefull.dasomtutorial.utils.Resource
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.onethefull.dasomtutorial.utils.speech.GCSpeechToText
import com.onethefull.dasomtutorial.utils.speech.GCTextToSpeech
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2021/12/22
 */
class GuideViewModel(
    private val context: Activity,
    private val repository: GuideRepository,
) : BaseViewModel() {

    private val _question: MutableLiveData<String> = MutableLiveData<String>()
    val question: LiveData<String> = _question

    private val guideComment = MutableLiveData<Resource<GuideTts>>()
    fun guideComment() :LiveData<Resource<GuideTts>> {
        return guideComment
    }

    fun getGuideComment(type: String) {
        DWLog.e("type:: $type")
        var result =  repository.getGuideComment(type).random()

        _question.value = result.text
        guideComment.postValue(Resource.success(result))
    }

    // 어르신 정보 가져오기
    private val _elderlyList = MutableLiveData<ElderlyList>()
    fun elderlyList(): LiveData<ElderlyList> {
        return _elderlyList
    }

    private fun getElderlyListResult() {
        uiScope.launch {
            val getElderlyListApi = repository.getElderlyInfo()
            _elderlyList.postValue(getElderlyListApi)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun disconnect() {
//        mGCSpeechToText.release()
    }
}