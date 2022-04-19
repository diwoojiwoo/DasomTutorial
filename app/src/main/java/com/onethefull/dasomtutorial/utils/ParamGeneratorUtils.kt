package com.onethefull.dasomtutorial.utils

import android.os.Build
import java.util.HashMap

/**
 * Created by Douner on 2019. 4. 12..
 */
object ParamGeneratorUtils {
    fun getDeviceId(serialNumber: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["PUDDING_SERIALNUM"] = serialNumber
        return params
    }

    fun getDementiaQuizListReq(serialNumber: String, limit: String): HashMap<String, String> {
        return getDeviceId(serialNumber).apply {
            this["LIMIT"] = limit
        }
    }

    fun getCategory(category: String): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["CATEGORY"] = category
        return params
    }
}
