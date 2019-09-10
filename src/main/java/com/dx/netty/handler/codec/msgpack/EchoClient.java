package com.dx.netty.handler.codec.msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/15 20:08
 */
public class EchoClient {
    private final int port;
    private final String address;
    private final int sendNumber;


    public static void main(String[] args) throws Exception {
        EchoClient echoClient = new EchoClient(8000, "127.0.0.1", 3);
        echoClient.run();
    }

    public EchoClient(int port, String address, int sendNumber) {
        this.port = port;
        this.address = address;
        this.sendNumber = sendNumber;
    }

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("msgpack decoder", new MsgpackDecoder());
                            socketChannel.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler(sendNumber));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(address, port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException interExcep) {
            interExcep.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    class EchoClientHandler extends ChannelHandlerAdapter {

        private final int sendNumber;

        public EchoClientHandler(int sendNumber) {
            this.sendNumber = sendNumber;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            UserInfo[] userInfos = userInfos();
            for (UserInfo userInfo : userInfos) {
                ctx.write(userInfo);
            }
            ctx.flush();
        }

        private UserInfo[] userInfos() {
            UserInfo[] userInfos = new UserInfo[sendNumber];
            for (int i = 0; i < userInfos.length; i++) {
                userInfos[i] = new UserInfo();
                userInfos[i].setAge(12);
                userInfos[i].setName("userInfo========>" + i);
            }
            return userInfos;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Client receive the msgpack message : " + msg);
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }
}
