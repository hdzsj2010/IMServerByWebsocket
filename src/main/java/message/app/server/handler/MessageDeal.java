package message.app.server.handler;
/**
 * @author Jason_zh
 * @date 2016年7月14日
 * @version 1.0
 * 消息处理类，包括消息redis存储，mysql存储和消息转发
 */
public class MessageDeal {
	private String sendName;
	private String receiveName;
	private String content;
	
	public MessageDeal(String send,String rec,String content){
		this.sendName = send;
		this.receiveName = rec;
		this.content = content;
	}
	
	
}
