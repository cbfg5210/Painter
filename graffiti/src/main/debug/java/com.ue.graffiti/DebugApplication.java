package com.ue.graffiti;

import android.app.Application;

import com.ue.graffiti.util.SPUtils;

/**
 * Created by hawk on 2018/1/23.
 */

public class DebugApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this);
    }
}
