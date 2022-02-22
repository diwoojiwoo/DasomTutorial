package com.roobo.vision.capture;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by macro on 16/9/26.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    PreviewCallback mPreviewCallback;

    interface PreviewCallback {
        void onCreated(SurfaceHolder holder);
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraPreview(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        mHolder = getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
//        Log.e(TAG, "surfaceCreated");
        try {
            if(mCamera == null)
                return;
            
            mCamera.setPreviewDisplay(holder);

            if (mPreviewCallback != null){
                mPreviewCallback.onCreated(holder);
            }

        } catch (IOException e) {
//            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.e(TAG, "surfaceDestroyed");

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        Log.e(TAG, "surfaceChanged");
        if (mHolder.getSurface() == null || mCamera == null)
            return;

        Camera.Parameters e1 = mCamera.getParameters();
        e1.setPictureSize(1280, 720);
        e1.setFlashMode("off");
        e1.setPictureFormat(256);
        e1.setPreviewFormat(17);
        e1.setRecordingHint(true);
        e1.setFlashMode("off");
        e1.setWhiteBalance("auto");
        e1.setSceneMode("auto");
        e1.setFocusMode("auto");
        e1.setPreviewFrameRate(30);
        mCamera.setDisplayOrientation(0);
        mCamera.setParameters(e1);

        try {
            mCamera.startPreview();
//            Log.e(TAG, "startPreview called!");
        } catch (Exception e) {
//            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCamera = null;
    }
}
