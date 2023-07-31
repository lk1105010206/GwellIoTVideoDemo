package com.tencentcs.iotvideodemo.netconfig;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.tencentcs.iotvideo.IoTVideoSdk;
import com.tencentcs.iotvideo.accountmgr.AccountMgr;
import com.tencentcs.iotvideo.messagemgr.DataMessage;
import com.tencentcs.iotvideo.netconfig.DeviceInfo;
import com.tencentcs.iotvideo.netconfig.NetConfig;
import com.tencentcs.iotvideo.netconfig.NetConfigResult;
import com.tencentcs.iotvideo.netconfig.data.NetMatchTokenResult;
import com.tencentcs.iotvideo.netconfig.wired.WiredNetConfig;
import com.tencentcs.iotvideo.utils.IoTP2PUtils;
import com.tencentcs.iotvideo.utils.JSONUtils;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideo.utils.rxjava.IResultListener;
import com.tencentcs.iotvideodemo.BuildConfig;
import com.tencentcs.iotvideodemo.base.HttpRequestState;
import com.tencentcs.iotvideodemo.base.MVVMSubscriberListener;
import com.tencentcs.iotvideodemo.utils.DeviceUtils;
import com.tencentcs.iotvideodemo.utils.Utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

class NetConfigHelper {
    private static final String TAG = "NetConfigHelper";

    private NetConfigViewModel mNetConfigViewModel;

    NetConfigHelper(NetConfigViewModel model) {
        mNetConfigViewModel = model;
    }

    void bindDevice(String devId, MutableLiveData<HttpRequestState> httpRequestStateMutableLiveData) {
        bindDevice(devId, null, null, httpRequestStateMutableLiveData);
    }

    void bindDevice(String devId, String devId2, String bindToken, MutableLiveData<HttpRequestState> httpRequestStateMutableLiveData) {
        LogUtils.i(TAG, "bindDevice devId:" + devId + "; devId2:" + devId2 + "; bindToken:" + bindToken);
        MVVMSubscriberListener mvvmSubscriberListener = new MVVMSubscriberListener(httpRequestStateMutableLiveData) {
            @Override
            public void onSuccess(@NonNull JsonObject response) {
                super.onSuccess(response);
                BindDeviceResult bindDeviceResult = JSONUtils.JsonToEntity(response.toString(),
                        BindDeviceResult.class);
                NetConfig.getInstance().subscribeDevice(bindDeviceResult.getData().getDevToken(), DeviceUtils.isValidTid(devId) ? devId : devId2);
            }
        };

        if (Utils.isOemVersion()) {
            AccountMgr.getHttpService().deviceBind(devId2, devId, devId2, 0, true, mvvmSubscriberListener);
        } else {
            // 如果tid有效，则直接使用使用tid绑定，如果tid无效，且did有效，则使用did绑定，did格式需要做一定的转换，格式：$+32进制did字符串
            if (DeviceUtils.isValidTid(devId)) {
                AccountMgr.getHttpService().deviceBind(devId, true, bindToken, mvvmSubscriberListener);
            } else if (!TextUtils.isEmpty(devId2)) {
                Long did;
                try {
                    did = Long.valueOf(devId2);
                } catch (Exception exception) {
                    did = 0L;
                    LogUtils.e(TAG, "bindDevice exception:" + exception.getMessage());
                }

                if (did != 0) {
                    String formatDid = "$" + IoTP2PUtils.decimalTo32Str(did);
                    LogUtils.d(TAG, "formatDid:" + formatDid);
                    AccountMgr.getHttpService().deviceBind(formatDid, true, bindToken, mvvmSubscriberListener);
                    return;
                }
            } else {
                LogUtils.e(TAG, "bindDevice failure:input params is invalid, devId:" + devId + "; devId2:" + devId2);
            }
        }
    }

    void findDevices() {
        IoTVideoSdk.getNetConfig().newWiredNetConfig().getDeviceList(new WiredNetConfig.FindDeviceCallBack() {
            @Override
            public void onResult(DeviceInfo[] deviceInfos) {
                if(deviceInfos != null){
                    mNetConfigViewModel.getLanDeviceData().setValue(deviceInfos);
                    for (DeviceInfo deviceInfo : deviceInfos) {
                        LogUtils.d(TAG, "findDevices " + deviceInfo);
                    }
                } else {
                    mNetConfigViewModel.getLanDeviceData().setValue(new DeviceInfo[0]);
                }
            }
        });
    }

    void getNetConfigToken(String accessId,IResultListener<NetMatchTokenResult> listener) {
        IoTVideoSdk.getNetConfig().getNetConfigToken(accessId,null, listener);
    }
}
