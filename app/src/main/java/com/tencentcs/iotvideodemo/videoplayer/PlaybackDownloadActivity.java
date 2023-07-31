package com.tencentcs.iotvideodemo.videoplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.adapter.PlaybackFileDownloadAdapter;
import com.tencentcs.iotvideodemo.base.BaseActivity;
import com.tencentcs.iotvideodemo.entity.PlaybackDownloadFileEntity;
import com.tencentcs.iotvideodemo.messagemgr.PlaybackFileDownloadManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2021/3/26 15:17
 * @description:文件下载页面
 */
public class PlaybackDownloadActivity extends BaseActivity implements PlaybackFileDownloadAdapter.StateClickListener, PlaybackFileDownloadManager.DownloadItemStateListener{

    private final static String TAG = "PlaybackDownloadActivity";

    private PlaybackFileDownloadAdapter mDownloadAdapter;

    private RecyclerView mRVDownloaderView;

    private Disposable loadDownloadFileDisposable;

    @Override
    protected void onCreate(Bundle data) {
        super.onCreate(data);
        setContentView(R.layout.activity_playback_file_download);

        mRVDownloaderView = findViewById(R.id.rv_download_file_list);

        mRVDownloaderView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        mDownloadAdapter = new PlaybackFileDownloadAdapter(getApplicationContext());
        mDownloadAdapter.setStateClickListener(this);
        mRVDownloaderView.setAdapter(mDownloadAdapter);

        loadDownloadFileData();

        PlaybackFileDownloadManager.getInstance().setDownloadItemStateListener(this);
    }

    private void loadDownloadFileData() {
        if (null != loadDownloadFileDisposable && !loadDownloadFileDisposable.isDisposed()) {
            loadDownloadFileDisposable.dispose();
        }
        loadDownloadFileDisposable = Observable.create(new ObservableOnSubscribe<List<PlaybackDownloadFileEntity>>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<List<PlaybackDownloadFileEntity>> emitter) throws Exception {
                emitter.onNext(PlaybackFileDownloadManager.getInstance().getPlaybackFileDownloadingList());
                emitter.onComplete();
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<PlaybackDownloadFileEntity>>() {
        @Override
        public void accept(List<PlaybackDownloadFileEntity> playbackDownloadFileEntities) throws Exception {
            LogUtils.i(TAG,"loadDownloadFileData size:" + playbackDownloadFileEntities.size());
            mDownloadAdapter.addDataSource(playbackDownloadFileEntities);
        }
        });
    }

    @Override
    public void onPauseDownload(PlaybackDownloadFileEntity pauseEntity) {
        LogUtils.i(TAG,"onPauseDownload");
        PlaybackFileDownloadManager.getInstance().pauseDownload(pauseEntity);
    }

    @Override
    public void onResumeDownload(PlaybackDownloadFileEntity resumeEntity) {
        LogUtils.i(TAG,"onResumeDownload");
        PlaybackFileDownloadManager.getInstance().resumeDownload(resumeEntity);
    }

    @Override
    public void onRefreshDownload(PlaybackDownloadFileEntity resumeEntity) {
        LogUtils.i(TAG,"onRefreshDownload");
        PlaybackFileDownloadManager.getInstance().resumeDownload(resumeEntity);
    }

    @Override
    public void onLongPress(final int itemIndex, final PlaybackDownloadFileEntity deleteEntity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_delete_download)
                .setMessage(R.string.dialog_content_delete_download)
                .setNegativeButton(R.string.dialog_cancel, (dialogInterface, i) -> {

                })
                .setPositiveButton(R.string.dialog_confirm, (dialogInterface, i) -> {
                    PlaybackFileDownloadManager.getInstance().deleteDownloadItem(deleteEntity);
                    loadDownloadFileData();
                });
        builder.create().show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != loadDownloadFileDisposable && !loadDownloadFileDisposable.isDisposed()) {
            loadDownloadFileDisposable.dispose();
        }
        PlaybackFileDownloadManager.getInstance().setDownloadItemStateListener(null);

    }

    @Override
    public void onItemStateChange(PlaybackDownloadFileEntity changeEntity) {
        LogUtils.d(TAG,"onItemStateChange:" + changeEntity);
        if (null != mDownloadAdapter) {
            mDownloadAdapter.notifyItemEntityChange(changeEntity);
        }
    }

}
