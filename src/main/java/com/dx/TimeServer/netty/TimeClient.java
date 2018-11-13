package com.dx.TimeServer.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
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
        private final ByteBuf firstMessage;

        public TimeClientHandler() {
            byte[] req = "QUERY TIME ORDER".getBytes();
            firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(firstMessage);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf)msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req,"UTF-8");
            System.out.println("Now is : "+body);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 释放资源
            LOGGER.warning("Unecpected exception from downstream : "+cause.getMessage());
            ctx.close();
        }
    }
}
