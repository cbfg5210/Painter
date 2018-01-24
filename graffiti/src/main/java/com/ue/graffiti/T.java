package com.ue.graffiti;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by hawk on 2018/1/24.
 */

public class T {
    private void a(Context context){
        Animation[]a=new Animation[]{
                AnimationUtils.loadAnimation(context, R.anim.topappear),
                AnimationUtils.loadAnimation(context, R.anim.downappear)
        };
        int[]as=new int[4];
        for(int i=0;i<as.length;i++){
            as[i]=i;
        }
    }
}
