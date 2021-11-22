package com.onethefull.dasomtutorial.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.data.model.InnerTtsV2
import com.onethefull.dasomtutorial.provider.DasomProviderHelper
import java.lang.reflect.Type

/**
 * Created by sjw on 2021/11/10
 */
class LearnRepository private constructor(
    private val context: Context
) {
    fun getPracticeEmergencyList(key: String): List<InnerTtsV2> {
        return convertJson(DasomProviderHelper.getPracticeEmergencyValue(context, key))
    }


    private fun convertJson(jsonString: String): List<InnerTtsV2> {
        var list = ArrayList<InnerTtsV2>()
        if (jsonString == "") {
            list.add(InnerTtsV2(arrayListOf(), arrayListOf(), arrayListOf(), "", "", arrayListOf(), "", 0))
            return list as ArrayList<InnerTtsV2>
        }
        val type: Type = object : TypeToken<List<InnerTtsV2?>?>() {}.type
        return Gson().fromJson(jsonString, type) as ArrayList<InnerTtsV2>
    }

    companion object {
        // singleton initialization
        @Volatile
        private var instance: LearnRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: LearnRepository(context).also { instance = it }
            }
    }
}