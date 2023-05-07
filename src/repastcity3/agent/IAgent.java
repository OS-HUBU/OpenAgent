/*
锟紺opyright 2012 Nick Malleson
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

import java.util.List;

import repastcity3.environment.Building;

/**
 * All agents must implement this interface so that it the simulation knows how
 * to step them.
 * 
 * @author Nick Malleson
 * 
 */
public interface IAgent {	
	/**
	 * Controls the agent. This method will be called by the scheduler once per
	 * iteration.
	 */
	 void step() throws Exception;

	/**
	 * Used by Agents as a means of stating whether or not they can be
	 * run in parallel (i.e. there is no inter-agent communication which will
	 * make parallelisation non-trivial). If all the agents in a simulation
	 * return true, and the computer running the simulation has
	 * more than one core, it is possible to step agents simultaneously.
	 * 琚唬鐞嗙敤浣滃０鏄庡畠浠槸鍚﹀彲浠ュ苟琛岃繍琛岀殑涓�绉嶆柟寮忥紙鍗虫病鏈変唬鐞嗛棿閫氫俊灏�
	 * 浣垮苟琛屽寲鍙樺緱涓嶅钩鍑★級銆傚鏋滄ā鎷熶腑鐨勬墍鏈変唬鐞嗛兘杩斿洖 true锛屽苟涓旇繍琛屾ā鎷熺殑璁＄畻鏈哄叿鏈夊涓唴鏍革紝
	 * 鍒欏彲浠ュ悓鏃跺浠ｇ悊杩涜鍗曟銆�
	 * 
	 * @author Nick Malleson
	 */
	 //
	boolean isThreadable();
	
	/**
	 * Set where the agent lives.
	 */
	void setHome(Building home);
	
	/**
	 * Get the agent's home.
	 */
	Building getHome();
	
	/**
	 * (Optional). Add objects to the agents memory. Used to keep a record of all the
	 * buildings that they have passed.
	 * 灏嗗璞℃坊鍔犲埌浠ｇ悊鍐呭瓨銆傜敤浜庤褰曞畠浠粡杩囩殑鎵�鏈夊缓绛戠墿銆�
	 * @param <T>
	 * @param objects The objects to add to the memory.
	 * @param clazz The type of object.
	 */
	<T> void addToMemory(List<T> objects, Class<T> clazz);
	
	/**
	 * (Optional). Get the transport options available to this agent. E.g.
	 * an agent with a car who also could use public transport would return
	 * <code>{"bus", "car"}</code>. If null then it is assumed that the agent
	 * walks (the slowest of all transport methods). 
	 */
	List<String> getTransportAvailable();
	
	/**
	 * 璁剧疆agent鏄惁闇�瑕侀噸鏂板畾浣嶇洰鐨勫湴
	 * @param isReset
	 */
	public void setReset(boolean isReset);
	
	/**
	 * 璁剧疆鍓╀笅鐨刟gent鏄惁闇�瑕乺andom娓歌蛋
	 * @param random
	 */
	public void setRandom(boolean random);

	public int getID();
	public String getAgentType();
	public Double getCustomSpeed();
	public boolean getMasks();
	public void setMasks(boolean var1);
	
    public boolean getVaccine();
    public void setVaccine(boolean var1);
    public boolean getRehabilitated();
    public void setRehabilitated(boolean Rehabilitated);
    public String getAge();
    public void setAge(String Age);
    
 
	public void setStage(String stage);
	public String getStage();

	public void setSwitchStageDay(int day);
	public int getSwitchStageDay() ;
	
	public int getInfectRatio();
	
	public void setInfectRatio(int ratio);
	public double getTickCount();
	public void setTickCount(double count);
	
}
    
