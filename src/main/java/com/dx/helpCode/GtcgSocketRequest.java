package com.dx.helpCode;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class GtcgSocketRequest {

    protected Socket _dataSource;//客户端连接器

    protected OutputStream outputStream;//返回流

    protected String msg;

    protected byte[] arrayOfByte;

    protected String encoding = "gbk";

    protected InputStream inputStream;

    /**
     * 初始化数据
     */
    public void init(String ipAddress, int port, String msg, String encoding, int timeout) throws IOException {
        this.init2(ipAddress, port, msg, "gbk", timeout);
    }

    public void init2(String ipAddress, int port, String msg, String encoding, int timeout) throws IOException {
        InetSocketAddress localInetSocketAddress = new InetSocketAddress(ipAddress, port);
        _dataSource = connect(localInetSocketAddress, 2);
        _dataSource.setSoTimeout(timeout);
        this.msg = msg;
        this.arrayOfByte = this.msg.getBytes(encoding);
        this.encoding = encoding;
        this.outputStream = _dataSource.getOutputStream();
    }

    /**
     * 尝试连接
     */
    private Socket connect(SocketAddress paramSocketAddress, int time) throws IOException {
        Socket localSocket = null;
        int i = 1;
        while (i <= time) {
            try {
                localSocket = new Socket();
                localSocket.connect(paramSocketAddress, 10000 / i);
                return localSocket;
            } catch (SocketTimeoutException locaSocketTimeoutException) {
                i++;
                if (localSocket != null) {
                    localSocket.close();
                    localSocket = null;
                }
            } catch (IOException localIOException) {
                throw localIOException;
            }
        }
        if ((localSocket == null) && (i == time + 1)) {
            throw new IOException("重连超时");
        }
        return localSocket;
    }

    /**
     * 关闭
     */
    void close() throws IOException {
        if (_dataSource.isClosed()) {
            _dataSource.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    /**
     * 发送数据
     */
    void send() throws IOException {
        arrayOfByte = msg.getBytes(this.encoding);
        int i = arrayOfByte.length;
        ByteArrayOutputStream localByteArrayOutputStream;
        if (i > 4091) {
            int j = i / 4091;
            int k = i % 4091;
            String endFlag = "1";
            for (int m = 1; m <= j; m++) {
                if (m == j) {
                    if (k == 0) {
                        endFlag = "0";
                        localByteArrayOutputStream = new ByteArrayOutputStream();
                        localByteArrayOutputStream.write(("4091" + endFlag).getBytes(this.encoding));
                        localByteArrayOutputStream.write(getBytes(arrayOfByte, m - 1));
                        outputStream.write(localByteArrayOutputStream.toByteArray());
                        outputStream.flush();
                        break;//全部写完退出
                    }
                    endFlag = "1";
                }
                localByteArrayOutputStream = new ByteArrayOutputStream();
                localByteArrayOutputStream.write(("4091" + endFlag).getBytes(this.encoding));
                localByteArrayOutputStream.write(getBytes(arrayOfByte, m - 1));
                outputStream.write(localByteArrayOutputStream.toByteArray());
                outputStream.flush();
            }
            if (k != 0) {//尾巴处理
                int z = i - j * 4091;//剩余长度
                localByteArrayOutputStream = new ByteArrayOutputStream();
                localByteArrayOutputStream.write((fillstr(String.valueOf(z), 4) + "0").getBytes(this.encoding));
                localByteArrayOutputStream.write(getBytes(arrayOfByte, j, z));
                outputStream.write(localByteArrayOutputStream.toByteArray());
                outputStream.flush();
            }
        } else {
            localByteArrayOutputStream = new ByteArrayOutputStream();
            localByteArrayOutputStream.write((fillstr(String.valueOf(i), 4) + "0").getBytes(this.encoding));
            localByteArrayOutputStream.write(arrayOfByte);
            outputStream.write(localByteArrayOutputStream.toByteArray());
            outputStream.flush();
        }
    }

    /**
     * 接收数据
     *
     * @return
     */
    String receive() throws IOException {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        inputStream = _dataSource.getInputStream();
        DataInputStream _in = new DataInputStream(inputStream);
        byte[] headLength = new byte[5];
        _in.readFully(headLength);
        int i = Integer.parseInt(new String(headLength).substring(0, 4));
        String endFlag = new String(headLength).substring(4, 5);
        byte[] bodyArrayOfByte = new byte[i];
        _in.readFully(bodyArrayOfByte);
        localByteArrayOutputStream.write(bodyArrayOfByte);
        while (!endFlag.equals("0")) {
            headLength = new byte[5];
            _in.readFully(headLength);
            i = Integer.parseInt(new String(headLength).substring(0, 4));
            endFlag = new String(headLength).substring(4, 5);
            bodyArrayOfByte = new byte[i];
            _in.readFully(bodyArrayOfByte);
            localByteArrayOutputStream.write(bodyArrayOfByte);
        }
        byte[] arrayOfByte3 = localByteArrayOutputStream.toByteArray();
        return new String(arrayOfByte3, this.encoding);
    }

    /**
     * 拆数据包
     */
    private byte[] getBytes(byte[] paramArrayOfByte, int paramInt) {
        byte[] arrayOfByte = new byte[4091];
        int i = 0;
        int j = paramInt * 4091;
        while (j < (paramInt + 1) * 4091) {
            arrayOfByte[i] = paramArrayOfByte[j];
            j++;
            i++;
        }
        return arrayOfByte;
    }

    /**
     * 拆分byte[]数组
     */
    private byte[] getBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
        byte[] arrayOfByte = new byte[paramInt2];
        int i = 0;
        int j = paramInt1 * 4091;
        while (j < paramInt1 * 4091 + paramInt2) {
            arrayOfByte[i] = paramArrayOfByte[j];
            j++;
            i++;
        }
        return arrayOfByte;
    }


    /**
     * 数字类型填充长度
     */
    private String fillstr(String paramString, int paramInt) {
        String str = paramString;
        int i = paramString.getBytes().length;
        for (int j = i; j < paramInt; j++) {
            str = "0" + str;
        }
        return str;
    }


}