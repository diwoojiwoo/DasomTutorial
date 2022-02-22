package com.onethefull.dasomtutorial.ui.guide

/**
 * Created by sjw on 2021/12/22
 */

enum class GuideStatus {
    /**
     * 웨이크업 가이드 상태
     * */
    WAKEUP_INIT,
    WAKEUP_GUIDE_SERVICE,
    WAKEUP_GUIDE_FINISH,

    /**
     * 영상인식 가이드 상태
     * */
    VISION_INIT,
    VISION_GUIDE_SERVICE,
    VISION_GUIDE_SUCCESS,
    VISION_GUIDE_FAIL,

    /**
     * 복약알림 가이드
     * */
    MEDI_INIT,
    MEDI_GUIDE_SERVICE,
    MEDI_GUIDE_RETRY,
    MEDI_GUIDE_FINISH,

    /**
     * 커뮤니티 가이드
     * */
    COMM_INIT,
    COMM_GUIDE_SERVICE,
    COMM_GUIDE_RETRY,
    COMM_GUIDE_FINISH,

    /**
     * 모니터링 가이드
     * */
    MONI_INIT,
    MONI_GUIDE_SERVICE,
    MONI_GUIDE_RETRY,
    MONI_GUIDE_FINISH,

    /**
     * 문자전화알림 가이드
     * */
    MESSAGE_INIT,
    MESSAGE_GUIDE_SERVICE,
    MESSAGE_GUIDE_RETRY,
    MESSAGE_GUIDE_FINISH,

    EMPTY
}
