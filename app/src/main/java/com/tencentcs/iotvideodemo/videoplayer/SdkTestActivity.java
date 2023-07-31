package com.tencentcs.iotvideodemo.videoplayer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonObject;
import com.tencentcs.iotvideo.constant.ResUploadType;
import com.tencentcs.iotvideo.iotvideoplayer.iotview.IoTVideoRenderView;
import com.tencentcs.iotvideo.iotvideoplayer.player.MonitorPlayer;
import com.tencentcs.iotvideo.messagemgr.SdkHttpViaP2PMgr;
import com.tencentcs.iotvideo.utils.FileIOUtils;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideo.utils.ResUploadUtils;
import com.tencentcs.iotvideo.utils.rxjava.SubscriberListener;
import com.tencentcs.iotvideodemo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 该类供临时调试SDK接口使用.
 */
public class SdkTestActivity extends Activity implements View.OnClickListener {

    private final static String TAG = "SdkTestActivity";
    private String mDeviceId = null;
    private MonitorPlayer monitorPlayer;
    private IoTVideoRenderView mVideoView;

    private SeekBar mSeekBar;

    private int videoAreaWidth;

    private int videoAreaHeight;

    private ValueAnimator zoomMinAnim;

    private ValueAnimator zoomMaxAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sdk_test);
        mDeviceId = getIntent().getStringExtra("deviceID");
        mVideoView = findViewById(R.id.mRenderView);
        findViewById(R.id.bt_upload_res).setOnClickListener(view -> {
            uploadResFile();
        });
        findViewById(R.id.bt_query_all_res_list).setOnClickListener(view -> {
            queryAllRes();
        });
        findViewById(R.id.bt_query_url_by_res_id).setOnClickListener(view -> {
            getResUrlByResId();
        });
        findViewById(R.id.bt_delete_res).setOnClickListener(view -> {
            deleteResFile();
        });
        findViewById(R.id.bt_update_res_info).setOnClickListener(view -> {
            updateResFileInfo();
        });
        mVideoView.setRenderView(IoTVideoRenderView.RENDER_VIEW_TEXTURE_VIEW);

        startPlay();
        mSeekBar = findViewById(R.id.sb_revise_view_size);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float percent = i * 1f/100;
                ConstraintLayout.LayoutParams videoLayoutParams = (ConstraintLayout.LayoutParams)mVideoView.getLayoutParams();
                videoLayoutParams.width = (int)(videoAreaWidth * percent);
                videoLayoutParams.height = (int)(videoAreaHeight * percent);
                LogUtils.d(TAG, "onProgressChanged  width:" + videoAreaWidth * percent + "; height:" + videoAreaHeight * percent);

                mVideoView.setLayoutParams(videoLayoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVideoView.post(() -> {
            videoAreaWidth = mVideoView.getWidth();
            videoAreaHeight = mVideoView.getHeight();

            zoomMinAnim = ValueAnimator.ofInt(videoAreaWidth, (int) (videoAreaWidth * 0.5f));
            zoomMinAnim.addUpdateListener(valueAnimator -> {
                int currentWidth = (int)valueAnimator.getAnimatedValue();
                ConstraintLayout.LayoutParams videoLayoutParams = (ConstraintLayout.LayoutParams)mVideoView.getLayoutParams();
                videoLayoutParams.width = currentWidth;
                videoLayoutParams.height = currentWidth * 9 / 16;
                LogUtils.d(TAG, "zoom min video area width:" + videoLayoutParams.width + "; height:" + videoLayoutParams.height);
                mVideoView.setLayoutParams(videoLayoutParams);
            });

            zoomMaxAnim = ValueAnimator.ofInt((int) (videoAreaWidth * 0.5f), videoAreaWidth);
            zoomMaxAnim.addUpdateListener(valueAnimator -> {
                int currentWidth = (int)valueAnimator.getAnimatedValue();
                ConstraintLayout.LayoutParams videoLayoutParams = (ConstraintLayout.LayoutParams)mVideoView.getLayoutParams();
                videoLayoutParams.width = currentWidth;
                videoLayoutParams.height = currentWidth * 9 / 16;
                LogUtils.d(TAG, "zoom min video area width:" + videoLayoutParams.width + "; height:" + videoLayoutParams.height);
                mVideoView.setLayoutParams(videoLayoutParams);
            });

            zoomMinAnim.setDuration(200);
            zoomMinAnim.setRepeatCount(0);

            zoomMaxAnim.setDuration(200);
            zoomMaxAnim.setRepeatCount(0);
        });

    }

    private void startPlay() {
        monitorPlayer = new MonitorPlayer();

        monitorPlayer.setDataResource(mDeviceId);

        monitorPlayer.setVideoRenderView(mVideoView);
        addPlayerListener();

        monitorPlayer.play();

    }

    private void addPlayerListener() {
        monitorPlayer.setStatusListener(status -> LogUtils.i(TAG,"onStatus:" + status));
        monitorPlayer.setErrorListener(error -> LogUtils.i(TAG,"onError:" + error));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (monitorPlayer != null && !monitorPlayer.isConnectedDevice()) {
            monitorPlayer.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (monitorPlayer != null) {
            monitorPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (monitorPlayer != null) {
            monitorPlayer.setErrorListener(null);
            monitorPlayer.setStatusListener(null);
            monitorPlayer = null;
        }
    }

    private void uploadResFile() {
        LogUtils.d(TAG, "uploadResFile start");
        String filePath = "/sdcard/crc32.rar";
        if (true) {
            zoomMinAnim.start();
            return;
        }

        int fileSize = (int)FileIOUtils.fileLength(filePath);
        LogUtils.d(TAG, "fileSize:" + fileSize);
        ResUploadUtils.uploadResFileToServer(ResUploadType.RES_TYPE_DEV_ALM_SOUND, filePath, -1, "Android测试上传，time：" + new Date().getTime(), "zh-cn", new SubscriberListener() {
            @Override
            public void onStart() {
                LogUtils.i(TAG, "uploadResFile onStart");
            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "uploadResFile onSuccess:" + response.toString());
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.i(TAG, "uploadResFile onFail:" + e.getMessage());
            }
        });
    }

    private void queryAllRes() {
        if (true) {
            zoomMaxAnim.start();
            return;
        }
        List<Integer> resTypes = new ArrayList<>();
        resTypes.add(ResUploadType.RES_TYPE_DEV_ALM_SOUND);
        SdkHttpViaP2PMgr.getInstance().getViaP2PService().queryAllResFileList(10, 0, resTypes, 1, "zh-cn", new SubscriberListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "queryAllRes onSuccess:" + response.toString());
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.i(TAG, "queryAllRes onFail:" + e.getMessage());
            }
        });
    }

    private void getResUrlByResId() {
        List<String> resIds = new ArrayList<>();
        resIds.add("BADH8BoAHAAAALCGAYARAAAA0j+T7QAAAAAAAAAAAAA=");
        SdkHttpViaP2PMgr.getInstance().getViaP2PService().getResFileDownloadUrl(resIds, new SubscriberListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "getResUrlByResId onSuccess:" + response.toString());
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.i(TAG, "getResUrlByResId onFail:" + e.getMessage());
            }
        });
    }

    private void deleteResFile() {
        List<String> resIds = new ArrayList<>();
        resIds.add("BAAAAMfwGgDUBwAABQAAgCMAAADSP5Pt");
        resIds.add("BAAAAMfwGgDUBwAABQAAgCIAAADSP5Pt");
        SdkHttpViaP2PMgr.getInstance().getViaP2PService().deleteResFileAtServer(resIds, new SubscriberListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "deleteResFile onSuccess:" + response.toString());
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.i(TAG, "deleteResFile onFail:" + e.getMessage());
            }
        });
    }

    private void updateResFileInfo() {
        SdkHttpViaP2PMgr.getInstance().getViaP2PService().modifyResFileInfo("BADH8BoAHAAAALCGAYARAAAA0j+T7QAAAAAAAAAAAAA=","Android测试更新描述, time:" + new Date().getTime(), new SubscriberListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "updateResFileInfo onSuccess:" + response.toString());
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.i(TAG, "updateResFileInfo onFail:" + e.getMessage());
            }
        });
    }
}
