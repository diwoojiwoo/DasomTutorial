package com.roobo.vision.face.detection;

import android.content.Context;

import com.roobo.vision.facedetect.FaceDet;
import com.roobo.vision.facedetect.FaceRect;
import com.roobo.vision.util.SingleThreadExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianli on 16-6-22.
 */
public class FaceDetector {
    public List<FaceRect> mFaceList;
    private SingleThreadExecutor mExecutor;
    private Context mContext;
    private FaceDet mDetector = null;
    private Object mDataLock = new Object();
    private byte[] mCameraData;
    private int mFrameWidth;
    private int mFrameHeight;
    private boolean mDectecting = false;

    private int mCount = 0;
    private FaceDetectListener mListener;

    private Runnable mInitRunner = new Runnable() {
        @Override
        public void run() {
            mDetector = new FaceDet();
            mDetector.faceDetModelInit();
        }
    };
    private Runnable mDetectRunner = new Runnable() {
        @Override
        public void run() {

            byte[] data;
            int width, height;
            synchronized (mDataLock) {
                data = mCameraData;
                width = mFrameWidth;
                height = mFrameHeight;
            }
            if (data == null) {
                return;
            }

            mDectecting = true;
            List<FaceRect> faceList = mDetector.detBitmapFace(data, width, height, 0, 1);
            List<String> nameList = new ArrayList<>();

            mFaceList = faceList;
            if (faceList.size() == 0) {
                mCount++;
                if (mCount > 3) {
                    notifyNoFace();
                }
            } else {
                mCount = 0;
//                notifyFace(mFaceList, width, height, nameList);
                notifyFace(data, width, height);
            }
            mDectecting = false;
        }
    };

    public FaceDetector(Context context) {
        mExecutor = new SingleThreadExecutor("FaceDetector");
        mContext = context.getApplicationContext();
        mExecutor.execute(mInitRunner);
    }

    public void detect(byte[] data, final int width, final int height) {
        if (!mDectecting) {
            synchronized (mDataLock) {
                mCameraData = data;
//                Log.d(TAG, "mFrameWidth : " + mFrameWidth + ", mFrameHeight: " +mFrameHeight);

                mFrameWidth = width;
                mFrameHeight = height;
            }
            mExecutor.execute(mDetectRunner);
        } else {
//            Log.d(TAG, "detect is doing! do nothing!");
        }
    }

    public void setFaceListener(FaceDetectListener l) {
        this.mListener = l;
    }


    private void notifyFace(List<FaceRect> list, int imageWidth, int imageHeight, List<String> nameList) {
        FaceDetectListener l = mListener;
        if (l != null) {
            l.onFace(list, imageWidth, imageHeight, nameList);
        }
    }

    private void notifyFace(byte[] data, final int width, final int height) {
        FaceDetectListener l = mListener;
        if (l != null) {
            l.onFace(data, width, height);
        }
    }

    private void notifyNoFace() {
        FaceDetectListener l = mListener;
        if (l != null) {
            l.onNoFace();
        }
    }

    public interface FaceDetectListener {

        void onFace(byte[] data, final int width, final int height);

        void onFace(List<FaceRect> featureList, int imageWidth, int imageHeight, List<String> nameList);

        void onNoFace();
    }
}