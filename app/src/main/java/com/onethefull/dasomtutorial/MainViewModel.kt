package com.onethefull.dasomtutorial

import android.os.Handler
import android.os.Looper
import com.onethefull.dasomtutorial.base.BaseViewModel
import com.onethefull.dasomtutorial.utils.bus.RxBus
import com.onethefull.dasomtutorial.utils.bus.RxEvent
import com.onethefull.dasomtutorial.utils.logger.DWLog
import com.roobo.core.scene.SceneHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by sjw on 2021/11/23
 */
class MainViewModel : BaseViewModel() {
    /**
     * Excute Event Bus
     */
    private val BusExcute: (event: RxEvent.Event) -> Unit = { event ->

        DWLog.d("BusExcute $event")

        when (event.typeNumber) {
            RxEvent.AppDestroy -> {
                handler.removeMessages(MESSAGE_WHAT_TERMIANTE_SOODA)
                when (BuildConfig.TARGET_DEVICE) {
                    App.DEVICE_BEANQ -> {
                        DWLog.d("BusExcute SceneHelper.switchOut() $event")
                        SceneHelper.switchOut()
                    }
//                    App.DEVICE_CLOI -> {
//                        CloiSceneHelper.switchOut()
//                        mainActivity.finish()
//                    }
                }
            }

            RxEvent.AppDestoryUpdate -> {
                updateTerminator(event.time)
            }

            RxEvent.AppDestroyRemove -> {
                removeTerminator()
            }
        }
    }

    fun start() {
        DWLog.d("Start")
        eventRegister()
        updateTerminator(TIME_TERMIANTE_APP) // 앱종료 체커 시작
    }

    fun release() {
        compositeDisposable.dispose()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Register Event
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     *
     */
    private val eventRegister: () -> Unit = {
        compositeDisposable.add(
            RxBus.listen(RxEvent.Event::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(BusExcute)
        )
    }


    private val mHandlerCallback = Handler.Callback { msg ->
        when (msg.what) {
            MESSAGE_WHAT_TERMIANTE_SOODA -> {
                DWLog.d("MESSAGE_WHAT_TERMIANTE_SOODA")
                if (BuildConfig.TARGET_DEVICE == App.DEVICE_BEANQ) SceneHelper.switchOut()
//                else if (BuildConfig.TARGET_DEVICE == App.DEVICE_CLOI) {
//                    CloiSceneHelper.switchOut()
//                    mainActivity.finish()
//                }
            }
        }
        false
    }

    private val handler = Handler(Looper.getMainLooper(), mHandlerCallback)

    /**
     * 자동 종료 시간 갱신
     */
    private fun updateTerminator(time: Long) {
        DWLog.i("MESSAGE_WHAT_TERMIANTE_SOODA ==> updateTerminator $time")
        removeTerminator()
        handler.sendMessageDelayed(
            handler.obtainMessage(MESSAGE_WHAT_TERMIANTE_SOODA),
            time
        )
    }


    /**
     * 자동 종료 제거
     */
    private fun removeTerminator() {
        DWLog.i("MESSAGE_WHAT_TERMIANTE_SOODA ==> removeTerminator")
        handler.removeMessages(MESSAGE_WHAT_TERMIANTE_SOODA)
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object{
        // TIME
        const val TIME_TERMIANTE_APP = 90 * 1000L

        // MESSAGE ID
        const val MESSAGE_WHAT_TERMIANTE_SOODA = 0x202
    }

}