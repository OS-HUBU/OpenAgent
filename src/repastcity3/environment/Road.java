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

package repastcity3.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.exceptions.DuplicateRoadException;
import repastcity3.exceptions.NoIdentifierException;
import repastcity3.main.GlobalVars;

/**
 * Represents road objects.
 * 
 * @author Nick Malleson
 */
public class Road implements FixedGeography {

	private static Logger LOGGER = Logger.getLogger(Road.class.getName());

	/*
	 * An identifier which can be used to link Roads (in a spatial GIS) with Edges (in a Network). Should be found using
	 * the column name in a GIS table (e.g. TOID). Each road *must* have a unique ID
	 * 
	 * 可用于将道路（在空间 GIS 中）与边（在网络中）链接的标识符。
	 * 应使用 GIS 表中的列名（例如 TOID）进行查找。每条道路必须具有唯一的 ID
	 */
	private String identifier;
	// Map to make sure road IDs are unique
	//用于确保道路 ID 唯一的地图
	private static Map<String, Object> idMap = new HashMap<String, Object>();

	// The junctions at either end of the road
	//道路两端的交叉路口
	transient private ArrayList<Junction> junctions;

	private Coordinate coord;
	
	// The NetworkEdge which represents this Road in the roadNetwork
	//在道路网络中代表这条路的网络边缘
	transient private NetworkEdge<Junction> edge;

	// These determine whether or not the the road can be traversed on foot and/or by car.
	//这些决定了道路是否可以步行和/或开车穿越（道路属性？）
	//// To be used by ShapefileLoader, should contain string of words separated by spaces
	//要由形状文件加载器使用，应包含由空格分隔的单词字符串
	private String access; 
	
	// access String should be parsed into this list (see initialise()).
	//access 字符串应解析到此列表中
	private List<String> accessibility; 

	// Doesn't affect model but useful for debugging
	//不影响模型，但对调试有用
	private String name; 

	private boolean majorRoad = false;

	/**
	 * The null road represents Road objects that do not actually exist, preventing NullPointerExceptions. This is
	 * necessary for routes that include transport networks as these wont necessarily have a Road object associated with
	 * them (e.g. train lines).
	 * 
	 * 空道路表示实际上不存在的道路对象，从而阻止空点表示异常。这是
	 * 对于包含运输网络的路线是必需的，因为这些路线不一定具有与之关联的道路对象（例如火车线路）
	 */
	public static Road nullRoad;
	
	static {
		Road.nullRoad = new Road();
		try {
			Road.nullRoad.setIdentifier("NULLROAD");
			Road.nullRoad.setCoords(new Coordinate());
		} catch (DuplicateRoadException e) { // This should never happen
			LOGGER.log(Level.SEVERE, "", e);
		}

	}

	public Road() {
		this.junctions = new ArrayList<Junction>();
	}

	/**
	 * This should be called once this Road object has been created to perform some extra initialisation (e.g. setting
	 * the accessibility methods available to this Road).
	 * 
	 * 创建此 Road 对象以执行一些额外的初始化（例如，设置可用于此 Road 的辅助功能方法），应调用此函数。
	 * 
	 * @throws NoIdentifierException
	 */
	public void initialise() throws NoIdentifierException {
		if (this.identifier == null || this.identifier == "") {
			throw new NoIdentifierException("This road has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		}
		
		// Parse the access string and work out which accessibility methods can be used to travel this Road
		//解析访问字符串并找出哪些可访问性方法可用于在这条路上行驶
		if (this.access != null) { // Could be null because not using accessibility in GRID environment for example 可能为 null，因为例如在 GRID 环境中不使用辅助功能
			this.accessibility = new ArrayList<String>();
			
			//将 car major 分词  car  和  major
			for (String word : this.access.split(" ")) {
				if (word.equals(GlobalVars.TRANSPORT_PARAMS.MAJOR_ROAD)) {
					// Special case: 'majorRoad' isn't a type of access, means the road is quick for car drivers
					this.majorRoad = true;
				} else {
					// Otherwise just add the accessibility type to the list
					//否则，只需将辅助功能类型添加到列表中
					this.accessibility.add(word);
				}
			}
		}
	}
	
	public void initialiseFirst() {
		if (this.access != null) { // Could be null because not using accessibility in GRID environment for example 可能为 null，因为例如在 GRID 环境中不使用辅助功能
			this.accessibility = new ArrayList<String>();
			
			//将 car major 分词  car  和  major
			for (String word : this.access.split(" ")) {
				if (word.equals(GlobalVars.TRANSPORT_PARAMS.MAJOR_ROAD)) {
					// Special case: 'majorRoad' isn't a type of access, means the road is quick for car drivers
					this.majorRoad = true;
				} else {
					// Otherwise just add the accessibility type to the list
					//否则，只需将辅助功能类型添加到列表中
					this.accessibility.add(word);
				}
			}
		}
		
	}

	/**
	 * Sets the access methods which can be used to get down this road (e.g. "walk", "car" etc).
	 * 设置可用于走上这条路的访问方法
	 * <p>
	 * Different roads can be accessed differently depending on the transportation available to the agents. The 'access'
	 * variable can be used by ShapefileLoader to set the different accessibility methods, but it must be parsed and the
	 * accessibility list populated in initialise() (once the Road has been created). E.g. the String "walk car"
	 * indicates agents can either walk or drive down the Road. Note that, ultimately, Roads might also form parts of
	 * transport networks (e.g. busses) but this is done by changing the edges in the roadNetwork directly (in
	 * EnvironmentFactory.createTransportNetworks) and does not affect Road objects.
	 * 
	 * 根据agent可用的交通工具，可以以不同的方式访问不同的道路。
	 * “访问”变量可以由形状文件加载器来设置不同的可访问性方法，但必须解析它和在初始化（）中填充的可访问性列表（创建道路后）。
	 * 例如，字符串“步行车”
	 * 表示agent可以步行或开车上路。请注意，最终，道路也可能构成运输网络（例如公共汽车），
	 * 但这是通过直接更改道路网络中的边缘来完成的（在环境工厂.创建交通网络），并且不会影响道路对象。
	 * 
	 * @param access
	 *            A string indicating how this road can be traversed, separated by spaces.
	 *            指示如何遍历此道路的字符串，由空格分隔
	 */
	public void setAccess(String access) {
		this.access = access;
	}

	//额外添加方法 MR.WANG
	public String getAccess() {
		 return this.access ;
	}
	
	
	
	public boolean isMajorRoad() {
		return this.majorRoad;
	}

	/**
	 * Get the accessibility methods (not including public transport) which agents can use to travel along this road.
	 * 获取agent可用于沿此道路行驶的辅助功能方法（不包括公共交通）。
	 * 
	 * @return
	 * @see setAccess
	 */
	//里面存放的就是解析后的car  、 walk等字符
	public List<String> getAccessibility() {
		return this.accessibility;
	}

	@Override
	public String toString() {
		return "road: " + this.identifier + this.isMajorRoad() + (this.name == null ? "" : "(" + this.name + ")");
	}

	/**
	 * Get the unique identifier for this Road. This identifier is used to link road features in a GIS with Edges added
	 * to the RoadNetwork (a repast Network Projection).
	 * 获取此道路的唯一标识符。此标识符用于将 GIS 中的道路要素与添加的边缘链接起来
	 * 到道路网络（重新刷新网络投影）。
	 * 
	 * @return the identifier for this Road.
	 * @throws NoIdentifierException
	 *             if the identifier has not been set correctly. This might occur if the roads are not initialised
	 *             correctly (e.g. there is no attribute called 'identifier' present in the shapefile used to create
	 *             this Road).
	 */
	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This road has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}

	/**
	 * Set the road's identifier. Will check that it is unique.
	 * 设置道路的标识符。将检查它是否唯一。
	 * 
	 * @param identifier
	 * @throws DuplicateRoadException
	 *             If a road with the given identifier has already been created
	 */
	public void setIdentifier(String identifier) throws DuplicateRoadException {
		// Check the ID is unique

		if (Road.idMap.containsKey(identifier)) {
			throw new DuplicateRoadException("A road with identifier '" + identifier + "' has already "
					+ "been created - cannot have two roads with the same unique ID.");
		}
		this.identifier = identifier;
	}

	/**
	 * Used to tell this Road who it's Junctions (endpoints) are.
	 * 用于告诉这条路的交汇点（端点）是谁
	 * 
	 * junction 实际意义有待考究？   到底是个什么样的集合
	 * 
	 * @param j
	 *            the Junction at either end of this Road.
	 */
	public void addJunction(Junction j) {
		if (this.junctions.size() == 2) {
			System.err.println("Road: Error: this Road object already has two Junctions.");
		}
		this.junctions.add(j);
	}

	public ArrayList<Junction> getJunctions() {
		if (this.junctions.size() != 2) {
			System.err.println("Road: Error: This Road does not have two Junctions");
		}
		return this.junctions;
	}

	/**
	 * @return the coord
	 */
	public Coordinate getCoords() {
		return coord;
	}

	/**
	 * @param coord
	 *            the coord to set
	 */
	public void setCoords(Coordinate coord) {
		this.coord = coord;
	}

	/**
	 * Get the NetworkEdge which represents this Road object in the roadNetwork
	 * 获取表示道路中此道路对象的网络边缘网络
	 * 
	 * 什么是边缘网络？    
	 * 
	 * @return the edge
	 */
	public NetworkEdge<Junction> getEdge() {
		return edge;
	}

	/**
	 * @param edge
	 *            the edge to set
	 */
	public void setEdge(NetworkEdge<Junction> edge) {
		this.edge = edge;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks to see if passed object is a Road and if the unique id's are equal
	 * 检查传递的对象是否为 Road，以及唯一 ID 是否相等
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Road))
			return false;
		Road b = (Road) obj;
		return this.identifier == b.identifier;
	}

	/**
	 * Returns the hash code of this Road's unique identifier.
	 */
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}
}
