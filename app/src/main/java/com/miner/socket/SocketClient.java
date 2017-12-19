package com.miner.socket;

import android.util.Log;

import com.miner.listener.OnSocketStateListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mr.Zhang on 2017/12/18.
 */

public class SocketClient {
    static Socket client;
    OnSocketStateListener listener;
    private String host="192.168.0.110";
    private int port=50000;
    private String message;

    public SocketClient(String shost, int sport,OnSocketStateListener listener) {
        this.listener = listener;
        host = shost;
        port = sport;
        conn();
    }


    private void conn() {
        try {
            listener.socketstate("Connecting");
            client = new Socket(host, port);
            System.out.println("Client is created! host:" + host + " port:" + port);
            listener.socketstate("Connected");
        } catch (UnknownHostException e) {
            listener.socketstate("DisConnected");
            e.printStackTrace();
        } catch (IOException e) {
            listener.socketstate("DisConnected");
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }

    public String sendMsg(String msg) {
        try {
            message=msg;
            listener.socketstate("Transmission");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(msg);
            out.flush();
            listener.socketstate("Transmission Completion");
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            listener.socketstate("Transmission abnormal" + e.toString());
            conn();
            sendMsg(message);
            Log.e("TAG", "sendMsg: "+message );
        } finally {
            //关闭Socket
            try {
                client.close();
                System.out.println("Socket is closed");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    public void closeSocket() {
        try {
            listener.socketstate("Socket is closed");
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
