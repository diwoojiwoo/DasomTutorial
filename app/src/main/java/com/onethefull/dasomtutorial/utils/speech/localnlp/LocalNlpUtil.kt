package com.onethefull.dasomtutorial.utils.speech.localnlp

/**
 * Created by Douner on 2020/06/30.
 */
object LocalNlpUtil {
    fun isChatStarter(text: String): Boolean {
        if (text.contains("대화") ||
            text.contains("이야기") ||
            text.contains("채팅") ||
            text.contains("말하기")
        ) {
            if (text.contains("연결") ||
                text.contains("시작") ||
                text.contains("만남") ||
                text.contains("하고") ||
                text.contains("시켜") ||
                text.contains("틀어") ||
                text.contains("하자")
            ) {
                return !(text.contains("그만") || text.contains("안해") || text.contains("나중에"))
            }
        }
        if (text.contains("친구") && text.contains("찾아")) {
            return true
        }
        return false
    }
}