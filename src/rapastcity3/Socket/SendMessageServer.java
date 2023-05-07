package rapastcity3.Socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import rapastcity3.utils.AnalysisRequest;
import rapastcity3.utils.FlagClazz;
import rapastcity3.utils.OutputJsonUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.environment.Runner;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.ui.RSApplication;
import repast.simphony.ws.ScheduleRunner;
import repastcity3.agent.AgentFactory;
import repastcity3.agent.IAgent;
import repastcity3.agent.InfectedAgent;
import repastcity3.agent.MyAgent;
import repastcity3.environment.contexts.AgentContext;
import repastcity3.main.ContextManager;
import rapastcity3.pojo.Request;
/**
 * 
 * @author nmsc
 *
 */
public class SendMessageServer implements Runnable{
	public ServerSocket serverSocket;
	public static int PORT=9998;
	public String encoding="utf-8";
	public Object lock;
	public SendMessageServer(Object lock) {
		try {
			this.lock=lock;
			serverSocket=new ServerSocket(PORT);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		new Thread(this).start();
		System.out.println("数据Socket启动成功");
	}
	@Override
	public void run() {
		Socket client = null;
		InputStream inputStream=null;
		OutputStream outputStream=null;
		BufferedReader reader=null;
		BufferedWriter writer=null;
		try {
			
		while(true) {
				 client=serverSocket.accept();
				 inputStream=client.getInputStream();
				  outputStream = client.getOutputStream();
	                 reader=new BufferedReader(new InputStreamReader(inputStream,encoding));
	                 writer=new BufferedWriter(new OutputStreamWriter(outputStream,encoding));
	            byte[] inputBuffer=new byte[2048];
	            int inputMessageLength= inputStream.read(inputBuffer,0,2048);
	            String inputMessage = new String(inputBuffer, 0, inputMessageLength);
	                 
	                //澶勭悊璇锋眰
	                Request request=AnalysisRequest.analysisRequestHandler(inputMessage);
	                //鍒ゆ柇鍋氫粈涔堟搷浣滅殑绫诲瀷
	                String type=request.getMessageJson().getString("type");
	                System.out.println(request.getMessageJson());
	                if (type.equals("updateSetting"))
	                {
	                	if(FlagClazz.stepflag==false)
	                	{
	                		FlagClazz.stepflag=true;
	                	}
	                	String agentNumber1=request.getMessageJson().getString("agentNumber");
	                	String infectedagentNumber1=request.getMessageJson().getString("infectedagentNumber");
	                	String asymptomaticagentNumber1=request.getMessageJson().getString("asymptomaticagentNumber");
	                	Context<IAgent> agentContext=ContextManager.getAgentContext();
	                	agentContext=new AgentContext();
	                	int agentNumber=Integer.parseInt(agentNumber1);
	                	int infectedagentNumber=Integer.parseInt(infectedagentNumber1);
	                	int asymptomaticagentNumber=Integer.parseInt(asymptomaticagentNumber1);
	                	//计算总人数
	                	int allAgentNumbers=agentNumber+infectedagentNumber;
	                	//添加到全局变量
	                	ContextManager.AllAgentNumbers=allAgentNumbers;
	                	//添加
	                	int agentMasksratio=request.getMessageJson().getInteger("agentMasksratio");
	                	int agentVaccineratio=request.getMessageJson().getInteger("agentVaccineratio");
	                	int infectedagenMasksratio=request.getMessageJson().getInteger("infectedagenMasksratio");
	                	int infectedagenVaccineratio=request.getMessageJson().getInteger("infectedagenVaccineratio");
	                	//所有老年和青年的比例
	                	Float AlldagentYoungAgeratio=request.getMessageJson().getFloat("yangerAndOlderProportion")/100;
	                	System.out.println("老年和青年的比例"+AlldagentYoungAgeratio);
	                	//计算出青年人数
	                	int YoungAgentNumber=(int) (AlldagentYoungAgeratio*allAgentNumbers);
	    
	                	//计算出老年人数
	                	int OldAgentNumber=allAgentNumbers-YoungAgentNumber;
//	                	System.out.println("青年人数是"+YoungAgentNumber+"老年人数是"+OldAgentNumber);
	                	//获取感染者的比例
	                	float ratro=(float)infectedagentNumber/(float)allAgentNumbers;
	                	float ratrotemp=(float)Math.round(ratro*100)/100;
//	                	System.out.println(ratrotemp);
	                	//获取青年感染者人数
	                	int infectorYoungPeople=(int) (YoungAgentNumber*ratrotemp)+1;
	                	int healthyYoungPeople=YoungAgentNumber-infectorYoungPeople;
	                	//获取老年感染者人数
	                	int infectorOldPeople=(int)(OldAgentNumber*ratrotemp);
	                	int healthyOldPeople=OldAgentNumber-infectorOldPeople;

	                	//最大医疗救助
	                	int helpMax=request.getMessageJson().getInteger("helpMax");
//	                	ContextManager.MAssistanceThreshold=helpMax;
	                	ContextManager.MAssistanceThreshold = (int)(agentNumber+infectedagentNumber)*2/1000;//记录医护人员占全人数的比例
//	                	String agentDefn="Agent"+":"+agentNumber+","+"agentMasksratio"+
//	                	":"+agentMasksratio+","+"agentVaccineratio"+":"+agentVaccineratio+","+"agentYoungAgeratio"+":"+90;
	                	
//	                	String infectedDefn="InfectedAgent"+":"+infectedagentNumber+","+
//	                	"infectedagenMasksratio"+":"+infectedagenMasksratio+","+"infectedagenVaccineratio"+
//	                			":"+infectedagenVaccineratio+","+"infectedagentYoungAgeratio"+":"+90;
	                	String agentDefn="Agent"+":"+agentNumber+","+"agentMasksratio"+
	    	                	":"+agentMasksratio+","+"agentVaccineratio"+":"+agentVaccineratio+
	    	                	","+"healthYoungNumber"+":"+healthyYoungPeople+","+"healthOldNumber"+":"+healthyOldPeople;
	                	String infectedDefn="InfectedAgent"+":"+infectedagentNumber+","+
	                	"infectedagenMasksratio"+":"+infectedagenMasksratio+","+"infectedagenVaccineratio"+
	                			":"+infectedagenVaccineratio+","+"infectorYoungNumber"+":"+infectorYoungPeople+","+
	                	"InfectorOldNumber"+":"+infectorOldPeople;
	                	
	                	//重置人数
	                	ContextManager.DeathNumber=0;
	                	ContextManager.infectedagentLatentCount=0;
	                	ContextManager.infectedagentOnsetCount=0;
	                	ContextManager.infectedagentSevereCount=0;
	                	
	                	AgentFactory agentFactory=new AgentFactory(agentDefn);
	                	AgentFactory infectFactory=new AgentFactory(infectedDefn);
//	                	AgentFactory asymtomaticagentFactory=new AgentFactory(asymptomaticDefn);
	                	infectFactory.createAgents(agentContext);
	                	agentFactory.createAgents(agentContext);
//	                	asymtomaticagentFactory.createAgents(agentContext);
	                	ContextManager.agentCount=agentNumber;//更新全局变量健康人数
	                	ContextManager.agentnotAthome = agentNumber;
	                	ContextManager.infectedagentCount = infectedagentNumber;//更新全局感染者人数
	                	ContextManager.infectedagentnotAthome=infectedagentNumber;
	                	
	                	//获取速度
	                	Double speed=request.getMessageJson().getDouble("step");
	                	//获取所有MyAgent并且设置速度
	                	Iterable<IAgent> MyagentIterator=ContextManager.getAllMyAgents();
	                	while(MyagentIterator.iterator().hasNext()) {
	                		MyAgent myagent=(MyAgent)MyagentIterator.iterator().next();
	                		myagent.setCustomSpeed(speed);
//	                		myagent.setCustomSpeed(100d);
	                	}
	                	//获取所有InfecterAgent并且设置速度
	                	Iterable<IAgent> infectorIterator=ContextManager.getAllInfectedAgent();
	                	while(infectorIterator.iterator().hasNext()) {
	                		InfectedAgent infectedagent=(InfectedAgent)infectorIterator.iterator().next();
	                		infectedagent.setCustomSpeed(speed);
//	                		infectedagent.setCustomSpeed(100d);
	                	}
	                	synchronized(lock)
	                	{
	                		lock.notifyAll();
	                	}
	                }
	                else if(type.equals("resumeRunAgent")) //结束暂停
	                {
	                	
	                	RunEnvironment.getInstance().resumeRun();
	                }
	                else if(type.equals("stopAgent")) {//暂停
	                	RunEnvironment.getInstance().pauseRun();
	                }else if(type.equals("endAgent"))//终止
	                {
	                	ISchedule schedule=RunEnvironment.getInstance().getCurrentSchedule();
	                	schedule.executeEndActions();
	                	RunEnvironment.getInstance().endRun();
	                }
	                else if(type.equals("startAgent")){//开始
	                	RSApplication.getRSApplicationInstance().start();
	                }
	                else if(type.equals("reLoadAgent")) {//重置
	                	RSApplication.getRSApplicationInstance().getController().getScheduleRunner().stop();
	                	StepDataServer stepDataServer=(StepDataServer) SocketStatusManager.serverSocketManager.get("stepDataServer");
	                	stepDataServer.serverSocket.close();
	                	SendMessageServer sendMessageServer=(SendMessageServer)SocketStatusManager.serverSocketManager.get("sendMessage");
	                	sendMessageServer.serverSocket.close();
	                	//在socket状态管理中清空所有socket
	                	SocketStatusManager.serverSocketManager.clear();
	                	RSApplication.getRSApplicationInstance().reset();
	                	RSApplication.getRSApplicationInstance().initSim();
	                	
	                }
	                //灏佽鍝嶅簲澶�
	                OutputJsonUtils.OutputSuccessJson(client);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
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
		
		
	

