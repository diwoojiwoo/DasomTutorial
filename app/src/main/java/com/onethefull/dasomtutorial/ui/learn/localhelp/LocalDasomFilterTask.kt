package com.onethefull.dasomtutorial.ui.learn.localhelp

/**
 * Created by Douner on 2020-01-10.
 */

object LocalDasomFilterTask {
    var cmd = Command.EMPTY

    fun checkDasom(text: String): Boolean {
        return (text == ("다솜") ||
                text == ("다시마") ||
                text == ("다소미") ||
                text == ("가슴아") ||
                text == ("다솜아") ||
                text == ("다소마"))
    }

    fun checkGenie(text: String): Boolean {
        return (text.contains("지니") ||
                text == ("기니") ||
                text == ("지니야") ||
                text == ("기니야") ||
                text == ("기니아") ||
                text == ("제니아") ||
                text == ("제니야"))
    }

    fun checkSOS(text: String): Boolean {
        return (text.contains("그래") ||
                text.contains("살려") ||
                text == ("도와줘") ||
                text == ("도와 줘") ||
                text == ("필요해") ||
                text == ("살려줘") ||
                text == "구해줘")
    }

    fun checkPosWord(text: String): Boolean {
        return (text.length <= 4 && text == "응") ||
                (text.length <= 4 && text == "어") ||
                (text.length <= 4 && text == "예") ||
                (text.length <= 4 && text == "네") ||
                text.contains("그래") ||
                text.contains("좋다") ||
                text.contains("좋아") ||
                text.contains("다시")
    }

    fun setCommand(cmd: Command) {
        this.cmd = cmd
    }

    enum class Command {
        EMPTY
    }
}

