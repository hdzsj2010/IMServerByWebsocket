package message.app.server.handler;
/**
 * @author Jason_zh
 * @date 2016��7��14��
 * @version 1.0
 * ��Ϣ�����࣬������Ϣredis�洢��mysql�洢����Ϣת��
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
