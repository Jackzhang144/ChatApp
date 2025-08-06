package com.easychat.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(1024)) {
            System.out.println("服务已启动，等待客户端连接");
            Socket socket = server.accept();
            String ip = socket.getInetAddress().getHostAddress();
            System.out.println("有客户端连接ip:" + ip + "端口：" + socket.getPort());

            new Thread(() -> {
                while (true) {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"GBK");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String readData = bufferedReader.readLine();
                        System.out.println("收到客户端消息->" + readData);

                        OutputStream outputStream = socket.getOutputStream();
                        PrintWriter printWriter = new PrintWriter(outputStream);
                        printWriter.println("我是服务端，我已经收到了你发的消息：" + readData);
                        printWriter.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
