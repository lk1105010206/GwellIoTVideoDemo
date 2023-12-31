package com.tencentcs.iotvideodemo.videoplayer.monitor;

import android.Manifest;
import android.content.Context;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencentcs.iotvideo.iotvideoplayer.AvReceiveRateListener;
import com.tencentcs.iotvideo.iotvideoplayer.CallTypeEnum;
import com.tencentcs.iotvideo.iotvideoplayer.IAudioCaptureFilter;
import com.tencentcs.iotvideo.iotvideoplayer.IErrorListener;
import com.tencentcs.iotvideo.iotvideoplayer.IPreparedListener;
import com.tencentcs.iotvideo.iotvideoplayer.IRecordListener;
import com.tencentcs.iotvideo.iotvideoplayer.ISnapShotListener;
import com.tencentcs.iotvideo.iotvideoplayer.IStatusListener;
import com.tencentcs.iotvideo.iotvideoplayer.IUserDataListener;
import com.tencentcs.iotvideo.iotvideoplayer.IoTVideoView;
import com.tencentcs.iotvideo.iotvideoplayer.mediacodec.MediaCodecAudioDecoder;
import com.tencentcs.iotvideo.iotvideoplayer.mediacodec.MediaCodecAudioEncoder;
import com.tencentcs.iotvideo.iotvideoplayer.mediacodec.MediaCodecVideoDecoder;
import com.tencentcs.iotvideo.iotvideoplayer.options.IIoTPlayerOptions;
import com.tencentcs.iotvideo.iotvideoplayer.player.LivePlayer;
import com.tencentcs.iotvideo.iotvideoplayer.render.GLConfigChooser;
import com.tencentcs.iotvideo.messagemgr.AInnerUserDataLister;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideodemo.videoplayer.MonitorConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class IoTMonitorPlayerOwner implements IPreparedListener, IStatusListener,
        IErrorListener, IUserDataListener, MonitorPlayerOwner, AvReceiveRateListener, IAudioCaptureFilter {
    private static final String TAG = "IoTMonitorPlayerOwner";

    private static final SimpleDateFormat mSimpleDateFormat =
            new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);

    private Fragment mViewOwner;
    private LivePlayer mMonitorPlayer;
    private List<IPreparedListener> mPreparedListeners = new ArrayList<>();
    private List<IStatusListener> mStatusListeners = new ArrayList<>();
    private List<IErrorListener> mErrorListeners = new ArrayList<>();
    private List<IUserDataListener> mUserDataListeners = new ArrayList<>();
    private List<RecordListener> mRecordListeners = new ArrayList<>();
    private List<ISnapShotListener> mSnapShotListeners = new ArrayList<>();
    private List<OnSpeedChangedListener> mOnSpeedChangedListener = new ArrayList<>();
    private String mRecordPath;
    private String mSnapPath;
    private Disposable mRequestAudioDis;
    private int mCurrentSpeed;
    private SeekBar mSeekBar;
    private IoTVideoView videoView;

    private int videoAreaWidth;

    private int videoAreaHeight;

    public IoTMonitorPlayerOwner(Fragment fragment, IoTVideoView videoView, String deviceId, MonitorConfig config) {
        mViewOwner = fragment;
        mMonitorPlayer = new LivePlayer();
        if (config != null) {
            mMonitorPlayer.setDataResource(deviceId, config.definition, config.sourceId);
            if (config.useMediaCodecAudioDecode) {
                mMonitorPlayer.setAudioDecoder(new MediaCodecAudioDecoder());
            }
            if (config.useMediaCodecVideoDecode) {
                mMonitorPlayer.setVideoDecoder(new MediaCodecVideoDecoder());
            }
            if (config.useMediaCodecAudioEncode) {
                mMonitorPlayer.setAudioEncoder(new MediaCodecAudioEncoder());
            }

            if (config.useTencentAEC) {
                mMonitorPlayer.setAecSwitch(true, fragment.getActivity().getApplicationContext());
            }

            if (config.useSystemAEC) {
                mMonitorPlayer.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            }

            if (config.useTalkAGC) {
                mMonitorPlayer.setTalkAgcSwitch(true, 20);
            }

            Context appContext = null;
            if (null != fragment && null != fragment.getActivity()) {
                appContext = fragment.getActivity().getApplicationContext();
            }
            if (null == appContext) {
                LogUtils.e(TAG, "IoTMonitorPlayerOwner failure:appContext is null");
                return;
            }
            if (config.saveTalkPCMFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_AUDIO_RECORD_PCM_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "auidio_talk.pcm");
            }
            if (config.saveTalkFromP2PPCMFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_AUDIO_RECORD_FROM_P2P_PCM_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "auidio_talk_from_p2p.pcm");
            }
            if (config.saveTalkAudioRawFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_AUDIO_RECORD_RAW_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "auidio_talk.raw");
            }
            if (config.saveDevAudioRawFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_AUDIO_RECEIVE_RAW_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "auidio_receive_raw.raw");
            }
            if (config.saveDevAudioPCMFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_AUDIO_RECEIVE_PCM_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "auidio_receive_pcm.pcm");

            }
            if (config.saveDevVideoRawFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_VIDEO_RECEIVE_RAW_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "video_receive_raw.raw");

            }
            if (config.saveDevVideoYUVFile) {
                mMonitorPlayer.setOption(IIoTPlayerOptions.Category.PLAYER_OPTION_CATEGORY_AV_DEBUG,
                        IIoTPlayerOptions.OptionName.AV_VIDEO_RECEIVE_YUV_FILE_SAVE_PATH,
                        appContext.getExternalFilesDir("Movies" + File.separator + deviceId).getAbsolutePath() + File.separator + "video_receive_yuv.yuv");

            }
        } else {
            mMonitorPlayer.setDataResource(deviceId, CallTypeEnum.VIDEO_DEFINITION_HD, (short) 0);
        }
//        mMonitorPlayer.setTalkAgcSwitch(true,20);
//        mMonitorPlayer.setAecSwitch(true, fragment.getActivity().getApplicationContext());
//        mMonitorPlayer.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        mMonitorPlayer.setPreparedListener(this);
        mMonitorPlayer.setStatusListener(this);
        mMonitorPlayer.setErrorListener(this);
        mMonitorPlayer.setUserDataListener(this);
        mMonitorPlayer.setVideoView(videoView);
        mMonitorPlayer.setAvReceiveRateListener(this);
        mMonitorPlayer.setConnectDevStateListener(status -> LogUtils.i(TAG, "setConnectDevStateListener onStatus:" + status));
        mMonitorPlayer.setAudioCaptureFilter(this);
        this.videoView = videoView;
        videoView.post(() -> {
            videoAreaWidth = videoView.getWidth();
            videoAreaHeight = videoView.getHeight();
            LogUtils.d(TAG, "videoAreaWidth:" + videoAreaWidth + "; videoAreaHeight:" + videoAreaHeight);
        });
    }

    @Override
    public void onPrepared() {
        LogUtils.i(TAG, "onPrepared");
        for (IPreparedListener listener : mPreparedListeners) {
            listener.onPrepared();
        }
    }

    @Override
    public void onStatus(int status) {
        LogUtils.i(TAG, "onStatus status " + status);
        for (IStatusListener listener : mStatusListeners) {
            listener.onStatus(status);
        }
    }

    @Override
    public void onError(int error) {
        LogUtils.i(TAG, "onError " + error);
        for (IErrorListener listener : mErrorListeners) {
            listener.onError(error);
        }
    }

    @Override
    public void onReceive(byte[] data) {
        LogUtils.d(TAG, "onReceive");
        for (IUserDataListener listener : mUserDataListeners) {
            listener.onReceive(data);
        }
    }

    @Override
    public void addPreparedListener(IPreparedListener listener) {
        mPreparedListeners.add(listener);
    }

    @Override
    public void addStatusListener(IStatusListener listener) {
        mStatusListeners.add(listener);
    }

    @Override
    public void addErrorListener(IErrorListener listener) {
        mErrorListeners.add(listener);
    }

    @Override
    public void addUserDataListener(IUserDataListener listener) {
        mUserDataListeners.add(listener);
    }

    @Override
    public void addRecordListener(RecordListener listener) {
        mRecordListeners.add(listener);
    }

    @Override
    public void addSnapShotListener(ISnapShotListener listener) {
        mSnapShotListeners.add(listener);
    }

    @Override
    public void addOnSpeedChangedListener(OnSpeedChangedListener listener) {
        mOnSpeedChangedListener.add(listener);
    }

    @Override
    public void setRecordPath(String path) {
        mRecordPath = path;
    }

    @Override
    public void setSnapPath(String path) {
        mSnapPath = path;
    }

    @Override
    public void play() {
        mMonitorPlayer.play();
    }

    @Override
    public void stop() {
        mMonitorPlayer.stop();
    }

    @Override
    public void release() {
        mMonitorPlayer.release();
        if (mRequestAudioDis != null && !mRequestAudioDis.isDisposed()) {
            mRequestAudioDis.dispose();
        }
    }

    @Override
    public boolean isMute() {
        return mMonitorPlayer.isMute();
    }

    @Override
    public void mute(boolean on) {
        mMonitorPlayer.mute(on);
    }

    @Override
    public boolean startRecord() {
        if (TextUtils.isEmpty(mRecordPath)) {
            return false;
        }
        boolean success = mMonitorPlayer.startRecord(mRecordPath, mSimpleDateFormat.format(new Date()) + ".mp4",
                new IRecordListener() {
                    @Override
                    public void onResult(int code, String path) {
                        LogUtils.i(TAG, "onResult,code:" + code + "; path:" + path);
                        for (RecordListener listener : mRecordListeners) {
                            listener.onEnd(code, path);
                        }
                    }

                    @Override
                    public void onPositionUpdated(long videoDuration, long audioDuration) {
                        LogUtils.i(TAG, "onPositionUpdated,videoDuration:" + videoDuration + "; audioDuration:" + audioDuration);
                    }

                    @Override
                    public void onStartRecord() {
                        Toast.makeText(mViewOwner.getContext(), "开始录像", Toast.LENGTH_LONG).show();
                    }

                });
        if (success) {
            for (RecordListener listener : mRecordListeners) {
                listener.onStart();
            }
        } else {
            for (RecordListener listener : mRecordListeners) {
                listener.onEnd(-1, "Failed to start recording");
            }
            Toast.makeText(mViewOwner.getContext(), "开启录像失败", Toast.LENGTH_LONG).show();
        }

        return success;
    }

    @Override
    public boolean isRecording() {
        return mMonitorPlayer.isRecording();
    }

    @Override
    public void stopRecord() {
        mMonitorPlayer.stopRecord();
    }

    @Override
    public void snapShot() {
        if (TextUtils.isEmpty(mSnapPath)) {
            return;
        }
        mMonitorPlayer.snapShot(mSnapPath + File.separator + mSimpleDateFormat.format(new Date()) + ".jpeg",
                new ISnapShotListener() {
                    @Override
                    public void onResult(int code, String path) {
                        for (ISnapShotListener listener : mSnapShotListeners) {
                            listener.onResult(code, path);
                        }
                    }
                });
    }

    @Override
    public int getPlayState() {
        return mMonitorPlayer.getPlayState();
    }

    @Override
    public int getVideoWidth() {
        return mMonitorPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mMonitorPlayer.getVideoHeight();
    }

    @Override
    public boolean isPlaying() {
        return mMonitorPlayer.isPlaying();
    }

    @Override
    public void changeDefinition(IoTMonitorControl.Definition definition) {
        switch (definition) {
            case LD:
                mMonitorPlayer.changeDefinition(CallTypeEnum.VIDEO_DEFINITION_FL);
                break;
            case SD:
                mMonitorPlayer.changeDefinition(CallTypeEnum.VIDEO_DEFINITION_SD);
                break;
            case HD:
                mMonitorPlayer.changeDefinition(CallTypeEnum.VIDEO_DEFINITION_HD);
                break;
        }
    }

    @Override
    public void directionCtl(boolean isStart, IoTMonitorControl.Direction direction) {

    }

    @Override
    public void startTalk() {
        RxPermissions rxPermissions = new RxPermissions(mViewOwner);
        boolean isGrant = rxPermissions.isGranted(Manifest.permission.RECORD_AUDIO);
        if (isGrant) {
            //开启后APP开始发送音频数据，若开启前后需发送其他通信数据，如唤醒设备扬声器等，请使用`发送自定义数据`方法，参见`MonitorPlayer.sendUserData`
            mMonitorPlayer.startTalk((code, errorReason) -> {

            });
        } else {
            mRequestAudioDis = rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean granted) throws Exception {
                            if (granted) {
                                mMonitorPlayer.startTalk((code, errorReason) -> {

                                });
                            }
                        }
                    });
        }
    }

    @Override
    public boolean isTalking() {
        return mMonitorPlayer.isTalking();
    }

    @Override
    public void stopTalk() {
        //关闭后APP停止发送音频数据，若关闭前后需发送其他通信数据，如关闭设备扬声器等，请使用`发送自定义数据`方法，参见`MonitorPlayer.sendUserData`
        mMonitorPlayer.stopTalk((code, errorReason) -> {

        });
    }

    @Override
    public int getSpeed() {
        return mMonitorPlayer.getAvBytesPerSec();
    }

    @Override
    public void setInnerUserDataListener(AInnerUserDataLister listener) {
        mMonitorPlayer.setMonitorInnerUserDataLister(listener);
    }

    public void setPlayerView(IoTVideoView view) {
        if (null != mMonitorPlayer) {
            mMonitorPlayer.setVideoView(view);
        }
    }

    @Override
    public void onAvBytesPerSec(int speed) {
        if (speed != mCurrentSpeed) {
            mCurrentSpeed = speed;
            for (OnSpeedChangedListener listener : mOnSpeedChangedListener) {
                listener.onSpeedChanged(speed);
            }
        }
    }

    /**
     * SDK麦克风采集到的音频数据，如果上层需要对采集到的数据进行处理，可以在这个方法中处理
     * */
    @Override
    public byte[] audioFilter(byte[] inputAudio) {
        return inputAudio;
    }

    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
        mSeekBar.setMax(100);
        mSeekBar.setProgress(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float percent = i * 1f/100;
                ConstraintLayout.LayoutParams videoLayoutParams = (ConstraintLayout.LayoutParams)videoView.getLayoutParams();
                videoLayoutParams.width = (int)(videoAreaWidth * percent);
                videoLayoutParams.height = (int)(videoAreaHeight * percent);
                LogUtils.d(TAG, "onProgressChanged  width:" + videoAreaWidth * percent + "; height:" + videoAreaHeight * percent);

                videoView.setLayoutParams(videoLayoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
