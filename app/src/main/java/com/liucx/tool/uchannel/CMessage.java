package com.liucx.tool.uchannel;

public class CMessage {

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    private byte[] ack;
    private byte[] data;
    private String sData;

    private byte[] response;
    private String sResponse;

    public void setWaitMs(long waitMs) {
        this.waitMs = waitMs;
    }

    private long waitMs;

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    private int failCount;



    public boolean isNeedRetry() {
        boolean ret = false;
        return ret;
    }

    public boolean needWait() {
        boolean ret = false;
        return ret;
    }

    public boolean needAck() {
        boolean ret = false;
        return ret;
    }

    public byte[] getAck() {
        String str = new String(data);
        String id = str.substring(str.lastIndexOf("#"),str.indexOf("&"));
        return String.format("##%s&ackok**",id).getBytes();
    }

    public void response() {

    }

    public boolean waitResponse() {
        long ms = waitMs;
        boolean ret = false;
        return ret;
    }

    public boolean isResult(String cmd) {
        boolean ret = false;
        return ret;
    }

    public void setResponse(String result) {
        sResponse = result;
        response = result.getBytes();
    }

    public String getResponse() {
        return sResponse;
    }
}
