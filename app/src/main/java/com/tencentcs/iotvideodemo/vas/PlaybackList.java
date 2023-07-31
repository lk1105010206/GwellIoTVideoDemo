package com.tencentcs.iotvideodemo.vas;

import java.util.List;

public class PlaybackList {


    /**
     * requestId : 880186b00000001c-fbc7a564-614bdc03
     * code : 0
     * msg : Success
     * data : {"endflag":true,"list":[{"endTime":1632330034,"startTime":1632326431,"url":"http://139.155.88.203:8083/vas/playback/m3u8?ownerid=9223801602303852572&deviceid=429565449076807&startTime=1632326431&endTime=1632330034&sign=d7690781c3b2b37428ffd970e27a7b3d32dbdb0b&port=45521"}]}
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
         * endflag : true
         * list : [{"endTime":1632330034,"startTime":1632326431,"url":"http://139.155.88.203:8083/vas/playback/m3u8?ownerid=9223801602303852572&deviceid=429565449076807&startTime=1632326431&endTime=1632330034&sign=d7690781c3b2b37428ffd970e27a7b3d32dbdb0b&port=45521"}]
         */
        /**
         * 是否已经是请求时间段最后一个URL
         * */
        private Boolean endflag;
        private List<ListDTO> list;

        public Boolean getEndflag() {
            return endflag;
        }

        public void setEndflag(Boolean endflag) {
            this.endflag = endflag;
        }

        public List<ListDTO> getList() {
            return list;
        }

        public void setList(List<ListDTO> list) {
            this.list = list;
        }

        public static class ListDTO {
            /**
             * endTime : 1632330034
             * startTime : 1632326431
             * url : http://139.155.88.203:8083/vas/playback/m3u8?ownerid=9223801602303852572&deviceid=429565449076807&startTime=1632326431&endTime=1632330034&sign=d7690781c3b2b37428ffd970e27a7b3d32dbdb0b&port=45521
             */
            /**
             * url结束时间，单位：秒
             * */
            private Integer endTime;
            /**
             * url开始时间，单位：秒
             * */
            private Integer startTime;
            private String url;

            public Integer getEndTime() {
                return endTime;
            }

            public void setEndTime(Integer endTime) {
                this.endTime = endTime;
            }

            public Integer getStartTime() {
                return startTime;
            }

            public void setStartTime(Integer startTime) {
                this.startTime = startTime;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
