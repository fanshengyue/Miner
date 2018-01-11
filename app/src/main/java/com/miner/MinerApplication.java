package com.miner;

import android.app.Application;

import com.miner.utils.CrashHandler;

/**
 * Created by fanshengyue on 2017/11/28.
 */

public class MinerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
