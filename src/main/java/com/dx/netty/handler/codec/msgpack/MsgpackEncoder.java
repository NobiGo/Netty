package com.dx.netty.handler.codec.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/15 19:53
 */


/**
 * 负责将Object类型的POJO对象编码为byte数组，然后写入到ByteBuf中
 */
public class MsgpackEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        MessagePack messagePack = new MessagePack();
        // Serialize
        byte[] raw = messagePack.write(o);
        byteBuf.writeBytes(raw);
    }


}
