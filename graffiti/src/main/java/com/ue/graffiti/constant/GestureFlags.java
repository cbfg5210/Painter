package com.ue.graffiti.constant;

/**
 * Created by hawk on 2018/1/18.
 */

public interface GestureFlags {
    float MIN_ZOOM = 10f; //缩放下限
    float MAX_DY = 70f; //缩放与旋转切换上限
    int NONE = 0; // 平移操作
    int DRAG = 1; // 平移操作
    int ZOOM = 2; // 缩放操作
    int ROTATE = 3; // 旋转操作
}