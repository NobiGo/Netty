package com.dx.TimeServer.fbio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dx
 * @date 2018/11/3 10:37 AM
 */
public class TimeServer {
    public ServerSocket serverSocket;
    public TimeServerHandlerExecutePool timeServerHandlerExecutePool;

    public static void main(String[] args) {
        TimeServer timeServer = new TimeServer();
        int point = 8000;
        if (args != null && args.length != 0) {
            try {
                point = Integer.parseInt(args[0]);
            } catch (NumberFormatException numberExcep) {
                System.out.println("采用默认端口号：" + point);
            }
        }
        timeServer.startService(point);
        timeServer.initThreadPool();
        timeServer.accept();
    }

    // 开启服务端
    public void startService(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务端开启，端口号为：" + port);
        } catch (IOException ioException) {
            System.out.printf("服务端开启失败");
            System.exit(1);
        }
    }

    //初始化线程池
    public void initThreadPool() {
        timeServerHandlerExecutePool = new TimeServerHandlerExecutePool(30, 30);
    }

    // 接受请求
    public void accept() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                timeServerHandlerExecutePool.execute(new TimeServerHandler(socket));
            } catch (IOException ioExcep) {
                ioExcep.printStackTrace();
            }
        }
    }
}

class TimeServerHandlerExecutePool {
    private ExecutorService executorService;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSize) {
        executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS, new ArrayBlockingQueue<java.lang.Runnable>(queueSize));
    }

    public void execute(java.lang.Runnable task) {
        executorService.execute(task);
    }
}

class TimeServerHandler implements Runnable {

    public Socket socket;
    public BufferedReader bufferedReader;
    public PrintWriter printWriter;

    public TimeServerHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            String body = null;
            while (true) {
                body = bufferedReader.readLine();
                if (body == null) {
                    break;
                }
                System.out.println("收到来自客户端IP" + socket.getInetAddress().toString() + "的消息：" + body);
                if ("QUERY TIME ORDER".equalsIgnoreCase(body)) {
                    printWriter.println(new Date(System.currentTimeMillis()));
                } else {
                    printWriter.println("bad order");
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ioExcep) {
                ioExcep.printStackTrace();
            }
            bufferedReader = null;
        }
        if (printWriter != null) {
            printWriter.close();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }


    }
}