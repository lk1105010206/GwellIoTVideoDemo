package com.tencentcs.iotvideodemo.utils;

import android.text.TextUtils;

import com.tencentcs.iotvideo.utils.LogUtils;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2022/3/22 15:02
 * @description:处理设备属性相关的工具类
 */
public final class DeviceUtils {

    private final static String TAG = "DeviceUtils";
    /**
     * 判断是否有效的tid.
     *
     * @param tid tid
     * @return true：有效的tid；false:无效的tid
     */
    public static boolean isValidTid(String tid) {
        if (TextUtils.isEmpty(tid)) {
            return false;
        }
        long testValue;
        try {
            testValue = Long.parseLong(tid);
        } catch (Exception exception) {
            testValue = -1;
            LogUtils.e(TAG, "isValidTid exception:" + exception.getMessage());
        }
        return testValue != 0;
    }
}
