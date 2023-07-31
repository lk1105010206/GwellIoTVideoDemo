package com.tencentcs.iotvideodemo.vas;

/**
 * @author: 2020
 * @email: liukang@gwell.cc
 * @date: 2021/11/16 17:46
 * @description:
 */
public class CloudSpeedPlayEntity {

    /**
     * requestId : 9b000005000007d4-f76fd15b-619380a9
     * code : 0
     * msg : Success
     * data : {"url":"https://speedplayback-cn-1.cloudlinks.cn/vas/speedplayback/m3u8?sessionId=126a8d5d-d424-4452-bbd6-08263084e233"}
     */

    private String requestId;
    private Integer code;
    private String msg;
    private DataDTO data;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public static class DataDTO {
        /**
         * url : https://speedplayback-cn-1.cloudlinks.cn/vas/speedplayback/m3u8?sessionId=126a8d5d-d424-4452-bbd6-08263084e233
         * 倍速播放地址
         */

        private String url;
        /**
         * 视频时长
         * */
        private long videoDuration;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getVideoDuration() {
            return videoDuration;
        }

        public void setVideoDuration(long videoDuration) {
            this.videoDuration = videoDuration;
        }
    }
}
