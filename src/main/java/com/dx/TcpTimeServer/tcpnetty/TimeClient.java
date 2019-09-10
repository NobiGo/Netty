package com.dx.TcpTimeServer.tcpnetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.util.logging.Logger;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/13 19:32
 */
public class TimeClient {

    public static void main(String[] args) throws Exception {
        new TimeClient().connect(8000, "127.0.0.1");

    }

    public void connect(int port, String host) throws Exception {
        // 客户端处理I/O读写的NioEventLoopGroup线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建客户端辅助启动类Bootstrap
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 将ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件
                    socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    socketChannel.pipeline().addLast(new StringDecoder());
                    socketChannel.pipeline().addLast(new TimeClientHandler());
                }
            });
            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 等待客户端链路关闭
            future.channel().closeFuture().sync();
        } finally {
            // 释放NIO线程组
            group.shutdownGracefully();
        }
    }

    private class TimeClientHandler extends ChannelHandlerAdapter {
        private final Logger LOGGER = Logger.getLogger(TimeClientHandler.class.getName());
        private ByteBuf firstMessage;
        private byte[] req;
        private int counter;

        public TimeClientHandler() {
            req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 将请求消息发送给服务端
            for (int i = 0; i < 100; i++) {
                firstMessage = Unpooled.buffer(req.length);
                firstMessage.writeBytes(req);
                // 每写入一次就刷新一次
                ctx.writeAndFlush(firstMessage);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 拿到的Message已经是字符串之后的应答消息了
            String body = (String) msg;
            System.out.println("Now is : " + body + "; the counter is:" + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 释放资源
            LOGGER.warning("Unecpected exception from downstream : " + cause.getMessage());
            ctx.close();
        }
    }
}
