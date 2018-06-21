package com.ec.study.demo1.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



public class Server {
	private Selector selector = null;
	private UserServer userServer;
	
	private Map<Long,String> remainStrMap = new ConcurrentHashMap<Long,String>();
	
	public static void main(String[] args) throws IOException {
		Server server = new Server();
		server.start();
	}

	private void start() throws IOException {
		// 1. 获取选择器
		selector = Selector.open();
		
		userServer = new UserServer();

		// 2. 绑定 端口，构建 ServerSocket
		ServerSocketChannel sschannel = ServerSocketChannel.open();
		sschannel.bind(new InetSocketAddress(Common.port));

		// 3. 将 ServerSocketChannel 注册到 selector， 订阅 连接到来 事件
		register(sschannel, SelectionKey.OP_ACCEPT);

		// 4. 单线程轮询 检查 通道 各种准备就绪的状态 的事件
		doServer();
	}

	private void doServer() throws IOException {
		while (true) {
			int num = selector.select(Common.timeout);
			if (num < 1) {
				Utils.print("高傲的独自等待");
			}

			Set<SelectionKey> keySet = selector.selectedKeys();
			Iterator<SelectionKey> it = keySet.iterator();

			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();

				if (key.isAcceptable()) {
					doAcceptable(key);
				}

				if (key.isReadable()) {
					try{
						doReadable(key);
					}catch (Exception e) {
						e.printStackTrace();
					}
					
				}

				if (key.isValid() && key.isWritable()) {
					doWritable(key);
				}
			}

		}
	}

	private void doWritable(SelectionKey key) {
		// to do , 此处不需要实现逻辑
	}

	/**
	 * 核心业务处理入库
	 * @param key
	 * @throws IOException 
	 */
	private void doReadable(SelectionKey key) throws IOException {
		SocketChannel schannel = (SocketChannel) key.channel();
		ByteBuffer dst = (ByteBuffer) key.attachment();

		System.out.println("开始处理数据了");
		int n = schannel.read(dst);
		if (n == -1) {
			System.out.println("没有数据可用读了");
			schannel.close();
			return;
		}

		dst.flip();
		String param = new String(dst.array(), Common.charset);
		List<Args> list = parseArgs(param);
				
		if(list!=null){
			for(Args ag : list){
				try{
					if(ag.getCmd()==null){
						continue;
					}
					else if("exists".equals(ag.getCmd())){
						Boolean res = userServer.exists(ag.getId());
						writeDataToClient(schannel,res.toString());
					}else if("findById".equals(ag.getCmd())){
						User user = userServer.findById(ag.getId());
						writeDataToClient(schannel,user.toString());
					}else{
						writeDataToClient(schannel,"unknow cmd");
					}
				}catch (Exception e) {
					writeDataToClient(schannel,"server error! , message = " + e.getMessage());
				}
				
				
			}
		}else{
			writeDataToClient(schannel,"非法参数");
		}
		
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

	}

	private void writeDataToClient(SocketChannel schannel, String json)  {
		
		try {
			ByteBuffer src = ByteBuffer.wrap(json.getBytes(Common.charset));
			schannel.write(src);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Args> parseArgs(String param) {
		
		if(param==null || param.trim().equals("")){
			return null;
		}
		Long thd = Thread.currentThread().getId();
		String remainStr = remainStrMap.get(thd);
		if(remainStr==null){
			remainStr = "";
		}
		
		param = remainStr + param;
		
		if(!param.contains("|")){
			remainStr = param;
			remainStrMap.put(thd, remainStr);
			return null;
		}
		
		String p1 = param.substring(0,param.lastIndexOf("|"));
		remainStr = param.substring(param.lastIndexOf("|"),param.length());
		remainStrMap.put(thd, remainStr);
		List<Args>  list = new ArrayList<Args>();
		String args[] = p1.split("\\|");
		if(args!=null && args.length > 0){
			for(String args2 : args){
				if(args2!=null && args2.length()>0){
					
					Args argsobj = new Args();
					argsobj.build(args2);
					list.add(argsobj);
				}
			}
			
		}
		return list;
	}

	/**
	 * 当连接准备就绪的时候， 获取 连接通道， 然后注册 读/写 事件 到选择器
	 * @param key
	 * @throws IOException
	 */
	private void doAcceptable(SelectionKey key) throws IOException {
		ServerSocketChannel sschannel = (ServerSocketChannel) key.channel();
		SocketChannel schannel = sschannel.accept();
		register(schannel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		System.out.println("接收到新的连接");

	}

	// 将通道注册到选择器
	private void register(SelectableChannel channel, int opAccept) throws IOException {
		channel.configureBlocking(false);
		channel.register(selector, opAccept, ByteBuffer.allocate(Common.capicicy));

	}
}
