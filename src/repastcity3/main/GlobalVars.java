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

package repastcity3.main;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;

import repastcity3.agent.IAgent;
import repastcity3.environment.Route;

/**
 * 
 * @author nick
 *
 */
public abstract class GlobalVars {
	
	private static Logger LOGGER = Logger.getLogger(GlobalVars.class.getName());
	
	/* These are strings that match entries in the repastcity.properties file.*/
	public static final String GISDataDirectory = "GISDataDirectory";
	public static final String BuildingShapefile = "BuildingShapefile";
	public static final String RoadShapefile = "RoadShapefile";
	public static final String BuildingsRoadsCoordsCache = "BuildingsRoadsCoordsCache";
	public static final String BuildingsRoadsCache = "BuildingsRoadsCache";

	
	public static final class GEOGRAPHY_PARAMS {
		
		/**
		 * Different search distances used in functions that need to find objects that are
		 * close to them. A bigger buffer means that more objects will be analysed (less
		 * efficient) but if the buffer is too small then no objects might be found. 
		 * The units represent a lat/long distance so I'm not entirely sure what they are,
		 * but the <code>Route.distanceToMeters()</code> method can be used to roughly 
		 * convert between these units and meters.
		 * 需要查找靠近它们的对象的函数中使用的不同搜索距离。缓冲区越大意味着将分析更多对象（效率较低），
		 * 但如果缓冲区太小，则可能找不到任何对象。这些单位代表一个纬度/长距离，所以我不完全确定它们是什么，
		 * 但是<code>Route.distanceToMeters（）</code> 方法可用于在这些单位和米之间进行粗略的转换。
		 * @see Geometry
		 * @see Route
		 */
		public static int cishu = 0;
//		不同的搜索距离使用的功能，需要找到接近他们的对象。一个更大的缓冲意味着将更多的对象分析，但是如果缓冲区太小就没有对象可以发现，单位代表一个纬度/经度的距离，所以我不确定它们是什么，但<code>distancetometers()</code>方法可以大致将这些单位和米之间。
		public enum BUFFER_DISTANCE {
			/** The smallest distance, rarely used. Approximately 0.001m*/
			SMALL(0.00001, "1"),//最小的距离，很少使用。约0.001m
			/** Most commonly used distance, OK for looking for nearby houses or roads.
			 * Approximatey 110m */
			MEDIUM(0.009,"990"),//最常用的距离，可以查找附近的房屋或道路
			/** Largest buffer, approximately 550m. I use this when doing things that
			 * don't need to be done often, like populating caches.*/
			LARGE(0.09,"9900");
			/**
			 * @param dist The distance to be passed to the search function (in lat/long?)
			 * @param distInMeters An approximate equivalent distance in meters.
			 */
			BUFFER_DISTANCE(double dist, String distInMeters) {
				this.dist = dist;
				this.distInMeters = distInMeters;
			}
			public double dist;
			public String distInMeters;
		}

		public static final double TRAVEL_PER_TURN = 5; // TODO Make a proper value for this
	}
	
	/** Names of contexts and projections. These names must match those in the
	 * parameters.xml file so that they can be displayed properly in the GUI. */
	public static final class CONTEXT_NAMES {
		
		public static final String MAIN_CONTEXT = "maincontext";
		public static final String MAIN_GEOGRAPHY = "MainGeography";
		
		public static final String BUILDING_CONTEXT = "BuildingContext";
		public static final String BUILDING_GEOGRAPHY = "BuildingGeography";
		
		public static final String ROAD_CONTEXT = "RoadContext";
		public static final String ROAD_GEOGRAPHY = "RoadGeography";
		
		public static final String JUNCTION_CONTEXT = "JunctionContext";
		public static final String JUNCTION_GEOGRAPHY = "JunctionGeography";
		
		public static final String ROAD_NETWORK = "RoadNetwork";
		
		public static final String AGENT_CONTEXT = "AgentContext";
		public static final String AGENT_GEOGRAPHY = "AgentGeography";
	
	}
	
	// Parameters used by transport networks
	public static final class TRANSPORT_PARAMS {

		// This variable is used by NetworkEdge.getWeight() function so that it knows what travel options
		// are available to the agent (e.g. has a car). Can't be passed as a parameter because NetworkEdge.getWeight()
		// must override function in RepastEdge because this is the one called by ShortestPath.
		public static IAgent currentAgent = null;
		public static Object currentBurglarLock = new Object();
		public static currentBurglar currentBurglar;
		
		public static final String WALK = "walk";
		public static final String BUS = "bus";
		public static final String METRO = "metro";	//地铁类型
		public static final String CAR = "car";
		// List of all transport methods in order of quickest first
		public static final List<String> ALL_PARAMS = Arrays.asList(new String[]{METRO, CAR, BUS, WALK});

		// Used in 'access' field by Roads to indicate that they are a 'majorRoad' (i.e. motorway or a-road).
		public static final String MAJOR_ROAD = "majorRoad";		
		// Speed advantage for car drivers if the road is a major road' 如果道路是主要道路，则汽车驾驶员的速度优势
		public static final double MAJOR_ROAD_ADVANTAGE = 1;//原始初值为1
		
		//临时添加类
		public static final class currentBurglar{
			public static String transportAvailable = null;
			public static String getTransportAvailable() {
				return transportAvailable;
			}
			public static void setTransportAvailable(String transport) {
				transportAvailable = transport;
			}
			
			public static boolean isAvailableTransport(String transport) {
				return true;
			}
		}
		
		//倍数关系！！！！ MR.wang
		// The speed associated with different types of road (a multiplier, i.e. x times faster than walking)
		public static double getSpeed(String type) {
			if (type.equals(WALK))
				return 1;
			else if (type.equals(BUS))
				return 2;
			else if (type.equals(METRO))
				return 10000;  //初始值为10
			else if (type.equals(CAR))
				return 5;//初始值为5
			
			else {
				LOGGER.log(Level.SEVERE, "Error getting speed: unrecognised type: "+type);
				return 1;
			}
		}
	}
	

}
