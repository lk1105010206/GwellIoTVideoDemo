package com.tencentcs.iotvideodemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.tencentcs.iotvideo.iotvideoplayer.player.PlaybackFileDownloader;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.entity.PlaybackDownloadFileEntity;

import java.util.List;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2021/3/29 9:59
 * @description:sd卡下载列表适配器
 */
public class PlaybackFileDownloadAdapter extends RecyclerView.Adapter<PlaybackFileDownloadAdapter.FileDownloadHolder> {

    private final static String TAG = "PlaybackFileDownloadAdapter";


    private List<PlaybackDownloadFileEntity> dataList;

    private Context mContext;

    private StateClickListener mListener;

    /**
     * 点击切换下载状态监听器.
     */
    public interface StateClickListener {
        /**
         * 暂停下载按钮被点击回调.
         *
         * @param pauseEntity 被操作的实体
         */
        void onPauseDownload(PlaybackDownloadFileEntity pauseEntity);

        /**
         * 恢复下载按钮被点击回调.
         *
         * @param resumeEntity 被操作的实体
         */
        void onResumeDownload(PlaybackDownloadFileEntity resumeEntity);

        /**
         * 下载失败按钮被点击回调.
         *
         * @param resumeEntity 被操作的实体
         */
        void onRefreshDownload(PlaybackDownloadFileEntity resumeEntity);

        /**
         * 长按某个item.
         *
         * @param itemIndex    item在列表中的index
         * @param resumeEntity item对应的数据实体
         */
        void onLongPress(int itemIndex, PlaybackDownloadFileEntity resumeEntity);
    }

    public PlaybackFileDownloadAdapter(Context context) {
        this(context, null);
    }

    public PlaybackFileDownloadAdapter(Context context, List<PlaybackDownloadFileEntity> inputDataList) {
        this.mContext = context;
        if (null == this.dataList) {
            this.dataList = new ArrayList<>();
        }

        if (null != inputDataList && inputDataList.size() > 0) {
            this.dataList.addAll(inputDataList);
        }
        LogUtils.d(TAG, "PlaybackFileDownloadAdapter, data size:" + dataList.size());
    }

    /**
     * 下载列表中的实例下载状态发生改变.
     *
     * @param entity 状态发生变化的实例
     */
    public void notifyItemEntityChange(PlaybackDownloadFileEntity entity) {
        LogUtils.i(TAG,"notifyItemEntityChange, start");
        if (null == entity) {
            return;
        }
        if (null == dataList) {
            return;
        }
        int existedIndex = -1;
        for (int i = 0; i < dataList.size(); i++ ) {
            if (entity.equals(dataList.get(i))) {
                existedIndex = i;
                break;
            }
        }
        if (existedIndex >= 0 ) {
            dataList.get(existedIndex).copyObj(entity);
            notifyItemChanged(existedIndex);
        }
        LogUtils.i(TAG,"notifyItemEntityChange, existedIndex:" + existedIndex);
    }

    public void addDataSource(List<PlaybackDownloadFileEntity> entityList) {
        if (null == entityList) {
            return;
        }
        dataList.clear();
        dataList.addAll(entityList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileDownloadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FileDownloadHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        itemView = inflater.inflate(R.layout.item_playback_download_file, parent, false);
        viewHolder = new FileDownloadHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileDownloadHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setStateClickListener(StateClickListener mListener) {
        this.mListener = mListener;
    }

    public class FileDownloadHolder extends RecyclerView.ViewHolder {

        protected View mContentView;
        final private TextView mTVFileName;
        final private TextView mTVDownloadState;
        final private ImageView mIVChangeState;

        public FileDownloadHolder(@NonNull View itemView) {
            super(itemView);
            this.mContentView = itemView;
            mTVFileName = mContentView.findViewById(R.id.tv_file_name);
            mTVDownloadState = mContentView.findViewById(R.id.tv_download_state);
            mIVChangeState = mContentView.findViewById(R.id.iv_change_state);
        }

        void onBindView(int itemPosition) {
            if (null == dataList || itemPosition >= dataList.size()) {
                LogUtils.e(TAG, "null == dataList || itemPosition >= dataList.size()");
                return;
            }
            final PlaybackDownloadFileEntity itemEntity = dataList.get(itemPosition);
            if (null == itemEntity) {
                LogUtils.e(TAG, "null == itemEntity");
                return;
            }

            mTVFileName.setText(String.valueOf(itemEntity.getFileStartTime()));

            mTVDownloadState.setText(getStateHintText(itemEntity.getDownloadState()));

            setChangeViewRes(itemEntity.getDownloadState());

            mIVChangeState.setOnClickListener(view -> {
                if (PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING == itemEntity.getDownloadState()) {
                    if (null != mListener) {
                        mListener.onPauseDownload(itemEntity);
                    }
                } else if (PlaybackFileDownloader.DOWNLOAD_STATE_PAUSE == itemEntity.getDownloadState()) {
                    if (null != mListener) {
                        mListener.onResumeDownload(itemEntity);
                    }
                } else if (PlaybackFileDownloader.DOWNLOAD_STATE_ERROR == itemEntity.getDownloadState()) {
                    if (null != mListener) {
                        mListener.onRefreshDownload(itemEntity);
                    }
                }
            });
            mContentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (null != mListener) {
                        mListener.onLongPress(itemPosition, itemEntity);
                    }
                    return false;
                }
            });
        }

        private void setChangeViewRes(int state) {
            switch (state) {
                case PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING:
                    mIVChangeState.setImageResource(R.drawable.ic_downloading);
                    mIVChangeState.setVisibility(View.VISIBLE);
                    break;
                case PlaybackFileDownloader.DOWNLOAD_STATE_PAUSE:
                    mIVChangeState.setImageResource(R.drawable.ic_download_pause);
                    mIVChangeState.setVisibility(View.VISIBLE);
                    break;
                case PlaybackFileDownloader.DOWNLOAD_STATE_ERROR:
                    mIVChangeState.setImageResource(R.drawable.ic_download_refresh);
                    mIVChangeState.setVisibility(View.VISIBLE);
                    break;
                default:
                    mIVChangeState.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private String getStateHintText(int state) {
        if (null == mContext) {
            return "";
        }
        String hintText;
        switch (state) {
            case PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING:
                hintText = mContext.getString(R.string.download_state_downloading);
                break;
            case PlaybackFileDownloader.DOWNLOAD_STATE_PAUSE:
                hintText = mContext.getString(R.string.download_state_pause);
                break;
            case PlaybackFileDownloader.DOWNLOAD_STATE_FINISHED:
                hintText = mContext.getString(R.string.download_state_downloaded);
                break;
            case PlaybackFileDownloader.DOWNLOAD_STATE_ERROR:
                hintText = mContext.getString(R.string.download_state_failure);
                break;
            default:
                hintText = "";
                break;
        }
        return hintText;
    }
}
