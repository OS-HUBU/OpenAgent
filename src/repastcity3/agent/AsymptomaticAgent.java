package repastcity3.agent;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;

public class AsymptomaticAgent implements IAgent {
	private static Logger LOGGER = Logger.getLogger(AsymptomaticAgent.class.getName());

	private Building home; // Where the agent lives
	/** 闁哄嫷鍨伴幆浣割啅閼碱剛鐥呴柛鎴濇惈瑜板倹绂嶉敓锟�(濞戞捁妗ㄧ花锟犲礌閸濆嫬鐎荤紒妤婂厸缁旀潙鈻庨敓锟�) */
	private boolean hasSetoff = false;
	private Route route; // An object to move the agent around the world
	
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	private static int uniqueID = 0;	//agent闁汇劌鍤楧
	private int id;	
	
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
	
	/**
	 * 闁诡剝顔婂Ч澶愬极閹峰瞼孝閺夆晛娲︽晶宥夊嫉婵夌爆elter闁汇劌瀚鎰棯閾忣偅娈舵鐐存构缁楁牕顭ㄩ垾鑼晩闁汇劌瀚鍌炲磹濞嗘瑧绀夐柛鎾櫃缁楀懘鎯冮崟顏呯溄闂傚浄鎷烽悷鏇氳喘andom
	 */
	private boolean random;
	
	private String AgentType = "AsymptomaticAgent";
	
	public String getAgentType(){
		return this.AgentType;
	}
	
//	//闁煎浜滈悾鐐▕婢跺瞼鎽滈柣锝忔嫹
//	private boolean infection = false;
	
	public AsymptomaticAgent() {	
		this.id = uniqueID++;
	}
	public AsymptomaticAgent(boolean random) {
		this();
		this.random = random;
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
	
	/**
	 * 閻熸瑥瀚悾楣冨疾妤﹀灝鍘村ù锝嗘尭閸╁本娼忛崜褎鐣遍柣鈺婂枤濞堟垿宕烽敓锟�
	 * @param home 闁兼眹鍎插﹢顓㈠礆濠靛棭娼楅柛鏍ㄧ壄缁辨繈宕氬顩昺e濞戞挸绉靛Σ鍝ョ矚閻氬骞㈤柛姘剧畱閸ㄧ棢ome闁哄嫷鍨抽埞锟�;agent 濞戞挾鐨猦is
	 * @return
	 */
	private Building judgeDestination(Building home,AsymptomaticAgent agent){
//		int buildingIDnum=0;
//		int num=0,firstDim=0,lastPart=0;
//		for(;num<agent.getID();){
//			num++;
//			if(num-lastPart>(Route.initInfos[firstDim][3]-1)){
//				lastPart=num;
//				firstDim++;
//			}
//		}
//		buildingIDnum=Route.initInfos[firstDim][1]+1;
//		Building building = ContextManager.shelter.get(Integer.toString(buildingIDnum));
		Random r1 = new Random();
		int i1 = r1.nextInt(20);
//		String i2 =String.valueOf(i1);
		String i2 =Integer.toString(i1);
		Building building = ContextManager.shelter.get(i2);
//		Building building = ContextManager.shelter.get(Integer.toString(723));
		return building;
//		Building building = ContextManager.shelter.get(Integer.toString(26));
//		return building;
	}
	
	private boolean atDestination = false;
	@Override
	public void step() throws Exception {
//		LOGGER.log(Level.FINE, "AsymptomaticAgent " + this.id + " is stepping.");
		
		
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
			if(this.atDestination){//闁哄嫷鍨伴幆渚�宕氶幏灞惧涧闁烩晩鍠氬▓鎴﹀捶閿燂拷
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
//				LOGGER.log(Level.INFO, "AsymptomaticAgent " + this.id + "缂佹鍏涚粩鏉戔枎閳ュ啿姣夐柛娆欐嫹");
				// 闁圭數鍋撳〒鑸垫交閹寸姵鐣眘helter
				Building building = judgeDestination(this.home,this);
//				LOGGER.log(Level.INFO, this.id + " AsymptomaticAgent get building id = " + building.toString());
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.INFO, this.id + " AsymptomaticAgent闁兼儳鍢茶ぐ鍥儎椤旂偓鐣遍柛锔兼嫹 " + this.route.getDestinationBuilding());
					LOGGER.log(Level.FINE, this.toString() + " created new route to " + building.toString());
				}
				
	//			//Building b = ContextManager.buildingContext.getRandomObject();
	//			Building dst = ContextManager.buildingContext.getObjects(Building.class).get(276);
	//			this.route = new Route(this, dst.getCoords(), dst);
	//			LOGGER.log(Level.FINE, this.toString() + " created new route to " + dst.toString());
				
			}
//			if(this.route == null)
//				return;
			if (!this.route.atDestination()) {
				this.route.travel(this);									//闁稿繈鍎辫ぐ娑㈡晬閿燂拷 婵縿鍊曢崵閬嶅极閹殿喗鏆忛柡澶堝劥椤㈡垶娼诲☉娆愵仭闁艰櫕鍨濈紞瀣晬鐏炵厧鈻忛柡鍛寸細閸忔ɑ鎷呴幘鍐茬厒閺夊牆澧藉ú浼存儍閸曨偅鍕鹃懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼村疇鍔ら懘瀹犲姢閼存娊鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁辨帡鏁嶆笟濠勭＜闁挎稐绶ょ槐鎺楁晬娓氬﹦纾奸柨娑楃筏缁憋拷
//				LOGGER.log(Level.INFO,  "AsymptomaticAgent"+this.getID()+ " 閻犲鍟慨鈺傜閸炴ⅳavel闁哄倽顫夌涵锟�"   );
//				this.infect();
				//闁瑰吋绮庨崒銊ノ熼垾铏仴
//				double searchDistance = 50;
//				List nearAgent = Utils.getObjectsWithinDistance(this, 
//						MyAgent.class, searchDistance);
//				
//				System.out.println(nearAgent.toString());
				
				LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else {
				//1. 閻犲浂娅攅stination闁告瑥鐗愮换姗�寮堕妷銈囶伇濞戞搩浜欏Ч锟�
//				LOGGER.log(Level.INFO, "AsymptomaticAgent " + this.id + "闁告帗濯介幓顏堟儎椤旂偓鐣遍柛锔兼嫹");
				Building dst = this.route.getDestinationBuilding();
				dst.addOccuption(1);
//				LOGGER.log(Level.INFO, "AsymptomaticAgent " + this.id + "闁告帗濯介幓顏堟儎椤旂偓鐣遍柛锔兼嫹");
//				System.out.println("AsymptomaticAgent " + this.id + "闁告帗濯介幓顏堟儎椤旂偓鐣遍柛锔兼嫹");
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
					ContextManager.AsymptomaticnotAthome--;
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
		return "AsymptomaticAgent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AsymptomaticAgent))
			return false;
		AsymptomaticAgent b = (AsymptomaticAgent) obj;
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
	public String getAge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAge(String Age) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStage(String stage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSwitchStageDay(int day) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSwitchStageDay() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInfectRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInfectRatio(int ratio) {
		// TODO Auto-generated method stub
		
	}
}
