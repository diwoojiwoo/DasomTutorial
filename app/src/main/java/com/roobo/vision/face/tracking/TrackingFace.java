package com.roobo.vision.face.tracking;

import android.content.Context;
import android.util.Log;

import com.roobo.hardware.MotionCtrlManager;
import com.roobo.vision.facedetect.FaceRect;

import java.util.List;

public class TrackingFace {
    private static final String TAG = "TrackingFace";

    private static final int LOWEST_DEGREE = 10;

    private Context mContext;
    private MotionCtrlManager mMotionCtrl;

    public TrackingFace(Context context) {
        mContext = context.getApplicationContext();
        mMotionCtrl = MotionCtrlManager.getInstance();
        mMotionCtrl.connect();
        mMotionCtrl.reset(0);
    }

    public void trackFace(List<FaceRect> faceLists) {

        FaceRect faceRect = getBigFace(faceLists);

        if (faceRect == null) {
            Log.e(TAG, "faceList get rect error! return");
            return;
        }

        float lev_center = (faceRect.getLeft() + faceRect.getRight()) / (float) 2;

        int mid = 319/2;
        float f = 565/2;
        double angle_in_rad = Math.atan2(lev_center - mid, f);
        double angle_in_degree = Math.toDegrees(angle_in_rad);

        double move_scale = 0.8;
        Double relative_angle = move_scale * angle_in_degree;
        Double msPerDegree = 26 - 10 * angle_in_degree / 50;
        int degreeInt = relative_angle.intValue();
        Log.e(TAG, "current degree is:" + relative_angle + " int value:" + degreeInt + " msPerDegree:" + msPerDegree);
        if (Math.abs(relative_angle) > LOWEST_DEGREE) {
            mMotionCtrl.turn(degreeInt, msPerDegree.intValue());
        } else {
            Log.e(TAG, "degree is below " + LOWEST_DEGREE + " do nothing!");
//            mMotionCtrl.turnLeftRelative(0, msPerDegree.intValue());
        }
    }

    private FaceRect getBigFace(List<FaceRect> rets) {
        FaceRect bigFace = null;
        if (rets != null && rets.size() > 0) {
            for (FaceRect ret : rets) {
                if (bigFace != null) {
                    if (Math.abs((bigFace.getTop() - bigFace.getBottom())) < Math.abs((ret.getTop() - ret.getBottom()
                    ))) {
                        bigFace = ret;
                    }
                } else {
                    bigFace = ret;
                }
            }
        }

        return bigFace;
    }
}
