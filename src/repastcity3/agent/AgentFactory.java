/*锟紺opyright 2012 Nick Malleson
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
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.agent;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.environment.Building;
import repastcity3.environment.GISFunctions;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.exceptions.AgentCreationException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

/**
 * Create agents. There are three methods that can be used to create agents: randomly create a number of agents, create
 * agents from a point shapefile or create a certain number of agents per neighbourhood specified in an area shapefile.
 * 
 * <P>
 * The method to use is specified by the 'agent_definition' parameter in <code>parameters.xml</code>. The parameter
 * takes the following form:
 * </P>
 * 
 * <pre>
 * {@code
 * <method>:<definition>
 * }
 * </pre>
 * 
 * <P>
 * where method and can be one of the following:
 * </P>
 * 
 * <ul>
 * <li>
 * 
 * <pre>
 * {@code random:<num_agents>}
 * </pre>
 * 
 * Create 'num_agents' agents in randomly chosen houses. The agents are of type <code>DefaultAgent</code>. For example,
 * this will create 10 agents in randomly chosen houses: '<code>random:1</code>'. See the
 * <code>createRandomAgents</code> function for implementation details.</li>
 * 
 * <li>
 * 
 * <pre>
 * {@code point:<filename>%<agent_class>}
 * </pre>
 * 
 * Create agents from the given point shapefile (one agent per point). If a point in the agent shapefile is within a
 * building object then the agent's home will be set to that building. The type of the agent can be given in two ways:
 * <ol>
 * <li>The 'agent_class' parameter can be used - this is the fully qualified (e.g. including package) name of a class
 * that will be used to create all the agents. For example the following will create instances of <code>MyAgent</code>
 * at each point in the shapefile '<code>point:data/my_shapefile.shp$my_package.agents.MyAgent</code>'.</li>
 * <li>A String column in the input shapefile called 'agent_type' provides the class of the agents. IIn this manner
 * agents of different types can be created from the same input. For example, the following will read the shapefile and
 * look at the values in the 'agent_type' column to create agents: '<code>point:data/my_shapefile.shp</code>' (note that
 * unlike the previous method there is no '$').</li>
 * </ol>
 * 
 * See the <code>createPointAgents</code> function for implementation details.
 * 
 * <li>
 * 
 * <pre>
 * {@code area:<filename>$BglrC1%<agent_class1>$ .. $BglrC5%<agent_class5>}
 * </pre>
 * 
 * Create agents from the given areas shapefile. Up to five different types of agents can be created. Columns in the
 * shapefile specify how many agents of each type to create per area and the agents created are randomly assigned to
 * houses withing their area. The columns names must follow the format 'BglrCX' where 1 <= X <= 5. For example the
 * following string:<br>
 * 
 * <pre>
 * {@code area:area.shp$BglrC1%BurglarAgent$BglrC2%EmployedAgent}
 * </pre>
 * 
 * will read the <code>area.shp</code> and, for each area, create a number of <code>BurglarAgent</code> and
 * <code>EmployedAgent</code> agents in each area, the number being specied by columns called <code>BglrC1</code> and
 * <code>BglrC2</code> respectively. See the <code>createAreaAgents</code> function for implementation details.</li>
 * </ul>
 * 
 * @author Nick Malleson
 * @see DefaultAgent
 */
public class AgentFactory {

	private static Logger LOGGER = Logger.getLogger(AgentFactory.class.getName());

	/** The method to use when creating agents (determined in constructor). */
	private AGENT_FACTORY_METHODS methodToUse;

	/** The definition of the agents - specific to the method being used */
	private String definition;
	private String Masksratiodefinition;
	private String Vaccineratiodefinition;
//	private String Ageratiodefinition;
	private Integer YoungNumbers;
	private Integer OldNumber;
	public String getdefinition() {
		return this.definition;
	}
	public String getMasksratiodefinition() {
		return this.Masksratiodefinition;
	}
	public String getVaccineratiodefinition() {
		return this.Vaccineratiodefinition;
	}
	public Integer getYoungNumbers(){
		return this.YoungNumbers;
	}
	public Integer getOldNumbers(){
		return this.OldNumber;
		
		
	}
	/**
	 * Create a new agent factory from the given definition.
	 * 
	 * @param agentDefinition
	 */
	public AgentFactory(String agentDefinition) throws AgentCreationException {

		System.out.println(agentDefinition);

		// First try to parse the definition 棣栧厛灏濊瘯瑙ｆ瀽瀹氫箟
//		String[] split = agentDefinition.split(":");
//		if (split.length != 2) {
//			throw new AgentCreationException("Problem parsin the definition string '" + agentDefinition
//					+ "': it split into " + split.length + " parts but should split into 2.");
//		}
		String[] spilt = agentDefinition.split(",");
		String spilt1 = spilt[0];//AgentNumber
		String spilt2 = spilt[1];//AgentMasksratio
		String spilt3 = spilt[2];//AgentVaccineratio
		String spilt4 = spilt[3];
		String spilt5=spilt[4];
	 	String[] AgentNumber = spilt1.split(":");
	 	String[] AgentMasksratio = spilt2.split(":");
	 	String[] AgentVaccineratio = spilt3.split(":");
		String[] YoungNumber=spilt4.split(":");//年轻人个数
		int YoungNumbers=Integer.parseInt(YoungNumber[1]);
		String[] OldNumber=spilt5.split(":");//老年人个数
		int OldNumbers=Integer.parseInt(OldNumber[1]);
		String method = AgentNumber[0]; // The method to create agents
		String defn = AgentNumber[1]; // Information about the agents themselves

		
		
		
		
		//鐢熸垚鏅鸿兘浣撶殑鍥涚鏂规硶锛屽鏋滈兘涓嶆弧瓒冲氨鎶涘嚭寮傚父
		if (method.equals(AGENT_FACTORY_METHODS.RANDOM.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.RANDOM;
		} else if (method.equals(AGENT_FACTORY_METHODS.BUILDINGPOINT.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.BUILDINGPOINT;
		}else if (method.equals(AGENT_FACTORY_METHODS.POINT_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.POINT_FILE;
		}else if (method.equals(AGENT_FACTORY_METHODS.AREA_FILE.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.AREA_FILE;
		}else if (method.equals(AGENT_FACTORY_METHODS.INFECTEDBUILDINGPOINT.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.INFECTEDBUILDINGPOINT;
		}else if(method.equals(AGENT_FACTORY_METHODS.INFECTEDAGENT.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.INFECTEDAGENT;
		}else if(method.equals(AGENT_FACTORY_METHODS.AGENT.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.AGENT;
		}else if(method.equals(AGENT_FACTORY_METHODS.Asymptomatic.toString())) {
			this.methodToUse = AGENT_FACTORY_METHODS.Asymptomatic;
		}else {
			throw new AgentCreationException("Unrecognised method of creating agents: '" + method
					+ "'. Method must be one of " + AGENT_FACTORY_METHODS.RANDOM.toString() + ", "
					+ AGENT_FACTORY_METHODS.POINT_FILE.toString() + " or " + AGENT_FACTORY_METHODS.AREA_FILE.toString());
		}
		
		//娴嬭瘯    杈撳嚭鐨勭粨鏋�====> AGENT_FACTORY_METHODS.AREA_FILE: buildingpoint
//		LOGGER.log(Level.INFO, "杈撳嚭AGENT_FACTORY_METHODS.AREA_FILE: "+ AGENT_FACTORY_METHODS.BUILDINGPOINT);
//		LOGGER.log(Level.INFO, "this: "+ this);
//		LOGGER.log(Level.INFO, "this.methodToUse: "+ this.methodToUse);
		
		this.definition = defn; // Method is OK, save the definition for creating agents later.
		this.Masksratiodefinition = AgentMasksratio[1];
		this.Vaccineratiodefinition = AgentVaccineratio[1];
//		this.Ageratiodefinition= 0.56;
		this.OldNumber=OldNumbers;
		this.YoungNumbers=YoungNumbers;
		// Check the rest of the definition is also correct (passing false means don't create agents)
		// An exception will be thrown if it doesn't work.
		//璋冪敤鐢熸垚鏅鸿兘浣撶殑鍑芥暟浣嗘槸鍗翠笉鐢熸垚鏅鸿兘浣擄紝鐩殑鏄负浜嗘娴媎efinition鐨勬纭�э紝涓嶆纭氨浼氭姏鍑轰竴涓紓甯�
		this.methodToUse.createAgMeth().createagents(false, this);
	}

	public void createAgents(Context<? extends IAgent> context) throws AgentCreationException {
		this.methodToUse.createAgMeth().createagents(true, this);
		
		

		
		
		
	}

	/**
	 * Create a number of in randomly chosen houses. If there are more agents than houses then some houses will have
	 * more than one agent in them.
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then just check that the definition can be
	 *            parsed.
	 * @throws AgentCreationException
	 */
	private void createRandomAgents(boolean dummy) throws AgentCreationException {
		// Check the definition is as expected, in this case it should be a number
		int numAgents = -1;
		
		//瑙ｆ瀽definition鏄惁姝ｇ‘
		try {
			//numAgents鏄敱definition杞寲鑰屾潵鐨刬nt绫诲瀷
			numAgents = Integer.parseInt(this.definition);
		} catch (NumberFormatException ex) {
			throw new AgentCreationException("Using " + this.methodToUse + " method to create "
					+ "agents but cannot convert " + this.definition + " into an integer.");
		}
		// The definition has been parsed OK, no can either stop or create the agents
		if (dummy) {
			return;
		}

		// Create agents in randomly chosen houses. Use two while loops in case there are more agents
		// than houses, so that houses have to be looped over twice.
		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
		int agentsCreated = 0;
//		ContextManager.agentCount = numAgents;
		while (agentsCreated < numAgents) {
			Iterator<Building> i = ContextManager.buildingContext.getRandomObjects(Building.class, numAgents)
					.iterator();
//			@SuppressWarnings("unused")
//			Building build = ContextManager.buildingContext.getObjects(Building.class).get(111);
			//TODO 鍒濆鍖栵紝闅忔満浜х敓agent锛岀劧鍚庣Щ鍔ㄥ埌浠栦滑鐨凥ome銆傚湪step鍑芥暟涓紝濡傛灉鍒板浜嗭紝閭ｄ箞灏遍噸鏂拌瀹歨ome
//			while (i.hasNext() && agentsCreated < numAgents) {
//				//Building b = i.next(); // Find a building
//				Building b = ContextManager.buildingContext.getObjects(Building.class).get(276);
//				IAgent a = new DefaultAgent(); // Create a new agent
//				a.setHome(b); // Tell the agent where it lives
//				b.addAgent(a); // Tell the building that the agent lives there
//				ContextManager.addAgentToContext(a); // Add the agent to the context
//				// Finally move the agent to the place where it lives.
//				ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
//				agentsCreated++;
//			}
			while (i.hasNext() && agentsCreated < numAgents) {
				Building b = i.next(); // Find a building
//				IAgent a = new DefaultAgent(); // Create a new agent
				IAgent a = new MyAgent();
				a.setHome(b); // Tell the agent where it lives
				b.addAgent(a); // Tell the building that the agent lives there
				ContextManager.addAgentToContext(a); // Add the agent to the context
				// Finally move the agent to the place where it lives.
				ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
				agentsCreated++;
			}
		}
	}
	
	private void createBuildingPointAgents(boolean dummy) throws AgentCreationException {
		Random r1 = new Random();
		// Check the definition is as expected, in this case it should be a number
//		int numAgents = ContextManager.agentCount;
		// The definition has been parsed OK, no can either stop or create the agents
		//156琛� 璁剧疆鐨刦alse Mr.wang
		if (dummy) {
			return;
		}

		// Create agents in randomly chosen houses. Use two while loops in case there are more agents
		// than houses, so that houses have to be looped over twice.
//		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
//		ContextManager.agentCount = numAgents;
		IndexedIterable<Building>  biter = ContextManager.buildingContext.getObjects(Building.class);
//		for(int i=0;i<ContextManager.buildingInfo.size();i++){
//			BuildingInfo info = ContextManager.buildingInfo.get(i);
//			Building b = biter.get(info.getId());
//			if(b == null){
//				throw new AgentCreationException("id涓� "+info.getId()+" 鐨凚uilding涓嶅瓨鍦�!!!!");
//			}	
		int numberofagent = Integer.parseInt(this.getdefinition());
		int agentNum = numberofagent;
		int MasksNumber = Math.round(numberofagent*Integer.parseInt(this.Masksratiodefinition));
		int VaccineNumber = Math.round(numberofagent*Integer.parseInt(this.Vaccineratiodefinition));
//		int AgeNumber = Math.round(numberofagent*Integer.parseInt(this.Vaccineratiodefinition));
		int YoungNumbers=this.getYoungNumbers();
		int OldNumbers=this.getOldNumbers();
		//ceshi   Mr.wang
//		 int[] arrceshi = {73,91,109,371,512,514,550,708,59,81,82,
//				 74,141,147,128,139,126,138,249,468,446,479,368,358,
//				 365,507,499,557,558,549,691,709,688,704,683,690,723,696};
		List<Integer> birthAgentList=ContextManager.getBirthAgentList();
		//合并两数组
		
		for(int i=0;i<numberofagent;i++) {
			
//			for(int j=0;j<info.getGenNum();j++){
				//锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛�
				//鍒涘缓agent鐨勪綅缃�
//				IAgent a = new DefaultAgent(); // Create a new agent
				
			IAgent a = new MyAgent();
			int i2 = r1.nextInt(2);
			int i3 = r1.nextInt(2);
			int i4 = r1.nextInt(2);
			int infect=0;
			if(agentNum!=MasksNumber ) {
				if(i2==1 && MasksNumber>0) {
					a.setMasks(true);
					infect = infect-10;
					MasksNumber--;
				}else {
					a.setMasks(false);
				}
			}else if(agentNum==MasksNumber&&agentNum!=0) {
				a.setMasks(true);
				infect = infect-10;
				MasksNumber--;
			}else {
				a.setMasks(false);
			}
			
			if(agentNum!=VaccineNumber) {
				if(i3==1 && VaccineNumber>0) {
					a.setVaccine(true);
					infect = infect-20;
					VaccineNumber--;
				}else {
					a.setVaccine(false);
				}
			}else if(agentNum==VaccineNumber&&agentNum!=0){
				a.setVaccine(true);
				infect = infect-20;
				VaccineNumber--;
			}else {
				a.setVaccine(false);
			}
			
//			if(agentNum!=AgeNumber) {
//				if(i4==1 && AgeNumber>0) {
//					a.setAge("young");
//					infect = infect-30;
//					AgeNumber--;
//				}else {
//					a.setAge("old");
//				}
//			}else if(agentNum==AgeNumber&&agentNum!=0) {
//				a.setAge("young");
//				infect = infect-30;
//				AgeNumber--;
//			}else {
//				a.setAge("old");
//			}
			if(YoungNumbers>0)
			{
				a.setAge("young");
				infect=infect-30;
				YoungNumbers--;
			}
			else if(OldNumbers>0)
			{
				a.setAge("old");
				OldNumbers--;
			}
			a.setInfectRatio(infect);
//			int i1 = r1.nextInt(20);
			int i1=r1.nextInt(birthAgentList.size()-1);
			Building b = biter.get(birthAgentList.get(i1));
//			Building b = biter.get(i1);
//				int energy = RandomHelper.nextIntFromTo(4, 10);
//				IAgent a = new MyAgent(ContextManager.space,ContextManager.grid,energy);
				
			a.setHome(b); // Tell the agent where it lives
			b.addAgent(a); // Tell the building that the agent lives there
			ContextManager.addAgentToContext(a); // Add the agent to the context
			// Finally move the agent to the place where it lives.
			ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
			agentNum--;
			}
//		}
	}
	
	private void createBuildingPointInfectedAgents(boolean dummy) throws AgentCreationException {
		Random r2 = new Random();
//		System.out.println("杩涘叆鍒涘缓鏂规硶");
		// Check the definition is as expected, in this case it should be a number
//		int numAgents = ContextManager.infectedagentCount;
		// The definition has been parsed OK, no can either stop or create the agents
		//156琛� 璁剧疆鐨刦alse Mr.wang
		if (dummy) {
			return;
		}

		// Create agents in randomly chosen houses. Use two while loops in case there are more agents
		// than houses, so that houses have to be looped over twice.
//		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
//		ContextManager.agentCount = numAgents;
		IndexedIterable<Building>  biter = ContextManager.buildingContext.getObjects(Building.class);
//		for(int i=0;i<ContextManager.InfectedbuildingInfo.size();i++){
//			BuildingInfo info = ContextManager.InfectedbuildingInfo.get(i);
//			Building b = biter.get(info.getId());
//			if(b == null){
//				throw new AgentCreationException("id涓� "+info.getId()+" 鐨凚uilding涓嶅瓨鍦�!!!!");
//			}			
//			for(int j=0;j<info.getGenNum();j++){
				//锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛�
				//鍒涘缓agent鐨勪綅缃�
//				IAgent a = new DefaultAgent(); // Create a new agent
		int numberofagent = Integer.parseInt(this.getdefinition());
		int agentNum = numberofagent;
		int MasksNumber = Math.round(numberofagent*Integer.parseInt(this.Masksratiodefinition)/100);
		int VaccineNumber = Math.round(numberofagent*Integer.parseInt(this.Vaccineratiodefinition)/100);
//		int AgeNumber = Math.round(numberofagent*Integer.parseInt(this.Ageratiodefinition)/100);
		int OldNumbers=this.getOldNumbers();
		int YoungNumbers=this.getYoungNumbers();
		LOGGER.log(Level.INFO, "MasksNumber "+MasksNumber );
		LOGGER.log(Level.INFO, "VaccineNumber "+VaccineNumber );
//		LOGGER.log(Level.INFO, "AgeNumber "+AgeNumber );
//		int[] arrceshi = {73,119,109,371,512,514,550,708,59,81,82,
//				 74,141,147,128,139,126,138,249,468,446,479,368,358,
//				 365,507,499,557,558,549,691,709,688,704,683,690,723,696};
		List<Integer> birthAgentList=ContextManager.getBirthAgentList();
		for(int i=0;i<numberofagent;i++) {
			
				IAgent a = new InfectedAgent();
				
				int i2 = r2.nextInt(2);
				int i3 = r2.nextInt(2);
				int i4 = r2.nextInt(2);
				int i5 = r2.nextInt(71);//
				if(agentNum!=MasksNumber ) {
					if(i2==1 && MasksNumber>0) {
						a.setMasks(true);
						MasksNumber--;
					}else {
						a.setMasks(false);
					}
				}else if(agentNum==MasksNumber&&agentNum!=0) {
					a.setMasks(true);
					MasksNumber--;
				}else {
					a.setMasks(false);
				}
				
				if(agentNum!=VaccineNumber) {
					if(i3==1 && VaccineNumber>0) {
						a.setVaccine(true);
						VaccineNumber--;
					}else {
						a.setVaccine(false);
					}
				}else if(agentNum==VaccineNumber&&agentNum!=0){
					a.setVaccine(true);
					VaccineNumber--;
				}else {
					a.setVaccine(false);
				}
				if(YoungNumbers>0)
				{
					a.setAge("young");
					YoungNumbers--;
				}else if(OldNumbers>0)
				{
					a.setAge("old");
					OldNumbers--;
				}
					
//				if(agentNum>AgeNumber) {
//					if(i4==1 && AgeNumber>0) {
//						a.setAge("young");
//						AgeNumber--;
//					}else {
//						a.setAge("old");
//					}
//				}else if(agentNum==AgeNumber&&agentNum!=0) {
//					a.setAge("young");
//					AgeNumber--;
//				}else {
//					a.setAge("old");
//				}
				

				a.setStage("latent period");
				ContextManager.infectedagentLatentCount++;
				a.setSwitchStageDay(i5);
//				int i1 = r2.nextInt(20);
				int i1=r2.nextInt(birthAgentList.size()-1);
				Building b = biter.get(birthAgentList.get(i1));
//				Building b = biter.get(i1);
//				int energy = RandomHelper.nextIntFromTo(4, 10);
//				IAgent a = new MyAgent(ContextManager.space,ContextManager.grid,energy);
				
				a.setHome(b); // Tell the agent where it lives
				b.addAgent(a); // Tell the building that the agent lives there
				ContextManager.addAgentToContext(a); // Add the agent to the context
				// Finally move the agent to the place where it lives.
				ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
				agentNum--;
			}
//		}
	}
	private void createBuildingPointAsymptomaticAgents(boolean dummy) throws AgentCreationException {
		Random r2 = new Random();
//		System.out.println("杩涘叆鍒涘缓鏂规硶");
		// Check the definition is as expected, in this case it should be a number
//		int numAgents = ContextManager.infectedagentCount;
		// The definition has been parsed OK, no can either stop or create the agents
		//156琛� 璁剧疆鐨刦alse Mr.wang
		if (dummy) {
			return;
		}

		// Create agents in randomly chosen houses. Use two while loops in case there are more agents
		// than houses, so that houses have to be looped over twice.
//		LOGGER.info("Creating " + numAgents + " agents using " + this.methodToUse + " method.");
//		ContextManager.agentCount = numAgents;
		IndexedIterable<Building>  biter = ContextManager.buildingContext.getObjects(Building.class);
//		for(int i=0;i<ContextManager.InfectedbuildingInfo.size();i++){
//			BuildingInfo info = ContextManager.InfectedbuildingInfo.get(i);
//			Building b = biter.get(info.getId());
//			if(b == null){
//				throw new AgentCreationException("id涓� "+info.getId()+" 鐨凚uilding涓嶅瓨鍦�!!!!");
//			}			
//			for(int j=0;j<info.getGenNum();j++){
				//锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛侊紒锛�
				//鍒涘缓agent鐨勪綅缃�
//				IAgent a = new DefaultAgent(); // Create a new agent
		int numberofagent = Integer.parseInt(this.getdefinition());
		for(int i=0;i<numberofagent;i++) {
			
				IAgent a = new AsymptomaticAgent();
//				int i1 = r2.nextInt(20);
				int i1 = r2.nextInt(300);
				Building b = biter.get(i1);
//				int energy = RandomHelper.nextIntFromTo(4, 10);
//				IAgent a = new MyAgent(ContextManager.space,ContextManager.grid,energy);
				
				a.setHome(b); // Tell the agent where it lives
				b.addAgent(a); // Tell the building that the agent lives there
				ContextManager.addAgentToContext(a); // Add the agent to the context
				// Finally move the agent to the place where it lives.
				ContextManager.moveAgent(a, ContextManager.buildingProjection.getGeometry(b).getCentroid());
			
			}
//		}
	}

	/**
	 * Read a shapefile and create an agent at each location. If there is a column called
	 * 
	 * @param dummy
	 *            Whether or not to actually create agents. If this is false then just check that the definition can be
	 *            parsed.
	 * @throws AgentCreationException
	 */
	@SuppressWarnings("unchecked")
	private void createPointAgents(boolean dummy) throws AgentCreationException {

		// See if there is a single type of agent to create or should read a colum in shapefile
		boolean singleType = this.definition.contains("$");

		//鐢ㄦ潵鍒ゆ柇璋冪敤閭ｄ釜鏂囦欢鐨勭被鏉ュ垱寤烘櫤鑳戒綋
		String fileName;
		String className;
		Class<IAgent> clazz;
		if (singleType) {
			// Agent class provided, can use the Simphony Shapefile loader to load agents of the given class
			// Work out the file and class names from the agent definition
			String[] split = this.definition.split("\\$");
			if (split.length != 2) {
				throw new AgentCreationException("There is a problem with the agent definition, I should be "
						+ "able to split the definition into two parts on '$', but only split it into " + split.length
						+ ". The definition is: '" + this.definition + "'");
			}
			 // (Need to append root data directory to the filename).
			fileName = ContextManager.getProperty(GlobalVars.GISDataDirectory)+split[0];
			className = split[1];
			// Try to create a class from the given name.
			try {
				clazz = (Class<IAgent>) Class.forName(className);
				GISFunctions.readAgentShapefile(clazz, fileName, ContextManager.getAgentGeography(), ContextManager
						.getAgentContext());
			} catch (Exception e) {
				throw new AgentCreationException(e);
			}
		} else {
			// TODO Implement agent creation from shapefile value;
			throw new AgentCreationException("Have not implemented the method of reading agent classes from a "
					+ "shapefile yet.");
		}

		// Assign agents to houses
		int numAgents = 0;
		for (IAgent a : ContextManager.getAllAgents()) {
			numAgents++;
			Geometry g = ContextManager.getAgentGeometry(a);
			for (Building b : SpatialIndexManager.search(ContextManager.buildingProjection, g)) {
				if (ContextManager.buildingProjection.getGeometry(b).contains(g)) {
					b.addAgent(a);
					a.setHome(b);
				}
			}
		}
//		ContextManager.agentCount = numAgents;

		if (singleType) {
			LOGGER.info("Have created " + numAgents + " of type " + clazz.getName().toString() + " from file "
					+ fileName);
		} else {
			// (NOTE: at the moment this will never happen because not implemented yet.)
			LOGGER.info("Have created " + numAgents + " of different types from file " + fileName);
		}

	}

	private void createAreaAgents(boolean dummy) throws AgentCreationException {
		throw new AgentCreationException("Have not implemented the createAreaAgents method yet.");
	}

	/**
	 * The methods that can be used to create agents. The CreateAgentMethod stuff is just a long-winded way of
	 * hard-coding the specific method to use for creating agents into the enum (much simpler in python).
	 * 
	 * @author Nick Malleson
	 */
	private enum AGENT_FACTORY_METHODS {                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
		/** Default: create a number of agents randomly assigned to buildings */ //鍒涘缓闅忔満鍒嗛厤缁欏缓绛戠墿鐨勫涓唬鐞�
		RANDOM("random", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createRandomAgents(b);
			}
		}),
		BUILDINGPOINT("buildingpoint", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createBuildingPointAgents(b);
			}
		}),
		INFECTEDBUILDINGPOINT("buildingpointInfected", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createBuildingPointInfectedAgents(b);
			}
		}),
		AGENT("Agent", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createBuildingPointAgents(b);
			}
		}),
		INFECTEDAGENT("InfectedAgent", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createBuildingPointInfectedAgents(b);
			}
		}),
		Asymptomatic("Asymptomatic", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createBuildingPointAsymptomaticAgents(b);
			}
		}),
		/** Specify an agent shapefile, one agent will be created per point */
		POINT_FILE("point", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createPointAgents(b);
			}
		}),
		/**
		 * Specify the number of agents per area as a shaefile. Agents will be randomly assigned to houses within the
		 * area.
		 */
		AREA_FILE("area", new CreateAgentMethod() {
			@Override
			public void createagents(boolean b, AgentFactory af) throws AgentCreationException {
				af.createAreaAgents(b);
			}
		});

		String stringVal;
		CreateAgentMethod meth;

		/**
		 * @param val
		 *            The string representation of the enum which must match the method given in the 'agent_definition'
		 *            parameter in parameters.xml.
		 * @param f
		 */
		AGENT_FACTORY_METHODS(String val, CreateAgentMethod f) {
			this.stringVal = val;
			this.meth = f;
		}

		public String toString() {
			return this.stringVal;
		}

		public CreateAgentMethod createAgMeth() {
			return this.meth;
		}

		interface CreateAgentMethod {
			void createagents(boolean dummy, AgentFactory af) throws AgentCreationException;
		}
	}

}
