package com.onethefull.dasomtutorial.utils.bus

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Douner on 2019. 5. 8..
 */
class RxEvent {
    @Parcelize
    data class Event(
        val typeNumber: Int,
        val time: Long,
        val name: String
    ) : Parcelable

    companion object {
        const val AppDestroy = 0x00
        const val AppDestoryUpdate = 0x01
        const val AppDestroyRemove = -0x01
        const val noResponse = 0x30
        val map = HashMap<Int, String>()


        val noResponseShortTime = Event(noResponse, 10 * 1000L, "noResponse")

        val noResponseLongTime = Event(noResponse, 20 * 1000L, "noResponse")

        val destroyApp = Event(AppDestroy, 1 * 1000L, "AppDestroy")

        val destroyAppUpdate = Event(AppDestoryUpdate, 3 * 60 * 1000L, "AppDestoryUpdate")

        val destroyLongTimeUpdate = Event(AppDestoryUpdate, 10 * 60 * 1000L, "AppDestoryUpdate")

        val destroyAppRemove = Event(AppDestroyRemove, 0, "AppDestroyRemove")
    }

    init {
        map[AppDestroy] = "AppDestroy"
        map[AppDestoryUpdate] = "AppDestoryUpdate"
        map[AppDestroyRemove] = "AppDestroyRemove"
        map[noResponse] ="noResponse"
    }
}
