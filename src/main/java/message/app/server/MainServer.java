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
			//EventLoopGroup用于接收和处理新连接
			serverBootstrap.group(bossGroup, workerGroup)
			//指定NioServerSocketChannel为信道类型
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			//当一个新的连接被接收，一个新的子channel将被创建，ChannelHandler实例将被添加到channel的channelpipeline
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					// pipeline的设置, 参看下面
					ChannelPipeline cp = socketChannel.pipeline();
					// 支持http协议的解析
					//HttpServerCodec和HttpObjectAggregator已经帮我们封装好了WebSocket的握手FullHttpRequest/FullHttpResponse包和各类数据Frame包.
					cp.addLast(new HttpServerCodec());
					cp.addLast(new HttpObjectAggregator(65535));
					// 对于大文件支持 chunked方式写
					cp.addLast(new ChunkedWriteHandler());
					// 对websocket协议的处理--握手处理，ping/pong心跳，关闭
					//WebSocketServerProtocolHandler隐藏了握手的细节处理, 以及心跳处理和关闭响应. 多个ChannelHanlder的叠加和WebSocket协议本身的复杂是密切先关的.
					cp.addLast(new WebSocketServerProtocolHandler("/chatserver"));
					// 对TextWebSocketFrame的处理
					cp.addLast(new TextWebSocketFrameHandler());
				}
			});
			//绑定服务器
			ChannelFuture f = serverBootstrap.bind(port).sync();
			
			//InterpreterComponent component = new InterpreterComponent();
			
			
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
