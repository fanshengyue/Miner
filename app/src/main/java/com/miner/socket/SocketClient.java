package com.miner.socket;

import android.util.Log;

import com.miner.listener.OnSocketStateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mr.Zhang on 2017/12/18.
 */

public class SocketClient {
    static Socket client;
    OnSocketStateListener listener;
    private String host = "";
    private int port = 7777;
    private String message;
    private String TAG = "SocketClient";

    public SocketClient(String shost, int sport, OnSocketStateListener listener) {
        this.listener = listener;
        host = shost;
        port = sport;
        conn();
    }


    private void conn() {
        try {

            client = new Socket();
            //设置连接超时
            client.connect(new InetSocketAddress(host, port), 5000);
            client.setSoTimeout(10000);
            //            System.out.println("Client is created! host:" + host + " port:" + port);
            //listener.socketstate("Connected");
            //            while (!isClient) {
            //                client.sendUrgentData(0xFF); // 发送心跳包
            //                Thread.sleep(2 * 1000);//线程睡眠2秒
            //            }
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (canConnectToServer()) {
//                        listener.socketstate("DisConnected");
//                        conn();
//                    } else {
//                        listener.socketstate("Connected");
//                    }
//                }
//            }).start();

        } catch (Exception e) {
            listener.socketstate("DisConnected");
            e.printStackTrace();
            conn();
        }
    }

    /**
     * 服务器是否关闭，通过发送一个socket信息
     *
     * @return
     */
    public boolean canConnectToServer() {
        try {
            if (client != null) {
                client.sendUrgentData(0xff);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Socket getClient() {
        return client;
    }

    public String sendMsg(String msg) {
        try {
//            if (canConnectToServer()) {
//                conn();
//            } else {
                message = msg;
                //System.out.println(msg);
                listener.socketstate("Transmission");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream());
                out.println(msg);
                out.flush();
                listener.socketstate("Transmission Completion");
                return in.readLine();
//            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.socketstate("Transmission abnormal" + e.toString());
            //            conn();
            sendMsg(message);
            Log.e(TAG, "sendMsg: " + message);
        }
        return "";
    }

    public void closeSocket() {
        try {
            listener.socketstate("Socket is closed");
            client.close();
            client = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
