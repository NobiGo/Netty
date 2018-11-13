package com.dx.TimeServer.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author dx
 * @date 2018/11/3 8:00 AM
 */
public class TimeClient {
    public static void main(String[] args) {
        // 制定需要进行连接的端口号
        int port = 8000;
        if(args!=null&&args.length!=0){
            try{
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException numberException){
                numberException.printStackTrace();
                System.out.println("采用默认值：8000");
            }
        }
        Socket socket = null;
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try{
            socket = new Socket("127.0.0.1",port);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(),true);
            printWriter.println("QUERY TIME ORDER");
            System.out.println("Send order 2 server succeed");
            String resp = bufferedReader.readLine();
            System.out.println("Now is"+resp);
        }catch (Exception exception){

        }finally {
            if(printWriter!=null){
                printWriter.close();
            }
            if(bufferedReader!=null){
                try {
                    bufferedReader.close();
                }catch (IOException ioExcep){
                 ioExcep.printStackTrace();
                }
            }
            if(socket!=null){
                try{
                    socket.close();
                }catch (IOException ioExcep){
                    ioExcep.printStackTrace();
                }
            }
            socket = null;
        }
    }
}
