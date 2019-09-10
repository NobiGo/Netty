package com.dx.netty.handler.codec.msgpack;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/15 19:34
 */
public class Demo {
    byte[] values = null;

    public static void main(String[] args) throws IOException {
        Demo demo = new Demo();
        demo.enCode();
        demo.deCode();
    }

    public void enCode() throws IOException {
        List<String> src = new ArrayList<String>();
        src.add("msgpack");
        src.add("kumofs");
        src.add("viver");
        MessagePack messagePack = new MessagePack();
        values = messagePack.write(src);
    }

    public void deCode() throws IOException {
        MessagePack messagePack = new MessagePack();
        List<String> dst = messagePack.read(values, Templates.tList(Templates.TString));
        for (String string : dst) {
            System.out.println(string);
        }
    }

}
