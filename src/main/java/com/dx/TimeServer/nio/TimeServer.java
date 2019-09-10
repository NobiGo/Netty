package com.dx.TimeServer.nio;

/**
 * @author dx
 * @date 2018/11/4 9:01 AM
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 8000;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException numberExcep) {
                System.out.println("采用默认端口：" + port);
            }
        }
        MultiplexerTimeServer multiplexerTimeServer = new MultiplexerTimeServer(port);
        new Thread(multiplexerTimeServer, "NIO-MultiplexerTimeServer-001").start();
    }
}


