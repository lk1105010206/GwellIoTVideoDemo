package com.tencentcs.iotvideodemo.entity;

import android.text.TextUtils;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2021/3/29 10:36
 * @description:sd卡文件下载列表item 实体类
 */
public class PlaybackDownloadFileEntity {

    private String deviceId;

    private String fileName;

    private int downloadState;

    private long fileStartTime;

    private int downloadErrorCode = 0;

    private String downloadErrorReason = "";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getFileStartTime() {
        return fileStartTime;
    }

    public void setFileStartTime(long fileStartTime) {
        this.fileStartTime = fileStartTime;
    }

    public int getDownloadErrorCode() {
        return downloadErrorCode;
    }

    public void setDownloadErrorCode(int downloadErrorCode) {
        this.downloadErrorCode = downloadErrorCode;
    }

    public String getDownloadErrorReason() {
        return downloadErrorReason;
    }

    public void setDownloadErrorReason(String downloadErrorReason) {
        this.downloadErrorReason = downloadErrorReason;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof PlaybackDownloadFileEntity)) {
            return false;
        }
        if (TextUtils.isEmpty(deviceId)) {
            return false;
        }
        if (fileStartTime <= 0) {
            return false;
        }
        return deviceId.equals(((PlaybackDownloadFileEntity)other).deviceId)
                && fileStartTime == (((PlaybackDownloadFileEntity)other).fileStartTime);
    }

    public void copyObj(PlaybackDownloadFileEntity srcEntity) {
        if (null == srcEntity) {
            return;
        }
        this.fileName = srcEntity.fileName;
        this.downloadState = srcEntity.downloadState;
        this.deviceId = srcEntity.deviceId;
        this.fileStartTime = srcEntity.fileStartTime;
        this.downloadErrorCode = srcEntity.downloadErrorCode;
        this.downloadErrorReason = srcEntity.downloadErrorReason;
    }

    @Override
    public String toString() {
        return "PlaybackDownloadFileEntity{" +
                "deviceId='" + deviceId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", downloadState=" + downloadState +
                ", fileStartTime=" + fileStartTime +
                '}';
    }
}
