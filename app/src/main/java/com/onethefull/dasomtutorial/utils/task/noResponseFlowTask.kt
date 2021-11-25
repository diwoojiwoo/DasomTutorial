package com.onethefull.dasomtutorial.utils.task

import android.content.Context
import com.onethefull.dasomtutorial.provider.DasomProviderHelper

/**
 * Created by sjw on 2021/11/10
 */
object noResponseFlowTask {

    fun get(context: Context): Boolean {
        when (DasomProviderHelper.getPracticeEmergencyTaskCnt(context)) {
            "0", DasomProviderHelper.SETTING_ETC_EMPTY -> return true
        }
        return false
    }

    fun insert(context: Context, cnt: Int) {
        DasomProviderHelper.insert(
            context,
            DasomProviderHelper.ProviderInsertData(
                DasomProviderHelper.KEY_PRACTICE_EMERGENCY_TASK_COUNT,
                cnt.toString(),
                DasomProviderHelper.SETTING_ETC_EMPTY
            )
        )
    }
}