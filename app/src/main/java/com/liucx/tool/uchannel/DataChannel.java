package com.liucx.tool.uchannel;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class DataChannel {
    final static private String TAG = "DataChannel";

    private LinkedBlockingQueue<CMessage> sendQueue;
    private LinkedBlockingQueue<CMessage> waitAckQueue;
    private LinkedBlockingQueue<CMessage> sendFailQueue;
    private LinkedBlockingQueue<CMessage> sendPriorityLevelOneQueue;//for send ack
    private LinkedBlockingQueue<CMessage> sendPriorityLevelTwoQueue;

    private LinkedBlockingQueue<byte[]> receiveQueue;

    private CMessage mCurSendMsg;

    private Handler mSendHandler;
    private Handler mRecHandler;
    private static DataChannel mDataChannel;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public synchronized static DataChannel getInstance() {
        if(mDataChannel == null) {
            mDataChannel = new DataChannel();
        }
        return mDataChannel;
    }

    private DataChannel() {
        init();
    }

    public void sendMsg(CMessage msg) {
        sendQueue.add(msg);
        mSendHandler.sendEmptyMessage(MSG_SEND);
    }

    public void receiveQueue(byte[] msg) {
        receiveQueue.offer(msg);
        mSendHandler.sendEmptyMessage(MSG_REC);
    }

    private void init() {
        HandlerThread thread = new HandlerThread("DataChannelSend");
        thread.start();
        mSendHandler = new DataHandler(thread.getLooper(), this);
        HandlerThread recThread = new HandlerThread("DataChannelRev");
        thread.start();
        mSendHandler = new DataHandler(recThread.getLooper(), this);
    }

    static final private int MSG_SEND = 1;
    static final private int MSG_REC = 2;

    public static class DataHandler extends Handler {
        private DataChannel channel;
        public DataHandler(Looper looper, DataChannel dataChannel) {
            super(looper);
            channel = dataChannel;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REC:
                    channel.handlerReceiveMsg();
                    break;
                case MSG_SEND:
                    channel.handlerSendMsg();
                    break;
            }
        }
    }

    private void handlerSendMsg() {
        while(!sendQueue.isEmpty() || !sendFailQueue.isEmpty()) {
            if(sendFailQueue.isEmpty()) {
                mCurSendMsg = sendQueue.poll();
            } else {
                mCurSendMsg = sendFailQueue.poll();
            }
            if(mCurSendMsg == null)
                continue;
            try {
                mOutputStream.write(mCurSendMsg.getData());
                if(mCurSendMsg.needWait()) {
                    if(mCurSendMsg.waitResponse()) {//response
                        if(mCurSendMsg.needAck()) {
                            mOutputStream.write(mCurSendMsg.getAck());
                        }
                        handlerRecMsg(mCurSendMsg.getResponse());
                    } else {//time out
                        Log.e(TAG, "timeout cmd:" + new String(mCurSendMsg.getData()));
                        if(mCurSendMsg.isNeedRetry()) {
                            mCurSendMsg.setFailCount(mCurSendMsg.getFailCount() + 1);
                            sendFailQueue.add(mCurSendMsg);
                        } else {
                            Log.e(TAG, "fail msg:" + new String(mCurSendMsg.getData()));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mCurSendMsg = null;
            }
        }
    }

    private void handlerReceiveMsg() {
        byte[] response;
        while(!receiveQueue.isEmpty()) {
            response = receiveQueue.poll();
            String cmd = new String(response);
            if(mCurSendMsg!= null && mCurSendMsg.isResult(cmd)) {
                mCurSendMsg.setResponse(cmd);
                mCurSendMsg.response();
            } else {
                Log.e(TAG, "miss response cmd:" + cmd);
                handlerRecMsg(cmd);
            }
        }
    }

    private void handlerRecMsg(String cmd) {
        Log.e(TAG, "this is end cmd:" + cmd);
    }
}
