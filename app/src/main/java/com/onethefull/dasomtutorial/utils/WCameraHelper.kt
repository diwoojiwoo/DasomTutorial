package com.onethefull.dasomtutorial.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Environment
import android.view.SurfaceHolder
import com.onethefull.dasomtutorial.utils.logger.DWLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class WCameraHelper {
    fun setWCameraHelperListener(listener: OnWCameraHelperListener) {
        mListener = listener
    }

    interface OnWCameraHelperListener {
        fun onComplete()
//        fun onSent(filePath: String)
    }

    companion object {
        private val TAG = WCameraHelper::class.java.simpleName
        val inst = WCameraHelper()
        var isCameraOpen = false
        private var mCamera: Camera? = null
        private var mListener: OnWCameraHelperListener? = null
        private var bitmapPicture: Bitmap? = null
        
        val photoDirPath = Environment.getExternalStorageDirectory().toString() + "/gallary/temp/"
        val photoExt = ".jpg"
        val storage = File(photoDirPath)

        val instance: Camera?
            @Synchronized get() {
                if (mCamera == null) {
                    val num = Camera.getNumberOfCameras()
                    DWLog.d(TAG, "begin open camera num=$num")
                    mCamera = Camera.open(0)
                    if (mCamera == null) {
//                        Log.e(TAG, "end open camera fail")
                    } else {
                        isCameraOpen = true
//                        Log.d(TAG, "end open camera succ")
                    }
                }
                return mCamera
            }


        fun initCamera(holder: SurfaceHolder?, width: Int, height: Int): Boolean {
            if (mCamera == null) {
                DWLog.e(TAG, "camera object is null, please call getInstance() first")
                return false
            }

            if (holder == null) {
                DWLog.e(TAG, "surfaceholder object is null")
                return false
            }

            mCamera!!.setDisplayOrientation(0)
            try {
                mCamera!!.setPreviewDisplay(holder)
            } catch (e: IOException) {
                DWLog.e(TAG, "setPreviewDisplay fail. e=$e")
                freeCameraResource()
                return false
            }

            val parameters = mCamera!!.parameters
            val mSupportedPreviewSizes = parameters.supportedPreviewSizes
            val mSupportedVideoSizes = parameters.supportedVideoSizes
            val optimalSize = WCameraHelper.getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes, height, width)

            parameters.setPreviewSize(optimalSize!!.width, optimalSize.height) // 设置预览图像大小
            DWLog.d(TAG, "preview width=" + optimalSize.width.toString() + " height=" + optimalSize.height.toString())

            parameters.set("orientation", "landscape")
            val focusModes = parameters.supportedFocusModes
            if (focusModes.contains("continuous-video")) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            DWLog.d(TAG, "set focus mode is " + parameters.focusMode)
            mCamera!!.parameters = parameters //设置聚焦模式

            return true
        }

        fun startPreview() {
            if (mCamera == null) {
                DWLog.e( "camera object is null, please call getInstance() first")
                return
            }
            DWLog.d( "startPreview")
            mCamera!!.startPreview()
        }


        fun stopPreview() {
            if (mCamera == null) {
                DWLog.e(TAG, "camera object is null, please call getInstance() first")
                return
            }
            mCamera!!.stopPreview()

        }


        fun takePicture() {
            if (mCamera == null) {
                DWLog.e(TAG, "camera object is null, please call getInstance() first")
                return
            }

            mCamera!!.takePicture(shutterCallback, pictureCallbackRaw, pictureCallbackJpeg)
        }

        internal var shutterCallback: Camera.ShutterCallback = Camera.ShutterCallback {
        }

        internal var pictureCallbackRaw: Camera.PictureCallback = Camera.PictureCallback { bytes, camera -> }

        internal var pictureCallbackJpeg: Camera.PictureCallback = Camera.PictureCallback { bytes, camera ->
            val option = BitmapFactory.Options()
            option.inPurgeable = true
            option.inDither = true
            bitmapPicture = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, option)

            mListener!!.onComplete()
        }

        fun getPictureFile(context: Context) {
            val storage = context.cacheDir
            val fileName = "msg_photo.jpg"
            val tempFile = File(storage, fileName)
            try {
                tempFile.createNewFile()
                val out = FileOutputStream(tempFile)
                bitmapPicture!!.compress(Bitmap.CompressFormat.JPEG, 50, out)  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
                out.close() // 마무리로 닫아줍니다.
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
//            mListener!!.onSent(tempFile.absolutePath)
        }

        fun freeCameraResource(): Boolean {
            isCameraOpen = false
            if (mCamera == null) {
                DWLog.e(TAG, "call freeCameraResource may be wrong, camera object is null")
                return false
            }

            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.lock()
            mCamera!!.release()
            mCamera = null
            return true
        }

        /**
         * Iterate over supported camera video sizes to see which one best fits the
         * dimensions of the given view while maintaining the aspect ratio. If none can,
         * be lenient with the aspect ratio.
         *
         * @param supportedVideoSizes Supported camera video sizes.
         * @param previewSizes        Supported camera preview sizes.
         * @param w                   The width of the view.
         * @param h                   The height of the view.
         * @return Best match camera video size to fit in the view.
         */
        fun getOptimalVideoSize(supportedVideoSizes: List<Camera.Size>?,
                                previewSizes: List<Camera.Size>, w: Int, h: Int): Camera.Size? {
            // Use a very small tolerance because we want an exact match.
            val ASPECT_TOLERANCE = 0.1
            val targetRatio = w.toDouble() / h

            // Supported video sizes list might be null, it means that we are allowed to use the preview
            // sizes
            val videoSizes: List<Camera.Size>
            if (supportedVideoSizes != null) {
                videoSizes = supportedVideoSizes
            } else {
                videoSizes = previewSizes
            }
            var optimalSize: Camera.Size? = null

            // Start with max value and refine as we iterate over available video sizes. This is the
            // minimum difference between view and camera height.
            var minDiff = java.lang.Double.MAX_VALUE

            // Target view height

            // Try to find a video size that matches aspect ratio and the target view size.
            // Iterate over all available sizes and pick the largest size that can fit in the view and
            // still maintain the aspect ratio.
            for (size in videoSizes) {
                val ratio = size.width.toDouble() / size.height
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue
                if (Math.abs(size.height - h) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }

            // Cannot find video size that matches the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = java.lang.Double.MAX_VALUE
                for (size in videoSizes) {
                    if (Math.abs(size.height - h) < minDiff && previewSizes.contains(size)) {
                        optimalSize = size
                        minDiff = Math.abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }
    }
}
