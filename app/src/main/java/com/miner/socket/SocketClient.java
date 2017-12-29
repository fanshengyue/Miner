package com.miner.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.miner.listener.OnSocketStateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mr.Zhang on 2017/12/18.
 */

public class SocketClient {
    static Socket client;
    OnSocketStateListener listener;
    private int port = 7777;
    private String message;
    private String TAG = "SocketClient";
    /*连接线程*/
    private Thread connectThread;
    private Timer timer = new Timer();
    private OutputStream outputStream;

    private String host;
    private TimerTask task;
    /*默认重连*/
    private boolean isReConnect = true;


    public SocketClient(String shost, int sport, OnSocketStateListener listener) {
        this.listener = listener;
        host = shost;
        port = sport;
        conn();
    }
//    public void setIsReConnect(boolean b){
//        isReConnect=b;
//    }
    public void onDestroy(){
        isReConnect = false;
        releaseSocket();
    }

    private void conn() {
        if (client == null && connectThread == null) {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    client = new Socket();
                    //设置连接超时
                    try {
                        client.connect(new InetSocketAddress(host, port), 5000);
                        if (client.isConnected()) {
                           /*发送心跳数据*/
                            sendBeatData();
                            listener.socketstate("Connected");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.socketstate("Disconnected");
                        releaseSocket();
                    }
                }
            });
              /*启动连接线程*/
            connectThread.start();
        }
    }

    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        outputStream = client.getOutputStream();
                        /*这里的编码方式根据你的需求去改*/
                        outputStream.write(("test").getBytes("gbk"));
                        outputStream.flush();
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        listener.socketstate("Disconnected");
                        /*重连*/
                        releaseSocket();
                        e.printStackTrace();


                    }
                }
            };
        }

        timer.schedule(task, 0, 2000);
    }


    /*释放资源*/
    private void releaseSocket() {

        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (client != null) {
            try {
                client.close();

            } catch (IOException e) {
            }
            client = null;
        }

        if (connectThread != null) {
            connectThread = null;
        }

          /*重新初始化socket*/
        if (isReConnect) {
            conn();
        }

    }

    //    /**
    //     * 服务器是否关闭，通过发送一个socket信息
    //     *
    //     * @return
    //     */
    //    public boolean canConnectToServer() {
    //        try {
    //            if (client != null) {
    //                client.sendUrgentData(0xff);
    //            }
    //        } catch (IOException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //            return false;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            return false;
    //        }
    //        return true;
    //    }

//    public Socket getClient() {
//        return client;
//    }

//    public String sendMsg(String msg) {
//            try {
//                message = msg;
//                //System.out.println(msg);
//                listener.socketstate("Transmission");
//                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                PrintWriter out = new PrintWriter(client.getOutputStream());
//                out.println(msg);
//                out.flush();
//                listener.socketstate("Transmission Completion");
//                return in.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//                listener.socketstate("Transmission abnormal" + e.toString());
//                //            conn();
//                sendMsg(message);
//                Log.e(TAG, "sendMsg: " + message);
//            }
//            return "";
//        }

    /*发送数据*/
    public void sendOrder(final String order) {
        if (client != null && client.isConnected()) {
            /*发送指令*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream = client.getOutputStream();
                        if (outputStream != null) {
                            PrintWriter out = new PrintWriter(outputStream);
                            out.println(order);
                            out.flush();
                            listener.socketstate("Transmission Completion");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } else {
            listener.socketstate("Disconnected");
        }
    }

//    public void closeSocket() {
//        try {
//            listener.socketstate("Socket is closed");
//            client.close();
//            client = null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
