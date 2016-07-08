package message.app.server.util.jedis;

import java.util.List;
import java.util.Set;


public class JedisUtil {
	
	//���ؼ����е����г�Ա
	public static Set<String> smembers(String key){
		Set<String> set = JedisPoolUtils.getJedis().zrange(key,0,-1);
		return set;
	}
	
	public static void main(String[] args){
		JedisPoolUtils.getJedis().set("key", "test");
	}
	
}
