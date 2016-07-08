package message.app.server.util;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalChannelManger {
	//存储用户名与连接上下文对象的映射关系
	final private Map<String, ChannelHandlerContext> sessions = new ConcurrentHashMap<String, ChannelHandlerContext>();
	//存储连接上下文与用户名的映射关系
	final private Map<String, String> relations = new ConcurrentHashMap<String, String>();
	
	//静态变量实现单例
	private static LocalChannelManger instance = new LocalChannelManger();
	
	public static LocalChannelManger getInstance(){
		return instance;
	}
	
	public void addContext(String id, ChannelHandlerContext ctx){
		//synchronized (sessions) {
			sessions.put(id, ctx);
			relations.put(ctx.toString(), id);
		//}
	}
	
	//根据账号获取接入上下文
	public ChannelHandlerContext getContext(String id){
		return sessions.get(id);
	}
	//根据上下文获取账号
	public String getName(ChannelHandlerContext ctx){
		return relations.get(ctx.toString());
	}
	
	public void removeContextById(String id){
		if(id!=null)
			sessions.remove(id);
	}
	
	public boolean isAvailable(String id){
		return sessions.containsKey(id) && sessions.get(id)!=null;
	}
	
	//获取所有的用户名
	public synchronized Set<String> getAll(){
		return sessions.keySet();
	}
	//获取所有的连接上下文对象
	public synchronized Collection<ChannelHandlerContext> getAllClient(){
		return sessions.values();
	}
	
	public void removeContextByctx(ChannelHandlerContext ctx){
		String id = relations.get(ctx.toString());
		if(id!=null){
			sessions.remove(id);
			relations.remove(ctx.toString());
		}
	}
	
	public int staticClients(){
		return relations.size();
	}
}
