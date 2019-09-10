package com.dx.netty.handler.codec.msgpack;

/**
 * @Author: duanxiong5
 * @Date: 2018/11/15 20:35
 */
public class UserInfo {
    private int age;
    private String name;

    public UserInfo() {

    }

    public UserInfo(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
