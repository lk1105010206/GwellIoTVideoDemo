package com.tencentcs.iotvideodemo.vas;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.tencentcs.iotvideo.utils.JSONUtils;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideo.utils.rxjava.SubscriberListener;
import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.base.BaseFragment;
import com.tencentcs.iotvideodemo.videoplayer.ExoPlayerActivity;
import com.tencentcs.iotvideodemo.videoplayer.ijkplayer.IjkPlayerActivity;
import com.tencentcs.iotvideodemo.widget.ChooseeDialog;
import com.tencentcs.iotvideodemo.widget.RecycleViewDivider;
import com.tencentcs.iotvideodemo.widget.SimpleRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaybackListFragment extends BaseFragment implements View.OnClickListener, SimpleRecyclerViewAdapter.OnItemClickListener{
    private static final String TAG = "PlaybackListFragment";
    public static final String KEY_PLAY_URL = "KEY_play_url";
    public static final String KEY_SPEED_DURATION = "key_speed_duration";

    private List<String> mM3U8List;
    private SimpleRecyclerViewAdapter<String> mAdapter;
    private RecyclerView mRVPlaybackList;
    private CloudStorageActivity mParentInstance;
    private Button mBtRefresh;
    private EditText mEtServiceId;
    private ClipboardManager mClipboardManager;
    private Spinner mSPPlaySpeed;
    /**
     * 倍速链接时长
     * */
    private long speedUrlDuration = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cloud_play_back, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mParentInstance = (CloudStorageActivity) getActivity();
        mM3U8List = new ArrayList<>();
        mAdapter = new SimpleRecyclerViewAdapter<>(getActivity(), mM3U8List);
        mRVPlaybackList = view.findViewById(R.id.playback_list);
        mBtRefresh = view.findViewById(R.id.bt_refresh);
        mEtServiceId = view.findViewById(R.id.et_service_id);
        mSPPlaySpeed = view.findViewById(R.id.sp_play_speed);

        mBtRefresh.setOnClickListener(this);

        mRVPlaybackList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mRVPlaybackList.addItemDecoration(new RecycleViewDivider(getActivity(), RecycleViewDivider.VERTICAL));
        mRVPlaybackList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        mClipboardManager = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item);
        adapter.addAll(getResources().getTextArray(R.array.cloud_play_speed));
        mSPPlaySpeed.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_refresh) {
            mM3U8List.clear();
            queryPlayList(getPlaySpeed(),mParentInstance.getmPlaybackStartTime() / 1000, mParentInstance.getmPlaybackEndTime() / 1000);
        }
    }

    @Override
    public void onRecyclerViewItemClick(int position) {
        if (null != mM3U8List && position >= 0 && position < mM3U8List.size()) {
            ClipData urlData = ClipData.newPlainText("m3u8 url", mM3U8List.get(position));
            mClipboardManager.setPrimaryClip(urlData);
            Snackbar.make(mBtRefresh, "Url已复制", Snackbar.LENGTH_LONG).show();
            startPlayActivity(mM3U8List.get(position));
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }

    private void queryPlayList(final int playSpeed, long startTime, long endTime) {
        LogUtils.i(TAG, "queryPlayList, playSpeed:" +   playSpeed);
        // 获取倍速播放地址
        if (playSpeed > 1) {
            mParentInstance.getmVasService().getVideoSpeedPlayUrl(mParentInstance.getmDevice().getDevId(), startTime, endTime, playSpeed, getCloudServiceId(), new SubscriberListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(@NonNull JsonObject response) {
                    LogUtils.i(TAG, "queryPlayList speed play, onSuccess:" + response.toString());
                    if (!isVisible()) {
                        LogUtils.i(TAG, "view is invisible");
                        return;
                    }
                    CloudSpeedPlayEntity playSpeedEntity = JSONUtils.JsonToEntity(response.toString(), CloudSpeedPlayEntity.class);
                    if (playSpeedEntity == null) {
                        Snackbar.make(mBtRefresh, "invalid data:failure transform to entity", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (playSpeedEntity.getData() != null && !TextUtils.isEmpty(playSpeedEntity.getData().getUrl())) {
                        mM3U8List.clear();
                        mM3U8List.add(playSpeedEntity.getData().getUrl());
                        mAdapter.notifyDataSetChanged();
                        speedUrlDuration = playSpeedEntity.getData().getVideoDuration();
                    } else {
                        Snackbar.make(mBtRefresh, "invalid data", Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFail(@NonNull Throwable e) {
                    LogUtils.i(TAG, "queryPlayList speed play, onFail:" + e.getMessage());
                }
            });
            return;
        }

        speedUrlDuration = 0;
        // 正常1倍速播放
        mParentInstance.getmVasService().getVideoPlayUrl(mParentInstance.getmDevice().getDevId(), startTime, endTime, getCloudServiceId(), new SubscriberListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(@NonNull JsonObject response) {
                LogUtils.i(TAG, "queryPlayList:" + response.toString());
                if (!isVisible()) {
                    LogUtils.i(TAG, "view is invisible");
                    return;
                }
                PlaybackList playbackList = JSONUtils.JsonToEntity(response.toString(), PlaybackList.class);
                if (playbackList == null) {
                    Snackbar.make(mBtRefresh, "invalid data:failure transform to entity", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (playbackList.getData() != null && null != playbackList.getData().getList()) {
                    if (playbackList.getData().getList().size() <= 0) {
                        LogUtils.i(TAG, "queryPlayList url list is null");
                        return;
                    }
                    for (int i = 0; i < playbackList.getData().getList().size(); i++) {
                        mM3U8List.add(playbackList.getData().getList().get(i).getUrl());
                    }
                    mAdapter.notifyDataSetChanged();
                    if (!playbackList.getData().getEndflag()) {
                        queryPlayList(playSpeed, playbackList.getData().getList().get(playbackList.getData().getList().size()-1).getEndTime(), endTime);
                    } else {
                        Snackbar.make(mBtRefresh, "count = " + mM3U8List.size(), Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mBtRefresh, "invalid data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFail(@NonNull Throwable e) {
                LogUtils.e(TAG, "onFail:" + e.getMessage());
                if (!isVisible()) {
                    LogUtils.i(TAG, "view is invisible");
                    return;
                }
                if (null == mM3U8List || mM3U8List.size() <= 0) {
                    Snackbar.make(mBtRefresh, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    private void startPlayActivity(String url) {
        final List<String> supportPlayerList = new ArrayList<>();
        supportPlayerList.add("default(Android ExoPlayer)");
        supportPlayerList.add("ijkPlayer");
        ChooseeDialog chooseeDialog = new ChooseeDialog(getContext(), "选择播放器", supportPlayerList);
        chooseeDialog.setonClickListener(new ChooseeDialog.onClickListener() {
            @Override
            public void leftClick(View view, int position) {
                chooseeDialog.dismiss();
            }

            @Override
            public void rightClick(View view, int position) {
                chooseeDialog.dismiss();
                Intent intent;
                if (1 == position) {
                    intent = new Intent(getActivity(), IjkPlayerActivity.class);
                } else {
                    intent = new Intent(getActivity(), ExoPlayerActivity.class);
                }
                intent.putExtra("URI", url);
                intent.putExtra(KEY_SPEED_DURATION, speedUrlDuration);
                startActivity(intent);
            }
        });
        chooseeDialog.showDialg();
    }

    private String getCloudServiceId() {
        if (null == mEtServiceId.getText()) {
            return null;
        }
        String serviceId = mEtServiceId.getText().toString().trim();
        LogUtils.i(TAG,"getCloudServiceId serviceId:" + serviceId);
        return serviceId;
    }

    private int getPlaySpeed() {
        if (null != mSPPlaySpeed ) {
            String speedText = mSPPlaySpeed.getSelectedItem().toString();
            LogUtils.i(TAG, "getPlaySpeed, speedText:" + speedText);
            if (speedText.length() >= 2) {
                String speedValue = speedText.substring(0, speedText.length() - 1);
                LogUtils.i(TAG, "getPlaySpeed, speedValue:" + speedValue);
                if (TextUtils.isDigitsOnly(speedValue)) {
                    return Integer.parseInt(speedValue);
                }
            }

        }

        return 1;
    }
}
