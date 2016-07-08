package message.app.server;


public class Main {
	public static void main(String[] args){
		MainServer server = new MainServer(8123);
		try {
			System.out.println("！！Netty Server Start！！");
			server.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("！！Netty Server error！！");
		}
	}
	
}
