package com.roobo.vision.facedetect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiwoo on 2019. 9. 9..
 */
public class FaceDet {
    protected static boolean sInitialized = false;
    static {
        try {
            System.loadLibrary("face_det");
            jniNativeClassInit();
            sInitialized = true;
        } catch (UnsatisfiedLinkError e) {

        }
    }
    //初始化人脸检测模型，1返回成功，0返回失败
    public int faceDetModelInit() {

        jniModelInit("");

        return 1;
    }
    //人脸检测,返回人脸检测结果，yuv图像数组，w图像宽，h图像高，angle 图像旋转角度，isflip是否镜像
    public List<FaceRect> detBitmapFace(byte []yuv, int w, int h, int angle, int isflip) {
        List<FaceRect> faceList = new ArrayList<FaceRect>();
        int size = jniFaceDect(yuv, w, h, angle,isflip,2);
        for (int i = 0; i != size; i++) {
            FaceRect faceRect = new FaceRect();
            long  time1=System.currentTimeMillis();
            int success = jniGetFaceRet(faceRect, i);
            long  time2=System.currentTimeMillis();
//            Log.e("wangqi", "pose-time:" + (time2-time1));

            if (success >= 0) {
                faceList.add(faceRect);
            }
        }
        return faceList;
    }
    private native static void jniNativeClassInit();

    private native int jniGetFaceRet(FaceRect faceRect, int index);
    private native int jniFaceDect(byte[] image,int w,int h,int angle,int isflip,int mode);
    private native void  jniModelInit(String landmarkPath);

}
