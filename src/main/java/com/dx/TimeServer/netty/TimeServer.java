package com.dx.TimeServer.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Date;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/13 18:57
 */
public class TimeServer {

    public void bind(int port) throws Exception {
        // 用于服务端接受客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用于用户进行SocketChannel的网络读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 是Netty用于启动NIO服务器的辅助启动类，降低服务端的开发复杂度
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChildChannelHandler());
            // 绑定端口，同步等待成功（sync等待绑定完成）
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 等待服务器端口关闭（等待服务器链路关闭之后main函数才推出）
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    // ChannelHandlerAdapter 用于对网络事件进行读写操作
    private class TimeServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 将msg转换为Netty的ByteBuf对象
            ByteBuf byteBuf = (ByteBuf) msg;
            // 获取缓冲区可读的字节数，根据可读字节数创建byte数组
            byte[] req = new byte[byteBuf.readableBytes()];
            // 将缓冲区中的字节数组复制到新建的byte数组中
            byteBuf.readBytes(req);
            // 获取请求消息内容
            String body = new String(req, "UTF-8");
            System.out.println("The time server receive order:" + body);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
            // 异步发送消息给客户端
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // 将消息发送对来中的消息写入到SocketChannel中发送给对方
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new TimeServer().bind(8000);
    }
}

