package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.api.chatbot.ApiChatbotHelper
import com.onethefull.dasomtutorial.data.api.chatbot.ApiChatbotHelperImpl
import com.onethefull.dasomtutorial.data.api.chatbot.ChatbotApiService
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.data.model.Status
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialRequestData
import com.onethefull.dasomtutorial.data.model.chatbot.MealTutorialResponseData
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataRequest
import com.onethefull.dasomtutorial.data.model.check.CheckChatBotDataResponse
import com.onethefull.dasomtutorial.data.model.check.GetMessageListResponse
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReq
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQAReqDetail
import com.onethefull.dasomtutorial.data.model.quiz.DementiaQuizListResponse
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.learn.LearnStatus
import com.onethefull.dasomtutorial.utils.ani.KebbiAnimData
import com.onethefull.wonderfulrobotmodule.ext.dasomLanguageCodeValue
import com.onethefull.wonderfulrobotmodule.robot.KebbiMotion
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
                list.add(
                    InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "Dasom,Avadin",
                        arrayListOf(
                            "어르신, 위급할 때 구조 요청을 빠르게 하는 것은 정말 중요해요! " +
                                    "다솜이가 멀리 있을 때 또는 전화기가 손이 닿지 않는 곳에 있을 때, 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요. " +
                                    "지금 제가 하는 말을 따라하면서 연습해볼게요!"
                        ),
                        "practice_emergency",
                        1
                    )
                )
                list.add(
                    InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "Dasom,Avadin",
                        arrayListOf(
                            "어르신, 위급 상황이 발생했을 때 신속한 조치가 가장 중요해요!" +
                                    "갑자기 어지럽거나, 심한 두통에 시달리는 위급 상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                                    "지금 제가 하는 말을 따라하면서 연습해볼게요!"
                        ),
                        "practice_emergency",
                        1
                    )
                )
                list.add(
                    InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "Dasom,Avadin",
                        arrayListOf(
                            "어르신, 갑작스런 응급상황이 발생했을 때 효과적으로 대응할 수 있는 방법이 있어요!\n" +
                                    "갑자기 몸을 움직일 수 없고, 눈을 뜰 수가 없는 위급상황이 발생한다면 \"다솜아, 도와줘\"라고 말해보세요." +
                                    "지금 제가 하는 말을 따라하면서 연습해볼게요!"
                        ),
                        "practice_emergency",
                        1
                    )
                )
                list.add(
                    InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "Dasom,Avadin",
                        arrayListOf(
                            "어르신, 어느날 갑자기 몸을 가누기 힘들 때 제일 먼저 다솜이를 찾아주세요!\n" +
                                    "갑작스러운 고통에 정신을 잃을지 모른다는 판단이 든다면 \"다솜아, 도와줘\"라고 말해보세요." +
                                    "지금 제가 하는 말을 따라하면서 연습해볼게요!"
                        ),
                        "practice_emergency",
                        1
                    )
                )
                list.add(
                    InnerTtsV2(
                        arrayListOf(),
                        arrayListOf(),
                        arrayListOf(),
                        "",
                        "Dasom,Avadin",
                        arrayListOf(
                            "어르신, 갑작스런 응급한 상황에 대비해 대처법을 미리 알아둔다면 위험한 상황을 막을 수 있어요!\n" +
                                    "갑자기 다치거나 지병으로 몸을 움직일 수 없는 위급한 상황이 발생한다면, \"다솜아, 도와줘\"라고 말해보세요.\n" +
                                    "지금 제가 하는 말을 따라하면서 연습해볼게요!"
                        ),
                        "practice_emergency",
                        1
                    )
                )
            }
        }

        return list
    }


    fun getIntroduceList(status: LearnStatus?): String {
        when (status) {
            LearnStatus.START_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_tutorial_1)
            }

            LearnStatus.START_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_start_tutorial_1_1)
            }

            LearnStatus.START_TUTORIAL_1_2 -> {
                return context.getString(R.string.text_start_tutorial_1_2)
            }

            LearnStatus.START_TUTORIAL_1_3 -> {
                return context.getString(R.string.text_start_tutorial_1_3)
            }

            LearnStatus.START_TUTORIAL_2 -> {
                return context.getString(R.string.text_start_tutorial_2)
            }

            LearnStatus.START_TUTORIAL_3 -> {
                return context.getString(R.string.text_start_tutorial_3)
            }

            LearnStatus.START_TUTORIAL_3_1 -> {
                return context.getString(R.string.text_start_tutorial_3_1)
            }

            LearnStatus.START_TUTORIAL_3_2 -> {
                return context.getString(R.string.text_start_tutorial_3_2)
            }

            LearnStatus.START_TUTORIAL_3_3 -> {
                return context.getString(R.string.text_start_tutorial_3_3)
            }

            LearnStatus.START_TUTORIAL_3_4 -> {
                return context.getString(R.string.text_start_tutorial_3_4)
            }

            LearnStatus.START_TUTORIAL_3_5 -> {
                return context.getString(R.string.text_start_tutorial_3_5)
            }

            LearnStatus.START_TUTORIAL_4 -> {
                return context.getString(R.string.text_start_tutorial_4)
            }

            LearnStatus.START_TUTORIAL_4_1 -> {
                return context.getString(R.string.text_start_tutorial_4_1)
            }

            LearnStatus.START_TUTORIAL_4_2 -> {
                return context.getString(R.string.text_start_tutorial_4_2)
            }
            /*커뮤니티(다솜톡)*/
            LearnStatus.START_DASOMTALK_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_dasomtalk_tutorial_1)
            }

            LearnStatus.START_DASOMTALK_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_start_dasomtalk_tutorial_1_1)
            }

            LearnStatus.START_DASOMTALK_TUTORIAL_1_2 -> {
                return context.getString(R.string.text_start_dasomtalk_tutorial_1_2)
            }

            LearnStatus.START_DASOMTALK_VIDEO -> {
                return "https://youtu.be/sjT7LBxSTMI"
            }

            LearnStatus.START_DASOMTALK_TUTORIAL_2 -> {
//                return context.getString(R.string.text_start_dasomtalk_tutorial_2)
                return context.getString(R.string.text_start_dasomtalk_tutorial_2_1)
            }

            LearnStatus.START_DASOMTALK_TUTORIAL_2_1 -> {
                return context.getString(R.string.text_start_dasomtalk_tutorial_2_1)
            }

            LearnStatus.START_DASOMTALK_TUTORIAL_2_2 -> {
                return context.getString(R.string.text_start_dasomtalk_tutorial_2_2)
            }
            /*영상통화, 메신저*/
            LearnStatus.START_VIDEOCALL_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_videocall_tutorial_1)
            }

            LearnStatus.START_VIDEOCALL_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_start_videocall_tutorial_1_1)
            }

            LearnStatus.START_VIDEOCALL_TUTORIAL_1_2 -> {
                return context.getString(R.string.text_start_videocall_tutorial_1_2)
            }

            LearnStatus.START_VIDEOCALL_VIDEO -> {
                return "https://youtu.be/UvA9b2QaX3c"
            }

            LearnStatus.START_VIDEOCALL_TUTORIAL_2 -> {
                return context.getString(R.string.text_start_videocall_tutorial_2)
            }
            /*긴급콜*/
            LearnStatus.START_SOS_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_sos_tutorial_1)
            }

            LearnStatus.START_SOS_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_start_sos_tutorial_1_1)
            }

            LearnStatus.START_SOS_TUTORIAL_1_2 -> {
                return context.getString(R.string.text_start_sos_tutorial_1_2)
            }

            LearnStatus.START_SOS_VIDEO -> {
                return "https://youtu.be/4RURj7ScA9I"
            }

            LearnStatus.START_SOS_TUTORIAL_2 -> {
                return context.getString(R.string.text_start_sos_tutorial_2)
            }
            /*복약*/
            LearnStatus.START_MEDICATION_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_medication_tutorial_1)
            }

            LearnStatus.START_MEDICATION_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_start_medication_tutorial_1_1)
            }

            LearnStatus.START_MEDICATION_TUTORIAL_1_2 -> {
                return context.getString(R.string.text_start_medication_tutorial_1_2)
            }

            LearnStatus.START_MEDICATION_VIDEO -> {
                return "https://youtu.be/P-Q-4XxvARA"
            }

            LearnStatus.START_MEDICATION_TUTORIAL_2 -> {
                return context.getString(R.string.text_start_medication_tutorial_2)
            }
            /*동영상재생(유투브), 라디오*/
            LearnStatus.START_RADIO_TUTORIAL_1 -> {
                return context.getString(R.string.text_start_radio_tutorial_1)
            }

            LearnStatus.START_RADIO_VIDEO -> {
                return "https://youtu.be/TMjWrnisoFQ"
            }

            LearnStatus.START_RADIO_TUTORIAL_2 -> {
                return context.getString(R.string.text_start_radio_tutorial_2)
            }
            /*소개종료*/
            LearnStatus.END_TUTORIAL -> {
                return context.getString(R.string.text_end_tutorial)
            }

            LearnStatus.END_TUTORIAL_1_1 -> {
                return context.getString(R.string.text_end_tutorial_1_1)
            }

            LearnStatus.END_TUTORIAL_1_2_1 -> {
                return context.getString(R.string.text_end_tutorial_1_2_1)
            }

            LearnStatus.END_TUTORIAL_1_2_2 -> {
                return context.getString(R.string.text_end_tutorial_1_2_2)
            }

            LearnStatus.END_TUTORIAL_1_3 -> {
                return context.getString(R.string.text_end_tutorial_1_3)
            }

            LearnStatus.END_TUTORIAL_1_4 -> {
                return context.getString(R.string.text_end_tutorial_1_4)
            }

            else -> {
                return "Error"
            }
        }
    }

    fun getSleepWakeCheckMessages(category: String): KebbiAnimData {
        return when (category) {
            OnethefullBase.SLEEP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_music", context), context.getString(R.string.sleep_time_check1), KebbiMotion.RANDOMCHAT_WAIT),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.sleep_time_check2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.sleep_time_check3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }

            OnethefullBase.SLEEP_TIME_QUESTION_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_music", context), context.getString(R.string.sleep_time_question1), KebbiMotion.RANDOMCHAT_WAIT),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.sleep_time_question2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.sleep_time_question3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }

            OnethefullBase.WAKEUP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_chorong", context), context.getString(R.string.wake_time_check1), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.wake_time_check2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_listening", context), context.getString(R.string.wake_time_check3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }

            OnethefullBase.WAKEUP_TIME_QUESTION_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_chorong", context), context.getString(R.string.wake_time_question1), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.wake_time_question2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_listening", context), context.getString(R.string.wake_time_question3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }
            OnethefullBase.SLEEP_TIME_NO_RESPONSE-> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_chorong", context), "취침 시간은 다음에 여쭤볼게요. 오늘도 편안한 밤 되셨길 바라요.", KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), "취침 시간은 몰라도 다솜이는 항상 곁에 있어요. 편안하게 쉬세요.", KebbiMotion.RANDOMCHAT_START)
                ).random()
            }
            OnethefullBase.WAKEUP_TIME_NO_RESPONSE-> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_chorong", context), "오늘은 기상 시간은 알 수 없었지만, 괜찮아요! 다음에 또 물어볼게요.", KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), "일어나신 시간이 기억나지 않으셔도 괜찮아요. 다솜이는 늘 곁에 있을게요.", KebbiMotion.RANDOMCHAT_START)
                ).random()
            }
            else -> KebbiAnimData(-1, "", "")
        }
    }


    fun getMealFinishKebbiUiAction(category: String): KebbiAnimData {
        return when (category) {
            // 취침문답 - 아침맞이 브리핑 이후
            OnethefullBase.SLEEP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_music", context), context.getString(R.string.text_sleep_time_1), KebbiMotion.RANDOMCHAT_WAIT),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.text_sleep_time_2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.text_sleep_time_3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }
            // 기상문답 - 아침식사 알람 이후
            OnethefullBase.WAKEUP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_chorong", context), context.getString(R.string.text_wakeup_time_1), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.text_wakeup_time_2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_listening", context), context.getString(R.string.text_wakeup_time_3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }

            // 식사문답 - 점심식사 알람 이후
            OnethefullBase.BREAKFAST_NAME, OnethefullBase.BREAKFAST_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_listening", context), context.getString(R.string.text_breakfast_time_1), KebbiMotion.RANDOMCHAT_FINISH),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.text_breakfast_time_2), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_music", context), context.getString(R.string.text_breakfast_time_3), KebbiMotion.RANDOMCHAT_WAIT),
                ).random()
            }

            // 식사 문답 - 저녁 식사 알람 이후
            OnethefullBase.LUNCH_NAME, OnethefullBase.LUNCH_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_shy", context), context.getString(R.string.text_lunch_time_1), KebbiMotion.RANDOMCHAT_FINISH),
                    KebbiAnimData(getRawResIdByName("kid_love", context), context.getString(R.string.text_lunch_time_2), KebbiMotion.SHY),
                    KebbiAnimData(getRawResIdByName("kid_talking", context), context.getString(R.string.text_lunch_time_3), KebbiMotion.RANDOMCHAT_WAIT),
                ).random()
            }
            // 식사 문답 - 취침 알람 이후
            OnethefullBase.DINNER_NAME, OnethefullBase.DINNER_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(getRawResIdByName("kid_love", context), context.getString(R.string.text_dinner_time_1), KebbiMotion.RANDOMCHAT_START),
                    KebbiAnimData(getRawResIdByName("kid_shy", context), context.getString(R.string.text_dinner_time_2), KebbiMotion.CALL_SEND),
                    KebbiAnimData(getRawResIdByName("kid_shy", context), context.getString(R.string.text_dinner_time_3), KebbiMotion.CALL_ACCEPT),
                ).random()
            }

            else -> KebbiAnimData(-1, "", "")
        }
    }

    fun getMealFinishBeanQUiAction(category: String): KebbiAnimData {
        return when (category) {
            // 취침문답 - 아침맞이 브리핑 이후
            OnethefullBase.SLEEP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(R.raw.old_kid_music, "오늘 하루 즐거운 일이 많으셨으면 좋겠어요.", ""),
                    KebbiAnimData(R.raw.old_kid_smile, "행복한 하루 보내시길 바라요.", ""),
                    KebbiAnimData(R.raw.old_kid_chorong, "기분 좋은 하루 보내세요.", ""),
                ).random()
            }
            // 기상문답 - 아침식사 알람 이후
            OnethefullBase.WAKEUP_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(R.raw.old_kid_chorong, "아침 맛있게 드세요. 속에 부담가지 않도록 천천히 드셔야 해요.", ""),
                    KebbiAnimData(R.raw.old_kid_smile, "아침 식사 챙겨드세요. 언제나 건강이 우선이에요.", ""),
                    KebbiAnimData(R.raw.old_kid_listening, "아침 맛있게 드시고 물도 충분히 마셔주세요. 미지근한 물이 좋아요.", ""),
                ).random()
            }

            // 식사문답 - 점심식사 알람 이후
            OnethefullBase.BREAKFAST_NAME, OnethefullBase.BREAKFAST_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(R.raw.old_kid_listening, "점심 맛있게 드시고 즐거운 오후 보내세요.", ""),
                    KebbiAnimData(R.raw.old_kid_smile, "점심 맛있게 드세요. 오늘 오후도 다솜이가 응원할게요.", ""),
                    KebbiAnimData(R.raw.old_kid_music, "식사 챙겨 드시고 활기찬 오후 보내시길 바라요.", ""),
                ).random()
            }

            // 식사 문답 - 저녁 식사 알람 이후
            OnethefullBase.LUNCH_NAME, OnethefullBase.LUNCH_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(R.raw.old_kid_shy, "식사 맛있게 하시고 행복한 저녁 보내시길 바라요.", ""),
                    KebbiAnimData(R.raw.old_kid_love, "저녁 맛있게 드세요. 오늘 하루도 수고 많으셨어요.", ""),
                    KebbiAnimData(R.raw.old_kid_smile, "식사 드시고 휴식도 충분히 취해주세요. 언제나 건강하시길 바라요.", ""),
                ).random()
            }

            // 식사 문답 - 취침 알람 이후
            OnethefullBase.DINNER_NAME, OnethefullBase.DINNER_TIME_NAME -> {
                arrayListOf(
                    KebbiAnimData(R.raw.old_kid_love, "오늘도 다솜이와 대화 나눠주셔서 감사해요.", ""),
                    KebbiAnimData(R.raw.old_kid_shy, "오늘도 저와 함께해주셔서 감사해요, 내일은 더 행복한 일이 많으면 좋겠어요", ""),
                    KebbiAnimData(R.raw.kid_sleepy, "오늘 하루 대화를 나눠주셔서 감사해요. 푹 주무시고 내일 봬요.", ""),
                ).random()
            }

            else -> KebbiAnimData(-1, "", "")
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
            DasomProviderHelper.getDeviceCode(context),
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

    suspend fun getMealTutorial(requestData: MealTutorialRequestData) : MealTutorialResponseData {
        return chatApiHelper.getMealTutorial(
            requestData
        )
    }

    suspend fun check204(): Boolean {
        return apiHelper.check204()
    }

    fun getRawResIdByName(baseName: String, context: Context): Int {
        val localeSuffix = when (App.instance.getLocale()?.dasomLanguageCodeValue()) {
            "ko-KR" -> ""
            else -> "_en"
        }
        val resName = baseName + localeSuffix
        return context.resources.getIdentifier(resName, "raw", context.packageName)
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

        private val chatApiHelper : ApiChatbotHelper = ApiChatbotHelperImpl(RetrofitBuilder.chatbotApiService)
    }
}