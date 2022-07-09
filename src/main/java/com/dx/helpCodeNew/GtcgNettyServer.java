package com.dx.helpCodeNew;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/14 9:06
 */
public class GtcgNettyServer {

    // 固定信息长度
    private Integer messageFlagFixLength = 5;
    private Integer messageFixLength = 5;

    private Integer messageFullFixLength = messageFlagFixLength + messageFixLength;

    public static void main(String[] args) throws Exception {
        final String message = "fdafdasfdasfdsafdasfdasfdasfdasfdasfdasfdasfdas";
        GtcgNettyServer gtcgNettyServer = new GtcgNettyServer(8000, message, "UTF-8");
        gtcgNettyServer.bind();
    }

    private Integer port;
    private String message;

    protected String encoding;

    public GtcgNettyServer(Integer port, String message, String encoding) {
        this.port = port;
        this.message = message;
        this.encoding = encoding;
    }

    public void bind() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(messageFullFixLength));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    class EchoServerHandler extends ChannelHandlerAdapter {
        int count = 0;

        StringBuilder messageBuilder = new StringBuilder();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 将收到的消息直接打印出来
            String body = (String) msg;
            System.out.println("This is " + ++count + " times receive client: [ " + body + " ]");
            body += "$_";
            ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
            ctx.writeAndFlush(echo);


//            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
//            inputStream = _dataSource.getInputStream();
//            DataInputStream _in = new DataInputStream(inputStream);
//            byte[] headLength = new byte[5];
//            _in.readFully(headLength);
//            int i = Integer.parseInt(new String(headLength).substring(0, 4));
//            String endFlag = new String(headLength).substring(4, 5);
//            byte[] bodyArrayOfByte = new byte[i];
//            _in.readFully(bodyArrayOfByte);
//            localByteArrayOutputStream.write(bodyArrayOfByte);
//            while (!endFlag.equals("0")) {
//                headLength = new byte[5];
//                _in.readFully(headLength);
//                i = Integer.parseInt(new String(headLength).substring(0, 4));
//                endFlag = new String(headLength).substring(4, 5);
//                bodyArrayOfByte = new byte[i];
//                _in.readFully(bodyArrayOfByte);
//                localByteArrayOutputStream.write(bodyArrayOfByte);
//            }
//            byte[] arrayOfByte3 = localByteArrayOutputStream.toByteArray();
//            return new String(arrayOfByte3, this.encoding);


        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
