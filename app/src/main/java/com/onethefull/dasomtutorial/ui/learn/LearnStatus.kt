package com.onethefull.dasomtutorial.ui.learn

/**
 * Created by sjw on 2021/11/15
 */
enum class LearnStatus {
    START,
    CALL_DASOM,
    CALL_SOS,


    RETRY,
    HALF,
    COMPLETE,
    END,
    CALL_DASOM_RETRY, // 무응답
}