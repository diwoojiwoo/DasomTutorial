package com.onethefull.dasomtutorial.utils.touch;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import com.onethefull.dasomtutorial.utils.logger.DWLog;

/**
 * Created by macro on 16/11/22.
 */

public class TouchAreaManager {
    private static final String TAG = "TouchAreaManager";
    // Area IDs
    public static final int CONFUSED = -1; // 困惑
    public static final int LEFT_TOP = 1; // 眼睛往点击区域看
    public static final int LEFT_CENTER = 2; // 眼睛往点击区域看
    public static final int LEFT_BOTTOM = 3; // 眼睛往点击区域看

    public static final int CENTER_TOP = 4; // 眼睛往点击区域看
    public static final int CENTER = 5; // 眼睛往点击区域移动
    public static final int CENTER_BOTTOM = 6; // 眼睛往点击区域看

    public static final int RIGHT_TOP = 7; // 眼睛往点击区域看
    public static final int RIGHT_CENTER = 8; // 眼睛往点击区域看
    public static final int RIGHT_BOTTOM = 9; // 眼睛往点击区域看

    public static final int LEFT_EYE = 10; // 被单击的眼睛闭上，另外一只眼晴好奇
    public static final int RIGHT_EYE = 11; // 被单击的眼睛闭上，另外一只眼晴好奇

    public static final int LEFT_EYE_WITH_RIGHT_EYE = 12; // 两只眼晴都闭上
    public static final int ANGRY = 13; // 生气

    public static final SparseArray<TouchArea> mTouchAreas = new SparseArray<>();

    static {
        mTouchAreas.put(LEFT_TOP, new TouchArea(0, 0, 270, 255));
        mTouchAreas.put(LEFT_CENTER, new TouchArea(0, 255, 270, 465));
        mTouchAreas.put(LEFT_BOTTOM, new TouchArea(0, 465, 270, 720));

        mTouchAreas.put(CENTER_TOP, new TouchArea(270, 0, 1010, 255));
        mTouchAreas.put(CENTER, new TouchArea(460, 255, 820, 465));
        mTouchAreas.put(CENTER_BOTTOM, new TouchArea(270, 465, 1010, 720));

        mTouchAreas.put(RIGHT_TOP, new TouchArea(1010, 0, 1280, 255));
        mTouchAreas.put(RIGHT_CENTER, new TouchArea(1010, 255, 1280, 465));
        mTouchAreas.put(RIGHT_BOTTOM, new TouchArea(1010, 465, 1280, 720));

        mTouchAreas.put(LEFT_EYE, new TouchArea(270, 255, 460, 465));
        mTouchAreas.put(RIGHT_EYE, new TouchArea(820, 255, 1010, 465));
    }

    public static class TouchArea {
        public Rect mRect;

        public TouchArea(int left, int top, int right, int bottom) {
            mRect = new Rect(left, top, right, bottom);
        }

        public boolean contains(int left, int top) {
            return mRect.contains(left, top);
        }

        @Override
        public String toString() {
            return mRect.toShortString();
        }
    }

    public static int parse(int left, int top) {
        DWLog.INSTANCE.d( "left: " + left);
        DWLog.INSTANCE.d( "top: " + top);

        for (int i = 0; i < mTouchAreas.size(); i++) {
            TouchArea touchArea = mTouchAreas.valueAt(i);
            if (touchArea.contains(left, top)) {
                return mTouchAreas.keyAt(i);
            }
        }

        return CONFUSED;
    }

    public static int parse(Point p1, Point p2) {
        TouchArea leftEye = mTouchAreas.get(LEFT_EYE);
        TouchArea rightEye = mTouchAreas.get(RIGHT_EYE);

        if (leftEye.contains(p1.x, p1.y) && rightEye.contains(p2.x, p2.y)
                || leftEye.contains(p2.x, p2.y) && rightEye.contains(p1.x, p1.y)) {
            // 同时按下LEFT_EYE和RIGHT_EYE
            return LEFT_EYE_WITH_RIGHT_EYE;
        }
        return CONFUSED;
    }
}
