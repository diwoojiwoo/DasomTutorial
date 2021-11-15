package com.onethefull.dasomtutorial.ui.learn.localhelp

/**
 * Created by Douner on 2020-01-10.
 */

object LocalDasomFilterTask {
    var cmd= Command.EMPTY

    fun checkSOS(text: String): Boolean {
        if (text.length <= 4) {
            return text.contains("살려") || text == ("도와줘") || text == ("도와 줘") || text == "구해줘"
        }
        return false
    }

    fun checkAnswer(text: String): Boolean {
        return (text == ("다솜") ||
                text == ("다시마") ||
                text == ("다소미") ||
                text == ("가슴아") ||
                text == ("다솜아") ||
                text == ("다소마"))
    }

    enum class Command {
        EMPTY
    }
}

