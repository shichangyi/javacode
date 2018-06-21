package com.ec.study.demo1.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
	// 信道选择器
	private Selector selector;

	// 与服务器通信的信道
	SocketChannel socketChannel;

	/**
	 * 构造函数
	 * 
	 * @param HostIp
	 * @param HostListenningPort
	 * @throws IOException
	 */
	public Client() throws IOException {
		initialize();
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		// 打开监听信道并设置为非阻塞模式
		socketChannel = SocketChannel.open(new InetSocketAddress(Common.host, Common.port));
		socketChannel.configureBlocking(false);
		// 打开并注册选择器到信道
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 启动读取线程
		startReadThread();
	}

	private void startReadThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (selector.select() > 0) {
						// 遍历每个有可用IO操作Channel对应的SelectionKey
						for (SelectionKey sk : selector.selectedKeys()) {

							// 如果该SelectionKey对应的Channel中有可读的数据
							if (sk.isReadable()) {
								// 使用NIO读取Channel中的数据
								SocketChannel sc = (SocketChannel) sk.channel();
								ByteBuffer buffer = ByteBuffer.allocate(1024);
								int res = sc.read(buffer);
								if (res < 1) {
									continue;
								}
								buffer.flip();

								// 将字节转化为为UTF-16的字符串
								String receivedString = Charset.forName(Common.charset).newDecoder().decode(buffer)
										.toString();

								// 控制台打印出来
								System.out.println("客户端 <- 服务器:" + sc.socket().getRemoteSocketAddress() + "的信息: "
										+ receivedString);
								buffer.clear();
								// 为下一次读取作准备
								sk.interestOps(SelectionKey.OP_READ);
							}

							// 删除正在处理的SelectionKey
							selector.selectedKeys().remove(sk);
						}
					}
				} catch (Exception e) {

				}
			}

		}).start();
		
	}

	/**
	 * 发送字符串到服务器
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMsg(String message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes(Common.charset));
		int r = socketChannel.write(writeBuffer);
		System.out.println("客户端 -> 服务器:" + message);
	}

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		for (int i = 0; i < 10; i++) {
			String cmd = "cmd=findById&id=" + i + "|";
			client.sendMsg(cmd);
			Thread.sleep(20);
		}
		System.out.println("主线程发送完毕，进入等待阶段");
	

	}
}
