package message.app.server.util.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtils {
	private static JedisPool pool = null;
	private static final String host = "123.206.13.157";
	private static final int port = 6379;
	
	private JedisPoolUtils(){}
	
	//µ¥Àýjedis
	public static Jedis getJedis(){
		if(pool==null){
			synchronized (JedisPoolUtils.class) {
				if(pool==null)
					pool = new JedisPool(new JedisPoolConfig(),host,port,10000,"redischeck");
			}
		}
		return pool.getResource();
	}
	
	 
	public void closeJedisInstance(Jedis jedis) {
	     if (null != jedis) {
	         jedis.close();
	     }
	 }
	    
	public void destroyJedisPool() {
	     pool.destroy();
	}
}
