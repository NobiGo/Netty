package com.dx.helpCodeNew;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/14 9:31
 */
public class GtcgNettyClient {


    public static void main(String[] args) throws Exception {
        final String message = "fdafdasfdasfdsafdasfdasfdasfdasfdasfdasfdasfdas";
        GtcgNettyClient gtcgNettyClient = new GtcgNettyClient("127.0.0.1", 8000, message, "UTF-8");
        gtcgNettyClient.connect();
    }

    private String host;
    private Integer port;
    private String message;

    // 固定信息长度
    private Integer messageFlagFixLength = 5;
    private Integer messageFixLength = 5;

    private Integer messageFullFixLength = messageFlagFixLength + messageFixLength;
    protected String encoding;

    public GtcgNettyClient(String host, Integer port, String message, String encoding) {
        this.host = host;
        this.port = port;
        this.message = message;
        this.encoding = encoding;
    }

    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(messageFullFixLength));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new EchoClientHandler(message, encoding));
                        }
                    });
            // 发起异步连接操作
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // 等待客户端链路关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    class EchoClientHandler extends ChannelHandlerAdapter {
        private String message;

        protected String encoding;

        private int counter;

        public EchoClientHandler(String message, String encoding) {
            this.message = message;
            this.encoding = encoding;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 建立连接后，将消息给服务端
            byte[] arrayOfByte = message.getBytes(this.encoding);
            int messageLengh = arrayOfByte.length;
            if (messageLengh > messageFixLength) {
                int j = messageLengh / messageFixLength;
                int k = messageLengh % messageFixLength;
                for (int m = 1; m <= j; m++) {
                    if (m == j) {
                        if (k == 0) {
                            byte[] messageFlagByte = ("4091" + 0).getBytes(this.encoding);
                            byte[] messageByte = new byte[messageFixLength];
                            System.arraycopy(arrayOfByte, (m - 1) * messageFixLength, messageByte, 0, messageFixLength);
                            ctx.write(Unpooled.copiedBuffer(mergeByte(messageFlagByte, messageByte)));
                            ctx.flush();
                            break;//全部写完退出
                        }
                    }
                    byte[] messageFlagByte = ("4091" + 1).getBytes(this.encoding);
                    byte[] messageByte = new byte[messageFixLength];
                    System.arraycopy(arrayOfByte, (m - 1) * messageFixLength, messageByte, 0, messageFixLength);
                    ctx.write(Unpooled.copiedBuffer(mergeByte(messageFlagByte, messageByte)));
                    ctx.flush();
                }
                if (k != 0) {//尾巴处理
                    int lastMessageLengh = messageLengh - j * messageFixLength;//剩余长度
                    byte[] messageFlagByte = (fillstr(String.valueOf(lastMessageLengh), 4) + "0").getBytes(this.encoding);
                    byte[] messageByte = new byte[lastMessageLengh];
                    System.arraycopy(arrayOfByte, j * messageFixLength, messageByte, 0, lastMessageLengh);
                    byte[] fillter = new byte[messageFixLength - lastMessageLengh];
                    Arrays.fill(fillter, (byte) 0);
                    ctx.write(Unpooled.copiedBuffer(mergeByte(messageFlagByte, mergeByte(messageByte, fillter))));
                    ctx.flush();
                }
            } else {
                byte[] messageFlagByte = (fillstr(String.valueOf(messageLengh), 4) + "0").getBytes(this.encoding);
                byte[] fillter = new byte[messageFixLength - messageLengh];
                Arrays.fill(fillter, (byte) 0);
                ctx.write(Unpooled.copiedBuffer(mergeByte(messageFlagByte, mergeByte(arrayOfByte, fillter))));
                ctx.flush();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("This is " + ++counter + " times receive server : [" + msg + " ]");
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        private String fillstr(String paramString, int paramInt) {
            String str = paramString;
            int i = paramString.getBytes().length;
            for (int j = i; j < paramInt; j++) {
                str = "0" + str;
            }
            return str;
        }

        private byte[] mergeByte(byte[] byte1, byte[] byte2) {
            byte[] fullMessage = new byte[byte1.length+byte2.length];
            System.arraycopy(byte1, 0, fullMessage, 0, byte1.length);
            System.arraycopy(byte2, 0, fullMessage, byte1.length, byte2.length);
            return fullMessage;
        }
    }
}
