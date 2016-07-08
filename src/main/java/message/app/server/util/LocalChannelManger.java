package message.app.server.util;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalChannelManger {
	//�洢�û��������������Ķ����ӳ���ϵ
	final private Map<String, ChannelHandlerContext> sessions = new ConcurrentHashMap<String, ChannelHandlerContext>();
	//�洢�������������û�����ӳ���ϵ
	final private Map<String, String> relations = new ConcurrentHashMap<String, String>();
	
	//��̬����ʵ�ֵ���
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
	
	//�����˺Ż�ȡ����������
	public ChannelHandlerContext getContext(String id){
		return sessions.get(id);
	}
	//���������Ļ�ȡ�˺�
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
	
	//��ȡ���е��û���
	public synchronized Set<String> getAll(){
		return sessions.keySet();
	}
	//��ȡ���е����������Ķ���
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
