package com.roobo.vision.capture;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.onethefull.dasomtutorial.App;

import java.text.SimpleDateFormat;

/**
 * Created by tianli on 16-5-18.
 */
public class VideoCapturer {

    private final static String TAG = "VideoCapturer";
    //    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private WindowManager windowManager;
    private CameraPreview surfaceView;
    private SimpleDateFormat dateFormater;
    int mCurrentCamIndex = 0;
    private Context mContext;
    private Camera mCamera;
    private int mExpectedWidth;
    private int mExpectedHeight;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mFps;
    private final static int VIDEO_DURATION = 10 * 1000;
    private VideoCaptureListener mCaptureListener;
    private OnVideoingListener mOnVideoingListener;
    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            camera.addCallbackBuffer(data);
            if (mCaptureListener != null) {
                mCaptureListener.onCaptureData(data, mPreviewWidth, mPreviewHeight);
            }
        }
    };


    public VideoCapturer(Context context) {
        mContext = context.getApplicationContext();
        init();
    }

    private void init() {
        mExpectedWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mExpectedHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mFps = 15;
        windowManager = (WindowManager) App.Companion.getInstance().getSystemService(Context.WINDOW_SERVICE);
        dateFormater = new SimpleDateFormat("yyyyMMddHHmmss");
    }

    private void initCamera() {
        if (mCamera != null) {
//            Log.d(TAG, "current camera is not null! return");
            return;
        }
        mCamera = openFrontFacingCameraGingerbread();
        if (mCamera == null) return;
        setCameraParameters();
        startPreviewWithBuffer();

    }

    private void startPreviewWithBuffer() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
            mCamera.addCallbackBuffer(new byte[((mPreviewWidth * mPreviewHeight) * ImageFormat.getBitsPerPixel
                    (ImageFormat.NV21)) / 8]);
            mCamera.startPreview();
//            Log.e(TAG, "startPreview!");
        }
    }

    private void setCameraParameters() {
//        Log.e(TAG, "setCameraParameters called!");
        Camera.Parameters parameters = mCamera.getParameters();
        final int[] range = CameraEnumerationAndroid.getFramerateRange(parameters, mFps *
                1000);
        final Camera.Size previewSize = CameraEnumerationAndroid.getClosestSupportedSize(
                parameters.getSupportedPreviewSizes(), mExpectedWidth, mExpectedHeight);

        mPreviewWidth = previewSize.width;
        mPreviewHeight = previewSize.height;
        final CameraEnumerationAndroid.CaptureFormat captureFormat = new CameraEnumerationAndroid.CaptureFormat(
                previewSize.width, previewSize.height,
                range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }
        // parameters.setRecordingHint(true);
        if (captureFormat.maxFramerate > 0) {
            parameters.setPreviewFpsRange(captureFormat.minFramerate, captureFormat.maxFramerate);
        }
        parameters.setPreviewSize(captureFormat.width, captureFormat.height);
        parameters.setPreviewFormat(ImageFormat.NV21);
        final Camera.Size pictureSize = CameraEnumerationAndroid.getClosestSupportedSize(
                parameters.getSupportedPictureSizes(), mExpectedWidth, mExpectedHeight);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        mCamera.setParameters(parameters);
    }

    public void setPreviewSize(int width, int height, int fps) {
        mExpectedWidth = width;
        mExpectedHeight = height;
        mFps = fps;
    }

    public void startCapture() {
//        Log.i(TAG, "startCapture tid:" + hashCode());
        initCamera();
    }

    public void stopCapture() {
//        Log.i(TAG, "stopCapture tid:" + hashCode());
//        stopVideo();
        release();
//        clearTempFiles();
    }

    private synchronized void release() {
        if (surfaceView != null) {
//            Log.i(TAG, "remove surfaceView");
            surfaceView.releaseCamera();
            try {
                windowManager.removeView(surfaceView);
            } catch (Exception e) {
//                Log.w(TAG, e.getMessage());
            }
//            Log.i(TAG, "remove surfaceView over");
            surfaceView = null;
        }

        if (mCamera != null) {
            long now = System.currentTimeMillis();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
//            Log.e(TAG, "release spend time:" + (System.currentTimeMillis() - now) + "ms");
        } else {
//            Log.d(TAG, "release camera is null! do nothing!");
        }
    }

    public void setCaptureListener(VideoCaptureListener l) {
        mCaptureListener = l;
    }

    public void setOnVideoingListener(OnVideoingListener listener) {
        mOnVideoingListener = listener;
    }

    private Camera openFrontFacingCameraGingerbread() {
        long now = System.currentTimeMillis();
//        Log.e(TAG, "start open camera! threadID:" + Process.myTid());
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                    if (cam != null) {
                        mCurrentCamIndex = camIdx;
                    }
                } catch (RuntimeException e) {
                    Log.e("camera", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        if (cam == null) {
            Toast.makeText(mContext, "camera_error", Toast.LENGTH_SHORT).show();
//            throw new RuntimeException("Failed to open camera.");
//            Log.i(TAG, "open camera failed");
//            EventAgent.onEvent(Event.id.LAUNCHER_OPEN_CAMERA_FAILED);
            return null;
        }

//        Log.e(TAG, "finish open camera! threadID:" + Process.myTid() + " spend time:" + (System.currentTimeMillis() -
//                now) + "ms");
        return cam;
    }

    /**
     * 开始录像
     */
    public synchronized void startVideo(final long videoDuring, final boolean sendChance) {
//        Log.d(TAG, "startVideo isRecording:" + isRecording);
        if (!isRecording) {
//            final File newVideoFile = new File(BabyActiveFile.FILES_TEMP_DIR + String.valueOf(dateFormater.format(new Date
//                    (System.currentTimeMillis()))) + ".mp4");
//            if (!newVideoFile.getParentFile().exists()) {
//                newVideoFile.getParentFile().mkdirs();
//            }
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
//            prepareVideoRecorder(newVideoFile, sendChance, new MediaRecorderPrepareCallback() {
//                @Override
//                public void onSucc() {
//                    Log.i(TAG, "prepareVideoRecorder succ");
//                    if (mOnVideoingListener != null) {
//                        mOnVideoingListener.onVideoStart();
//                    }
//                    try {
//                        mMediaRecorder.start();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    Log.d(TAG, "startMediaRecording over");
//                }
//
//                @Override
//                public void onFail() {
//                    Log.i(TAG, "prepareVideoRecorder failed");
//                    stopVideo();
//                }
//            });
//        }
        }

//    private synchronized void prepareVideoRecorder(final File mNewVideoFile, final boolean sendChance, final MediaRecorderPrepareCallback callback) {
//        Log.i(TAG, "prepareVideoRecorder");
//        isRecording = true;
//        surfaceView = new CameraPreview(RooboUiApplication.getApp(), mCamera);
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
//                1, 1,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                PixelFormat.TRANSLUCENT
//        );
//        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//        windowManager.addView(surfaceView, layoutParams);
//        surfaceView.setPreviewCallback(new CameraPreview.PreviewCallback() {
//            @Override
//            public void onCreated(SurfaceHolder holder) {
//                if (holder == null) {
//                    Log.e(TAG, "holder is null");
//                    return;
//                }
//                mMediaRecorder = new MediaRecorder(RooboUiApplication.getApp());
//                mCamera.unlock();
//                mMediaRecorder.setCamera(mCamera);
//                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//                mMediaRecorder.setProfile(CamcorderProfile.get(0, CamcorderProfile.QUALITY_LOW));
//                mMediaRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
//                mMediaRecorder.setOutputFile(mNewVideoFile.getPath());
//                mMediaRecorder.setPreviewDisplay(holder.getSurface());
//                mMediaRecorder.setMaxDuration(VIDEO_DURATION);
//                mMediaRecorder.setOnInfoListener(new android.media.MediaRecorder.OnInfoListener() {
//                    @Override
//                    public void onInfo(android.media.MediaRecorder mr, int what, int extra) {
//                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//                            stopVideo();
//                            moveToUploadDir(mNewVideoFile);
//                            if (sendChance) {
//                                ChanceUtil.sendFaceChance(RooboUiApplication.getApp(), "", true);
//                            }
//                        }
//                    }
//                });
//                mMediaRecorder.setAudioFocusChangeListener(new AudioFocusChangeListener() {
//                    @Override
//                    public void onFocusChanged(boolean focused) {
//                        Log.d(TAG, "audio_recorder focus changed:" + focused);
//                        if (!focused) {
//                            stopVideo();
//                            clearTempFiles();
//                        }
//                    }
//                });
//                try {
//                    mMediaRecorder.prepare();
//                } catch (IllegalStateException e) {
//                    Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
//                    stopVideo();
//                    if (callback != null) {
//                        callback.onFail();
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
//                    stopVideo();
//                    if (callback != null) {
//                        callback.onFail();
//                    }
//                }
//                if (callback != null) {
//                    callback.onSucc();
//                }
//            }
//        });
//    }

//    private synchronized void stopVideo() {
//        if (!isRecording) {
//            Log.i(TAG, "not recordering return");
//            return;
//        }
//        Log.i(TAG, "releaseMediaRecorder");
//        if (mMediaRecorder != null) {
//            try {
//                mMediaRecorder.setPreviewDisplay(null);
//                mMediaRecorder.stop();
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
//            mMediaRecorder.reset();   // clear recorder configuration
//            mMediaRecorder.release(); // release the recorder object
//            mMediaRecorder = null;
//
//            if (mOnVideoingListener != null) {
//                mOnVideoingListener.onVideoStop();
//            }
//
//            isRecording = false;
//        }
//
//        if (surfaceView != null) {
//            Log.i(TAG, "remove surfaceView");
//            surfaceView.releaseCamera();
//            try {
//                windowManager.removeView(surfaceView);
//            } catch (Exception e) {
//                Log.w(TAG, e.getMessage());
//            }
//            Log.i(TAG, "remove surfaceView over");
//            surfaceView = null;
//        }
//
//        if (mCamera != null) {
//            mCamera.lock();           // lock camera for later use
//            startPreviewWithBuffer();
//        } else {
//            Log.e(TAG, "camera is null!");
//        }
//    }

//    private void moveToUploadDir(final File newVideoFile) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "move video from " + BabyActiveFile.FILES_TEMP_DIR + " to " + BabyActiveFile.FILES_DIR);
//                if (newVideoFile != null && newVideoFile.exists()) {
//                    File newDir = new File(newVideoFile.getAbsolutePath().replace(BabyActiveFile.FILES_TEMP_DIR, BabyActiveFile.FILES_DIR));
//                    BabyActiveFile.getInstance().setCreatingFile(true);
//                    FileUtil.moveFile(newVideoFile, newDir);
//                    BabyActiveFile.getInstance().setCreatingFile(false);
//                }
//
//            }
//        }).start();
//    }

        /**
         * 清空未完整录制10秒的临时视频文件
         */
//    private void clearTempFiles() {
//        Log.i(TAG, "clearTempAndAbandonFiles");
//        FileUtil.deleteFile(new File(BabyActiveFile.FILES_TEMP_DIR));
//        FileUtil.deleteFile(new File(Environment.getExternalStorageDirectory() + "/pudding_plus/babyActive_abandon"));
//    }


    }

    interface MediaRecorderPrepareCallback {
        void onSucc();

        void onFail();
    }

    public interface OnVideoingListener {
        void onVideoStart();

        void onVideoStop();
    }

    public interface VideoCaptureListener {
        void onCaptureData(byte[] data, int width, int height);
    }
}
