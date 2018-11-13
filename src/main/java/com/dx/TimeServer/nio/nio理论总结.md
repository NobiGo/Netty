1. 相对阻塞I/O的ServerSocket和Socket，非阻塞I/O提供了SocketChannel和ServerSocketChannel。
2. 缓冲区Buffer
    1. ByteBuffer
    2. CharBuffer
    3. ShortBuffer
    4. IntBuffer
    5. LongBuffer
    6. FloatBuffer
    7. DoubleBuffer

3. 通道Channel（网络数据通过通道进行读取和写入）
4. 多路复用器Selector（提供选择已经就绪的任务的能力）
    Selector会不断的轮询注册在其上的Channel，如果某个Channel上面发生读或者写事件，这个Channel就处于就绪状态，会被Selector轮询出来，然后通过
    SelectionKey可以获取就绪Channel的集合，进行后续的I/O操作。

优点总结：
1. 客户端发起的连接是异步的，可以通过在多路复用器注册OP_CONNECT等待后续结果，不需要像之前的客户端那样被同步阻塞
2. SocketChannel的读写操作是异步的，如果没有数据，不会同步等待，直接返回，这样I/O通信线程就可以处理其他线路

## NIO进行服务器开发步骤

1. 创建ServerSocketChannel，配置为非阻塞模式
2. 绑定监听，配置TCP参数，例如backlog大小
3. 创建独立的线程用于轮训多路复用器Selector
4. 创建Selector，将之前创建的ServerSocketChannel注册到Selector上，监听SelectionKey.ACCEPT
5. 启动I/O线程，在循环体执行Selector.select()方法，轮训就绪的Channel
6. 当轮训到了处于就绪状态的Channel时，需要对其进行判断，如果是OP_ACCEPT状态，说明是新的客户端接入，则调用ServerSocketChannel.accept()方法接受新的客户端
7. 设置新截图的客户端链路SocketChannel为非阻塞模式，配置其他的一些TCP参数
8. 将SocketChannel注册到Selector上，监听OP_READ操作位
9. 如果轮训的Channel为OP_READ，则说明SocketChannel中有新的就绪的数据包需要读取，则构造ByteBuffer对象，读取数据包
10. 如果轮训的Channel为OP_WRITE，说明还有数据没有发送完成，需要继续进行发送。



