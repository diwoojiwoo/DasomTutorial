package com.onethefull.dasomtutorial.ui.guide

/**
 * 로봇 서비스 사용법 안내 화면의 진행 상태
 */
enum class GuideStatus {
    /**
     * 대화 호출(웨이크워드) 사용법 안내
     */
    WAKEUP_INIT,
    WAKEUP_GUIDE_SERVICE,
    WAKEUP_GUIDE_FINISH,

    /**
     * 얼굴 인식·근접 대화 사용법 안내
     */
    VISION_INIT,
    VISION_GUIDE_SERVICE,
    VISION_GUIDE_SUCCESS,
    VISION_GUIDE_FAIL,

    /**
     * 복약 알림 사용법 안내
     */
    MEDI_INIT,
    MEDI_GUIDE_SERVICE,
    MEDI_GUIDE_RETRY,
    MEDI_GUIDE_FINISH,

    /**
     * 친구 찾기(커뮤니티) 사용법 안내
     */
    COMM_INIT,
    COMM_GUIDE_SERVICE,
    COMM_GUIDE_RETRY,
    COMM_GUIDE_FINISH,

    /**
     * 긴급콜·모니터링 사용법 안내
     */
    MONI_INIT,
    MONI_GUIDE_SERVICE,
    MONI_GUIDE_RETRY,
    MONI_GUIDE_FINISH,

    /**
     * 문자/전화 알림 사용법 안내
     */
    MESSAGE_INIT,
    MESSAGE_GUIDE_SERVICE,
    MESSAGE_GUIDE_RETRY,
    MESSAGE_GUIDE_FINISH,

    EMPTY
}
