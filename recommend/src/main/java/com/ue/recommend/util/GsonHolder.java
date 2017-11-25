package com.ue.recommend.util;

/**
 * Created by hawk on 2017/4/9.
 */

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class GsonHolder {
    private static final Gson gson;

    static {
        gson = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? new Gson() :
                new GsonBuilder()
                        .excludeFieldsWithModifiers(
                                Modifier.FINAL,
                                Modifier.TRANSIENT,
                                Modifier.STATIC)
                        .create();
    }

    private GsonHolder() {
        throw new UnsupportedOperationException();
    }

    public static Gson getGson() {
        return gson;
    }
}

