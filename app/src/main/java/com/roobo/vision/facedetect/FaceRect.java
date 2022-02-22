package com.roobo.vision.facedetect;

/**
 * Created by wangqi on 16-9-21.
 */
public class FaceRect {
    private String mPointList;
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    private int mFaceId;
    private float mRoll;
    private float mPith;
    private float mYaw;


    FaceRect() {
    }
    /**
     *
     * @param pointList face point List
     * @param l The X coordinate of the left side of the result
     * @param t The Y coordinate of the top of the result
     * @param r The X coordinate of the right side of the result
     * @param b The Y coordinate of the bottom of the result
     */
    public FaceRect(String pointList, int faceId, int l, int t, int r, int b, float roll, float pith, float yaw) {
        mPointList = pointList;
        mFaceId=faceId;
        mLeft = l;
        mTop = t;
        mRight = r;
        mBottom = b;
        mRoll=roll;
        mPith=pith;
        mYaw=yaw;
    }

    /**
     * @return The X coordinate of the left side of the result
     */

    public float getroll() {
        return mRoll;
    }
    public float getPith() {
        return mPith;
    }
    public float getYaw() {
        return mYaw;
    }


    public int getLeft() {
        return mLeft;
    }

    /**
     * @return The Y coordinate of the top of the result
     */
    public int getTop() {
        return mTop;
    }

    /**
     * @return The X coordinate of the right side of the result
     */
    public int getRight() {
        return mRight;
    }

    /**
     * @return The Y coordinate of the bottom of the result
     */
    public int getBottom() {
        return mBottom;
    }

    /**
     * @return  A mFacdId of the face.
     */
    public int getFaceId() {
        return mFaceId;
    }
    /**
     *
     * @return The Feature PointList of the face
     */
    public String getPointList() {
        return mPointList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Left:");
        sb.append(mLeft);
        sb.append(", Top:");
        sb.append(mTop);
        sb.append(", Right:");
        sb.append(mRight);
        sb.append(", Bottom:");
        sb.append(mBottom);
        sb.append(", Label:");
        sb.append(mPointList);
        return sb.toString();
    }
}