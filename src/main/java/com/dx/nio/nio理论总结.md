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
