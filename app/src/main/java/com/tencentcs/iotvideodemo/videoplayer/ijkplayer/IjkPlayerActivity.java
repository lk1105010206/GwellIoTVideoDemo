package com.tencentcs.iotvideodemo.videoplayer.ijkplayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideo.utils.NetUtils;
import com.tencentcs.iotvideo.utils.rxjava.IResultListener;
import com.tencentcs.iotvideo.vas.utils.IoTHLSUtils;
import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.base.BaseActivity;
import com.tencentcs.iotvideodemo.vas.PlaybackListFragment;
import com.tencentcs.iotvideodemo.videoplayer.ijkplayer.media.IRenderView;
import com.tencentcs.iotvideodemo.videoplayer.ijkplayer.media.IjkVideoView;
import com.tencentcs.iotvideodemo.videoplayer.ijkplayer.media.PlayerManager;

import java.util.Arrays;

public class IjkPlayerActivity extends BaseActivity {
    private static final String TAG = "IjkPlayerActivity";

    private IjkVideoView mVideoView;
    private TextView mTvUrl, mTvPlayStatus;
    private PlayerManager mPlayer;
    private TextView mTvSpeedPlayDuration;

    private String mFileUrl;

    private long speedDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijk_player);

        mVideoView = findViewById(R.id.video_view);

        mFileUrl = getIntent().getStringExtra("URI");

        speedDuration = getIntent().getLongExtra(PlaybackListFragment.KEY_SPEED_DURATION, 0);

        LogUtils.i(TAG, "mFileUrl = " + mFileUrl);
        mTvUrl = findViewById(R.id.tv_url);
        mTvUrl.setText(mFileUrl);
        mTvPlayStatus = findViewById(R.id.tv_play_status);
        mTvPlayStatus.setText("加载中...");
        mTvSpeedPlayDuration = findViewById(R.id.tv_play_speed_duration);

        mVideoView.setOnPreparedListener(iMediaPlayer -> mTvPlayStatus.setText("播放中"));
        if (speedDuration <= 0) {
            mTvSpeedPlayDuration.setVisibility(View.GONE);
        }else{
            mTvSpeedPlayDuration.setVisibility(View.VISIBLE);
            mTvSpeedPlayDuration.setText(getString(R.string.cloud_speed_play_url_duration, speedDuration));
        }

        /** 普通播放 start **/
        mVideoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        mVideoView.setVideoURI(Uri.parse(mFileUrl));
        mVideoView.start();
        /** 普通播放 end **/

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mVideoView != null){
            mVideoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mVideoView != null){
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mVideoView != null){
            mVideoView.releaseWithoutStop();
        }
    }
}
