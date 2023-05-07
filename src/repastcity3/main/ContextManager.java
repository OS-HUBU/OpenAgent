/*
閿熺春opyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
*/

package repastcity3.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import rapastcity3.Socket.FlagClazz;
import rapastcity3.Socket.SendMessageServer;
//import rapastcity3.Socket.SocketServer;
import rapastcity3.Socket.SocketStatusManager;
import rapastcity3.Socket.StepDataServer;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.CallBackAction;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repastcity3.agent.AgentFactory;
import repastcity3.agent.BuildingInfo;
import repastcity3.agent.IAgent;
import repastcity3.agent.InfectedAgent;
import repastcity3.agent.MyAgent;
import repastcity3.agent.ThreadedAgentScheduler;
import repastcity3.environment.Building;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.Junction;
import repastcity3.environment.NetworkEdgeCreator;
import repastcity3.environment.Road;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.environment.TrafficJam;
import repastcity3.environment.contexts.AgentContext;
import repastcity3.environment.contexts.BuildingContext;
import repastcity3.environment.contexts.JunctionContext;
import repastcity3.environment.contexts.RoadContext;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.exceptions.EnvironmentError;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.exceptions.ParameterNotFoundException;

public class ContextManager implements ContextBuilder<Object> {

	public static int TRIFFIC_JAM_COUNT=0;//閻€劍娼电�涙ê鍋嶇拋鍓х枂閹枫儱鐗崚銈嗘焽閻ㄥ嫯鐭鹃崣锝囨畱閺佷即鍣�
	public static int MAssistanceThreshold;
	
	/*
	 * A logger for this class. Note that there is a static block that is used to configure all logging for the model
	 * (at the bottom of this file).
	 */
	private static Logger LOGGER = Logger.getLogger(ContextManager.class.getName());

	// Optionally force agent threading off (good for debugging) 閿涘牆褰查柅澶涚礆瀵搫鍩楅崗鎶芥４娴狅絿鎮婄痪璺ㄢ柤閿涘牓锟藉倻鏁ゆ禍搴ょ殶鐠囨洩绱�
	private static final boolean TURN_OFF_THREADING = false;

	private static Properties properties;
	
	public static Map<String, Building> shelter = new HashMap<String, Building>();

	/*
	 * Pointers to contexts and projections (for convenience). Most of these can be made public, but the agent ones
	 * can't be because multi-threaded agents will simultaneously try to call 'move()' and interfere with each other. So
	 * methods like 'moveAgent()' are provided by ContextManager.
	 */

	private static Context<Object> mainContext;

	// building context and projection cab be public (thread safe) because buildings only queried
	public static Context<Building> buildingContext;
	public static Geography<Building> buildingProjection;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;

	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	public static Network<Junction> roadNetwork;
	//设置左下角的起始地址
	public static Integer[] leftBottomArea= {368,479,446,462,400,480,249,404,402,416,244,247,240,241,242};
	//设置左上脚的起始地址
	public static Integer[] leftTopArea= {371,265,193,198,112,220,231};
	//设置右上角的目的地址
	public static Integer[] rightTopDest= {514,512,550,708,553,554,509,520};
	//设置右下角的目的地址
	public static Integer[] rightBottomDest={365,365,507,499};
	//设置原始的地址
	public static Integer[] originArea= {28,20,30,31,34,33,11,79,67,5,8,26};
	//设置原始的目的地址
	public static Integer[] originDest= {28,20,30,31,34,33,11,79,67,5,8,26};
	public static Object agentlock = new Object();
	public static int agentCount; //正常人的总数
	public static  volatile  int infectedagentCount;  
	public static volatile int infectedagentLatentCount=0; //潜伏期人数 
	public static volatile int infectedagentOnsetCount=0; //发病期人数
	public static volatile int infectedagentSevereCount=0; //重症期人数
	public static volatile int DeathNumber=0;//死亡人数
	public static volatile int AsymptomaticAgentCount; 
    public static volatile int agentnotAthome;
    public static volatile int infectedagentnotAthome;
    public static volatile int AsymptomaticnotAthome;
    public static volatile int AllAgentNumbers=0;//总人数
	public static Context<IAgent> agentContext;
	private static Geography<IAgent> agentGeography;

	public static Integer noticedAgent = 0;
	public static List<Building> shelterList = new ArrayList<Building>();
	
//	public static List<BuildingInfo> buildingInfo = new ArrayList<BuildingInfo>();
//	public static List<BuildingInfo> InfectedbuildingInfo = new ArrayList<BuildingInfo>();
	
	public static ContinuousSpace<Object> space;
	public static Grid<Object> grid;
	
	public static Object infectedAgentContextLock = new Object();
	
	public static boolean Medical = true;
	/**
	 * 娴滅儤鏆熺搾鍛毉shelter鐎瑰湱鎾奸弫鎵畱閺佷即鍣洪敍宀冪箹娴滄稒鏆熼柌蹇曟畱agent闂囷拷鐟曚線娈㈤張楦胯泲閵嗭拷
	 */
	
	public static Integer homelessAgentCnt = 0;
	/**
	 * 获取agent出生地方法
	 * @return
	 */
	public static List<Integer> getBirthAgentList() {
		//定义生成agent位置集合
		List<Integer> birthAgentList=new ArrayList();
		int k=leftBottomArea.length;
		for (int i=0;i<k;i++)
		{
			birthAgentList.add(leftBottomArea[i]);
		}
		k=leftTopArea.length;
		for(int i=0;i<k;i++)
		{
			birthAgentList.add(leftTopArea[i]);
		}
		k=originArea.length;
		for(int i=0;i<k;i++)
		{
			birthAgentList.add(originArea[i]);
		}
		return birthAgentList;
		
	}
	@Override
	public Context<Object> build(Context<Object> con) {
		Object lock=new Object();
		RepastCityLogging.init();

		// Keep a useful static link to the main context
		mainContext = con;

		// This is the name of the 'root'context
		mainContext.setId(GlobalVars.CONTEXT_NAMES.MAIN_CONTEXT);

//		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
//				"infection network", mainContext, true);
//		netBuilder.buildNetwork();
//		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
//				.createContinuousSpaceFactory(null);
//		ContinuousSpace<Object> space1 = spaceFactory.createContinuousSpace(
//				"space", mainContext, new RandomCartesianAdder<Object>(),
//				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
//				50);
//		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
//		Grid<Object> grid1 = gridFactory.createGrid("grid", mainContext,
//				new GridBuilderParameters<Object>(new WrapAroundBorders(),
//						new SimpleGridAdder<Object>(), true, 50, 50));	
//		this.space = space1;
//		this.grid = grid1;
		
		
		
		
		// Read in the model properties
		try {
			readProperties();
		} catch (IOException ex) {
			throw new RuntimeException("Could not read model properties,  reason: " + ex.toString(), ex);
		}

		// Configure the environment
		String gisDataDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
		LOGGER.log(Level.FINE, "Configuring the environment with data from " + gisDataDir);
		
//		System.out.println("---------------------------------------------------------------------------------------------------------");
//		System.out.println(gisDataDir);
		
		//ContextManager.getParameter
		try {

			// Create the buildings - context and geography projection  閸掓稑缂撳铏圭摎閻楋拷 - 閼冲本娅欓崪灞芥勾閻炲棙濮囪ぐ锟�
			buildingContext = new BuildingContext();
			buildingProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.BUILDING_GEOGRAPHY, buildingContext,
					new GeographyParameters<Building>(new SimpleAdder<Building>()));
			
//			System.out.println("---------------------------------------------------------------------------------------------------------");
//			System.out.println(buildingProjection.getCRS());
			
			String buildingFile = gisDataDir + getProperty(GlobalVars.BuildingShapefile);
			
//			System.out.println("---------------------------------------------------------------------------------------------------------");
//			System.out.println(buildingFile);
		                                                                                                                                                                                     	
			GISFunctions.readShapefile(Building.class, buildingFile, buildingProjection, buildingContext);
			
			//濞ｈ濮瀊uildingContext  Mr.wang
			mainContext.addSubContext(buildingContext);
			SpatialIndexManager.createIndex(buildingProjection, Building.class);
			LOGGER.log(Level.FINER, "Read " + buildingContext.getObjects(Building.class).size() + " buildings from "
					+ buildingFile);
			
			
			
			
			try {
				
				//娴犲酣鍘ょ純顔芥瀮娴犳儼骞忓妤呬缉闂呯偓澧嶉崣鍌涙殶  MR.WANG
//				String shelterDef = ContextManager.getParameter(MODEL_PARAMETERS.SHELTER_DEFINITION.toString());
//				String[] shelterParams = shelterDef.split(",");
//				int allLimitCnt = 0;
//				for(int i=0;i<shelterParams.length;i++){
//					String[] params = shelterParams[i].split(":");
//					int id = Integer.parseInt(params[0].trim());
//					Building building = buildingContext.getObjects(Building.class).get(id);
//					int limit = Integer.parseInt(params[1].trim());
//					if(limit <= 0) {
//						
//					}else {
//					allLimitCnt+=limit;
//					building.setLimit(limit);
//					shelterList.add(building);
//					shelter.put(building.getIdentifier(), building);
//					}
//				}
				int num_of_building =buildingContext.getObjects(Building.class).size();
				for(int i=0;i<num_of_building;i++) {
					if(buildingContext.getObjects(Building.class).get(i)!=null) {
						Building building = buildingContext.getObjects(Building.class).get(i);
						building.setLimit(999);
						shelterList.add(building);
						shelter.put(building.getIdentifier(), building);
					}else {
						break;
					}
					
				}
				
				
				/**
				 * 
				
				
				//娴犲酣鍘ょ純顔芥瀮娴犳儼骞忓妤佹閼虫垝缍嬮崙鍝勫絺閻愯澧嶉崣鍌涙殶  MR.WANG
				String agentCnt = ContextManager.getParameter(MODEL_PARAMETERS.AGENT_DEFINITION.toString());
				String infectedagentCnt = ContextManager.getParameter(MODEL_PARAMETERS.INFECTED_DEFINITION.toString());

				
				String[] pattern = agentCnt.split(":");
				String[] infectedpattern = infectedagentCnt.split(":");
				
				
				if("random".equals(pattern[0])){
					ContextManager.agentCount = Integer.parseInt(pattern[1]);
				}
				else if("buildingpoint".equals(pattern[0])){
					ContextManager.agentCount = 0;
					String[] buildingInfos = pattern[1].split(",");
					for(int i=0;i<buildingInfos.length;i++){
						BuildingInfo info = new BuildingInfo();
						String[] curInfo = buildingInfos[i].split("-");
						info.setId(Integer.parseInt(curInfo[0]));
						info.setGenNum(Integer.parseInt(curInfo[1]));
						ContextManager.agentCount += info.getGenNum();//GenNum閸戝搫褰傞悙鍦畱娴滅儤鏆�
						ContextManager.buildingInfo.add(info);
					}
				}
				if("random".equals(infectedpattern[0])){
					ContextManager.infectedagentCount = Integer.parseInt(infectedpattern[1]);
				}
				else if("buildingpointInfected".equals(infectedpattern[0])){
					ContextManager.infectedagentCount = 0;
					String[] buildingInfos = infectedpattern[1].split(",");
					for(int i=0;i<buildingInfos.length;i++){
						BuildingInfo info = new BuildingInfo();
						String[] curInfo = buildingInfos[i].split("-");
						info.setId(Integer.parseInt(curInfo[0]));
						info.setGenNum(Integer.parseInt(curInfo[1]));
						ContextManager.infectedagentCount += info.getGenNum();
						ContextManager.InfectedbuildingInfo.add(info);
					}
				}
				
//				homelessAgentCnt = ContextManager.agentCount -allLimitCnt;
			 	* 
			 	*/
	
				
//			} catch (ParameterNotFoundException e) {
//				LOGGER.log(Level.SEVERE, "闁板秶鐤嗛崣鍌涙殶閺堫亣顕伴崚锟�", e);
//				return null;
			} catch (Exception e){
				LOGGER.log(Level.SEVERE, "Shelter闁板秶鐤嗙拠璇插絿闁挎瑨顕�", e);
				return null;
			}
//			Integer[] ids = {96, 105, 276};
//			for(int i=0;i<ids.length;i++){
//				Building building = buildingContext.getObjects(Building.class).get(ids[i]);
//				shelter.put(building.getIdentifier(), building);
//			}

			// TODO Cast the buildings to their correct subclass

			// Create the Roads - context and geography
			roadContext = new RoadContext();
			 
			roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY, roadContext,
					new GeographyParameters<Road>(new SimpleAdder<Road>()));
			
			String roadFile = gisDataDir + getProperty(GlobalVars.RoadShapefile);
			GISFunctions.readShapefile(Road.class, roadFile, roadProjection, roadContext);
			
			//濞ｈ濮瀝oadContext Mr.wang
			mainContext.addSubContext(roadContext);
			SpatialIndexManager.createIndex(roadProjection, Road.class);
			LOGGER.log(Level.FINER, "Read " + roadContext.getObjects(Road.class).size() + " roads from " + roadFile);

			// Create road network  閸掓稑缂撻柆鎾圭熅缂冩垹绮�

			// 1.junctionContext and junctionGeography
			junctionContext = new JunctionContext();
			
			//濞ｈ濮瀓unctionContext Mr.wang
			mainContext.addSubContext(junctionContext);
			junctionGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY, junctionContext,
					new GeographyParameters<Junction>(new SimpleAdder<Junction>()));

			// 2. roadNetwork
			NetworkBuilder<Junction> builder = new NetworkBuilder<Junction>(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK,
					junctionContext, false);
			
			//new NetworkEdgeCreator<Junction>() 鏉╂柨娲栨稉锟芥稉鐙絜tworkEdge鐎圭偘绶�<T>
			builder.setEdgeCreator(new NetworkEdgeCreator<Junction>());
			roadNetwork = builder.buildNetwork();
			
//			System.out.println("=====================================================================================================");
//			Road a = roadProjection.getAllObjects(1);
//			System.out.println(roadProjection.getAllObjects());
			
			GISFunctions.buildGISRoadNetwork(roadProjection, junctionContext, junctionGeography, roadNetwork);

			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			Iterable<Junction> junctionIt = junctionGeography.getAllObjects();
			for (Junction junction : junctionIt){
				ContextManager.TRIFFIC_JAM_COUNT++;
			}
			for (Junction junction : junctionIt) {
				// Create a LineString from the road so we can extract coordinates
				Geometry junctionGeom = junctionGeography.getGeometry(junction);
				Coordinate c1 = junctionGeom.getCoordinates()[0]; // First coord
				//Coordinate c2 = junctionGeom.getCoordinates()[junction.getNumPoints() - 1]; // Last coord
				TrafficJam jam=new TrafficJam(c1.x,c1.y);
				TrafficJam.list.add(jam);
				LOGGER.log(Level.SEVERE, "X閸ф劖鐖�: " +c1.x + "Y閸ф劖鐖�:"+c1.y);
			}
				// Create Junctions from these coordinates and add them to the JunctionGeograph 閺嶈宓佹潻娆庣昂閸ф劖鐖ｉ崚娑樼紦娴溿倖鐪归悙鐟拌嫙鐏忓棗鍙惧ǎ璇插閸掗姘﹀Ч鍥╁仯閸︽壆鎮婇崶鍙ヨ厬
			
			// Add the junctions to a spatial index (couldn't do this until the
			// road network had been created).
			SpatialIndexManager.createIndex(junctionGeography, Junction.class);

			testEnvironment();

		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "", e);
			return null;
		} catch (EnvironmentError e) {
			LOGGER.log(Level.SEVERE, "There is an eror with the environment, cannot start simulation", e);
			return null;
		} catch (NoIdentifierException e) {
			LOGGER.log(Level.SEVERE, "One of the input buildings had no identifier (this should be read"
					+ "from the 'identifier' column in an input GIS file)", e);
			return null;
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Could not find an input shapefile to read objects from.", e);
			return null;
		}
		StepDataServer stepDataServer=new StepDataServer();
		SocketStatusManager.serverSocketManager.put("stepDataServer", stepDataServer);
		// Now create the agents (note that their step methods are scheduled later   
				try {

					agentContext = new AgentContext();
					mainContext.addSubContext(agentContext);		
					agentGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
							GlobalVars.CONTEXT_NAMES.AGENT_GEOGRAPHY, agentContext,
							new GeographyParameters<IAgent>(new SimpleAdder<IAgent>()));
					SendMessageServer sendMessage=new SendMessageServer(lock);
					SocketStatusManager.serverSocketManager.put("sendMessage", sendMessage);
					
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "", e);
					return null;
				}
			synchronized(lock)
			{
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		// Create the schedule
		createSchedule();                    
		
		return mainContext;
	}

	private static <T> List<T> toList(Iterable i) {
		List<T> l = new ArrayList<T>();
		Iterator<T> it = i.iterator();
		while (it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}

	private void createSchedule() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		// Schedule something that outputs ticks every 1000 iterations.
		schedule.schedule(ScheduleParameters.createRepeating(1, 1000, ScheduleParameters.LAST_PRIORITY), this,
				"printTicks");

		/*
		 * Schedule the agents. This is slightly complicated because if all the agents can be stepped at the same time
		 * (i.e. there are no inter- agent communications that make this difficult) then the scheduling is controlled by
		 * a separate function that steps them in different threads. This massively improves performance on multi-core
		 * machines.
		 */
		boolean isThreadable = true;
		for (IAgent a : agentContext.getObjects(IAgent.class)) {
			if (!a.isThreadable()) {
				isThreadable = false;
				break;
			}
		}

		if (ContextManager.TURN_OFF_THREADING) { // Overide threading?
			isThreadable = false;
		}
		if (isThreadable && (Runtime.getRuntime().availableProcessors() > 1)) {
			/*
			 * Agents can be threaded so the step scheduling not actually done by repast scheduler, a method in
			 * ThreadedAgentScheduler is called which manually steps each agent.
			 * 娴狅絿鎮婇崣顖欎簰缁捐法鈻奸崠鏍电礉閸ョ姵顒濆銉╊�冪拫鍐ㄥ鐎圭偤妾稉濠佺瑝閺勵垳鏁遍柌宥嗘煀缁鍒涚拫鍐ㄥ缁嬪绨�瑰本鍨氶惃鍕剁礉缁捐法鈻兼禒锝囨倞鐠嬪啫瀹崇粙瀣碍娑擃厾娈戞稉锟芥稉顏呮煙濞夋洝顫︾拫鍐暏閿涘矁顕氶弬瑙勭《閹靛濮╅幍褑顢戝В蹇庨嚋閺呴缚鍏樻担鎾憋拷锟�
			 */
			LOGGER.log(Level.INFO, "The multi-threaded scheduler will be used.");
			ThreadedAgentScheduler s = new ThreadedAgentScheduler();
			ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
			schedule.schedule(agentStepParams, s, "agentStep");
		} else { // Agents will execute in serial, use the repast scheduler. 娴狅絿鎮婄亸鍡曡鐞涘本澧界悰宀嬬礉鐠囪渹濞囬悽锟� repast 鐠嬪啫瀹崇粙瀣碍閵嗭拷
			LOGGER.log(Level.INFO, "The single-threaded scheduler will be used.");
			ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
			// Schedule the agents' step methods.
			for (IAgent a : agentContext.getObjects(IAgent.class)) {
				schedule.schedule(agentStepParams, a, "step");
			}
		}

	}
	
	//Tick Count
	public void printTicks() {
		LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
	}
	//Mr.wang 娑撳瓨妞傚ǎ璇插 鏉╂柨娲杢ick
//	public double getTicks() {
//		 return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
//	}

	/**
	 * Convenience function to get a Simphony parameter
	 * 
	 * @param <T>
	 *            The type of the parameter
	 * @param paramName
	 *            The name of the parameter
	 * @return The parameter.
	 * @throws ParameterNotFoundException
	 *             If the parameter could not be found.
	 */
	public static <V> V getParameter(String paramName) throws ParameterNotFoundException {
		Parameters p = RunEnvironment.getInstance().getParameters();
		Object val = p.getValue(paramName);

		if (val == null) {
			throw new ParameterNotFoundException(paramName);
		}

		// Try to cast the value and return it
		@SuppressWarnings("unchecked")
		V value = (V) val;
		return value;
	}

	/**
	 * Get the value of a property in the properties file. If the input is empty or null or if there is no property with
	 * a matching name, throw a RuntimeException.
	 * 
	 * @param property
	 *            The property to look for.
	 * @return A value for the property with the given name.
	 */
	public static String getProperty(String property) {
		if (property == null || property.equals("")) {
			throw new RuntimeException("getProperty() error, input parameter (" + property + ") is "
					+ (property == null ? "null" : "empty"));
		} else {
			String val = ContextManager.properties.getProperty(property);
			if (val == null || val.equals("")) { // No value exists in the
													// properties file
				throw new RuntimeException("checkProperty() error, the required property (" + property + ") is "
						+ (property == null ? "null" : "empty"));
			}
			return val;
		}
	}

	/**
	 * Read the properties file and add properties. Will check if any properties have been included on the command line
	 * as well as in the properties file, in these cases the entries in the properties file are ignored in preference
	 * for those specified on the command line.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readProperties() throws FileNotFoundException, IOException {

		File propFile = new File("./repastcity.properties");
		if (!propFile.exists()) {
			throw new FileNotFoundException("Could not find properties file in the default location: "
					+ propFile.getAbsolutePath());
		}

		LOGGER.log(Level.FINE, "Initialising properties from file " + propFile.toString());

		ContextManager.properties = new Properties();

		FileInputStream in = new FileInputStream(propFile.getAbsolutePath());
		ContextManager.properties.load(in);
		in.close();

		// See if any properties are being overridden by command-line arguments
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String k = (String) e.nextElement();
			String newVal = System.getProperty(k);
			if (newVal != null) {
				// The system property has the same name as the one from the
				// properties file, replace the one in the properties file.
				LOGGER.log(Level.INFO, "Found a system property '" + k + "->" + newVal
						+ "' which matches a NeissModel property '" + k + "->" + properties.getProperty(k)
						+ "', replacing the non-system one.");
				properties.setProperty(k, newVal);
			}
		} // for
		return;
	} // readProperties

	/**
	 * Check that the environment looks ok
	 * 
	 * @throws NoIdentifierException
	 */
	private void testEnvironment() throws EnvironmentError, NoIdentifierException {

		LOGGER.log(Level.FINE, "Testing the environment");
		// Get copies of the contexts/projections from main context
		Context<Building> bc = (Context<Building>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.BUILDING_CONTEXT);
		Context<Road> rc = (Context<Road>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.ROAD_CONTEXT);
		Context<Junction> jc = (Context<Junction>) mainContext.getSubContext(GlobalVars.CONTEXT_NAMES.JUNCTION_CONTEXT);

		// Geography<Building> bg = (Geography<Building>)
		// bc.getProjection(GlobalVars.CONTEXT_NAMES.BUILDING_GEOGRAPHY);
		// Geography<Road> rg = (Geography<Road>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY);
		// Geography<Junction> jg = (Geography<Junction>)
		// rc.getProjection(GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY);
		Network<Junction> rn = (Network<Junction>) jc.getProjection(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK);

		// 1. Check that there are some objects in each of the contexts 濡拷閺屻儲鐦℃稉顏冪瑐娑撳鏋冩稉顓熸Ц閸氾附婀佹稉锟芥禍娑橆嚠鐠烇拷
		checkSize(bc, rc, jc);
		
//		for(Road road : rc.getObjects(Road.class)){
//			System.out.println(road);
//		}

		// 2. Check that the number of roads matches the number of edges 濡拷閺屻儵浜剧捄顖涙殶閺勵垰鎯佹稉搴ょ珶閺佹澘灏柊锟�
		if (sizeOfIterable(rc.getObjects(Road.class)) != sizeOfIterable(rn.getEdges())) {
			throw new EnvironmentError("There should be equal numbers of roads in the road "
					+ "context and edges in the road network. But there are "
					+ sizeOfIterable(rc.getObjects(Road.class)) + " and " + sizeOfIterable(rn.getEdges()));
		}

		// 3. Check that the number of junctions matches the number of nodes 濡拷閺屻儰姘﹀Ч鍥╁仯閺佺増妲搁崥锔跨瑢閼哄倻鍋ｉ弫鏉垮爱闁帮拷
		if (sizeOfIterable(jc.getObjects(Junction.class)) != sizeOfIterable(rn.getNodes())) {
			throw new EnvironmentError("There should be equal numbers of junctions in the junction "
					+ "context and nodes in the road network. But there are "
					+ sizeOfIterable(jc.getObjects(Junction.class)) + " and " + sizeOfIterable(rn.getNodes()));
		}

		LOGGER.log(Level.FINE, "The road network has " + sizeOfIterable(rn.getNodes()) + " nodes and "
				+ sizeOfIterable(rn.getEdges()) + " edges.");

		// 4. Check that Roads and Buildings have unique identifiers 濡拷閺屻儵浜剧捄顖氭嫲瀵よ櫣鐡氶悧鈺傛Ц閸氾箑鍙块張澶婃暜娑擄拷閺嶅洩鐦戠粭锟�
		HashMap<String, ?> idList = new HashMap<String, Object>();
		for (Building b : bc.getObjects(Building.class)) {
			if (idList.containsKey(b.getIdentifier()))
				throw new EnvironmentError("More than one building found with id " + b.getIdentifier());
			idList.put(b.getIdentifier(), null);
		}
		idList.clear();
		for (Road r : rc.getObjects(Road.class)) {
			if (idList.containsKey(r.getIdentifier()))
				throw new EnvironmentError("More than one building found with id " + r.getIdentifier());
			idList.put(r.getIdentifier(), null);
		}

	}

	public static int sizeOfIterable(Iterable i) {
		int size = 0;
		Iterator<Object> it = i.iterator();
		while (it.hasNext()) {
			size++;
			it.next();
		}
		return size;
	}

	/**
	 * Checks that the given <code>Context</code>s have more than zero objects in them
	 * 
	 * @param contexts
	 * @throws EnvironmentError
	 */
	public void checkSize(Context<?>... contexts) throws EnvironmentError {
		for (Context<?> c : contexts) {
			int numObjs = sizeOfIterable(c.getObjects(Object.class));
			if (numObjs == 0) {
				throw new EnvironmentError("There are no objects in the context: " + c.getId().toString());
			}
		}
	}

	public static void stopSim(Exception ex, Class<?> clazz) {
		ISchedule sched = RunEnvironment.getInstance().getCurrentSchedule();
		sched.setFinishing(true);
		sched.executeEndActions();
		LOGGER.log(Level.SEVERE, "ContextManager has been told to stop by " + clazz.getName(), ex);
	}

	/**
	 * Move an agent by a vector. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param distToTravel
	 *            The distance that they will travel
	 * @param angle
	 *            The angle at which to travel.
	 * @see Geography
	 */
	public static synchronized void moveAgentByVector(IAgent agent, double distToTravel, double angle) {
		ContextManager.agentGeography.moveByVector(agent, distToTravel, angle);
	}

	/**
	 * Move an agent. This method is required -- rather than giving agents direct access to the agentGeography --
	 * because when multiple threads are used they can interfere with each other and agents end up moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param point
	 *            The point to move the agent to
	 */
	public static synchronized void moveAgent(IAgent agent, Point point) {
		ContextManager.agentGeography.move(agent, point);
	}

	/**
	 * Add an agent to the agent context. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to add.
	 */
	public static synchronized void addAgentToContext(IAgent agent) {
		ContextManager.agentContext.add(agent);
	}

	/**
	 * Get all the agents in the agent context. This method is required -- rather than giving agents direct access to
	 * the agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @return An iterable over all agents, chosen in a random order. See the <code>getRandomObjects</code> function in
	 *         <code>DefaultContext</code>
	 * @see DefaultContext
	 */
	public static synchronized Iterable<IAgent> getAllAgents() {
		return ContextManager.agentContext.getRandomObjects(IAgent.class, ContextManager.agentContext.size());
	}
//濞ｈ濮�
	public static synchronized Iterable<IAgent> getAllMyAgents() {
		return ContextManager.agentContext.getRandomObjects(MyAgent.class, ContextManager.agentContext.size());
	}
	public static synchronized Iterable<IAgent> getAllInfectedAgent(){
		return ContextManager.agentContext.getRandomObjects(InfectedAgent.class, ContextManager.agentContext.size());
	}
	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(IAgent agent) {
		return ContextManager.agentGeography.getGeometry(agent);
	}

	/**
	 * Get a pointer to the agent context.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be used with care. The functions
	 * <code>getAllAgents()</code> and <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Context<IAgent> getAgentContext() {
		return ContextManager.agentContext;
	}

	/**
	 * Get a pointer to the agent geography.
	 * 
	 * <p>
	 * Warning: accessing the context directly is not thread safe so this should be used with care. The functions
	 * <code>getAllAgents()</code> and <code>getAgentGeometry()</code> can be used to query the agent context or
	 * projection.
	 * </p>
	 */
	public static Geography<IAgent> getAgentGeography() {
		return ContextManager.agentGeography;
	}

}
