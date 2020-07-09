package com.walnutin.hardsdktest;

import android.app.Application;
import android.content.Context;

import com.walnutin.hardsdk.ProductList.sdk.HardSdk;

import java.util.TimeZone;

/**
 * Created by chenliu on 2017/4/14.
 */

public class MyApplication extends Application {

    static public Context context;
    static  public String deviceName;
    static  public String deviceAddr;

    @Override
    public void onCreate() {
        super.onCreate();
        HardSdk.getInstance().init(this);
        context = this;
        TimeZone tz = TimeZone.getDefault();
        int gmt = (tz.getRawOffset() / 60000)/60;
        System.out.println("gmt:"+gmt);
    }

    public static Context getContext() {
        return context;
    }
}
