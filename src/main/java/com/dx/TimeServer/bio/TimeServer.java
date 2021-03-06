package com.dx.TimeServer.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * @author dx
 * @date 2018/11/3 8:17 AM
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;

        if (args != null && args.length != 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException numException) {
                numException.printStackTrace();
            }

        }
        // 创建一个新的ServerSocket，用以监听指定端口上的连接请求
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            while (true) {
                // 对accept方法的调用将被阻塞，直到一个连接的建立
                Socket socket = serverSocket.accept();
                new Thread(new HandleAccept(socket)).start();
            }
        } catch (IOException ioExcep) {
            ioExcep.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    System.out.println("The Server is Close");
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException ioException) {

                }
            }
        }
    }

}

class HandleAccept implements Runnable {

    public Socket socket = null;

    private HandleAccept() {
    }

    public HandleAccept(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            // 获取套接字的流对象
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("客户端的IP为：" + socket.getInetAddress().toString());
            String currentTime = null;
            String body = null;
            while (true) {
                body = bufferedReader.readLine();
                System.out.println(body);
                if (body == null) {
                    break;
                }
                System.out.println("The time server receive order:" + body);
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "Bad Order";
                // 服务器的响应发送给客户端
                printWriter.println(currentTime);
            }
        } catch (IOException ioException) {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioExcep) {
                    ioExcep.printStackTrace();
                    bufferedReader = null;
                }
            }
            if (printWriter != null) {
                printWriter.close();
                printWriter = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioExcep) {
                    ioExcep.printStackTrace();
                }
            }
            this.socket = null;
        }
    }
}