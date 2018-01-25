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
                AnimationUtils.loadAnimation(context, R.anim.gr_top_appear),
                AnimationUtils.loadAnimation(context, R.anim.gr_down_appear)
        };
        int[]ass=new int[4];
        int b=4;
        for(int i=0;i<b;i++){
            ass[i]=i;
        }
    }
}
