package com.tencentcs.iotvideodemo.messagemgr;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencentcs.iotvideo.IPlaybackDownloadListener;
import com.tencentcs.iotvideo.IoTVideoError;
import com.tencentcs.iotvideo.iotvideoplayer.player.PlaybackFileDownloader;
import com.tencentcs.iotvideo.messagemgr.DownloadFileHead;
import com.tencentcs.iotvideo.messagemgr.PlaybackMessage;
import com.tencentcs.iotvideo.utils.FileIOUtils;
import com.tencentcs.iotvideo.utils.LogUtils;
import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.entity.PlaybackDownloadFileEntity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2021/3/26 14:21
 * @description:SD卡文件下载管理器
 */
public class PlaybackFileDownloadManager {

    private final static String TAG = "PlaybackFileDownloadManager";

    private final static String TEMP_FILE_PREFIX = "temp_";

    private Context mContext;

    final private List<PlaybackFileDownloader> mFileDownloaderList;

    /**
     * 存储下载文件的文件夹目录
     */
    private String mDownloadFileDirectory;

    private DownloadItemStateListener mDownloadListStateListener;

    /**
     * 下载队列中实例下载状态发生变化监听器.
     */
    public interface DownloadItemStateListener {
        /**
         * 下载实例状态发生变化.
         *
         * @param changeEntity 发生变化的实例
         */
        void onItemStateChange(PlaybackDownloadFileEntity changeEntity);
    }

    private static class Holder {
        private final static PlaybackFileDownloadManager INSTANCE = new PlaybackFileDownloadManager();
    }

    private PlaybackFileDownloadManager() {
        mFileDownloaderList = new ArrayList<>();
    }

    public static PlaybackFileDownloadManager getInstance() {
        return Holder.INSTANCE;
    }

    public void setContext(Context context) {
        this.mContext = context;
        mDownloadFileDirectory = mContext.getExternalFilesDir("playbackDownloadFile").getAbsolutePath();
    }

    /**
     * 添加新的下载文件.
     *
     * @param playbackNode 需要的文件信息
     */
    public void addDownloadNote(String deviceId, int sourceId, PlaybackMessage.PlaybackNode playbackNode) {
        if (null == mContext) {
            LogUtils.e(TAG, "addDownloadNote failure:null == mContext ");
            return;
        }

        if (fileIsDownloaded(deviceId, playbackNode.startTime)) {
            Toast.makeText(mContext, "文件已下载", Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "file has downloaded");
            return;
        }

        PlaybackFileDownloader existDownloader = getDownloaderWithDeviceId(deviceId, playbackNode.startTime);
        if (null != existDownloader) {
            Toast.makeText(mContext, R.string.file_exist_download_list, Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "file has existed in download list");
            return;
        }

        File[] tempFile = getExistedTempFileWithFileName(deviceId, playbackNode.startTime);
        LogUtils.i(TAG, "existed tempFile size:" + (null == tempFile ? "0" :tempFile.length));
        deleteFile(tempFile);

        final PlaybackFileDownloader fileDownloader = new PlaybackFileDownloader();
        fileDownloader.initDownloader(deviceId, (byte) sourceId);

        mFileDownloaderList.add(fileDownloader);

        IPlaybackDownloadListener downloadListener = new IPlaybackDownloadListener() {
            private DownloadFileHead fileHead = null;
            private PlaybackDownloadFileEntity downloadFileEntity = null;

            @Override
            public void onPrepared(DownloadFileHead fileHead) {
                this.fileHead = fileHead;
                LogUtils.d(TAG, "onPrepared fileName:" + fileHead.getFileName());
                downloadFileEntity = onDownloadItemChange(downloadFileEntity,
                        deviceId,
                        fileDownloader.getDownloadFileStartTime(),
                        null != fileHead ? fileHead.getFileName() : "null==fileName",
                        PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING);
            }

            @Override
            public void onReceiveFileData(byte[] fileData) {
                //LogUtils.d(TAG, "onReceiveFileData, dataSize:" + fileData.length);
                saveDownloadDataToFile(fileData, deviceId, fileHead);
            }

            @Override
            public void onSuccess() {
                Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
                boolean saveRet = saveFileFromTempFile(getDownloadTempFilePath(deviceId, fileHead));
                LogUtils.d(TAG, "onSuccess, saveRet:" + saveRet);
                mFileDownloaderList.remove(fileDownloader);
                downloadFileEntity = onDownloadItemChange(downloadFileEntity,
                        deviceId,
                        fileDownloader.getDownloadFileStartTime(),
                        null != fileHead ? fileHead.getFileName() : "null==fileName",
                        PlaybackFileDownloader.DOWNLOAD_STATE_FINISHED);
            }

            @Override
            public void onPause() {
                LogUtils.d(TAG, "onPause");
                downloadFileEntity = onDownloadItemChange(downloadFileEntity,
                        deviceId,
                        fileDownloader.getDownloadFileStartTime(),
                        null != fileHead ? fileHead.getFileName() : "null==fileName",
                        PlaybackFileDownloader.DOWNLOAD_STATE_PAUSE);
            }

            @Override
            public void onFailure(int errorCode, String errorReason) {
                LogUtils.e(TAG, "onFailure, errorCode:" + errorCode + "; errorReason:" + errorReason);
                Toast.makeText(mContext, getDownloadErrorHint(errorCode), Toast.LENGTH_LONG).show();
                if (null != downloadFileEntity) {
                    downloadFileEntity.setDownloadErrorCode(errorCode);
                    downloadFileEntity.setDownloadErrorReason(errorReason);
                }
                downloadFileEntity = onDownloadItemChange(errorCode,
                        errorReason,
                        downloadFileEntity,
                        deviceId,
                        fileDownloader.getDownloadFileStartTime(),
                        null != fileHead ? fileHead.getFileName() : "null==fileName",
                        PlaybackFileDownloader.DOWNLOAD_STATE_ERROR);
            }
        };
        fileDownloader.setDownloadListener(downloadListener);
        fileDownloader.downloadPlaybackFile(playbackNode.startTime, 0);
    }

    public List<PlaybackDownloadFileEntity> getPlaybackFileDownloadingList() {

        List<PlaybackDownloadFileEntity> fileEntityList = new ArrayList<>();
        if (null != mFileDownloaderList && !mFileDownloaderList.isEmpty()) {
            // 获取正在下载、暂停、异常的文件队列
            for (PlaybackFileDownloader downloader : mFileDownloaderList) {
                if (null == downloader) {
                    continue;
                }
                PlaybackDownloadFileEntity itemEntity = new PlaybackDownloadFileEntity();
                itemEntity.setDeviceId(downloader.getDeviceId());
                itemEntity.setDownloadState(downloader.getDownloadState());
                itemEntity.setFileName(null == downloader.getFileHeadInfo() ? String.valueOf(downloader.getDownloadFileStartTime()) : downloader.getFileHeadInfo().getFileName());
                itemEntity.setFileStartTime(downloader.getDownloadFileStartTime());
                fileEntityList.add(itemEntity);
            }
        }

        LogUtils.i(TAG, "downloading list size:" + fileEntityList.size());

        if (TextUtils.isEmpty(mDownloadFileDirectory) && null != mContext) {
            mDownloadFileDirectory = mContext.getExternalFilesDir("playbackDownloadFile").getAbsolutePath();
        }

        // 加载已经下载完成的文件
        if (!TextUtils.isEmpty(mDownloadFileDirectory)) {
            File fileDirectory = new File(mDownloadFileDirectory);
            if (fileDirectory.exists() && fileDirectory.isDirectory()) {
                File[] subFileDirectory = fileDirectory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                });

                if (null != subFileDirectory && subFileDirectory.length > 0) {
                    for (File itemFile : subFileDirectory) {
                        // 获取已经下载完成的文件
                        File[] finishedFileList = itemFile.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                return file.isFile() && !file.getName().startsWith(TEMP_FILE_PREFIX);
                            }
                        });
                        if (null == finishedFileList || finishedFileList.length <= 0) {
                            continue;
                        }
                        for (File finishedFile : finishedFileList) {
                            PlaybackDownloadFileEntity fileEntity = new PlaybackDownloadFileEntity();
                            fileEntity.setFileName(finishedFile.getName());
                            fileEntity.setFileStartTime(getFileStartTimeFromFileName(finishedFile.getName()));
                            // 父文件夹名称就是设备的ID
                            fileEntity.setDeviceId(finishedFile.getParentFile().getName());
                            fileEntity.setDownloadState(PlaybackFileDownloader.DOWNLOAD_STATE_FINISHED);

                            fileEntityList.add(fileEntity);
                        }
                    }
                }
            }
        }

        return fileEntityList;
    }

    public void setDownloadItemStateListener(DownloadItemStateListener downloadListStateListener) {
        this.mDownloadListStateListener = downloadListStateListener;
    }

    public void pauseDownload(PlaybackDownloadFileEntity pauseEntity) {
        if (null == pauseEntity) {
            LogUtils.e(TAG, "pauseDownload failure:entity is null");
            return;
        }
        PlaybackFileDownloader fileDownloader = getDownloaderWithEntity(pauseEntity);

        if (null != fileDownloader) {
            fileDownloader.pause();
        }

        LogUtils.i(TAG, "pauseDownload hasExisted:" + (null != fileDownloader));
    }

    public void resumeDownload(PlaybackDownloadFileEntity resumeEntity) {
        LogUtils.i(TAG, "resumeDownload start");
        if (null == resumeEntity) {
            LogUtils.e(TAG, "resumeDownload failure:entity is null");
            return;
        }
        long tempFileSize = 0;
        long fileStartTime = resumeEntity.getFileStartTime();
        String tempDownloadFilePath = getDownloadTempFilePath(resumeEntity.getDeviceId(), resumeEntity.getFileName());
        if (!TextUtils.isEmpty(tempDownloadFilePath)) {
            File tempDownloadFile = new File(tempDownloadFilePath);
            if (tempDownloadFile.exists() && tempDownloadFile.isFile()) {
                tempFileSize = tempDownloadFile.length();
            }
        }
        if (fileStartTime <= 0) {
            LogUtils.e(TAG, "resumeDownload failure: fileStartTime <= 0");
            return;
        }

        PlaybackFileDownloader fileDownloader = getDownloaderWithEntity(resumeEntity);

        if (null == fileDownloader) {
            LogUtils.e(TAG, "resumeDownload failure: null == fileDownloader");
            return;
        }

        fileDownloader.downloadPlaybackFile(fileStartTime, (int) tempFileSize);
        LogUtils.i(TAG, "resumeDownload end,fileStartTime:" + fileStartTime + "; tempFileSize:" + tempFileSize);
    }

    /**
     * 删除下载项.
     *
     * @param deleteDownloadEntity 需要删除的下载项
     */
    public void deleteDownloadItem(PlaybackDownloadFileEntity deleteDownloadEntity) {
        if (null == deleteDownloadEntity) {
            LogUtils.e(TAG,"deleteDownloadItem failure:entity is null");
            return;
        }
        if (TextUtils.isEmpty(deleteDownloadEntity.getFileName())) {
            LogUtils.e(TAG,"deleteDownloadItem failure:file name is null");
            return;
        }
        boolean hasDownloaded = fileIsDownloaded(deleteDownloadEntity.getDeviceId(), deleteDownloadEntity.getFileStartTime());
        LogUtils.i(TAG, "deleteDownloadItem hasDownloaded:" + hasDownloaded);
        if (hasDownloaded) {
            String filePath = getDownloadedFilePath(deleteDownloadEntity.getDeviceId(), deleteDownloadEntity.getFileName());
            File downloadedFile = new File(filePath);
            if (downloadedFile.exists() && downloadedFile.isFile()) {
                downloadedFile.delete();
                LogUtils.i(TAG,"delete downloaded file");
            }
        }else {
            String tempFilePath = getDownloadTempFilePath(deleteDownloadEntity.getDeviceId(), deleteDownloadEntity.getFileName());
            if (!TextUtils.isEmpty(tempFilePath)) {
                File tempFile = new File(tempFilePath);
                PlaybackFileDownloader downloader = getDownloaderWithEntity(deleteDownloadEntity);
                if (null != downloader) {
                    if (PlaybackFileDownloader.DOWNLOAD_STATE_DOWNLOADING == downloader.getDownloadState()) {
                        downloader.pause();
                    }
                    downloader.stop();
                    mFileDownloaderList.remove(downloader);
                }
                if (tempFile.exists() && tempFile.isFile()) {
                    tempFile.delete();
                }
                LogUtils.i(TAG,"delete temp file");
            }
        }

    }

    /**
     * 获取存储临时文件的路径,eg:parentPath/deviceId/temp_fileName.mp4
     */
    private String getDownloadTempFilePath(String deviceId, DownloadFileHead fileHead) {
        if (null == fileHead) {
            return null;
        }

        return getDownloadTempFilePath(deviceId, fileHead.getFileName());
    }

    /**
     * 获取存储临时文件的路径,eg:parentPath/deviceId/temp_fileName.mp4
     */
    private String getDownloadTempFilePath(String deviceId, String fileName) {
        if (TextUtils.isEmpty(deviceId)
                || TextUtils.isEmpty(fileName)
                || TextUtils.isEmpty(mDownloadFileDirectory)) {
            return null;
        }
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder.append(mDownloadFileDirectory);
        pathBuilder.append(File.separator);
        pathBuilder.append(deviceId);
        pathBuilder.append(File.separator);
        pathBuilder.append(TEMP_FILE_PREFIX);
        pathBuilder.append(fileName);

        return pathBuilder.toString();
    }

    /**
     * 生成最终的下载文件,eg:parentPath/deviceId/fileName.mp4
     */
    private boolean saveFileFromTempFile(String tempFlePath) {
        if (TextUtils.isEmpty(tempFlePath)) {
            LogUtils.e(TAG, "saveFileFromTempFile failure:temp file path is null");
            return false;
        }
        File tempFile = new File(tempFlePath);
        if (!tempFile.exists()) {
            LogUtils.e(TAG, "saveFileFromTempFile failure:temp file is not exist");
            return false;
        }

        if (tempFile.getName().startsWith(TEMP_FILE_PREFIX)) {
            // 去掉前缀
            String downloadFileName = tempFile.getName().replace(TEMP_FILE_PREFIX, "");
            File downloadFile = new File(tempFile.getParentFile(), downloadFileName);
            LogUtils.d(TAG, "downloadFile filePath:" + downloadFile.getAbsolutePath());
            boolean ret = tempFile.renameTo(downloadFile);
            LogUtils.i(TAG, "saveFileFromTempFile ret:" + ret);
            return ret;
        }
        return false;
    }

    private boolean saveDownloadDataToFile(byte[] downloadData, String deviceId, DownloadFileHead fileHead) {
        if (null == downloadData || downloadData.length <= 0) {
            return false;
        }
        String filePath = getDownloadTempFilePath(deviceId, fileHead);
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            file = null;
            LogUtils.e(TAG, "saveDownloadDataToFile exception:" + e.getMessage());
        }
        if (null != file) {
            return FileIOUtils.writeFileFromBytesByChannel(file, downloadData, true, true);
        } else {
            return false;
        }
    }

    /**
     * 文件是否已经下载完成该判断方法要求设备端生成SD卡文件时，名称名称是以文件开始时间开头的，如果设备的
     * 命名规则不是以开始时间开始，则不能用该方法判断），
     */
    private boolean fileIsDownloaded(String deviceId, long fileStartTime) {
        String fileDirectory = mDownloadFileDirectory + File.separator + deviceId;
        File directory = new File(fileDirectory);
        if (!directory.exists()) {
            return false;
        }
        File[] existFileList = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().startsWith(String.valueOf(fileStartTime));
            }
        });
        return null != existFileList && existFileList.length > 0;
    }

    /**
     * 通过文件名称判断当前需要下载的文件是否还未下载完成（该判断方法要求设备端生成SD卡文件时，名称名称是以文件开始时间开头的，如果设备的
     * 命名规则不是以开始时间开始，则不能用该方法判断），
     */
    private File[] getExistedTempFileWithFileName(String deviceId, long fileStartTime) {
        String fileDirectory = mDownloadFileDirectory + File.separator + deviceId;
        File directory = new File(fileDirectory);
        if (!directory.exists()) {
            return null;
        }
        File[] existFileList = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().startsWith(TEMP_FILE_PREFIX + fileStartTime);
            }
        });
        return existFileList;
    }

    private void deleteFile(File[] files) {
        if (null == files || files.length <= 0) {
            return;
        }
        for (File itemFile : files) {
            itemFile.delete();
        }
    }

    private PlaybackDownloadFileEntity onDownloadItemChange(PlaybackDownloadFileEntity srcEntity, String deviceId, long fileStartTime, String fileName, int state) {
        return onDownloadItemChange(0, "", srcEntity, deviceId, fileStartTime, fileName, state);
    }

    private PlaybackDownloadFileEntity onDownloadItemChange(int errorCode, String errorReason, PlaybackDownloadFileEntity srcEntity, String deviceId, long fileStartTime, String fileName, int state) {
        if (null == srcEntity) {
            srcEntity = new PlaybackDownloadFileEntity();
        }
        srcEntity.setDeviceId(deviceId);
        srcEntity.setFileName(fileName);
        srcEntity.setDownloadState(state);
        srcEntity.setFileStartTime(fileStartTime);
        srcEntity.setDownloadErrorCode(errorCode);
        srcEntity.setDownloadErrorReason(errorReason);
        if (null != mDownloadListStateListener) {
            mDownloadListStateListener.onItemStateChange(srcEntity);
        }else {
            LogUtils.e(TAG,"null == mDownloadListStateListener");
        }
        return srcEntity;
    }

    /**
     * 从文件名称中解析出文件开始时间（只有设备端打包的文件名称里面包含文件开始时间才能这么使用！！！）
     *
     * @param fileName 文件名称
     * @return 文件的开始时间，-1:表示获取失败
     */
    private long getFileStartTimeFromFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return -1;
        }

        String tempNameStr = fileName.replace(TEMP_FILE_PREFIX, "");
        if (!TextUtils.isEmpty(tempNameStr) && tempNameStr.contains("-")) {
            String[] splitFileList = tempNameStr.split("-");
            if (TextUtils.isDigitsOnly(splitFileList[0])) {
                long fileStartTime = Long.parseLong(splitFileList[0]);
                return fileStartTime;
            }
        }
        return -1;
    }

    private PlaybackFileDownloader getDownloaderWithEntity(PlaybackDownloadFileEntity fileEntity) {
        PlaybackFileDownloader existedFileDownloader = null;
        for (PlaybackFileDownloader fileDownloader : mFileDownloaderList) {
            if (null != fileDownloader
                    && !TextUtils.isEmpty(fileDownloader.getDeviceId())
                    && fileDownloader.getDeviceId().equals(fileEntity.getDeviceId())
                    && fileDownloader.getDownloadFileStartTime() > 0
                    && fileDownloader.getDownloadFileStartTime() == fileEntity.getFileStartTime()) {
                existedFileDownloader = fileDownloader;
                break;
            }
        }
        return existedFileDownloader;
    }

    private PlaybackFileDownloader getDownloaderWithDeviceId(String deviceId, long fileStartTime) {
        PlaybackFileDownloader existedFileDownloader = null;
        for (PlaybackFileDownloader fileDownloader : mFileDownloaderList) {
            if (null != fileDownloader
                    && !TextUtils.isEmpty(fileDownloader.getDeviceId())
                    && fileDownloader.getDeviceId().equals(deviceId)
                    && fileDownloader.getDownloadFileStartTime() > 0
                    && fileDownloader.getDownloadFileStartTime() == fileStartTime) {
                existedFileDownloader = fileDownloader;
                break;
            }
        }
        return existedFileDownloader;
    }

    /**
     * 获取已经下载完成的文件路径.
     *
     * @param deviceId =设备ID
     * @param fileName 文件名称
     * @return 文件的绝对路径
     */
    private String getDownloadedFilePath(String deviceId, String fileName) {
        StringBuilder builder = new StringBuilder();
        builder.append(mDownloadFileDirectory);
        builder.append(File.separator);
        builder.append(deviceId);
        builder.append(File.separator);
        builder.append(fileName);
        return builder.toString();
    }

    public String getDownloadErrorHint(int errorCode) {
        String errorHint;
        if (null == mContext) {
            return "";
        }
        switch (errorCode) {
            case IoTVideoError.DOWNLOAD_ERROR_DOWNLOADER_BUSY:
                errorHint = mContext.getString(R.string.download_error_22060);
                break;
            default:
                errorHint = mContext.getString(R.string.download_state_failure) + ":" + errorCode;
                break;
        }
        return errorHint;
    }
}
