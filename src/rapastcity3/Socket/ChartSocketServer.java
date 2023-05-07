package rapastcity3.Socket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;

import rapastcity3.utils.OutputJsonUtils;
import repast.simphony.engine.environment.RunEnvironment;
import repastcity3.agent.IAgent;
import repastcity3.main.ContextManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
/**
 * chartsSocket
 * @author nmsc
 *
 */
public class ChartSocketServer implements Runnable{
	public ServerSocket serverSocket;
	public static int PORT=9997;
	public String encoding="utf-8";
	public Object lock;
	public ChartSocketServer(Object lock)
	{
		try {
			this.lock=lock;
			serverSocket=new ServerSocket(PORT);
		}catch(Exception e)
		{
			
		}
		new Thread(this).start();
		System.out.println("图表Socket启动成功");
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Socket client=null;
		InputStream inputStream=null;
		OutputStream outputStream=null;
		BufferedReader reader=null;
		BufferedWriter writer=null;
		try {
			while(true) {
				client=serverSocket.accept();
				inputStream=client.getInputStream();
				outputStream=client.getOutputStream();
				reader=new BufferedReader(new InputStreamReader(inputStream,encoding));
				writer=new BufferedWriter(new OutputStreamWriter(outputStream,encoding));
				Iterable<IAgent> lists=ContextManager.getAllMyAgents();
				int agentCount=0;
				Iterator<IAgent> it=lists.iterator();
				while(it.hasNext()) {
					agentCount++;
					it.next();
				}
				//获取tickCount
				Double tickCount=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				JSONObject jsonObject=new JSONObject();
				long timestamp=System.currentTimeMillis();
				jsonObject.put("timestamp", timestamp);
				jsonObject.put("agentCount", agentCount);
				jsonObject.put("tickCount", tickCount);
				OutputJsonUtils.OutputDataJson(client,jsonObject);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				client.close();
				inputStream.close();
				outputStream.close();
				reader.close();
				writer.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	

}
