package com.dx.TimeServer.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dx
 * @date 2018/11/5 9:45 PM
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8000;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(port);
            } catch (NumberFormatException numExcep) {
                System.out.println("使用默认端口号：" + port);
            }
        }
        new Thread(new TimeClientHandle("127.0.0.1", port)).run();
    }
}

class TimeClientHandle implements Runnable {

    private String addr = null;
    private Integer port = 0;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;


    public TimeClientHandle(String addr, Integer port) {
        this.addr = addr;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException ioExvep) {
            ioExvep.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
            System.exit(1);
        }
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(key);
                    } catch (Exception excep) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Exception exCep) {
                exCep.printStackTrace();
                System.exit(1);
            }
        }
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException ioExcep) {
                ioExcep.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        // 对Key进行判断，看处于什么状态
        if (key.isValid()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (socketChannel.finishConnect()) {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    doWrite(socketChannel);
                } else {
                    System.exit(1);
                }
            }
            if (key.isReadable()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(byteBuffer);
                if (readBytes > 0) {
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is " + body);
                    this.stop = true;
                } else {
                    ;
                }
            }
        }
    }


    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(addr, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(req.length);
        byteBuffer.put(req);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if (!byteBuffer.hasRemaining()) {
            System.out.println("Send over 2 server succeed");
        }
    }
}
