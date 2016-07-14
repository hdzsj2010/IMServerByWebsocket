package message.app.server.handler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;

import message.app.model.User;
import message.app.server.util.LocalChannelManger;
import message.app.server.util.jedis.JedisUtil;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
	
	private User user;
	
	//channelGroup通道广播由LocalChannelManger的函数代替
	//public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	//登录通过通道连接后客户端再次发送MSG注册session
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
		super.handlerAdded(ctx);
		Channel incoming = ctx.channel();
		user = new User();
		//channels.add(incoming);
		//channels.writeAndFlush(new TextWebSocketFrame("[SERVER-]"+incoming.remoteAddress()+"加入"));
		System.out.println("Client:"+incoming.remoteAddress()+"加入");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx){
		Channel incoming = ctx.channel();
		LocalChannelManger.getInstance().removeContextByctx(ctx);
		syncRoster("quit");
		//channels.writeAndFlush(new TextWebSocketFrame("[SERVER-]"+incoming.remoteAddress()+"离开"));
		System.out.println("Client:"+incoming.remoteAddress()+"离开");
		int count = LocalChannelManger.getInstance().staticClients();  
        System.out.println("current clients : " + count);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			TextWebSocketFrame msg) throws Exception {
		//Channel incoming = ctx.channel();
		String message = msg.text();
		if (message.startsWith("AUTH:")) {
			String username = message.split(":")[1];
			
			//初始化user，并从redis获取好友数据
			Set<String> friendsAll = JedisUtil.smembers(username);
			user.setUsername(username);
			user.setFriends(friendsAll);
			LocalChannelManger.getInstance().addContext(username, ctx);
			
			Set<String> online = LocalChannelManger.getInstance().getAll();
			Set<String> onlineFriends = new HashSet<String>();
			onlineFriends.addAll(friendsAll);
			onlineFriends.retainAll(online);
			//返回在线好友列表
			StringBuffer stringBuffer = new StringBuffer("FRIENDSON:");
			for (String string : onlineFriends) {
				stringBuffer.append(string).append(",");
			}
			ctx.writeAndFlush(new TextWebSocketFrame(stringBuffer.toString()));
			
			int count = LocalChannelManger.getInstance().staticClients();
			System.out.println("Current clients:"+count);
			syncRoster("IN");
		//发送信息格式为"MSG:[to]#[body]"
		}else if (message.startsWith("SEND:")) {
			String content = message.substring(5);
			if (content==null || content=="") {
				System.out.println("message type wrong");
				return;
			}
			String[] temp = content.split("#");
			String to = temp[0];
			String body = "";
			for (int i = 1; i < temp.length; i++) {
				//处理消息中有#的特殊情况
				if (i>1) 
					body += "#";
				body += temp[i];
			}
			//发消息格式——注：如果消息中含有冒号则需要特殊处理
			String from = LocalChannelManger.getInstance().getName(ctx);
			MessageDeal messageDeal = new MessageDeal(from, to, body);
			
			body = "RECEIVE:"+from+"#"+body;
			if (LocalChannelManger.getInstance().isAvailable(to)) {
				LocalChannelManger.getInstance().getContext(to)
				.writeAndFlush(new TextWebSocketFrame(body));
			}
		}else if (message.startsWith("QUIT:")) {
			String username = message.split(":")[1];
			LocalChannelManger.getInstance().removeContextById(username);
			int count = LocalChannelManger.getInstance().staticClients();
			System.out.println("current clients:"+count);
			syncRoster("OUT");
		}
		System.out.println(message);
		/*for (Channel channel : channels) {
			if(channel!=incoming)
				channel.writeAndFlush(new TextWebSocketFrame(
						"["+incoming.remoteAddress()+"]"+msg.text()));
			else {
				channel.writeAndFlush(new TextWebSocketFrame(
						"[ME]"+msg.text()));
			}
		}*/
		
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx){
		Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"在线");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx){
		Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"下线");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
		Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
		cause.printStackTrace();
		ctx.close();
	}
	
	//更新所有好友的在线列表
	private void syncRoster(String state){
		Set<String> online = LocalChannelManger.getInstance().getAll();
		Set<String> friends = user.getFriends();
		
		Set<String> onlineFriends = new HashSet<String>();
		onlineFriends.addAll(friends);
		onlineFriends.retainAll(online);
		
		String respone = state+":"+user.getUsername();
		
		//取并集后找出在线的好友，进行通知
		Iterator<String> iterator = onlineFriends.iterator();
		while (iterator.hasNext()) {
			LocalChannelManger.getInstance()
				.getContext(iterator.next()).writeAndFlush(new TextWebSocketFrame(respone));
		}
	}
	
}
