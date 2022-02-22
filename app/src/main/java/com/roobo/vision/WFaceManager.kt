package com.roobo.vision

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import com.onethefull.dasomtutorial.App
import com.roobo.focusinterface.FocusManager
import com.roobo.logcat.Log

import com.roobo.vision.capture.VideoCapturer
import com.roobo.vision.capture.VideoCapturer.VideoCaptureListener
import com.roobo.vision.face.detection.FaceDetector
import com.roobo.vision.face.tracking.TrackingFace
import com.roobo.vision.facedetect.FaceRect
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("HardwareIds")
class WFaceManager private constructor() {
    lateinit var mFaceDector: FaceDetector
    lateinit var mTrackingFace: TrackingFace
    lateinit var mVideoCapturer: VideoCapturer
    private val context: Context? = App.instance.applicationContext

    private var gotResponse = true
    var face: ArrayList<Bitmap>? = null

    private var callback: Callback? = null

    interface Callback {
        fun onFaceDetected()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    private val mFaceDetectListener: FaceDetector.FaceDetectListener = object : FaceDetector.FaceDetectListener {
        override fun onFace(data: ByteArray, width: Int, height: Int) {
//            Log.e(KTAG + "::::" + TAG, data.toString());
            requestWonderfulCV(data, width, height)
        }

        override fun onFace(featureList: List<FaceRect>, imageWidth: Int, imageHeight: Int, nameList: List<String>) {
//            Log.e(KTAG+"::::"+TAG, " get! size:" + (featureList != null ? featureList.size() : 0) +  " ,nameList Size:: " +nameList.size() );
            mTrackingFace.trackFace(featureList)
        }

        override fun onNoFace() {
//            Log.e(KTAG, "onNoFace");
        }
    }

    //[WonderfulCV] 얼굴 정보 요청
    private fun requestWonderfulCV(data: ByteArray, width: Int, height: Int) {
        if (gotResponse) {
            Log.e(KTAG, "requestWonderfulCV:gotResponse : true")
            if (data != null) {
                gotResponse = false
                callback?.onFaceDetected()
            }
        } else {
            Log.e(KTAG, "gotResponse is false")
        }
    }

    private var traceFaceDisposable = CompositeDisposable()
    var disposableStarter: Disposable? = null
    var disposableStop: Disposable? = null
    var isAlready = true

    @SuppressLint("CheckResult")
    @Synchronized
    fun startTrackFace(c: Context?) {
        Log.e(TAG, Thread.currentThread().name + " startTrackFace")
        mVideoCapturer.setCaptureListener(mCaptureListener)
        mVideoCapturer.setPreviewSize(320, 240, 1)
        mVideoCapturer.startCapture()

//        if (disposableStarter != null && !disposableStarter!!.isDisposed) disposableStarter!!.dispose()
//        disposableStarter = Observable
//            .timer(OPERATION_FACE_DETECT_TIME, TimeUnit.MILLISECONDS)
//            .map {
//                Log.i(TAG, Thread.currentThread().name + " ==> observable map, OPERATION_FACE_DETECT_TIME:: $OPERATION_FACE_DETECT_TIME")
//                if (!FocusManager.getInstance(c).requestFocus("camera")) {
//                    Log.e(TAG, Thread.currentThread().name + "startTrackFace don't have camera focus!")
//                    return@map false
//                }
//                mVideoCapturer.setCaptureListener(mCaptureListener)
//                mVideoCapturer.setPreviewSize(320, 240, 1)
//                mVideoCapturer.startCapture()
//                true
//            }
//            .subscribeOn(Schedulers.newThread())
//            .subscribe { result: Boolean ->
//                if (result) {
//                    Log.i(TAG, Thread.currentThread().name + " ==> FaceDetect")
//                    restartTraceFace()
//                } else Log.e(TAG, Thread.currentThread().name + "Can not start FaceDetect - Focus Fail")
//            }
//        disposableStarter?.let { traceFaceDisposable.add(it) }
    }

    @Synchronized
    fun stopTrackFace() {
        Log.e(TAG, Thread.currentThread().name + " stopTrackFace")
        mVideoCapturer.stopCapture()
        mVideoCapturer.setCaptureListener(null)
//        release()
    }

    @Synchronized
    fun restartTraceFace() {
        Log.e(TAG, Thread.currentThread().name + " restartTraceFace")
        if (disposableStop != null && !disposableStop!!.isDisposed) disposableStop!!.dispose()
        disposableStop = Observable.timer(RELEASE_FACE_DETECT_TIME, TimeUnit.MILLISECONDS)
            .map { _ ->
                Log.e(TAG, Thread.currentThread().name + " stop")
                mVideoCapturer.stopCapture()
                mVideoCapturer.setCaptureListener(null)
                true
            }
            .subscribeOn(Schedulers.newThread())
            .subscribe { result: Boolean ->
                Log.e(TAG, "${Thread.currentThread().name} restartTraceFace => startTraceFace $result")
                startTrackFace(context)
            }
        disposableStop?.let { traceFaceDisposable.add(it) }
    }

    private val mCaptureListener = object : VideoCaptureListener {
        override fun onCaptureData(data: ByteArray?, width: Int, height: Int) {
            mFaceDector.detect(data, width, height)
        }
    }

    fun release() {
        if (disposableStarter != null && !disposableStarter!!.isDisposed) disposableStarter!!.dispose()
        if (disposableStop != null && !disposableStop!!.isDisposed) disposableStop!!.dispose()
    }

    companion object {
        private val TAG = WFaceManager::class.java.simpleName
        private const val KTAG = "KayKwon"
        val instance = WFaceManager()

        private var OPERATION_FACE_DETECT_TIME = 5 * 1000L
        private const val RELEASE_FACE_DETECT_TIME = 20 * 1000L
    }

    init {
        Log.d(KTAG, "initialize WFaceManager")
        App.instance?.let { context ->
            mFaceDector = FaceDetector(context)
            mFaceDector.setFaceListener(mFaceDetectListener)
            mTrackingFace = TrackingFace(context)
            mVideoCapturer = VideoCapturer(context)
        }
    }
}