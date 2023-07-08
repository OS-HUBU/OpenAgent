package rapastcity3.Socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import rapastcity3.Socket.ModuleFlagClazz;
import java.util.List;
import com.alibaba.fastjson.JSONObject;
import rapastcity3.pojo.GeoJson;
import rapastcity3.utils.CSVUtils;
import rapastcity3.utils.FlagClazz;
import repast.simphony.engine.environment.RunEnvironment;
import repastcity3.agent.IAgent;
import repastcity3.main.ContextManager;

public class StepDataServer implements Runnable{
	public ServerSocket serverSocket;
	public static int port=9999;
	public String encoding="GBK";
	public StepDataServer() {
		try {
			serverSocket=new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(this).start();
		System.out.println("9999服务器正在运行.....");
		
	}
	@Override
	public void run() {
		Socket client = null;
		InputStream inputStream=null;
		OutputStream outputStream=null;
		BufferedReader reader=null;
		BufferedWriter writer=null;
		try {
		while(true)
		{
				 client=serverSocket.accept();
				 inputStream=client.getInputStream();
				 outputStream=client.getOutputStream();
				 reader=new BufferedReader(new InputStreamReader(inputStream,encoding));
				 writer=new BufferedWriter(new OutputStreamWriter(outputStream,encoding));
				
				Iterable<IAgent> lists=ContextManager.getAllAgents();
				 writer.write("HTTP/1.1 200 OK\r\n");
			     writer.write("Connection: close\r\n");
			     writer.write("Content-Type: application/json\r\n");
			     writer.write("\r\n");
			     List<GeoJson> stepList=new ArrayList();
			     String unit="km";
				JSONObject jsonObject=new JSONObject();
			     long datetime=System.currentTimeMillis();
				for (IAgent agent:lists) {
					String lng=null;
					String lat=null;
				    if(ModuleFlagClazz.module==0) {
						String agentGeoStr=ContextManager.getAgentGeometry(agent).toString();
						String agentLngAndLat=agentGeoStr.substring(agentGeoStr.indexOf("(")+1
								,agentGeoStr.indexOf(")"));
						String [] arr=agentLngAndLat.split("\\s+");
						lng=arr[0];
						lat=arr[1];
				    }
				    else {
					String agentGeoStr=ContextManager.getAgentGeometry(agent).toString();
					if(ContextManager.getAgentGeometryEnd(agent)==null){
						String agentLngAndLat=agentGeoStr.substring(agentGeoStr.indexOf("(")+1
								,agentGeoStr.indexOf(")"));
						String [] arr=agentLngAndLat.split("\\s+");
						lng=arr[0];
						lat=arr[1];
					}
					else {
						lng=String.valueOf(ContextManager.getAgentGeometryEnd(agent).getOrdinate(0));
						lat=String.valueOf(ContextManager.getAgentGeometryEnd(agent).getOrdinate(1));
						System.out.println("lng"+lng+"lat"+lat);
					}
				    }
//					String agentLngAndLat=agentGeoStr.substring(agentGeoStr.indexOf("(")+1
//							,agentGeoStr.indexOf(")"));
//					String [] arr=agentLngAndLat.split("\\s+");
//					String lng=arr[0],lat=arr[1];
					GeoJson geojson=new GeoJson();
					geojson.setId(agent.getID());
					geojson.setLat(lat);
					geojson.setLng(lng);
					geojson.setSpeed(agent.getCustomSpeed());
					geojson.setAgentType(agent.getAgentType());
	                geojson.setMasks(String.valueOf(agent.getMasks()));
	                geojson.setVaccine(String.valueOf(agent.getVaccine()));
	                if(agent.getStage()==null) {
	                	geojson.setStage("NULL");
	                }else {
	                	geojson.setStage(agent.getStage());
	                }
					stepList.add(geojson);
				}
				jsonObject.put("unit",unit);
				jsonObject.put("datetime", datetime);
				jsonObject.put("stepflag", FlagClazz.stepflag);
	            jsonObject.put("agent", ContextManager.agentCount);
	            jsonObject.put("infectedagentCount", ContextManager.infectedagentCount);
//	            jsonObject.put("AsymptomaticAgentCount", ContextManager.AsymptomaticAgentCount);
	            
	            jsonObject.put("infectedagentLatentCount", ContextManager.infectedagentLatentCount);
	            jsonObject.put("infectedagentOnsetCount", ContextManager.infectedagentOnsetCount);
	            jsonObject.put("infectedagentSevereCount", ContextManager.infectedagentSevereCount);
	            jsonObject.put("DeathNumber", ContextManager.DeathNumber);
				jsonObject.put("geojsonList",stepList);
				//save csv
				double timeCount1=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				System.out.println("bushu"+timeCount1);
				if(timeCount1>0)
				{
					String fileString="D://"+timeCount1+".csv";
					CSVUtils.WriteCsv(fileString,jsonObject);
				}
			     writer.write(jsonObject.toString());
			     writer.write("\r\n");
			     writer.flush();
			     client.shutdownOutput();
			
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
			if(client!=null)
			{
				client.close();
			}
			if(inputStream!=null)
			{
				inputStream.close();	
			}
			if(outputStream!=null)
			{
				outputStream.close();	
			}
			if (reader!=null)
			{
				reader.close();
			}
			reader.close();
			if(writer!=null)
			{
			writer.close();	
			}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

