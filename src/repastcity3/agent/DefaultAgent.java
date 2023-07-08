/*
�Copyright 2012 Nick Malleson
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

package repastcity3.agent;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	private Building home; // Where the agent lives
	/** 是否已经出发了(为了区分第一次) */
	private boolean hasSetoff = false;
	private Route route; // An object to move the agent around the world
	
	private boolean goingHome = false; // Whether the agent is going to or from their home
	
	private static int uniqueID = 0;	//agent的ID
	private int id;									
	public Route getRoute() {
		return this.route;
	}
	/**
	 * 总人数超过所有shelter的容纳数并且满了的时候，剩下的人需要random
	 */
	private boolean random;
	
//	//自定义策略
//	private boolean infection = false;
	
	public DefaultAgent() {	
		this.id = uniqueID++;
	}
	public DefaultAgent(boolean random) {
		this();
		this.random = random;
	}
	/**
	 * 找到最近的shelter
	 * @param home 若未初始化，则Home不是空；否则home是空
	 * @return
	 */
//	private Building judgeNearestBuilding(Building home){
//		Coordinate currentCoord = null;	//当前坐标置为空
//		if(home == null)
//			currentCoord = this.route.getCurrentCoord();//当前agent坐标设置为当前位置
//		else
//			currentCoord = home.getCoords();//把agent的位置设置在家中
//		String buildingId = null;
//		double distance = -1;
////		synchronized(ContextManager.shelter){ 20140312
//		//遍历图中的所有shelter，找到最近的，存在buildingID和distance中************************************
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
//			return null;*///去掉注释/× ×/ 删除下一行的return    所有的agent都前往 shelter 48   ×××××××××××××××××××××××××××××××××××××××！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
//		return ContextManager.shelter.get("377");
//	}
	
	private static Object shelterLock = new Object();
	
	/**
	 * 规定智能体到达的目的地
	 * @param home 若未初始化，则Home不是空；否则home是空;agent 为this
	 * @return
	 */
	public Building judgeDestination(Building home,DefaultAgent agent){
		int buildingIDnum=0;
		int num=0,firstDim=0,lastPart=0;
		for(;num<agent.getID();){
			num++;
			if(num-lastPart>(Route.initInfo[firstDim][3]-1)){
				lastPart=num;
				firstDim++;
			}
		}
		buildingIDnum=Route.initInfo[firstDim][1]+1;
		Building building = ContextManager.shelter.get(Integer.toString(buildingIDnum));
		return building;
//		Building building = ContextManager.shelter.get(Integer.toString(26));
//		return building;
	}
	
	private boolean atDestination = false;
	@Override
	public void step() throws Exception {
		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		
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
			if(this.atDestination){//是否到达目的地
				//测试  一直输出同一个id  mr.wang
//				LOGGER.log(Level.INFO, "uniqueID " + this.uniqueID);
				return;}
			//重新定义目的地
			if(reset && this.route != null){
				this.reset = false;
				Building building = judgeDestination(null,this);
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
//					LOGGER.log(Level.FINE, this.toString() + " created new route to " + building.toString());
					//测试  Mr.wang
					LOGGER.log(Level.INFO, this.toString() + " created new route to " + building.toString());
				}
			}
			else if (!hasSetoff && this.route == null) {
				this.goingHome = false; // Must be leaving home
				this.hasSetoff = true;
				// 找最近的shelter
				Building building = judgeDestination(this.home,this);
				if(building != null){
					this.route = new Route(this, building.getCoords(), building);
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
				this.route.travel(this);									//入口： 此函数用来行进智能体，使智能体到达目的地×××××××××××××××××××××××××××××××××××××××！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
				LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else {
				//1. 该destination又进来一个人
				Building dst = this.route.getDestinationBuilding();
				dst.addOccuption(1);
				if(dst.getOccupation() >= dst.getLimit()){
					synchronized(shelterLock){
						ContextManager.shelter.remove(dst.getIdentifier());
					}
	//				synchronized(ContextManager.noticedAgent){
	//					ContextManager.noticedAgent = ContextManager.agentContext.size();
	//				}
					//通知所有Agent该Shelter满了
//					Iterator<IAgent> i = ContextManager.getAllAgents().iterator();
//					while(i.hasNext()){
//						IAgent next = i.next();
//						next.setReset(true);
//					}
				}
				//2. 结束
				this.route = null;
				this.atDestination = true;


				
				synchronized(ContextManager.agentlock){
					ContextManager.agentCount--;
					this.infection =true;
					LOGGER.log(Level.INFO, "agent " + this.uniqueID + this.infection + ContextManager.agentCount  );
//					LOGGER.log(Level.INFO, "infection " + this.infection);
					//输出agentCount的数量  测试 Mr.wang
//					LOGGER.log(Level.INFO, "ContextManager.agentCount " + ContextManager.agentCount);
				}
				//从agentContext中删除该点 20140312
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
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
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

	//注意: 在IAgent接口中有定义
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
