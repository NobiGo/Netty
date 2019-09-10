package com.dx.Echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @Author: duanxiong5
 * @Date: 2018/12/12 9:16
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        EchoServer echoServer = new EchoServer(8000);
        echoServer.start();
    }

    private void start() throws Exception {
        final EchoServerHandler echoServerHandler = new EchoServerHandler();
        // NioEventLoopGroup进行事件的处理，如接受新连接，或者读写数据
        // 两个场景下共用同一个EventLoopGroup
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup).
                    channel(NioServerSocketChannel.class).
                    // 指定服务器绑定的本地的InetSocketAddress
                            localAddress(new InetSocketAddress(port)).
                    // ChannelInitializer将会把一个EchoServerHandler的实例添加到该Channel的ChannelPipeline中
                            childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(echoServerHandler);
                        }
                    });
            // 异步绑定服务器，调用sync方法阻塞等待直到绑定完成
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    // EchoServerHandler实现了业务逻辑
    @ChannelHandler.Sharable
    class EchoServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("建立连接");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            // 将消息记录到控制台
            System.out.println("Server received: " + byteBuf.toString(CharsetUtil.UTF_8));
            // 将接受到的消息发给发送者，而不充刷出站消息
            ctx.write(byteBuf);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // 将未决消息冲刷到远程节点，并且关闭该Channel
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 打印异常栈跟踪
            cause.printStackTrace();
            // 关闭该Channel
            ctx.close();
        }
    }
}
