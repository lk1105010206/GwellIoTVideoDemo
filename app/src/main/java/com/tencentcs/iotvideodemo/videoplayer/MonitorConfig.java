package com.tencentcs.iotvideodemo.videoplayer;

import android.content.Context;

import com.tencentcs.iotvideodemo.settings.DeviceSettingsSPUtils;

import java.io.Serializable;

import androidx.annotation.NonNull;

public class MonitorConfig implements Serializable {
    public boolean supportTalk;
    public boolean supportCamera;
    public boolean useMediaCodecAudioDecode;
    public boolean useMediaCodecVideoDecode;
    public boolean useMediaCodecAudioEncode;

    public boolean useTencentAEC;

    public boolean useSystemAEC;

    public boolean useTalkAGC;

    public boolean saveTalkPCMFile;

    public boolean saveTalkFromP2PPCMFile;

    public boolean saveTalkAudioRawFile;

    public boolean saveDevAudioRawFile;

    public boolean saveDevAudioPCMFile;

    public boolean saveDevVideoRawFile;

    public boolean saveDevVideoYUVFile;

    public int definition;
    public short sourceId;

    public static MonitorConfig defaultConfig(Context context) {
        MonitorConfig config = new MonitorConfig();
        config.supportTalk = DeviceSettingsSPUtils.getInstance().supportAudioTalk(context);
        config.supportCamera = DeviceSettingsSPUtils.getInstance().supportVideoTalk(context);
        config.useMediaCodecAudioDecode = DeviceSettingsSPUtils.getInstance().media_decode_audio(context);
        config.useMediaCodecVideoDecode = DeviceSettingsSPUtils.getInstance().media_decode_video(context);
        config.useMediaCodecAudioEncode = DeviceSettingsSPUtils.getInstance().media_encode_audio(context);
        config.definition = DeviceSettingsSPUtils.getInstance().default_definition(context);
        config.sourceId = DeviceSettingsSPUtils.getInstance().default_sourceId(context);
        config.useTencentAEC = DeviceSettingsSPUtils.getInstance().openTencentAEC(context);
        config.useSystemAEC = DeviceSettingsSPUtils.getInstance().openSystemAEC(context);
        config.useTalkAGC = DeviceSettingsSPUtils.getInstance().openAudioAGC(context);
        config.saveTalkPCMFile = DeviceSettingsSPUtils.getInstance().audioRecordPcm(context);
        config.saveTalkFromP2PPCMFile = DeviceSettingsSPUtils.getInstance().audioRecordP2PPcm(context);
        config.saveTalkAudioRawFile = DeviceSettingsSPUtils.getInstance().audioRecordRaw(context);
        config.saveDevAudioRawFile = DeviceSettingsSPUtils.getInstance().audioReceiveRaw(context);
        config.saveDevAudioPCMFile = DeviceSettingsSPUtils.getInstance().audioReceivePcm(context);
        config.saveDevVideoRawFile = DeviceSettingsSPUtils.getInstance().videoReceiveRaw(context);
        config.saveDevVideoYUVFile = DeviceSettingsSPUtils.getInstance().videoReceiveYuv(context);

        return config;
    }

    public static MonitorConfig simpleConfig(Context context) {
        MonitorConfig config = MonitorConfig.defaultConfig(context);
        config.supportCamera = false;
        config.sourceId = 0;

        return config;
    }

    public static MonitorConfig simpleConfig(Context context, short defaultSourceId) {
        MonitorConfig config = MonitorConfig.defaultConfig(context);
        config.supportCamera = false;
        config.sourceId = defaultSourceId;

        return config;
    }

    public static boolean compare(MonitorConfig config1, MonitorConfig config2) {
        if (config1 == null || config2 == null) {
            return false;
        }
        return config1.supportTalk == config2.supportTalk
                && config1.supportCamera == config2.supportCamera
                && config1.useMediaCodecAudioDecode == config2.useMediaCodecAudioDecode
                && config1.useMediaCodecVideoDecode == config2.useMediaCodecVideoDecode
                && config1.useMediaCodecAudioEncode == config2.useMediaCodecAudioEncode
                && config1.useTencentAEC == config2.useTencentAEC
                && config1.useSystemAEC == config2.useSystemAEC
                && config1.useTalkAGC == config2.useTalkAGC

                && config1.saveTalkPCMFile == config2.saveTalkPCMFile
                && config1.saveTalkAudioRawFile == config2.saveTalkAudioRawFile
                && config1.saveDevAudioRawFile == config2.saveDevAudioRawFile
                && config1.saveDevAudioPCMFile == config2.saveDevAudioPCMFile
                && config1.saveDevVideoRawFile == config2.saveDevVideoRawFile
                && config1.saveDevVideoYUVFile == config2.saveDevVideoYUVFile

                && config1.definition == config2.definition
                && config1.sourceId == config2.sourceId;
    }

    @Override
    public String toString() {
        return "MonitorConfig{" +
                "supportTalk=" + supportTalk +
                ", supportCamera=" + supportCamera +
                ", useMediaCodecAudioDecode=" + useMediaCodecAudioDecode +
                ", useMediaCodecVideoDecode=" + useMediaCodecVideoDecode +
                ", useMediaCodecAudioEncode=" + useMediaCodecAudioEncode +
                ", useTencentAEC=" + useTencentAEC +
                ", useSystemAEC=" + useSystemAEC +
                ", useTalkAGC=" + useTalkAGC +
                ", saveTalkPCMFile=" + saveTalkPCMFile +
                ", saveTalkFromP2PPCMFile=" + saveTalkFromP2PPCMFile +
                ", saveTalkAudioRawFile=" + saveTalkAudioRawFile +
                ", saveDevAudioRawFile=" + saveDevAudioRawFile +
                ", saveDevAudioPCMFile=" + saveDevAudioPCMFile +
                ", saveDevVideoRawFile=" + saveDevVideoRawFile +
                ", saveDevVideoYUVFile=" + saveDevVideoYUVFile +
                ", definition=" + definition +
                ", sourceId=" + sourceId +
                '}';
    }
}
