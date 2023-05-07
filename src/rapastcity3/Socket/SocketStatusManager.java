package rapastcity3.Socket;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class SocketStatusManager {
	public static  Map<String,Runnable> serverSocketManager=new HashMap();
	public static  Map<Integer,Runnable> ThreadedManager=new HashMap();
	
	//清空所有Socket
	public static void clearAllSocket() {
		if (!serverSocketManager.isEmpty())
		{
			serverSocketManager.clear();
		}
	}
}
