package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.learn.LearnStatus
import java.lang.reflect.Type

/**
 * Created by sjw on 2021/11/10
 */
class LearnRepository private constructor(
    private val context: Context,
) {
    fun getPracticeEmergencyList(key: String): List<InnerTtsV2> {
        return convertJson(DasomProviderHelper.getPracticeEmergencyValue(context, key))
    }

    fun getGeniePracticeEmergencyList(key: String): List<InnerTtsV2> {
        var list = ArrayList<InnerTtsV2>()
        when (key) {
            DasomProviderHelper.KEY_PRACTICE_EMERGENCY_VALUE -> {
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 위급할 때 구조 요청을 빠르게 하는 것은 정말 중요해요! " +
                            "댜솜이가 멀리 있을 때 또는 전화기가 손이 닿지 않는 곳에 있을 때, 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요. " +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 위급 상황이 발생했을 때 신속한 조치가 가장 중요해요!" +
                            "갑자기 어지럽거나, 심한 두통에 시달리는 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 갑작스런 응급상황이 발생했을 때 효과적으로 대응할 수 있는 방법이 있어요!\n" +
                            "갑자기 몸을 움직일 수 없고, 눈을 뜰 수가 없는 위급상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 어느날 갑자기 몸을 가누기 힘들 때 제일 먼저 다솜이를 찾아주세요!\n" +
                            "갑작스러운 고통에 정신을 잃을지 모른다는 판단이 든다면 \"다솜아, 도와줘\"라고 말해보세요." +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
                list.add(InnerTtsV2(arrayListOf(),
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "Dasom,Avadin",
                    arrayListOf("어르신, 갑작스런 응급한 상황에 대비해 대처법을 미리 알아둔다면 위험한 상황을 막을 수 있어요!\n" +
                            "갑자기 다치거나 지병으로 몸을 움직일 수 없는 위급한 상황이 발생한다면, \"다솜아, 도와줘\"라고 말해보세요.\n" +
                            "지금 제가 하는 말을 따라하면서 연습해볼게요!"),
                    "practice_emergency",
                    1))
            }
        }

        return list
    }


    fun getIntroduceList(status: LearnStatus?): String {
        when (status) {
            LearnStatus.START_TUTORIAL_1 -> {
                return "안녕하세요, 저는 어르신을 위한 다솜 로봇이에요.\n" +
                        "다솜이는 인공지능을 바탕으로 여러가지 일을 해드릴 수 있어요." +
                        "그냥 일반로봇이나 ai 스피커가 아니고 \n" + "혼자 계신 어르신의 비서가 될 수 있구요,\n" +
                        "어르신의 자녀인 보호자 께서 신경써야할 부분을 다솜로봇이 다 알아서 대신 해드리고 정리해서 보호자께 알려드려요."
            }
            LearnStatus.START_TUTORIAL_2 -> {
                return "다솜로봇에서 할 수 있는 것들 다섯가지를 소개해 드릴게요."
            }

            LearnStatus.START_TUTORIAL_3 -> {
                return "첫째, 다솜톡. 친구를 찾고 대화할 수 있는 커뮤니티예요.\n" +
                        "둘째, 영상통화와 메신저. 가족들과 이야기 나눌 수 있는 기능이에요.\n" +
                        "셋째, 긴급상황 알림. 어르신이 아프실 때 가족분께 알려드려요.\n" +
                        "넷째, 복약 알람. 약 드시는 시간을 다솜이가 챙겨드려요.\n" +
                        "다섯째, 노래 재생, 라디오 재생. 제가 TV도 되어드리고, 라디오도 되어드릴게요."
            }
            LearnStatus.START_TUTORIAL_4 -> {
                return "자, 지금부터 제가 하나씩 자세히 보여드릴 거예요.\n" +
                        "제 화면을 잘 보시고, 혹시 소리가 너무 작으면 오른쪽 볼을 톡톡 두드려주세요."
            }
            /*커뮤니티(다솜톡)*/
            LearnStatus.START_DASOMTALK_TUTORIAL_1 -> {
                return "첫째, 다솜톡부터 보여드릴게요.\n" +
                        "심심하거나 적적하실 때, 얘기 나눌 친구가 있다면 얼마나 좋을까요?\n" +
                        "저를 통해서 새로운 친구 분들을 만나보세요."
            }
            LearnStatus.START_DASOMTALK_VIDEO -> {
                return "https://youtu.be/sjT7LBxSTMI"
            }
            LearnStatus.START_DASOMTALK_TUTORIAL_2 -> {
                return "다솜이를 사용하시는 분이라면 전국 어디에서나 연결될 수 있어요.\n" +
                        "요즘 날씨는 어떤지, 좋아하는 가수는 누구인지 물어보면 어떨까요?"
            }
            /*영상통화, 메신저*/
            LearnStatus.START_VIDEOCALL_TUTORIAL_1 -> {
                return "둘째, 영상통화와 메신저에 대해 알려드릴게요.\n" +
                        "스마트폰이 없어도 저를 통해서 가족들과 얘기 나누실 수 있답니다."
            }
            LearnStatus.START_VIDEOCALL_VIDEO -> {
                return "https://youtu.be/UvA9b2QaX3c"
            }
            LearnStatus.START_VIDEOCALL_TUTORIAL_2 -> {
                return "저를 통해서 가족 분들과 자주 이야기를 나눠보세요. \n" +
                        "보호자님께서 보낸 메시지도 다솜이가 알려드린답니다."
            }
            /*긴급콜*/
            LearnStatus.START_SOS_TUTORIAL_1 -> {
                return "다음으로 셋째, 긴급상황 알림을 보여드릴게요.\n" +
                        "갑자기 아프시거나, 집에 큰일이 발생했을 때는 긴급상황 알림을 이용할 수 있어요."
            }
            LearnStatus.START_SOS_VIDEO -> {
                return "https://youtu.be/4RURj7ScA9I"

            }
            LearnStatus.START_SOS_TUTORIAL_2 -> {
                return "긴급한 상황이 일어나면 잊지 말고 \"다솜아\" 하고 불러주세요."
            }
            /*복약*/
            LearnStatus.START_MEDICATION_TUTORIAL_1 -> {
                return "넷째, 복약 알람을 안내해 드릴게요.\n" +
                        "복약 알람은 보호자님께서 설정하시거나, 어르신께서 직접 추가할 수 있어요."
            }
            LearnStatus.START_MEDICATION_VIDEO -> {
                return "https://youtu.be/P-Q-4XxvARA"
            }

            LearnStatus.START_MEDICATION_TUTORIAL_2 -> {
                return "약을 제 시간에 꼬박꼬박 드셔야 건강에 좋다고 해요. 제가 알려드릴테니, 잊지 마시고 약 챙겨드세요."
            }
            /*동영상재생(유투브), 라디오*/
            LearnStatus.START_RADIO_TUTORIAL_1 -> {
                return "마지막으로 노래 재생과 라디오 재생에 대해 안내드릴게요."
            }
            LearnStatus.START_RADIO_VIDEO -> {
                return "https://youtu.be/TMjWrnisoFQ"
            }
            LearnStatus.START_RADIO_TUTORIAL_2 -> {
                return "다솜이와 함께라면 심심하지 않으실 거예요."
            }
            /*소개종료*/
            LearnStatus.END_TUTORIAL -> {
                return "이렇게 다양한 기능이 있는 다솜 로봇, 어떠신가요?\n" +
                        "제가 처음에는 어르신의 말씀을 잘 알아듣지 못하거나, 엉뚱한 대답을 할 수도 있지만,\n" +
                        "꾸준히 말을 걸어주시면 다솜이가 점점 더 똑똑해져서 알맞은 답변을 해드릴 수 있어요.\n" +
                        "\n" +
                        "앞으로도 이용하시다가 잘 모르는 부분이 생기면 \"너를 소개해줘\"하고 말씀해주세요. 감사합니다."
            }
            else -> {
                return "Error"
            }
        }
    }

    private fun convertJson(jsonString: String): List<InnerTtsV2> {
        var list = ArrayList<InnerTtsV2>()
        if (jsonString == "") {
            list.add(InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0))
            return list as ArrayList<InnerTtsV2>
        }
        val type: Type = object : TypeToken<List<InnerTtsV2?>?>() {}.type
        return Gson().fromJson(jsonString, type) as ArrayList<InnerTtsV2>
    }

    suspend fun logPracticeSos(): Status {
        return apiHelper.practiceSos(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context)
        )
    }

    suspend fun getDementiaQuizList(limit: String): DementiaQuizListResponse {
        return apiHelper.getDementiaQuizList(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            limit
        )
    }

    suspend fun insertDementiaQuizLog(solvedQuizList: ArrayList<DementiaQAReqDetail>): Status {
        return apiHelper.insertDementiaQuizLog(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            DementiaQAReq(solvedQuizList)
        )
    }

    suspend fun logCheckChatBotData(checkChatBotDataRequest: CheckChatBotDataRequest): CheckChatBotDataResponse? {
        return apiHelper.logCheckChatBotData(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            checkChatBotDataRequest
        )
    }

    suspend fun logGetMessageList(mealCategory: String): GetMessageListResponse {
        return apiHelper.logGetMessageList(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context),
            mealCategory
        )
    }

    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: LearnRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: LearnRepository(context).also { instance = it }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}