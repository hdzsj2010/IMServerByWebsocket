package message.app.server;

import sun.org.mozilla.javascript.internal.Interpreter;
import message.app.server.handler.TextWebSocketFrameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class MainServer {
	
	private int port;
	
	public MainServer(int port){
		this.port = port;
	}
	
	public void start() throws InterruptedException{
		EventLoopGroup bossGroup = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup(4);
		 
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			//EventLoopGroup���ڽ��պʹ���������
			serverBootstrap.group(bossGroup, workerGroup)
			//ָ��NioServerSocketChannelΪ�ŵ�����
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			//��һ���µ����ӱ����գ�һ���µ���channel����������ChannelHandlerʵ��������ӵ�channel��channelpipeline
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					// pipeline������, �ο�����
					ChannelPipeline cp = socketChannel.pipeline();
					// ֧��httpЭ��Ľ���
					//HttpServerCodec��HttpObjectAggregator�Ѿ������Ƿ�װ����WebSocket������FullHttpRequest/FullHttpResponse���͸�������Frame��.
					cp.addLast(new HttpServerCodec());
					cp.addLast(new HttpObjectAggregator(65535));
					// ���ڴ��ļ�֧�� chunked��ʽд
					cp.addLast(new ChunkedWriteHandler());
					// ��websocketЭ��Ĵ���--���ִ���ping/pong�������ر�
					//WebSocketServerProtocolHandler���������ֵ�ϸ�ڴ���, �Լ���������͹ر���Ӧ. ���ChannelHanlder�ĵ��Ӻ�WebSocketЭ�鱾��ĸ����������ȹص�.
					cp.addLast(new WebSocketServerProtocolHandler("/chatserver"));
					// ��TextWebSocketFrame�Ĵ���
					cp.addLast(new TextWebSocketFrameHandler());
				}
			});
			//�󶨷�����
			ChannelFuture f = serverBootstrap.bind(port).sync();
			
			//InterpreterComponent component = new InterpreterComponent();
			
			
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
