package com.onethefull.dasomtutorial.utils.touch

import android.app.Activity
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import com.onethefull.dasomtutorial.App
import com.onethefull.dasomtutorial.BuildConfig
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.core.scene.SceneHelper

import java.util.ArrayList
import kotlin.math.abs

/**
 * Created by hailong on 17-1-11.
 */

class TouchEventHandler {
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mPoints = ArrayList<Point>()
    private var mClickTime = 0
    private val mTouchRunnable = TouchRunnable()
    private var TOUCH_POSITION_X_0_START = Integer.MAX_VALUE
    private var TOUCH_POSITION_X_0 = 0
    private var TOUCH_POSITION_Y_0_START = Integer.MAX_VALUE
    private var TOUCH_POSITION_Y_0 = 0
    private var mNeedIgnore = false
    private val mIgnoreSlip = false

    fun handle(event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                TOUCH_POSITION_X_0_START = event.getX(0).toInt()
                TOUCH_POSITION_Y_0_START = event.getY(0).toInt()
                var point = Point(event.x.toInt(), event.y.toInt())
                mPoints.add(point)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                var point = Point(
                    event.getX(event.actionIndex).toInt(),
                    event.getY(event.actionIndex).toInt()
                )
                mPoints.add(point)
                mMultiTouch = true
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_UP -> {
                if (mNeedIgnore) {
                    mNeedIgnore = false
                    return
                }

                var multiTouch = false
                if (mLastTouchMillis != 0L && System.currentTimeMillis() - mLastTouchMillis < DEFAULT_MULTI_TOUCH_INTERVAL_TIME) {
                    mHandler.removeCallbacks(mTouchRunnable)
                    mTouchTime++
                    if (mTouchTime == 2) {
                        mHandler.postDelayed(
                            mTouchRunnable,
                            DEFAULT_MULTI_TOUCH_INTERVAL_TIME.toLong()
                        )
                        multiTouch = true
                    }
                    if (mTouchTime >= 3) {
                        multiTouch = true
                    }
                }

                mClickTime++
                if (mClickTime >= 5 && !mIgnoreSlip) {
                    mClickTime = 0
                }

                if (!multiTouch) {
                    mTouchRunnable.setPoint(event.x.toInt(), event.y.toInt())
                    mHandler.postDelayed(mTouchRunnable, 0)
                }

                mLastTouchMillis = System.currentTimeMillis()
            }

            MotionEvent.ACTION_MOVE -> {

                TOUCH_POSITION_X_0 = event.getX(0).toInt()
                TOUCH_POSITION_Y_0 = event.getY(0).toInt()
                val dis0 = abs(TOUCH_POSITION_X_0 - TOUCH_POSITION_X_0_START)
                val dis0Y = abs(TOUCH_POSITION_Y_0 - TOUCH_POSITION_Y_0_START)
                if (!mIgnoreSlip) {
                    if (dis0 in DIS_UNLOCK_X..1279 || dis0Y in DIS_UNLOCK_Y..1279) {
                        if (BuildConfig.TARGET_DEVICE == App.DEVICE_BEANQ) SceneHelper.switchOut()
                        else activity?.finishAffinity()
                        TOUCH_POSITION_X_0_START = Integer.MAX_VALUE
                        TOUCH_POSITION_Y_0_START = Integer.MAX_VALUE
                        mNeedIgnore = true
                    }
                }
            }
        }
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    fun getActivity(): Activity? {
        return activity
    }

    private var activity: Activity? = null

    private inner class TouchRunnable : Runnable {
        private var x: Int = 0
        private var y: Int = 0

        fun setPoint(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
        override fun run() {
            val id: Int
            var interrupt = false
            if (mMultiTouch && mPoints.size == 2) {
                DWLog.d(TAG, "两点触摸事件")
                id = TouchAreaManager.parse(mPoints[0], mPoints[1])
                interrupt = true
            } else if (mMultiTouch && mPoints.size > 2) {
                DWLog.d(TAG, "多点触摸事件")
                id = TouchAreaManager.CONFUSED
                interrupt = true
            } else {
                if (mTouchTime == 2) {
                    id = TouchAreaManager.CONFUSED
                    interrupt = true
                } else {
                    id = TouchAreaManager.parse(x, y)
                }
            }

            DWLog.d(TAG, "id: $id")
            mPoints.clear()
            mTouchTime = 1
            mMultiTouch = false
        }
    }

    companion object {
        private val TAG = "TouchEventHandler"
        private val DEFAULT_MULTI_TOUCH_INTERVAL_TIME = 250
        private val DIS_UNLOCK_X = 300
        private val DIS_UNLOCK_Y = 300
        private var mMultiTouch = false
        private var mLastTouchMillis: Long = 0
        private var mTouchTime = 1
    }
}
