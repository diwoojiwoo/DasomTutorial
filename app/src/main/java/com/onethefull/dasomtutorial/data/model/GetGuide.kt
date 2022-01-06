package com.onethefull.dasomtutorial.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021/12/13
 */

//{
//    "wakeupCount": 0,
//    "status_code": 0,
//    "emergencyCount": 0,
//    "messageCount": 0,
//    "monitoringCount": 0,
//    "medicationCount": 0,
//    "visionCount": 0,
//    "communityCount": 0,
//    "status": "ok"
//}

//{
//    "url": "/",
//    "message": "elderly not registered",
//    "status": "ERROR",
//    "status_code": -1
//}
@Parcelize
data class GetGuide(
    var wakeupCount: Int,
    var emergencyCount : Int,
    var messageCount : Int,
    var monitoringCount : Int,
    var medicationCount : Int,
    var visionCount : Int,
    var communityCount : Int,
    var status_code : Int,
    var status : String
) : Parcelable