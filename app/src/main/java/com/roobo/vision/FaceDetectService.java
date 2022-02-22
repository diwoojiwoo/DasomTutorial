package com.roobo.vision;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.onethefull.dasomtutorial.utils.logger.DWLog;


/**
 * Created by jiwoo on 2019. 9. 10..
 */

public class FaceDetectService extends Service {
    private static final String ACTION_START_TRACK_FACE = "ACTION_START_TRACK_FACE";
    private static final String ACTION_STOP_TRACK_FACE = "ACTION_STOP_TRACK_FACE";
    private static final String ACTION_STOP_EVERYTHING = "ACTION_STOP_EVERYTHING";

    private static final String PARAM_KEY = "param";
    private static final String ACTION_KEY = "action";

    public static final void startTraceFace(Context c) {
        DWLog.INSTANCE.d(" startTraceFace!");
        Intent intent = new Intent(c, FaceDetectService.class);
        Bundle param = new Bundle();
        param.putString(ACTION_KEY, ACTION_START_TRACK_FACE);
        intent.putExtra(PARAM_KEY, param);
        c.startService(intent);
    }

    public static final void stopTraceFace(Context c) {
        DWLog.INSTANCE.e("stopTraceFace!");
        Intent intent = new Intent(c, FaceDetectService.class);
        Bundle param = new Bundle();
        param.putString(ACTION_KEY, ACTION_STOP_TRACK_FACE);
        intent.putExtra(PARAM_KEY, param);
        c.startService(intent);
    }

    public static final void stopAllFaceService(Context c) {
        Intent intent = new Intent(c, FaceDetectService.class);
        Bundle param = new Bundle();
        param.putString(ACTION_KEY, ACTION_STOP_EVERYTHING);
        intent.putExtra(PARAM_KEY, param);
        c.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            handleFaceAction(intent);
        } else {
            DWLog.INSTANCE.e(" onStartCommand intent is null!");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleFaceAction(Intent intent) {
        Bundle bundle = intent.getBundleExtra(PARAM_KEY);
        if (bundle != null) {
            String action = bundle.getString(ACTION_KEY);
            DWLog.INSTANCE.d("action is:" + action);

            if (TextUtils.equals(action, ACTION_START_TRACK_FACE)) {
                WFaceManager.Companion.getInstance().startTrackFace(this);
            } else if (TextUtils.equals(action, ACTION_STOP_TRACK_FACE)) {
                WFaceManager.Companion.getInstance().stopTrackFace();
            } else if (TextUtils.equals(action, ACTION_STOP_EVERYTHING)) {
                stopEveryThing();
            } else {
                DWLog.INSTANCE.e("unKnow action:" + action);
            }
        } else {
            DWLog.INSTANCE.e("handleFaceAction! param is null!");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        DWLog.INSTANCE.e(" onDestroy called!");
        super.onDestroy();
        stopEveryThing();
    }

    private void stopEveryThing() {
        WFaceManager.Companion.getInstance().stopTrackFace();
//        if (wake != null) {
//            wake.release();
//            wake = null;
//        }
//        if (handler != null)
//            handler.removeCallbacks(dismissThread);
    }
}
