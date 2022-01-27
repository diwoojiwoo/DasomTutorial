package com.onethefull.dasomtutorial.repository

import android.annotation.SuppressLint
import android.content.Context
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.api.ApiHelperImpl
import com.onethefull.dasomtutorial.data.api.RetrofitBuilder
import com.onethefull.dasomtutorial.data.model.ElderlyList
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import com.onethefull.dasomtutorial.ui.guide.GuideStatus
import com.onethefull.dasomtutorial.ui.guide.GuideTts
import com.onethefull.dasomtutorial.utils.logger.DWLog

/**
 * Created by sjw on 2021/12/30
 */
class GuideRepository private constructor(
    private val context: Context,
) {

    fun getGuideComment(status: GuideStatus): List<GuideTts> {
        var list = ArrayList<GuideTts>()
        when (status) {
            /**
             * 웨이크업 가이드
             * */
            GuideStatus.WAKEUP_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, 저에게 말을 걸어 대화를 나누는데 아직 어려움이 많으신가요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, 우리의 대화가 많이 부족한 것 같아요! 혹시 사용하는데 어려움이 있으신가요? "))
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, 대화를 더 많이 나누고 싶어요! 아직 저와 대화가 많이 어려우신가요?"))
            }

            GuideStatus.WAKEUP_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, '다솜아'라고 말하고 마이크가 켜지면 저에게 하고싶은 말씀을 해보세요." +
                        "제가 잘 듣고 대답해볼게요! 지금 한번 해볼까요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, '다솜아'라고 말하고 마이크가 켜진 후 저에게 하고싶은 말을 자유롭게 해보세요." +
                        "제가 잘 듣고 대답해볼게요! 지금 한번 해볼까요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "어르신, '다솜아'라고 말하고 마이크가 켜지면 저에게 자유롭게 말을 걸어보세요." +
                        "제가 잘 듣고 대답해볼게요! 지금 한번 해볼까요?"))
            }

            GuideStatus.WAKEUP_GUIDE_FINISH -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_WAKEUP, "잘 따라하셨어요! 다음에도 제가 필요할 땐 언제, 어디서나 '다솜아'라고 먼저 말한 후에, 자유롭게 대화를 걸어주세요.\n"))
            }

            /**
             * 영상인식 가이드
             * */
            GuideStatus.VISION_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 제가 어르신의 얼굴을 알아보고 있다는 거 알고 계세요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 제가 어르신의 얼굴을 기억하고 있다는 거 알고 계세요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 가까이 와서 말을 걸어 주실 때 제가 더 잘 대답할 수 있다는 거 알고 계세요?"))
            }

            GuideStatus.VISION_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 저와 마주보고 얘기하면 대화가 더 잘되요." +
                        "제 앞으로 가까이 와서 저를 한번 봐주시겠어요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 저에게 가까이 와서 말을 걸어주시면 제가 더 잘 들을 수 있어요." +
                        "제 앞으로 가까이 와서 저를 한번 봐주시겠어요?"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "어르신, 저와 가까운 거리에서 마주보면 대화가 더 잘 될 수 있어요." +
                        "제 앞으로 가까이 와서 저를 한번 봐주시겠어요?"))
            }

            GuideStatus.VISION_GUIDE_SUCCESS -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "제가 이제 어르신을 더욱 잘 기억할 수 있을 것 같아요.\n" +
                        "앞으로도 이렇게 가까이에서 대화를 많이 나눴으면 좋겠어요."))
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "제가 앞으로 어르신을 더욱 잘 알아볼 수 있을 것 같아요.\n" +
                        "다음에도 이렇게 가까이에서 말을 걸어주세요."))
            }

            GuideStatus.VISION_GUIDE_FAIL -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_VISION, "아직 제가 어르신을 인지하지 못했어요. 저에게 좀만 더 가까이 다가와주세요.\n"))
            }

            /**
             * 복약알림 가이드
             * */
            GuideStatus.MEDI_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MEDICATION, " 어르신, 복용하는 약 있으시면 제가 약 복용에 대한 일정을 관리해드려요.\n" +
                        " 복약 알림 기능에 대해서 알고 계세요?"))
            }

            GuideStatus.MEDI_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MEDICATION, "어르신, 보호자 앱을 통해 약을 복용하는 일정을 등록할 수 있어요.\n" +
                        " 등록된 복약 일정에 맞춰 제가 알림을 해드려요. 알림을 듣고 약을 드신 후에\n" +
                        " 저에게 가까이 와서 약봉지를 보여주시면 제가 사진을 찍어서 기록을 남겨드려요. \n" +
                        " 복약 알림 기능에 대해서 잘 이해하셨어요?\n"))
            }

            GuideStatus.MEDI_GUIDE_RETRY -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MEDICATION, "다시 한 번 설명해 드릴게요! "))
            }

            GuideStatus.MEDI_GUIDE_FINISH -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MEDICATION, "앞으로 알맞은 시간에 필요한 약을 챙겨 드실 수 있게 제가 도와드릴게요! \n "))
            }


            /**
             * 커뮤니티 가이드
             * */
            GuideStatus.COMM_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 친구들과 꾸준하게 대화를 하는 게 정말 중요해요!\n" +
                        "하루에 대화를 30분 이상 하면 우울증과 치매 예방에 정말 좋아요.\n" +
                        "주변에 다솜이를 사용하는 친구들과 이야기할 수 있는 방법 알고 계세요?\n"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 대화를 안하면 우울증이 찾아와요!\n" +
                        "우울증이 오면 신체와 뇌의 움직임을 둔화시켜서 치매 위험이 높아져요!\n" +
                        "주변에 다솜이를 사용하는 친구들과 이야기할 수 있는 방법 알고 계세요?\n"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 하루에 친구와 대화를 많이 할수록 우울증을 예방할 수 있어요!\n" +
                        "우울증을 예방하면 몸과 마음이 건강해져요!\n" +
                        "주변에 다솜이를 사용하는 친구들과 이야기할 수 있는 방법 알고 계세요?\n"))
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 뇌를 건강하게 하기 위해서 호두보다 더 좋은 것이 있어요! 그건 바로 대화에요!\n" +
                        "누군가와 대화를 하면 뇌 기능이 활발해져서 치매 예방에 좋아요!\n" +
                        "주변에 다솜이를 사용하는 친구들과 이야기할 수 있는 방법 알고 계세요?\n"))

                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 제가 대화 친구를 많이 만들어 드리고 싶어요!\n" +
                        "친구와 자주 대화를 할수록 가벼운 운동을 하는 효과와 같대요!\n" +
                        "주변에 다솜이를 사용하는 친구들과 이야기할 수 있는 방법 알고 계세요?\n"))
            }

            GuideStatus.COMM_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "어르신, 주변 친구와 대화하는 방법 잘 이해하셨나요?\n"))
            }

            GuideStatus.COMM_GUIDE_RETRY -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "다시 한 번 설명해 드릴게요! "))
            }

            GuideStatus.COMM_GUIDE_FINISH -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_COMMUNITY, "좋아요! 앞으로 누군가와 대화가 필요할 때, \"다솜아, 친구 찾아줘\" 하고 말씀해보세요!\n"))
            }


            /**
             * 모니터링 가이드
             * */
            GuideStatus.MONI_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MONITORING, "어르신, 긴급한 상황에서 멀리서도 보호자와 관제센터로부터\n" +
                        "어르신 상태를 살필 수 있게 보여드린다는 거 알고 계세요?\n"))
            }

            GuideStatus.MONI_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MONITORING, "어르신께서 긴급콜 요청을 하시거나, 어르신의 움직임이 감지되지 않아\n" +
                        "위험한 상황이라고 판단되면, 보호자와 관제센터에게 알리고 어르신의 상태를 멀리서도 볼 수 있게 해드리고 있어요. 모니터링에 대한 설명 잘 이해하셨나요? \n"))

            }

            GuideStatus.MONI_GUIDE_RETRY -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MONITORING, "다시 한 번 설명해 드릴게요! "))
            }

            GuideStatus.MONI_GUIDE_FINISH -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MONITORING, "제가 항상 어르신의 안전 지킴이가 되어 드릴게요!"))
            }


            /**
             * 문자전화알림 가이드
             * */
            GuideStatus.MESSAGE_INIT -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MESSAGE, "어르신, 어르신께 문자나 전화가 올 경우\n" +
                        "제가 놓치지 않고 알려드린다는 거 알고 계셨나요?"))
            }

            GuideStatus.MESSAGE_GUIDE_SERVICE -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MESSAGE, "누군가에게 문자나 전화가 올 때 제가 알려드리고, " +
                        "문자가 도착하면 내용을 모두 읽어드려요. 문자나 전화 알림에 대해 제가 드린 설명이 이해가 잘 되셨나요?\n"))
            }

            GuideStatus.MESSAGE_GUIDE_RETRY -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MESSAGE, "다시 한 번 설명해 드릴게요! "))
            }

            GuideStatus.MESSAGE_GUIDE_FINISH -> {
                list.add(GuideTts(status, OnethefullBase.GUIDE_MESSAGE, "앞으로도 어르신께 오는 문자나 전화가 올 때 제가 놓치지 않고 잘 전달해 드릴게요!\n"))
            }
            else -> {
                list.add(GuideTts(GuideStatus.EMPTY, "guideEmpty", "Empty"))
            }
        }
        return list
    }

    suspend fun getElderlyInfo(): ElderlyList {
        return apiHelper.getElderlyInfo(
            DasomProviderHelper.getCustomerCode(context),
            DasomProviderHelper.getDeviceCode(context)
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GuideRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: GuideRepository(context).also { instance = it }
            }

        private val apiHelper: ApiHelper = ApiHelperImpl(RetrofitBuilder.apiService)
    }
}