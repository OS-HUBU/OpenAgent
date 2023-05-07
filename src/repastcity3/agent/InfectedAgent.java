package repastcity3.agent;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.vividsolutions.jts.geom.Point;

import rapastcity3.Strategy.NoYoungAndNoVaccineStategy;
import rapastcity3.Strategy.StatusContext;
import rapastcity3.Strategy.YoungAndVaccineStategy;
import rapastcity3.Strategy.YoungORVaccineStategy;
//import geozombies.agents.Human;
//import geozombies.agents.ZombieUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.ContextManager;
import repast.simphony.engine.environment.RunEnvironment;


public class InfectedAgent implements IAgent {
	
	private static Logger LOGGER = Logger.getLogger(InfectedAgent.class.getName());

	private Building home; // Where the agent lives
	/** 闁哄嫷鍨伴幆浣割啅閼碱剛鐥呴柛鎴濇惈瑜板倹绂嶉敓锟�(濞戞捁妗ㄧ花锟犲礌閸濆嫬鐎荤紒妤婂厸缁旀潙鈻庨敓锟�) */
	private boolean hasSetoff = false;
	private Route route; // An object to move the agent around the world
	private boolean Rehabilitated = false;//是否为阳性康复人群
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	private static int uniqueID = 0;	//agent闁汇劌鍤楧
	private int id;									
	private static List<StatusContext> statusContext=new ArrayList();//策略模式上下文
	private String stage;//latent period Onset period Severe period 2 4 6
	private String age;//young old
	private int SwitchStageDay;
	public static int NumofSevere = 0;//记录重症人数
	private Double CustomSpeed;//自定义速度
	private double tickCount = 0 ;
	
	public Double getCustomSpeed() {
		return this.CustomSpeed;
	}
	public void setCustomSpeed(Double CustomSpeed) {
		this.CustomSpeed=CustomSpeed;
	}
	public void setSwitchStageDay(int day) {
		SwitchStageDay=day;
	}
	public int getSwitchStageDay() {
		return SwitchStageDay;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getAge() {
		return age;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}
	public String getStage() {
		return stage;
	}
    private boolean Masks;
    private boolean Vaccine;
    public boolean getMasks() {
        return this.Masks;
    }
//    static {
//    	statusContext.add(new StatusContext("YounyAndVaccine",new YoungAndVaccineStategy()));
//    	statusContext.add(new StatusContext("YoungORVaccine",new YoungORVaccineStategy()));
//    	statusContext.add(new StatusContext("NoYoungAndNoVaccine",new NoYoungAndNoVaccineStategy()));
//    }
    public void setMasks(boolean masks) {
        this.Masks = masks;
    }

    public boolean getVaccine() {
        return this.Vaccine;
    }

    public void setVaccine(boolean vaccine) {
        this.Vaccine = vaccine;
    }
	/**
	 * 闁诡剝顔婂Ч澶愬极閹峰瞼孝閺夆晛娲︽晶宥夊嫉婵夌爆elter闁汇劌瀚鎰棯閾忣偅娈舵鐐存构缁楁牕顭ㄩ垾鑼晩闁汇劌瀚鍌炲磹濞嗘瑧绀夐柛鎾櫃缁楀懘鎯冮崟顏呯溄闂傚浄鎷烽悷鏇氳喘andom
	 */
	private boolean random;
	private double stagetickCount = 0 ;
	public void setstageTickCount(double count) {
		this.stagetickCount = count;
	}
	public double getstageTickCount() {
		return this.stagetickCount;
	}
	
	
	private String AgentType = "InfectedAgent";
	
	public String getAgentType(){
		return this.AgentType;
	}
	public void setTickCount(double count) {
		this.tickCount = count;
	}
	public double getTickCount() {
		return this.tickCount;
	}
	
//	//闁煎浜滈悾鐐▕婢跺瞼鎽滈柣锝忔嫹
//	private boolean infection = false;
	
	public InfectedAgent() {	
		this.id = uniqueID++;
	}
	public InfectedAgent(boolean random) {
		this();
		this.random = random;
	}
	private int infectRatio ;
	
	public int getInfectRatio() {
		return this.infectRatio;
	}
	
	public void setInfectRatio(int ratio) {
		this.infectRatio = ratio;
	}
	
	
	//婵烇綀顕ф慨锟�
	public Route getRoute() {
		return this.route;
	}
	
	public void setRoute(Route route) {
		this.route = route;
	}
		
	/**
	 * 闁瑰灚鍎抽崺宀勫嫉閿熻姤娼婚幋鐘崇暠shelter
	 * @param home 闁兼眹鍎插﹢顓㈠礆濠靛棭娼楅柛鏍ㄧ壄缁辨繈宕氬顩昺e濞戞挸绉靛Σ鍝ョ矚閻氬骞㈤柛姘剧畱閸ㄧ棢ome闁哄嫷鍨抽埞锟�
	 * @return
	 */
//	private Building judgeNearestBuilding(Building home){
//		Coordinate currentCoord = null;	//鐟滅増鎸告晶鐘诲锤閹邦厾鍨肩紓鍐惧枙鐠愮喓绮氶敓锟�
//		if(home == null)
//			currentCoord = this.route.getCurrentCoord();//鐟滅増鎸告晶鐕糶ent闁秆勫姈閻栵絿鎷嬮崜褏鏋傚☉鎾虫惈缂嶅宕滃鍕Т缂傚喛鎷�
//		else
//			currentCoord = home.getCoords();//闁硅泛銈眊ent闁汇劌瀚紞鍛磾椤旀鍟庣紓鍐惧枛濠�顏嗭拷瑙勬构閼碉拷
//		String buildingId = null;
//		double distance = -1;
////		synchronized(ContextManager.shelter){ 20140312
//		//闂侇剙绉村濠氬炊閸欍儴鍘柣銊ュ婢у秹寮垫繅绫琫lter闁挎稑鏈竟姗�宕氶悧鍫熶粯閺夆晜鍨瑰▓鎴︽晬鐏炵晫鎽犻柛锔规殘uildingID闁告粌鐣砳stance濞戞搫鎷�************************************
//		/*
//		for(String id : ContextManager.shelter.keySet()){
//				Coordinate targetCoord = ContextManager.shelter.get(id).getCoords();
//				double[] distAndAngle = new double[2];
//				Route.distance(currentCoord, targetCoord, distAndA	ngle);
//				if((distance == -1) || (distance > 0 && distAndAngle[0] < distance)){
//					distance = distAndAngle[0];
//					buildingId = id;
//				}
//			}
////		}
//		if(buildingId != null){
//			Building building = ContextManager.shelter.get(buildingId);
//			return building;
//		}
//		else
//			return null;*///闁告绮敮锟芥繛澶堝姂閸ｏ拷/閼达拷 閼达拷/ 闁告帞濞�濞呭孩绋夌�ｂ晝顏遍悶娑樼灱濞堟唵eturn    闁圭鎷烽柡鍫濐槺濞堟叧gent闂侇喛妫勬晶鐘差嚗閿燂拷 shelter 48   閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘鎶芥晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐锟�
//		return ContextManager.shelter.get("377");
//	}
	
	private static Object shelterLock = new Object();
	

	public  void SwitchInfectedAgent(InfectedAgent agent) {
		synchronized(agent) {
		Random r1 = new Random();
		switch(agent.stage){
		case "latent period":
			agent.infectRatio=0;
			agent.setInfectRatio(0);
			agent.stage="Onset period";
			System.out.println("agent变成了发病期"+""+agent.id);
			//判断如果潜伏期人数>0就减去1
			if (ContextManager.infectedagentLatentCount>0)
			{
				ContextManager.infectedagentLatentCount--;
			}
			//发病期+1
			ContextManager.infectedagentOnsetCount++;
			int i2 = r1.nextInt(140);
			this.SwitchStageDay=i2;
			double Count=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			this.setstageTickCount(Count);//记录初始时间
			
			break;
		case "Onset period":
			agent.infectRatio=0;
			agent.setInfectRatio(0);
			agent.stage="Severe period";
			if (ContextManager.infectedagentOnsetCount>0)
			{
				ContextManager.infectedagentOnsetCount--;
			}
			//重症期+1
			ContextManager.infectedagentSevereCount++;
			int i3 = r1.nextInt(300);
			this.SwitchStageDay=i3;
			double Count1=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			this.setstageTickCount(Count1);//记录初始时间
//			this.NumofSevere++;
//			if(this.NumofSevere++>=ContextManager.MAssistanceThreshold) {
//				ContextManager.Medical=false;
//			}
			break;
		case "Severe period":
			if (ContextManager.infectedagentSevereCount>0)
			{
				ContextManager.infectedagentSevereCount--;
			}
//			System.out.println("dead"+agent);
//			System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
//			System.out.println(Thread.currentThread().getName());
			ContextManager.DeathNumber++;
			if(ContextManager.infectedagentCount>0) {
				ContextManager.infectedagentCount--;
			}
			ContextManager.agentContext.remove(agent);
			ContextManager.infectedagentnotAthome--;
			break;

		}
		}
	}
	public  void changeInfectedAgent(InfectedAgent agent) {
		synchronized(agent) {
		Building DestinationBuilding = agent.getRoute().getDestinationBuilding();
		if(ContextManager.getAgentGeometry(agent)==null)
		{
			return;
		}
		Point JzPoint = ContextManager.getAgentGeometry(agent).getCentroid();
		String Age = agent.getAge();
		boolean Masks=agent.getMasks();
		boolean Vaccine=agent.getVaccine();
		MyAgent myAgent = new MyAgent();
		Route JZroute = new Route(myAgent, DestinationBuilding.getCoords(), DestinationBuilding);
		myAgent.setRoute(JZroute);
		myAgent.setRehabilitated(true);
		myAgent.setMasks(Masks);
		myAgent.setVaccine(Vaccine);
		myAgent.setAge(Age);
		myAgent.setRehabilitated(true);
		myAgent.setCustomSpeed(agent.getCustomSpeed());//设置其自定义速度
		myAgent.setInfectRatio(0);
		//查询出agent当前状态
		String stage=agent.getStage();
		//更新全局计数变量
		updateHealthCount(stage);
		ContextManager.moveAgent(myAgent,JzPoint);
		synchronized(ContextManager.infectedAgentContextLock) {
		synchronized(ContextManager.agentContext){
			ContextManager.addAgentToContext(myAgent);
			ContextManager.agentContext.remove(agent);
			ContextManager.infectedagentnotAthome--;
			ContextManager.agentnotAthome++;
			agent = null;
			
			ContextManager.agentCount++;
			if(ContextManager.infectedagentCount>0) {
				ContextManager.infectedagentCount--;
			}
			
		}
		}
		}
	}
	//更新转变为健康人数后的病程计数更新
	public static void updateHealthCount(String stage) {
		switch(stage) {
			case "latent period":
				if (ContextManager.infectedagentLatentCount>0)
				{
					//潜伏期感染人数减一
					ContextManager.infectedagentLatentCount--;
					break;
				}
			case "Onset period":
				if (ContextManager.infectedagentOnsetCount>0)
				{
					//发病期人数减一
					ContextManager.infectedagentOnsetCount--;
					break;
				}
			case "Severe period":
				if (ContextManager.infectedagentSevereCount>0)
				{
					//重症期人数减一
					ContextManager.infectedagentSevereCount--;
					break;
				}
		}
	}
	private Building judgeDestination(Building home,InfectedAgent agent){
//		int buildingIDnum=0;
//		int num=0,firstDim=0,lastPart=0;
//		for(;num<agent.getID();){
//			num++;
//			if(num-lastPart>(Route.initInfos[firstDim][3]-1)){
//				lastPart=num;
//				firstDim++;
//			}
//		}
		Building building=GoDestination();
		
//		buildingIDnum=Route.initInfos[firstDim][1]+1;
//		Building building = ContextManager.shelter.get(Integer.toString(buildingIDnum));
//		Random r1 = new Random();
//		int i1 = r1.nextInt(20);
//		String i2 =String.valueOf(i1);
//		String i2 =Integer.toString(i1);
//		Building building = ContextManager.shelter.get(i2);
//		Building building = ContextManager.shelter.get(Integer.toString(371));
//		Building building = ContextManager.shelter.get(Integer.toString(723));

		return building;
//		Building building = ContextManager.shelter.get(Integer.toString(26));
//		return building;
	}
	public Building GoDestination() {
		//获取agent出发的地址以此来判断到去哪里
		try {
			Building originBuilding=this.getHome();
			String idstr=originBuilding.getIdentifier();
			Integer leftBottomArea[]=ContextManager.leftBottomArea;
			Integer leftTopArea[]=ContextManager.leftTopArea;
			Integer OriginArea[]=ContextManager.originArea;
			int bid=Integer.parseInt(idstr);
			if(Utils.contain(bid,leftBottomArea)) {
				Random r1=new Random();
				int i1=r1.nextInt(ContextManager.rightTopDest.length-1);
				Building building = ContextManager.shelter.get(Integer.toString(ContextManager.rightTopDest[i1]));
				return building;
			}else if(Utils.contain(bid, leftTopArea)) {
				Random r1=new Random();
				int i1=r1.nextInt(ContextManager.rightBottomDest.length-1);
				Building building = ContextManager.shelter.get(Integer.toString(ContextManager.rightBottomDest[i1]));
				return building;
			}else if(Utils.contain(bid, OriginArea))
			{
				Random r1=new Random();
				int i1=r1.nextInt(ContextManager.originDest.length-1);
				Building building = ContextManager.shelter.get(Integer.toString(ContextManager.originArea[i1]));
				return building;
			}
		} catch (NoIdentifierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean atDestination = false;
	@Override
	public void step() throws Exception {
//		LOGGER.log(Level.FINE, "InfectedAgent " + this.id + " is stepping.");
		
		Random r1 = new Random();
//		if(this.random){
//			if (this.route == null) {
//				this.goingHome = false; // Must be leaving home
//				// Choose a new building to go to
//				Building b = ContextManager.buildingContext.getRandomObject();
//				this.route = new Route(this, b.getCoords(), b);
//				LOGGER.log(Level.FINE, this.toString() + " created new route to " + b.toString());
//			}
//			if (!this.route.atDestination()) {
//				this.route.travel();
//				LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
//			} else {
//				// Have reached destination, now either go home or onto another building
//				if (this.goingHome) {
//					this.goingHome = false;
//					Building b = ContextManager.buildingContext.getRandomObject();
//					this.route = new Route(this, b.getCoords(), b);
//					LOGGER.log(Level.FINE, this.toString() + " reached home, now going to " + b.toString());
//				} else {
//					LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString()
//							+ ", now going home");
//					this.goingHome = true;
//					this.route = new Route(this, this.home.getCoords(), this.home);
//				}
//
//			}
//		}
//		else{
			if(this.atDestination){
				//婵炴潙顑堥惁锟�  濞戞搫鎷烽柣鈺冾棎缁额參宕欓崫鍕�卞☉鎿勬嫹濞戞挾灏巇  mr.wang
//				LOGGER.log(Level.INFO, "uniqueID " + this.uniqueID);
				return;}
			//闂佹彃绉甸弻濠勶拷瑙勭煯缁犵喖鎯勯鐐暠闁革讣鎷�
			if(reset && this.route != null){
				this.reset = false;
				Building building = judgeDestination(null,this);
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.FINE, this.toString() + " created new route to " + building.toString());
					//婵炴潙顑堥惁锟�  Mr.wang
//					LOGGER.log(Level.INFO, this.toString() + " created new route to " + building.toString());
				}
			}
			else if (!hasSetoff && this.route == null) {
				this.goingHome = false; // Must be leaving home
				this.hasSetoff = true;//缂佹鍏涚粩鏉戔枎閳ュ啿姣夐柛娆欐嫹
//				LOGGER.log(Level.INFO, "InfectedAgent " + this.id + "缂佹鍏涚粩鏉戔枎閳ュ啿姣夐柛娆欐嫹");
				// 闁圭數鍋撳〒鑸垫交閹寸姵鐣眘helter
				Building building = judgeDestination(this.home,this);
//				LOGGER.log(Level.INFO, this.id + " infectedAgent get building id = " + building.toString());
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.INFO, this.id + " infectedAgent闁兼儳鍢茶ぐ鍥儎椤旂偓鐣遍柛锔兼嫹 " + this.route.getDestinationBuilding());
					LOGGER.log(Level.FINE, this.toString() + " created new route to " + building.toString());
				}
				
	//			//Building b = ContextManager.buildingContext.getRandomObject();
	//			Building dst = ContextManager.buildingContext.getObjects(Building.class).get(276);
	//			this.route = new Route(this, dst.getCoords(), dst);
	//			LOGGER.log(Level.FINE, this.toString() + " created new route to " + dst.toString());
				
			}
			if(this.route == null)
				return;
			
			if (!this.route.atDestination()) {
				
				this.route.travel(this);
				int i1 = r1.nextInt(100);
				
				double Count=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				System.out.println("infectedagent的感染概率为"+this.getInfectRatio());
				LOGGER.log(Level.INFO, "agent"+""+this.id);
				if((Count-(double)this.getstageTickCount())==(double)this.SwitchStageDay) {
//					for (StatusContext statuscontext:statusContext) {
//						if (statuscontext.getStrategy().choose(this))
//						{
//							statuscontext.getStrategy().handler(this,i1);
//							break;
//						}
//					}
					if(this.getInfectRatio() > 50) {
						System.out.println("infectedagent的病情恶化"+this.id);
						LOGGER.log(Level.INFO, "agent的病情恶化"+""+this.id);			
						SwitchInfectedAgent(this);
					}else {
						System.out.println("infectedagent进入了康复期"+""+this.id);
						LOGGER.log(Level.INFO, "agent进入了康复期"+""+this.id);
						changeInfectedAgent(this);
					}
				}
				
				
				
				

//				double searchDistance = 50;
//				List nearAgent = Utils.getObjectsWithinDistance(this, 
//						MyAgent.class, searchDistance);
//				
//				System.out.println(nearAgent.toString());
				
//				LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else {
				//1. 閻犲浂娅攅stination闁告瑥鐗愮换姗�寮堕妷銈囶伇濞戞搩浜欏Ч锟�

				Building dst = this.route.getDestinationBuilding();
				dst.addOccuption(1);
				if(dst.getOccupation() >= dst.getLimit()){
					synchronized(shelterLock){
						ContextManager.shelter.remove(dst.getIdentifier());
					}
	//				synchronized(ContextManager.noticedAgent){
	//					ContextManager.noticedAgent = ContextManager.agentContext.size();
	//				}
					//闂侇偅姘ㄩ悡锟犲箥閿熶粙寮甸崼绺nt閻犲浂娅僪elter婵犲ň锟借尙鍟�
//					Iterator<IAgent> i = ContextManager.getAllAgents().iterator();
//					while(i.hasNext()){
//						IAgent next = i.next();
//						next.setReset(true);
//					}
				}
				//2. 缂備焦鎸诲锟�
				this.route = null;
				this.atDestination = true;


				
				synchronized(ContextManager.agentlock){
					ContextManager.infectedagentnotAthome--;
//					this.infection =true;
//					LOGGER.log(Level.INFO, "agent " + this.uniqueID + this.infection + ContextManager.agentCount  );
//					LOGGER.log(Level.INFO, "infection " + this.infection);
					//閺夊牊鎸搁崵鐠entCount闁汇劌瀚弳鐔兼煂閿燂拷  婵炴潙顑堥惁锟� Mr.wang
//					LOGGER.log(Level.INFO, "ContextManager.agentCount " + ContextManager.agentCount);
				}
				//濞寸姴绌琯entContext濞戞搩鍘奸崹褰掓⒔閵堝牜鍤夐柣鎰舵嫹 20140312
//				synchronized(ContextManager.agentContext){
//					ContextManager.agentContext.remove(this);
//				}
				
	//			// Have reached destination, now either go home or onto another building
	//			if (this.goingHome) {
	//				this.goingHome = false;
	//				Building b = ContextManager.buildingContext.getRandomObject();
	//				this.route = new Route(this, b.getCoords(), b);
	//				LOGGER.log(Level.FINE, this.toString() + " reached home, now going to " + b.toString());
	//			} else {
	//				LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString()
	//						+ ", now going home");
	//				this.goingHome = true;
	//				this.route = new Route(this, this.home.getCoords(), this.home);
	//			}
	
			}
//		}

	} // step()
	/**
	 * 闁哄倹婢橀·鍐箛閻斿摜鍘犻柡鍌濐潐绾拷
	 * 
	 * Infect nearby humans.
	 * 闁哄牆顦靛Λ鑸碉紣閿燂拷  婵炲备鍓濆﹢浣猴拷鐟拌嫰閺夛拷
	 */

	/**
	 * 
	 
	public void infect() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("AgentGeography");
		
		 
		//闁肩厧鍟ú鍧楀矗閸屾稒娈堕柡鍫濐槸缁剁喖宕崱姘煎悪
		double infectRadius =50;
		List humans = null;

		 humans = Utils.getObjectsWithinDistance(this, MyAgent.class, 
			  		infectRadius);
		System.out.println(humans.toString());
		
		
		if(humans.size() > 0) {
			
		MyAgent a = (MyAgent)humans.get(0);		

				
		InfectedAgent b = new InfectedAgent();	
		Building DestinationBuilding = a.getRoute().getDestinationBuilding();
		Route JZroute = new Route(this, DestinationBuilding.getCoords(), DestinationBuilding);
		b.setRoute(JZroute);
		Point JzPoint = (Point)geography.getGeometry(a).getCentroid();
		context.remove(a);
		
		context.add(b);
//		geography.move(b, myAgentGeo);
		ContextManager.moveAgent(b,JzPoint);
//		context = ContextUtils.getContext(this);
//		geography = (Geography)context.getProjection("AgentGeography");

		}

	}
	
	*/
	
	
	/**
	 * 
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	@Override
	public void setHome(Building home) {
		this.home = home;
	}

	@Override
	public Building getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "InfectedAgent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InfectedAgent))
			return false;
		InfectedAgent b = (InfectedAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	private boolean reset;
	@Override
	public void setReset(boolean isReset) {
		this.reset = isReset;
	}

	//婵炲鍔嶉崜锟�: 闁革腹鍟廇gent闁规亽鍎辫ぐ娑欑▔椤撶喐绠掗悗瑙勭煯缁狅拷
	public int getID(){
		return this.id;
	}
	public boolean isRandom() {
		return random;
	}
	@Override
	public void setRandom(boolean random) {
		this.random = random;
	}
	@Override
	public boolean getRehabilitated() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean setRehabilitated() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setRehabilitated(boolean Rehabilitated) {
		// TODO Auto-generated method stub
		
	}
}
