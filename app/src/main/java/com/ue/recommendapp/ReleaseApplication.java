package com.ue.recommendapp;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.support.v7.app.AppCompatDelegate;

import com.squareup.leakcanary.LeakCanary;
import com.ue.recommend.util.BmobUtils;
import com.ue.recommend.util.KLog;

import java.util.List;

/**
 * Created by hawk on 2017/8/23.
 */
public class ReleaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 多进程启动会多次调用 Application＃onCreate() 方法，区分进程作初始化操作，能有效减少不必要的开销
        if (getPackageName().equals(getProcessName(this))) {// init for main process
            //support svg for TextView drawableLeft
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            //init bmob
            BmobUtils.getInstance().initBmob("", "");
            //
            KLog.init(true);
            // init debug tools
            initDebugTools();
        } else {
            // init for other process
        }
    }

    /**
     */
    public void initDebugTools() {
        LeakCanary.install(this);
    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoApp = am.getRunningAppProcesses();
        if (infoApp == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : infoApp) {
            if (proInfo.pid == Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return "";
    }
}