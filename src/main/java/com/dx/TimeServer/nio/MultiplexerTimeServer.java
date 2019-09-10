package com.dx.TimeServer.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dx
 * @date 2018/11/4 9:19 AM
 */
public class MultiplexerTimeServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop = false;

    public MultiplexerTimeServer(int port) {
        try {
            // 创建多路复用器
            selector = Selector.open();
            // 打开ServerSocketChannel，用于监听客户端的连接，是所有客户端连接的父管道
            serverSocketChannel = ServerSocketChannel.open();
            // 配置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定监听端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            // 将ServerSocketChannel注册到多路复用器Selector上，监听Accept事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("时间服务器开启端口：" + port);
        } catch (IOException ioExcep) {
            ioExcep.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        stop = true;
    }

    public void run() {
        // 多路复用器在线程run方法的无限循环体内轮询准备就绪的key
        while (!stop) {
            try {
                // selector的休眠时间为1秒
                selector.select(1000);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iteratorKeys = selectedKeys.iterator();
                SelectionKey key = null;
                while (iteratorKeys.hasNext()) {
                    key = iteratorKeys.next();
                    iteratorKeys.remove();
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
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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

    public void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                // 监听到有新的请求接入，处理新的接入请求
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                // 接受客户端的连接请求并创建SocketChannel实例
                SocketChannel socketChannel = serverSocketChannel.accept();
                // 将SocketChannel设置为异步非阻塞模式
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                // 1M缓冲区的ByteBuffer
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(readBuffer);
                if (readBytes > 0) {
                    // 将limit设置为position，position设置为0，用于后续对缓冲区的读操作
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    // 读取到码流后需要进行解码
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order:" + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    doWrite(socketChannel, currentTime);
                } else if (readBytes < 0) {
                    key.channel();
                    socketChannel.close();
                } else {
                }
            }
        }
    }

    private void doWrite(SocketChannel socketChannel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            // 首先将字符串转换为字节数组
            byte[] bytes = response.getBytes();
            // 根据字节数组的容量创建bytebuffer
            ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
            // 将字节数组放入缓冲区
            byteBuffer.put(bytes);
            // 将缓冲区变为写模式
            byteBuffer.flip();
            // 由于SocketChannel是非阻塞的，因此并不能保证一次能把需要发送的字节数组发送完
            socketChannel.write(byteBuffer);
        }

    }
}
