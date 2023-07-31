package com.tencentcs.iotvideodemo.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceSettingsSPUtils {
    private static final String SP_FILE_TEMP = "com.tencentcs.iotvideodemo_preferences";

    public static final String supportAudioTalk = "supportAudioTalk";
    public static final String supportVideoTalk = "supportVideoTalk";

    public static final String OPEN_TENCENT_AEC = "open_tencent_aec";

    public static final String OPEN_SYSTEM_AEC = "open_system_aec";

    public static final String OPEN_AUDIO_AGC = "open_audio_agc";

    public static final String media_decode_audio = "media_decode_audio";
    public static final String media_decode_video = "media_decode_video";
    public static final String media_encode_audio = "media_encode_audio";
    public static final String default_definition = "default_definition";
    public static final String default_sourceId = "default_sourceId";

    public static final String AV_DEBUG_AUDIO_RECORD_PCM = "av_debug_audio_record_pcm";

    public static final String AV_DEBUG_AUDIO_RECORD_P2P_PCM = "av_debug_audio_record_p2p_pcm";

    public static final String AV_DEBUG_AUDIO_RECORD_RAW = "av_debug_audio_record_raw";

    public static final String AV_DEBUG_AUDIO_RECEIVE_RAW = "av_debug_audio_receive_raw";

    public static final String AV_DEBUG_AUDIO_RECEIVE_PCM = "av_debug_audio_receive_pcm";

    public static final String AV_DEBUG_VIDEO_RECEIVE_RAW = "av_debug_video_receive_raw";

    public static final String AV_DEBUG_VIDEO_RECEIVE_YUV = "av_debug_video_receive_yuv";


    private static class SPHolder {
        private static final DeviceSettingsSPUtils INSTANCE = new DeviceSettingsSPUtils();
    }

    public static DeviceSettingsSPUtils getInstance() {
        return SPHolder.INSTANCE;
    }

    public boolean supportAudioTalk(Context context) {
        return getBoolean(context, supportAudioTalk, false);
    }

    public boolean supportVideoTalk(Context context) {
        return getBoolean(context, supportVideoTalk, false);
    }

    public boolean media_decode_audio(Context context) {
        return getBoolean(context, media_decode_audio, false);
    }

    public boolean media_decode_video(Context context) {
        return getBoolean(context, media_decode_video, false);
    }

    public boolean media_encode_audio(Context context) {
        return getBoolean(context, media_encode_audio, false);
    }

    public boolean audioRecordPcm(Context context) {
        return getBoolean(context, AV_DEBUG_AUDIO_RECORD_PCM, false);
    }
    public boolean audioRecordP2PPcm(Context context) {
        return getBoolean(context, AV_DEBUG_AUDIO_RECORD_P2P_PCM, false);
    }

    public boolean audioRecordRaw(Context context) {
        return getBoolean(context, AV_DEBUG_AUDIO_RECORD_RAW, false);
    }

    public boolean audioReceiveRaw(Context context) {
        return getBoolean(context, AV_DEBUG_AUDIO_RECEIVE_RAW, false);
    }
    public boolean audioReceivePcm(Context context) {
        return getBoolean(context, AV_DEBUG_AUDIO_RECEIVE_PCM, false);
    }

    public boolean videoReceiveRaw(Context context) {
        return getBoolean(context, AV_DEBUG_VIDEO_RECEIVE_RAW, false);
    }

    public boolean videoReceiveYuv(Context context) {
        return getBoolean(context, AV_DEBUG_VIDEO_RECEIVE_YUV, false);
    }

    public boolean openTencentAEC(Context context) {
        return getBoolean(context, OPEN_TENCENT_AEC, false);
    }

    public boolean openSystemAEC(Context context) {
        return getBoolean(context, OPEN_SYSTEM_AEC, false);
    }

    public boolean openAudioAGC(Context context) {
        return getBoolean(context, OPEN_AUDIO_AGC, false);
    }

    public int default_definition(Context context) {
        String definition =  getString(context, default_definition, "2");
        int result = 0;
        try {
            result = Integer.parseInt(definition);
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public short default_sourceId(Context context) {
        String sourceId = getString(context, default_sourceId, "0");
        short result = 0;
        try {
            result = Short.parseShort(sourceId);
        }catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getString(Context context, String key, String defaultValue) {
        SharedPreferences sf = context.getSharedPreferences(SP_FILE_TEMP, Context.MODE_PRIVATE);
        return sf.getString(key, defaultValue);
    }

    public boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sf = context.getSharedPreferences(SP_FILE_TEMP, Context.MODE_PRIVATE);
        return sf.getBoolean(key, defaultValue);
    }

    public void clear(Context context) {
        SharedPreferences sf = context.getSharedPreferences(SP_FILE_TEMP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        editor.clear();
        editor.apply();
    }
}
