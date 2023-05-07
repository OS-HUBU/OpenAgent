package repastcity3.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


import repast.simphony.context.Context;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.ContextManager;
import repastcity3.agent.Utils;
import repastcity3.agent.InfectedAgent;

////濞村鐦ǎ璇插閸ョ偓鐖ｉ崝鐔诲厴
//import repastcity3.agent.BuildingFull;

public class MyAgent  implements IAgent {
	
	private static Logger LOGGER = Logger.getLogger(MyAgent.class.getName());
	
	private Building home; // Where the agent lives
	/** 閺勵垰鎯佸鑼病閸戝搫褰傛禍锟�(娑撹桨绨￠崠鍝勫瀻缁楊兛绔村▎锟�) */
	private boolean hasSetoff = false;
	private Route route; // An object to move the agent around the world
	private boolean Rehabilitated=false;
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	private static int uniqueID = 0;	//agent閻ㄥ嚘D
	private int id;	
	private Double CustomSpeed;//自定义速度
	
	public Double getCustomSpeed() {
		return this.CustomSpeed;
	}
	public void setCustomSpeed(Double CustomSpeed) {
		this.CustomSpeed=CustomSpeed;
	}
	public boolean getRehabilitated() {
        return this.Masks;
    }

    public void setRehabilitated(boolean Rehabilitated) {
        this.Rehabilitated = Rehabilitated;
    }


    private int SwitchStageDay;
    private String stage;
	public void setStage(String stage) {
		this.stage = stage;
	}
	public String getStage() {
		return stage;
	}
	
	public void setSwitchStageDay(int day) {
		SwitchStageDay=day;
	}
	public int getSwitchStageDay() {
		return SwitchStageDay;
	}
	private String age;//young old
	public void setAge(String age) {
		this.age = age;
	}
	public String getAge() {
		return age;
	}
	
	private boolean Masks;
    private boolean Vaccine;
    public boolean getMasks() {
        return this.Masks;
    }

    public void setMasks(boolean masks) {
        this.Masks = masks;
    }

    public boolean getVaccine() {
        return this.Vaccine;
    }

    public void setVaccine(boolean vaccine) {
        this.Vaccine = vaccine;
    }

	private int infectRatio = 0; //Mr.wang 
	private double tickCount = 0 ; //Mr.wang  
	private String AgentType = "NormalAgent";
	
	public String getAgentType(){
		return this.AgentType;
	}


	
	
	public void setTickCount(double count) {
		this.tickCount = count;
	}
	public double getTickCount() {
		return this.tickCount;
	}
	public int getInfectRatio() {
		return this.infectRatio;
	}
	
	public void setInfectRatio(int ratio) {
		this.infectRatio = ratio;
	}
	
	/**
	 * 閹姹夐弫鎷岀Т鏉╁洦澧嶉張濉籬elter閻ㄥ嫬顔愮痪铏殶楠炴湹绗栧鈥茬啊閻ㄥ嫭妞傞崐娆欑礉閸撯晙绗呴惃鍕眽闂囷拷鐟曚购andom
	 */
	private boolean random;
	
	//閼奉亜鐣炬稊澶岀摜閻ｏ拷
//	private boolean infection = false;
	
	//鐠佹澘缍峵ravel濞嗏剝鏆�
	public static int cishu =0;
	
	public MyAgent() {	
		this.id = uniqueID++;
	}
	public MyAgent(boolean random) {
		this();
		this.random = random;
	}
	
	//濞ｈ濮�
	public Route getRoute() {
		return this.route;
	}
	
	public void setRoute(Route route) {
		this.route = route;
	}
	
	/**
	 * 濞ｈ濮炵純鎴炵壐
	 * 
	 * **/
		
	
	/**
	 * 閹垫儳鍩岄張锟芥潻鎴犳畱shelter
	 * @param home 閼汇儲婀崚婵嗩潗閸栨牭绱濋崚姗me娑撳秵妲哥粚鐚寸幢閸氾箑鍨痟ome閺勵垳鈹�
	 * @return
	 */
	private Building judgeNearestBuilding(Building home){
		Coordinate currentCoord = null;	//瑜版挸澧犻崸鎰垼缂冾喕璐熺粚锟�
		if(home == null)
			currentCoord = this.route.getCurrentCoord();//瑜版挸澧燼gent閸ф劖鐖ｇ拋鍓х枂娑撳搫缍嬮崜宥勭秴缂冿拷
		else
			currentCoord = home.getCoords();//閹跺ケgent閻ㄥ嫪缍呯純顔款啎缂冾喖婀�规湹鑵�
		String buildingId = null;
		double distance = -1;
		synchronized(ContextManager.shelter){// 20140312
//		闁秴宸婚崶鍙ヨ厬閻ㄥ嫭澧嶉張濉籬elter閿涘本澹橀崚鐗堟付鏉╂垹娈戦敍灞界摠閸︹暈uildingID閸滃畳istance娑擄拷************************************
		for(String id : ContextManager.shelter.keySet()){
			Coordinate targetCoord = ContextManager.shelter.get(id).getCoords();
			double[] distAndAngle = new double[2];
			Route.distance(currentCoord, targetCoord, distAndAngle);
			if((distance == -1) || (distance > 0 && distAndAngle[0] < distance)){
				distance = distAndAngle[0];
				buildingId = id;
			}
		}
	}
		
		
		if(buildingId != null){
			Building building = ContextManager.shelter.get(buildingId);
			return building;
		}
		else
			return null;//閸樼粯甯�濞夈劑鍣�/鑴� 鑴�/ 閸掔娀娅庢稉瀣╃鐞涘瞼娈憆eturn    閹碉拷閺堝娈慳gent闁棄澧犲锟� shelter 48   鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴抽敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱�
		//return ContextManager.shelter.get("377");
	}
	
	private static Object shelterLock = new Object();
	
	/**
	 * 鐟欏嫬鐣鹃弲楦垮厴娴ｆ挸鍩屾潏鍓ф畱閻╊喚娈戦崷锟�
	 * @param home 閼汇儲婀崚婵嗩潗閸栨牭绱濋崚姗me娑撳秵妲哥粚鐚寸幢閸氾箑鍨痟ome閺勵垳鈹�;agent 娑撶皪his
	 * @return
	 */
	private Building judgeDestination(Building home,MyAgent agent){
		
//		if(agent.hasSetoff==false) {
//			
//			return home;
//		}else {
//		int buildingIDnum=0;
//		int num=0,firstDim=0,lastPart=0;
//		for(;num<agent.getID();){
//			num++;
//			if(num-lastPart>(Route.initInfo[firstDim][3]-1)){
//				lastPart=num;
//				firstDim++;
//			}
//		}
//		buildingIDnum=Route.initInfo[firstDim][1]+1;
//		Building building = ContextManager.shelter.get(Integer.toString(buildingIDnum));
		//获取agent出发的地址以此来判断到去哪里
		Building building=GoDestination();
//		Random r1 = new Random();
//		int i1 = r1.nextInt(20);
//		String i2 =Integer.toString(i1);
//		Building building = ContextManager.shelter.get(i2);
//		Building building = ContextManager.shelter.get(Integer.toString(424));
//		Building building = ContextManager.shelter.get(Integer.toString(723));
	
//		Building building = ContextManager.shelter.get(i1);
		return building;
//		}
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
				System.out.println("asd"+building);
				return building;
			}else if(Utils.contain(bid, leftTopArea)) {
				Random r1=new Random();
				int i1=r1.nextInt(ContextManager.rightBottomDest.length-1);
				Building building = ContextManager.shelter.get(Integer.toString(ContextManager.rightBottomDest[i1]));
				System.out.println("asd"+building);
				return building;
			}else if(Utils.contain(bid, OriginArea))
			{
				Random r1=new Random();
				int i1=r1.nextInt(ContextManager.originDest.length-1);
				Building building = ContextManager.shelter.get(Integer.toString(ContextManager.originArea[i1]));
				System.out.println("asd"+building);
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
		
//		if(this.route.atDestination() == true) {
//			
//		}
//		LOGGER.log(Level.INFO, "MyAgent " + this.id + " is stepping.");
		
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
//							+ ", now going home");
//					this.goingHome = true;
//					this.route = new Route(this, this.home.getCoords(), this.home);
//				}
//
//			}
//		}
//		else{
			if(this.atDestination){//閺勵垰鎯侀崚鎷屾彧閻╊喚娈戦崷锟�
				//濞村鐦�  娑擄拷閻╃绶崙鍝勬倱娑擄拷娑撶導d  mr.wang
//				LOGGER.log(Level.INFO, "uniqueID " + this.uniqueID);
				return;}
			//闁插秵鏌婄�规矮绠熼惄顔炬畱閸︼拷
			if(reset && this.route != null){
				this.reset = false;
//				Building building = judgeDestination(null,this);
				Building building = judgeNearestBuilding(null);
//				Building building =  ContextManager.shelter.get(Integer.toString());
//				LOGGER.log(Level.INFO,"閺屻儲澹橀張锟芥潻鎴犳畱闁潡姣﹂幍锟絠d============"+building.getIdentifier());
			
//				Building building =  ContextManager.shelter.get(Integer.toString(32));
//				LOGGER.log(Level.INFO, "agent閻ㄥ埇d閵嗗锟藉锟藉锟藉锟藉锟藉锟藉锟藉锟藉锟斤拷"+this.id+"鏉╂稐绨＄粭顑跨娑擄拷==============================="+this.route);
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.FINE, this.toString() + " created new route to " + building.toString());
					//濞村鐦�  Mr.wang
//					LOGGER.log(Level.INFO, this.toString() + " created new route to " + building.toString());
				}
			}
			else if (!hasSetoff && this.route == null) {
				
//				LOGGER.log(Level.INFO, "agent閻ㄥ埇d閵嗗锟藉锟藉锟藉锟藉锟藉锟藉锟藉锟藉锟斤拷"+this.id+"鏉╂稐绨＄粭顑跨癌娑擄拷==============================="+this.route);
				this.goingHome = false; // Must be leaving home
				this.hasSetoff = true;//濮濄倖顐肩亸杈ㄦЦ缁楊兛绔村▎鈥冲毉閸欙拷
//				LOGGER.log(Level.INFO, "MyAgent " + this.id + "缁楊兛绔村▎鈥冲毉閸欙拷");
				// 閹电偓娓舵潻鎴犳畱shelter
				Building building = judgeDestination(this.home,this);
				//LOGGER.log(Level.INFO, this.id + " MyAgent get building id = " + building.toString());
//				LOGGER.log(Level.INFO, this.id+"" +"building= " +building );
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.INFO, this.id + " MyAgent閼惧嘲褰囬惄顔炬畱閸︼拷 " + this.route.getDestinationBuilding());
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
				this.route.travel(this);//閸忋儱褰涢敍锟� 濮濄倕鍤遍弫鎵暏閺夈儴顢戞潻娑欐閼虫垝缍嬮敍灞煎▏閺呴缚鍏樻担鎾冲煂鏉堝墽娲伴惃鍕勾鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴抽敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱�
				LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else if(this.route.forbid == true){//娑撳瓨妞傚ǎ璇插 Mr.wang
				return;
			}else {
				//1. 鐠囶櫔estination閸欏牐绻橀弶銉ょ娑擃亙姹�
				Building dst = this.route.getDestinationBuilding();
				dst.addOccuption(1);
				if(dst.getOccupation() >= dst.getLimit()){
					synchronized(shelterLock){
						ContextManager.shelter.remove(dst.getIdentifier());
					}
					
					synchronized(ContextManager.noticedAgent){
						ContextManager.noticedAgent = ContextManager.agentContext.size();
					}
//					闁氨鐓￠幍锟介張鍫縢ent鐠囶櫃helter濠娾�茬啊
					Iterator<IAgent> i = ContextManager.getAllAgents().iterator();
					while(i.hasNext()){
						IAgent next = i.next();
						next.setReset(true);//閺�鐟板綁reast鐏炵偞锟斤拷
					}
				}
				//2. 缂佹挻娼�
				this.route = null;
				this.atDestination = true;

//				System.out.println("濞嗏剝鏆�="+this.cishu+"agentID:"+this.uniqueID);
				
				synchronized(ContextManager.agentlock){
					ContextManager.agentnotAthome--;
				}

			}

	} // step()

	/**
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

	//鐠佹澘缍嶇悰宀冭泲鏉╁洨娈慴uilding
	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "MyAgent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MyAgent))
			return false;
		MyAgent b = (MyAgent) obj;
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

	//濞夈劍鍓�: 閸︹問Agent閹恒儱褰涙稉顓熸箒鐎规矮绠�
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
}