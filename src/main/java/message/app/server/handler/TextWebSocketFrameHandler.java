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
	
	//channelGroupͨ���㲥��LocalChannelManger�ĺ�������
	//public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	//��¼ͨ��ͨ�����Ӻ�ͻ����ٴη���MSGע��session
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
		super.handlerAdded(ctx);
		Channel incoming = ctx.channel();
		user = new User();
		//channels.add(incoming);
		//channels.writeAndFlush(new TextWebSocketFrame("[SERVER-]"+incoming.remoteAddress()+"����"));
		System.out.println("Client:"+incoming.remoteAddress()+"����");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx){
		Channel incoming = ctx.channel();
		LocalChannelManger.getInstance().removeContextByctx(ctx);
		syncRoster("quit");
		//channels.writeAndFlush(new TextWebSocketFrame("[SERVER-]"+incoming.remoteAddress()+"�뿪"));
		System.out.println("Client:"+incoming.remoteAddress()+"�뿪");
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
			
			//��ʼ��user������redis��ȡ��������
			Set<String> friendsAll = JedisUtil.smembers(username);
			user.setUsername(username);
			user.setFriends(friendsAll);
			LocalChannelManger.getInstance().addContext(username, ctx);
			
			Set<String> online = LocalChannelManger.getInstance().getAll();
			Set<String> onlineFriends = new HashSet<String>();
			onlineFriends.addAll(friendsAll);
			onlineFriends.retainAll(online);
			//�������ߺ����б�
			StringBuffer stringBuffer = new StringBuffer("FRIENDSON:");
			for (String string : onlineFriends) {
				stringBuffer.append(string).append(",");
			}
			ctx.writeAndFlush(new TextWebSocketFrame(stringBuffer.toString()));
			
			int count = LocalChannelManger.getInstance().staticClients();
			System.out.println("Current clients:"+count);
			syncRoster("IN");
		//������Ϣ��ʽΪ"MSG:[to]#[body]"
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
				//������Ϣ����#���������
				if (i>1) 
					body += "#";
				body += temp[i];
			}
			//����Ϣ��ʽ����ע�������Ϣ�к���ð������Ҫ���⴦��
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
		System.out.println("Client:"+incoming.remoteAddress()+"����");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx){
		Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"����");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
		Channel incoming = ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"�쳣");
		cause.printStackTrace();
		ctx.close();
	}
	
	//�������к��ѵ������б�
	private void syncRoster(String state){
		Set<String> online = LocalChannelManger.getInstance().getAll();
		Set<String> friends = user.getFriends();
		
		Set<String> onlineFriends = new HashSet<String>();
		onlineFriends.addAll(friends);
		onlineFriends.retainAll(online);
		
		String respone = state+":"+user.getUsername();
		
		//ȡ�������ҳ����ߵĺ��ѣ�����֪ͨ
		Iterator<String> iterator = onlineFriends.iterator();
		while (iterator.hasNext()) {
			LocalChannelManger.getInstance()
				.getContext(iterator.next()).writeAndFlush(new TextWebSocketFrame(respone));
		}
	}
	
}
