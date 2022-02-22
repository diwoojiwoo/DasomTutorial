package com.onethefull.dasomtutorial.ui.guide.localhelp

/**
 * Created by Douner on 2020-01-10.
 */

object LocalGuideFilterTask {
    var cmd = Command.EMPTY

    fun checkDasom(text: String): Boolean {
        return (text == ("다솜") ||
                text == ("다시마") ||
                text == ("다소미") ||
                text == ("가슴아") ||
                text == ("다솜아") ||
                text == ("다소마"))
    }

    fun checkDifficulty(text: String): Boolean {
        return (text.length <= 4 && text == "응") ||
                (text.length <= 4 && text == "어") ||
                (text.length <= 4 && text == "예") ||
                (text.length <= 4 && text == "네") ||
                text.contains("맞아") ||
                text.contains("그래") ||
                text.contains("어려워") ||
                text.contains("어렵다") ||
                text.contains("어렵네")
    }

    fun checkPosWord(text: String): Boolean {
        return (text.length <= 4 && text == "응") ||
                (text.length <= 4 && text == "어") ||
                (text.length <= 4 && text == "예") ||
                (text.length <= 4 && text == "네") ||
                text.contains("그래") ||
                text.contains("안다") ||
                text.contains("좋다") ||
                text.contains("좋아") ||
                text.contains("알아") ||
                text.contains("알겠") ||
                text.contains("알고 있어")
    }

    fun checkNegaWord(text: String): Boolean {
        return (text.length <= 4 && text == "아니") ||
                (text.length <= 4 && text == "전혀") ||
                text.contains("몰라") ||
                text.contains("모른다") ||
                text.contains("모릅니다") ||
                text.contains("몰랐") ||
                text.contains("모름") ||
                text.contains("모르면") ||
                text.contains("모르겠어")
    }

    fun setCommand(cmd: Command) {
        LocalGuideFilterTask.cmd = cmd
    }

    enum class Command {
        EMPTY
    }
}

