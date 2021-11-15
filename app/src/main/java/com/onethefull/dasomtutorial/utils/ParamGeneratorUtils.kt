package com.onethefull.dasomtutorial.utils

import android.os.Build
import java.util.HashMap

/**
 * Created by Douner on 2019. 4. 12..
 */
object ParamGeneratorUtils {
    fun getDeviceId(): HashMap<String, String> {
        val params = HashMap<String, String>()
        params["PUDDING_SERIALNUM"] = Build.SERIAL
        return params
    }
}
