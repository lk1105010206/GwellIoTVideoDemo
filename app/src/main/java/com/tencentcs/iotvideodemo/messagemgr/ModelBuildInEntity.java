package com.tencentcs.iotvideodemo.messagemgr;

import java.util.ArrayList;
import java.util.List;

public class ModelBuildInEntity {

    /**
     * val : {"nickName":"good1026_7W6BTH81NMJH9","almEvtNoDisturb":0}
     * t : 1604458298
     */
    private ValEntity val;
    private int t;

    public void setVal(ValEntity val) {
        this.val = val;
    }

    public void setT(int t) {
        this.t = t;
    }

    public ValEntity getVal() {
        return val;
    }

    public int getT() {
        return t;
    }

    public class ValEntity {
        /**
         * nickName : good1026_7W6BTH81NMJH9
         * almEvtNoDisturb : 0
         */
        private String nickName;
        private int almEvtNoDisturb;

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public void setAlmEvtPushEna(int almEvtNoDisturb) {
            this.almEvtNoDisturb = almEvtNoDisturb;
        }

        public String getNickName() {
            return nickName;
        }

        public int getAlmEvtPushEna() {
            return almEvtNoDisturb;
        }

        public List<String> getEditData() {
            List<String> list = new ArrayList<>();
            list.add("nickName=" + nickName);
            list.add("almEvtNoDisturb=" + almEvtNoDisturb);
            return list;
        }
    }
}
