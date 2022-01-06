package com.onethefull.dasomtutorial.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by sjw on 2021/12/13
 */


//{
//    "status_code": 0,
//    "status": "ok"
//}

//{
//    "url": "/",
//    "message": "elderly not registered",
//    "status": "ERROR",
//    "status_code": -1
//}

@Parcelize
data class AddGuide(
    var status_code : Int,
    var status : String
) : Parcelable