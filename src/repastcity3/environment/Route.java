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

package repastcity3.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.datum.Ellipsoid;

import cern.colt.Arrays;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.engine.environment.RunEnvironment;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
//import repastcity3.agent.DefaultAgent;
import repastcity3.agent.InfectedAgent;
import repastcity3.agent.AsymptomaticAgent;
import repastcity3.agent.IAgent;
import repastcity3.agent.MyAgent;
import repastcity3.agent.ThreadedAgentScheduler;
import repastcity3.exceptions.RoutingException;
import repastcity3.main.ContextManager;
import repastcity3.main.GlobalVars;

/**
 * Create routes around a GIS road network. The <code>setRoute</code> function actually finds the route and can be
 * overridden by subclasses to create different types of Route. See the method documentation for details of how routes
 * are calculated.
 * 
 * <p>
 * A "unit of travel" is the distance that an agent can cover in one iteration (one square on a grid environment or the
 * distance covered at walking speed in an iteration on a GIS environment). This will change depending on the type of
 * transport the agent is using. E.g. if they are in a car they will be able to travel faster, similarly if they are
 * travelling along a transort route they will cover more ground.
 * </p>
 * 
 * @author Nick Malleson
 */
public class Route implements Cacheable {

//	public static int sleepNum=0;
//	public static boolean [] isSleep=new boolean[ContextManager.agentCount];
	
//									鐠у嘲顫愰悙绗紻-閻╊喚娈戦崷鐧怐-閸戝搫褰傚鎯扮箿閺冨爼妫�-閺佷即鍣�
	/*public static int [][] initInfo={{0,2,0,3},{0,3,0,4},{0,3,3600,1},{0,8,0,1},{0,13,0,1},{0,26,0,4},{0,26,3600,1},{0,27,0,33},{0,27,3600,12},{0,28,0,8},{0,28,3600,3},{0,29,0,9},{0,29,3600,3},{0,30,0,6},{0,30,3600,3},{0,31,0,7},{0,31,3600,3},{0,32,0,2},{0,32,3600,1},{0,33,0,4},{0,33,3600,1},
		                             {1,2,0,7},{1,3,0,1},{1,4,0,4},{1,4,3600,1},{1,5,0,3},{1,6,0,1},{1,8,0,6},{1,8,3600,1},{1,10,0,1},{1,11,0,1},{1,11,3600,1},{1,12,0,3},{1,13,0,41},{1,13,3600,19},{1,14,0,3},{1,14,3600,1},{1,15,0,3},{1,15,3600,2},{1,17,0,1},{1,17,3600,1},{1,19,0,1},{1,19,3600,1},{1,21,0,2},{1,21,3600,2},{1,22,0,1},{1,24,0,2},{1,25,0,3},{1,25,3600,1},{1,26,0,21},{1,26,3600,9},{1,27,0,39},{1,27,3600,16},{1,28,0,12},{1,28,3600,5},{1,29,0,18},{1,29,3600,7},{1,30,0,25},{1,30,3600,15},{1,31,0,22},{1,31,3600,14},{1,32,0,6},{1,32,3600,4},{1,33,0,13},{1,33,3600,7},
		                             {2,0,0,4},{2,1,0,8},{2,1,3600,1},{2,3,0,4},{2,4,0,10},{2,4,3600,1},{2,5,0,11},{2,5,3600,2},{2,8,0,3},{2,12,0,1},{2,13,0,5},{2,13,3600,3},{2,14,0,2},{2,14,3600,1},{2,15,0,2},{2,17,0,1},{2,18,0,1},{2,19,0,1},{2,21,0,1},{2,24,0,1},{2,25,0,3},{2,25,3600,1},{2,26,0,25},{2,26,3600,9},{2,27,0,66},{2,27,3600,34},{2,28,0,17},{2,28,3600,6},{2,29,0,43},{2,29,3600,17},{2,30,0,26},{2,30,3600,12},{2,31,0,35},{2,31,3600,20},{2,32,0,9},{2,32,3600,5},{2,33,0,16},{2,33,3600,6},
		                             {3,0,0,3},{3,1,0,1},{3,2,0,4},{3,4,0,1},{3,5,0,4},{3,13,0,2},{3,13,3600,1},{3,14,0,2},{3,15,0,1},{3,15,3600,1},{3,21,0,1},{3,24,0,1},{3,25,0,3},{3,25,3600,1},{3,26,0,18},{3,26,3600,5},{3,27,0,23},{3,27,3600,10},{3,28,0,18},{3,28,3600,8},{3,29,0,27},{3,29,3600,14},{3,30,0,17},{3,30,3600,8},{3,31,0,25},{3,31,3600,8},{3,32,0,5},{3,32,3600,2},{3,33,0,8},{3,33,3600,3},
		                             {4,1,0,7},{4,1,3600,1},{4,2,0,9},{4,2,3600,1},{4,3,0,1},{4,5,0,19},{4,5,3600,2},{4,6,0,2},{4,7,0,3},{4,8,0,6},{4,8,3600,1},{4,9,0,1},{4,10,0,1},{4,12,0,3},{4,12,3600,1},{4,13,0,8},{4,13,3600,3},{4,14,0,3},{4,14,3600,1},{4,15,0,2},{4,15,3600,1},{4,17,0,1},{4,17,3600,1},{4,19,0,1},{4,19,3600,1},{4,21,0,1},{4,22,0,1},{4,24,0,1},{4,25,0,3},{4,25,3600,2},{4,26,0,21},{4,26,3600,9},{4,27,0,16},{4,27,3600,8},{4,28,0,13},{4,28,3600,6},{4,29,0,42},{4,29,3600,17},{4,30,0,26},{4,30,3600,15},{4,31,0,36},{4,31,3600,19},{4,32,0,8},{4,32,3600,5},{4,33,0,21},{4,33,0,12},
		                             {5,1,0,4},{5,1,3600,1},{5,2,0,10},{5,2,3600,1},{5,3,0,4},{5,4,0,17},{5,4,3600,2},{5,6,0,10},{5,6,3600,1},{5,7,0,1},{5,8,0,5},{5,10,0,1},{5,11,0,1},{5,12,0,3},{5,12,0,1},{5,13,0,8},{5,13,3600,3},{5,14,0,4},{5,14,3600,1},{5,15,0,4},{5,15,3600,1},{5,17,0,1},{5,17,3600,1},{5,19,0,2},{5,19,3600,1},{5,20,0,1},{5,20,3600,1},{5,21,0,2},{5,22,0,1},{5,23,0,2},{5,24,0,2},{5,25,0,4},{5,25,3600,2},{5,26,0,38},{5,26,3600,15},{5,27,0,32},{5,27,3600,14},{5,28,0,25},{5,28,3600,9},{5,29,0,152},{5,29,3600,71},{5,30,0,59},{5,30,3600,26},{5,31,0,83},{5,31,3600,38},{5,32,0,16},{5,32,3600,7},{5,33,0,48},{5,33,3600,19}
		                             ,{6,1,0,1},{6,4,0,2},{6,5,0,8},{6,5,3600,1},{6,7,0,1},{6,12,0,1},{6,13,0,1},{6,26,0,5},{6,26,3600,1},{6,27,0,3},{6,27,3600,1},{6,28,0,2},{6,28,3600,1},{6,29,0,11},{6,29,3600,4},{6,30,0,4},{6,30,3600,2},{6,31,0,9},{6,31,3600,3},{6,32,0,2},{6,32,3600,2},{6,33,0,5},{6,33,3600,2}
		                             ,{7,1,0,1},{7,4,0,3},{7,5,0,2},{7,6,0,1},{7,8,0,1},{7,26,0,2},{7,27,0,1},{7,28,0,1},{7,29,0,4},{7,29,3600,2},{7,30,0,2},{7,30,3600,1},{7,31,0,3},{7,31,3600,2},{7,33,0,3},{7,33,3600,1}
		                             ,{8,1,0,5},{8,1,3600,1},{8,2,0,1},{8,4,0,6},{8,4,3600,1},{8,5,0,3},{8,7,0,1},{8,9,0,2},{8,10,0,2},{8,11,0,1},{8,12,0,3},{8,12,3600,2},{8,13,0,8},{8,13,3600,3},{8,14,0,3},{8,14,3600,1},{8,15,0,2},{8,24,0,1},{8,25,0,2},{8,25,3600,1},{8,26,0,7},{8,26,3600,5},{8,27,0,4},{8,27,3600,3},{8,28,0,4},{8,28,3600,2},{8,29,0,10},{8,29,3600,5},{8,30,0,13},{8,30,3600,8},{8,31,0,11},{8,31,3600,6},{8,32,0,3},{8,32,3600,2},{8,33,0,13},{8,33,3600,6}
		                             ,{9,4,0,1},{9,8,0,2},{9,10,0,1},{9,12,0,4},{9,12,3600,2},{9,13,0,2},{9,27,0,1},{9,29,0,2},{9,30,0,1},{9,30,3600,1},{9,31,0,1},{9,31,3600,1},{9,33,0,2},{9,33,3600,1}
		                             ,{10,1,0,2},{10,8,0,1},{10,12,0,5},{10,12,3600,2},{10,13,0,19},{10,13,3600,10},{10,14,0,2},{10,14,3600,1},{10,15,0,1},{10,21,0,1},{10,24,0,1},{10,25,0,2},{10,26,0,4},{10,26,3600,1},{10,27,0,2},{10,28,0,2},{10,28,3600,1},{10,29,0,3},{10,29,3600,2},{10,30,0,6},{10,30,3600,3},{10,31,0,4},{10,31,3600,2},{10,32,0,2},{10,32,3600,1},{10,33,0,5},{10,33,3600,2}
		                             ,{11,1,0,1},{11,1,3600,1},{11,3,0,1},{11,4,0,1},{11,5,0,1},{11,8,0,1},{11,12, 0,1},{11,13,0,3},{11,13,3600,1},{11,14,0,4},{11,14,3600,1},{11,15,0,7},{11,15,3600,4},{11,17,3600,1},{11,21,0,3},{11,21,3600,1},{11,22,0,1},{11,24,0,2},{11,24,3600,1},{11,25,0,2},{11,25,3600,1},{11,26,0,5},{11,26,3600,1},{11,27,0,3},{11,27,3600,1},{11,28,0,3},{11,28,3600,1},{11,29,0,4},{11,29,3600,1},{11,30,0,5},{11,30,3600,2},{11,31,0,5},{11,31,3600,2},{11,32,0,2},{11,32,3600,1},{11,33,0,3}
		                             ,{12,1,0,4},{12,1,3600,2},{12,2,0,1},{12,2,3600,1},{12,4,0,3},{12,4,3600,2},{12,5,0,3},{12,8,0,4},{12,8,3600,2},{12,9,0,5},{12,9,3600,3},{12,10,0,7},{12,10,3600,2},{12,11,0,1},{12,13,0,14},{12,13,3600,5},{12,14,0,39},{12,14,3600,11},{12,15,0,3},{12,15,3600,1},{12,19,3600,1},{12,21,0,1},{12,22,0,2},{12,23,0,1},{12,24,0,1},{12,24,3600,1},{12,25,0,4},{12,26,0,12},{12,26,3600,3},{12,27,0,4},{12,27,3600,2},{12,28,0,3},{12,28,3600,2},{12,29,0,7},{12,29,3600,3},{12,30,0,12},{12,30,3600,5},{12,31,0,10},{12,31,3600,4},{12,32,0,2},{12,32,3600,1},{12,33,0,14},{12,33,3600,4}
	*/
	
	//濞村鐦�
//	public static int [][] initInfo={{1,31,0,1},{1,31,0,1}};
//	public static int [][] initInfos={{2,31,0,1},{2,31,0,1}};
//	public static int [][] initInfo={{1,618,0,1},{1,618,100,1}    
		   
//	public static int [][] initInfo={{1,4,0,25},{1,5,0,25},{1,553,0,25},{1,554,0,25}   
//    ,{2,0,0,20},{2,6,0,20},{2,28,0,20},{2,549,0,40},{2,715,0,20}
//    ,{554,0,0,50},{554,1,0,50}
//    ,{682,479,0,100},{682,698,0,100}
//    ,{699,479,0,100},{699,479,0,100}};  // {2,407,0,10} -> 407閸樿绗夋禍鍡椾粻閸︺劌甯崷锟�2 
//	public static int [][] initInfos={{3,31,0,1}}; 

	public static Double currentSpeed;
	//ContextManager.TRIFFIC_JAM_COUNT 閹枫儱鐗捄顖氬經閻ㄥ嫭鏆熼柌锟�
	public static int [] crossAgentNums=new int[ContextManager.TRIFFIC_JAM_COUNT];		//閻€劍娼甸崒銊ョ摠閸︺劍瀚㈤崼浣冪熅閸欙絿娈戦弲楦垮厴娴ｆ挻鏆熼柌蹇撳灩闂勩倖顒濈悰灞嗘妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟妞曟绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涳拷
	public static boolean [][] isPass=new boolean[ContextManager.TRIFFIC_JAM_COUNT][ContextManager.agentCount];//閻€劍娼甸弽鍥唶閺呴缚鍏樻担鎾存Ц閸氾附娴樼紒蹇氱箖閹枫儱鐗捄顖氬經閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼閿涗緤绱掗敍渚婄磼鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤鑴宠劤
	//static int agentID=0; //閻€劍娼佃ぐ鎾充粵閺呴缚鍏樻担鎾舵畱缂傛牕褰块敍锟�
	

	
	
	private static Logger LOGGER = Logger.getLogger(Route.class.getName());

//	static {
//		Route.routeCache = new Hashtable<CachedRoute, CachedRoute>();
//	}

	private IAgent agent;
	private Coordinate destination;
	private Building destinationBuilding;//閻╊喚娈戦崷鏉跨紦缁涳拷
	
	
	public boolean forbid = false;//娑撳瓨妞傚ǎ璇插  Mr.wang
	private static Object AgentContextLock = new Object();
	public static final double CROSS_MAX_DIST=.00074852;////閻€劍娼电拋鍓х枂閸掋倕鐗弲楦垮厴娴ｆ挷绗岄幏銉ョ壄閸ф劖鐖ｉ惃鍕獩缁傜饱dd!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	/*
	 * The route consists of a list of coordinates which describe how to get to the destination. Each coordinate might
	 * have an attached 'speed' which acts as a multiplier and is used to indicate whether or not the agent is
	 * travelling along a transport route (i.e. if a coordinate has an attached speed of '2' the agent will be able to
	 * get to the next coordinate twice as fast as they would do if they were walking). The current position incicate
	 * where in the lists of coords the agent is up to. Other attribute information about the route can be included as
	 * separate arrays with indices that match those of the 'route' array below.
	 */
	//鐠囥儴鐭剧痪璺ㄦ暠娑擄拷娑擃亜娼楅弽鍥у灙鐞涖劍寮挎潻鏉款洤娴ｆ洖鍩屾潏鍓ф窗閻ㄥ嫬婀撮妴鍌涚槨娑擃亜娼楅弽鍥у讲閼宠姤婀佹稉锟芥稉顏堟閸旂姷娈戦垾婊堬拷鐔峰閳ユ繀缍旀稉杞扮娑擃亙绠婚弫甯礉閻€劋绨幐鍥┿仛閺勵垰鎯侀崜鍌涢儴鏉╂劘绶捄顖滃殠鐞涘矂鈹掗敍鍫濆祮婵″倹鐏夐崸鎰垼閺堝绔存稉顏堟閸旂姷娈戦柅鐔峰閳ワ拷2閳ユ繀鍞悶鍡楃殺閼宠棄顧勬稉銈咃拷宥勭铂娴狀兛绱伴崑姘洤閺嬫粈绮禒顒冭泲闁絼绠炶箛顐㈠煂鏉堝彞绗呮稉锟芥稉顏勬綏閺嶅浄绱氶妴鍌氭躬閸ф劖鐖ｉ惃鍕敩閻炲棗鍨悰銊ф畱瑜版挸澧犳担宥囩枂incicatewhere鏉堜勘锟藉倸鍙ф禍搴ょ熅閻㈣京娈戦崗鏈电铂鐏炵偞锟窖備繆閹垰褰叉禒銉ュ瘶閸氼偂璐熼崡鏇犲閻ㄥ嫭鏆熺紒鍕剁礉鏉╂瑤绨虹槐銏犵穿娑撳簼绗呴棃銏㈡畱閳ユ粏鐭鹃悽鎵侊拷婵囨殶缂佸嫬灏柊锟�
	private int currentPosition;//标记当前轨迹的索引
	private List<Coordinate> routeX;//[(-1.5288438419044588, 53.79513763369657, NaN), (-1.5286215229228963, 53.79515666844628, NaN), (-1.5248702294929621, 53.795741273937175, NaN), (-1.5248702294929621, 53.795741273937175, NaN), (-1.5248702294929621, 53.795741273937175, NaN), (-1.5247840031289315, 53.79575471098181, NaN), (-1.5232334288851135, 53.79598560527128, NaN), (-1.5225266711164647, 53.79609084310333, NaN), (-1.5219152306164303, 53.796114581628295, NaN), (-1.5206910160647824, 53.79616210512752, NaN), (-1.5208039798966904, 53.804069089419336, NaN)]
	//濞村鐦� Mr.wang
	public List<Coordinate> routeXX;
	private List<Double> routeSpeedsX;//[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 10000.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]
	/*
	 * This maps route coordinates to their containing Road, used so that when travelling we know which road/community
	 * the agent is on. private
	 */
	
	private List<Road> roadsX;//[road: NULLROADfalse, road: 21true, road: 21true, road: 21true, road: 20true, road: 20true, road: 20true, road: 20true, road: 20true, road: 18true, road: 18true, road: NULLROADfalse]

	// Record which function has added each coord, useful for debugging
	private List<String> routeDescriptionX;

	/**
	 * Cache every coordinate which forms a road so that Route.onRoad() is quicker. Also save the Road(s) they are part
	 * of, useful for the agent's awareness space (see getRoadFromCoordCache()).
	 */
	private static volatile Map<Coordinate, List<Road>> coordCache;
	/**
	 * Cache the nearest road Coordinate to every building for efficiency (agents usually/always need to get from the
	 * centroids of houses to/from the nearest road).
	 */
	private static volatile NearestRoadCoordCache nearestRoadCoordCache;
	/**
	 * Store which road every building is closest to. This is used to efficiently add buildings to the agent's awareness
	 * space
	 */
	private static volatile BuildingsOnRoadCache buildingsOnRoadCache;
	// To stop threads competing for the cache:
	private static Object buildingsOnRoadCacheLock = new Object();

	/**
	 * Store a route once it has been created, might be used later (note that the same object acts as key and value).
	 */
	// TODO Re-think route caching, would be better to cache the whole Route object
	// private static volatile Map<CachedRoute, CachedRoute> routeCache;
	// /** Store a route distance once it has been created */
	// private static volatile Map<CachedRouteDistance, Double> routeDistanceCache;

	/**
	 * Keep a record of the last community and road passed so that the same buildings/communities aren't added to the
	 * cognitive map multiple times (the agent could spend a number of iterations on the same road or community).
	 */
	private Road previousRoad;
	private Area previousArea;

	/**
	 * Creates a new Route object.
	 * 
	 * @param burglar
	 *            The burglar which this Route will control.
	 * 
	 * @param destination  //agent閻ㄥ嫮娲伴惃鍕勾
	 *            The agent's destination.
	 * 
	 * @param destinationBuilding   //娴犳牔婊戠憰浣稿箵閻ㄥ嫸绱欓崣顖烇拷澶涚礆瀵よ櫣鐡氶悧锟�
	 *            The (optional) building they're heading to.
	 * 
	 * @param type
	 *            The (optional) type of route, used by burglars who want to search.
	 */
	public Route(IAgent agent, Coordinate destination, Building destinationBuilding) {
		this.destination = destination;
		this.agent = agent;
		this.destinationBuilding = destinationBuilding;
	}
	
	
	
	
	
	//濞ｈ濮為弬瑙勭《 Mr.wang
//	public void gridRun(MyAgent agent) {
//		NdPoint myPoint = agent.getSpace().getLocation(agent);
//		NdPoint otherPoint = new NdPoint(this.routeX.get(agent.cishu).getOrdinate(0), this.routeX.get(agent.cishu).getOrdinate(1));
//		double angle = SpatialMath.calcAngleFor2DMovement(agent.getSpace(), myPoint, otherPoint);
//		agent.getSpace().moveByVector(agent, 2, angle, 0);
//		myPoint = agent.getSpace().getLocation(agent);
//		agent.getGrid().moveTo(agent, (int)myPoint.getX(), (int)myPoint.getY());
//		System.out.println("agentID="+agent.getID());
//		System.out.println("agent濞嗏剝鏆�="+agent.cishu);
//		System.out.println(agent.getID()+"agentRouteX="+this.routeX.toString());
//		
//	}
	
	
	/**
	 * Find a route from the origin to the destination. A route is a list of Coordinates which describe the route to a
	 * destination restricted to a road network. The algorithm consists of three major parts:
	 * <ol>
	 * <li>Find out if the agent is on a road already, if not then move to the nearest road segment</li>
	 * <li>Get from the current location (probably mid-point on a road) to the nearest junction</li>
	 * <li>Travel to the junction which is closest to our destination (using Dijkstra's shortest path)</li>
	 * <li>Get from the final junction to the road which is nearest to the destination
	 * <li>
	 * <li>Move from the road to the destination</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	protected void setRoute() throws Exception {
		long time = System.nanoTime();
		// this.routeX = new ArrayList<Coordinate>();
		// this.roadsX = new ArrayList<Road>();
		// this.routeDescriptionX = new ArrayList<String>();
		// this.routeSpeedsX = new ArrayList<Double>();
		this.routeX = new Vector<Coordinate>();
		this.roadsX = new Vector<Road>();
		this.routeDescriptionX = new Vector<String>();
		this.routeSpeedsX = new Vector<Double>();

		LOGGER.log(Level.FINER, "Planning route for: "
				+ this.agent.toString()
				+ " to: "
				+ this.destinationBuilding.toString()
				+ ((this.agent.getTransportAvailable() == null) ? "" : "using transport: "
						+ this.agent.getTransportAvailable().toString()));
		if (atDestination()) {
			LOGGER.log(Level.WARNING, "Already at destination, cannot create a route for " + this.agent.toString());
			return;
		}

		Coordinate currentCoord = ContextManager.getAgentGeometry(this.agent).getCoordinate();//agent瑜版挸澧犳担宥囩枂
		Coordinate destCoord = this.destination;//agent閻ㄥ嫮娲伴惃鍕勾閸ф劖鐖�   閸︹暞tep娑擃厼鍑＄紒蹇氱ゴ閸婏拷

		// See if a route has already been cached.
		// CachedRoute cachedRoute = new CachedRoute(currentCoord, destCoord, this.agent.getTransportAvailable());
		// synchronized (Route.routeCache) {
		// if (Route.routeCache.containsKey(cachedRoute)) {
		// TempLogger.out("Route.setRoute, found a cached route from " + currentCoord + " to " + destCoord
		// + " using available transport " + this.agent.getTransportAvailable() + ", returning it.");
		// // Return a clone of the route that is stored in the cache
		// // TODO do we need clones here? I don't think so...
		// CachedRoute cr = Route.routeCache.get(cachedRoute);
		// // this.routeX = Cloning.copy(cr.getRoute());
		// // this.roadsX = new ArrayList<Road>(cr.getRoads());
		// // this.routeSpeedsX = new ArrayList<Double>(cr.getRouteSpeeds());
		// // this.routeDescriptionX = new ArrayList<String>(cr.getDescriptions());
		// this.routeX = new Vector<Coordinate>(cr.getRoute());
		// this.roadsX = new Vector<Road>(cr.getRoads());
		// this.routeSpeedsX = new Vector<Double>(cr.getRouteSpeeds());
		// this.routeDescriptionX = new Vector<String>(cr.getDescriptions());
		//
		// return;
		// }
		// } // synchronized

		// No route cached, have to create a new one (and cache it at the end).
		try {
			/*
			 * See if the current position and the destination are on road segments. If the destination is not on a road
			 * segment we have to move to the closest road segment, then onto the destination.
			 */
			boolean destinationOnRoad = true;
			Coordinate finalDestination = null;
			//濞村鐦崸鎰垼閺勵垰鎯侀弰顖炰壕鐠侯垱顔岄惃鍕闁劌鍨庨妴锟� roadcache缂傛挸鐡ㄧ�圭偤妾稉濠冨絹閸撳秳绌堕崚鈺傚閺堝『oad  楠炶埖鏂侀崷鈺゛p闂嗗棗鎮庢稉锟� 闁款喕璐熼悙鐟版綏閺嶏拷 閸婇棿璐焤oad鐎圭偘绶ist闂嗗棗鎮�
			if (!coordOnRoad(currentCoord)) {
				/*
				 * Not on a road so the first coordinate to add to the route is the point on the closest road segment.
				 * 娑撳秴婀柆鎾圭熅娑撳绱濋崶鐘愁劃鐟曚焦鍧婇崝鐘插煂鐠侯垰绶為惃鍕儑娑擄拷娑擃亜娼楅弽鍥ㄦЦ閺堬拷鏉╂垼鐭惧▓鍏哥瑐閻ㄥ嫮鍋ｉ妴锟�
				 */
				currentCoord = getNearestRoadCoord(currentCoord);
				addToRoute(currentCoord, Road.nullRoad, 1, "setRoute() initial");
			}
			if (!coordOnRoad(destCoord)) {//destCoord娑撹櫣娲伴惃鍕勾閸ф劖鐖�
				/*
				 * Not on a road, so need to set the destination to be the closest point on a road, and set the
				 * destinationOnRoad boolean to false so we know to add the final dest coord at the end of the route
				 * 娑撳秴婀捄顖欑瑐閿涘苯娲滃銈夋付鐟曚礁鐨㈤惄顔炬畱閸︽媽顔曠純顔昏礋闁捁鐭炬稉濠冩付鏉╂垹娈戦悙鐧哥礉楠炶泛鐨㈤惄顔炬畱閸︾櫂nRoad鐢啫鐨甸崐鑹邦啎缂冾喕璐� false閿涘奔浜掓笟鎸庡灉娴狀剛鐓￠柆鎾虫躬鐠侯垳鍤庨惃鍕汞鐏忕偓鍧婇崝鐘虫付缂佸牏娈� dest 閸ф劖鐖�
				 */
				destinationOnRoad = false;
				finalDestination = destCoord; // Added to route at end of alg.
				destCoord = getNearestRoadCoord(destCoord);
			}

			/*
			 * Find the nearest junctions to our current position (road endpoints)
			 */

			// Start by Finding the road that this coordinate is on
			/*
			 * TODO EFFICIENCY: often the agent will be creating a new route from a building so will always find the
			 * same road, could use a cache. Even better, could implement a cache in FindNearestObject() method!
			 * 闁艾鐖堕弲楦垮厴娴ｆ挸鐨㈤崚娑樼紦娑擄拷娑擃亝鏌婇惃鍕熅閻㈠彉绮犳稉锟芥稉顏勭紦缁涙垹澧块敍灞惧娴犮儲锟界粯妲告导姘閸掓壆娴夐崥宀�娈戦柆鎾圭熅閿涘苯褰叉禒銉ゅ▏閻€劎绱︾�涙ǜ锟藉倹娲挎總鐣屾畱閺勵垽绱濋崣顖欎簰鐎圭偟骞囬崷鈺ndnearestobject()閺傝纭剁紓鎾崇摠閿涳拷
			 */
			//閸︺劎绮扮�规氨娈戦崷鎵倞閸ф劖鐖ｆ稉顓熺叀閹电偓娓舵潻鎴犳畱鐎电钖�
			Road currentRoad = Route.findNearestObject(currentCoord, ContextManager.roadProjection, null,
					GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.LARGE);
			// Find which Junction is closest to us on the road.
			List<Junction> currentJunctions = currentRoad.getJunctions();

			/* Find the nearest Junctions to our destination (road endpoints) */

			// Find the road that this coordinate is on
			Road destRoad = Route.findNearestObject(destCoord, ContextManager.roadProjection, null,
					GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.SMALL);
			// Find which Junction connected to the edge is closest to the coordinate.
			List<Junction> destJunctions = destRoad.getJunctions();
			/*
			 * Now have four possible routes (2 origin junctions, 2 destination junctions) need to pick which junctions
			 * form shortest route
			 * 閻滄澘婀張澶婃磽閺夆�冲讲閼崇晫娈戠捄顖滃殠閿涳拷2娑擃亜甯悙纭呯熅閸欙綇绱�2娑擃亞娲伴惃鍕勾鐠侯垰褰涢敍澶愭付鐟曚線锟藉瀚ㄩ崫顏冮嚋鐠侯垰褰涜ぐ銏″灇閺堬拷閻叀鐭剧痪锟�
			 */
			Junction[] routeEndpoints = new Junction[2];
			List<RepastEdge<Junction>> shortestPath = getShortestRoute(currentJunctions, destJunctions, routeEndpoints);//閸戞椽鏁婇惃鍕勾閺傜櫢绱掗敍渚婄磼
			// NetworkEdge<Junction> temp = (NetworkEdge<Junction>)
			// shortestPath.get(0);
			Junction currentJunction = routeEndpoints[0];
			Junction destJunction = routeEndpoints[1];

			/* Add the coordinates describing how to get to the nearest junction濞ｈ濮為崸鎰垼閹诲繗鍫俊鍌欑秿閸掓媽鎻張锟芥潻鎴犳畱鐠侯垰褰� */
			List<Coordinate> tempCoordList = new Vector<Coordinate>();
			//閼惧嘲褰囨禒搴ゆ崳閻愮懓鍩岀紒鍫㈠仯鐠侯垳鍤庨惃鍕毐閺侊拷
			this.getCoordsAlongRoad(currentCoord, currentJunction.getCoords(), currentRoad, true, tempCoordList);
			addToRoute(tempCoordList, currentRoad, 1, "getCoordsAlongRoad (toJunction)");

			/*
			 * Add the coordinates and speeds etc which describe how to move along the chosen path
			 */
			this.getRouteBetweenJunctions(shortestPath, currentJunction);

			/*
			 * Add the coordinates describing how to get from the final junction to the destination.
			 */

			tempCoordList.clear();
			this.getCoordsAlongRoad(ContextManager.junctionGeography.getGeometry(destJunction).getCoordinate(),
					destCoord, destRoad, false, tempCoordList);
			addToRoute(tempCoordList, destRoad, 1, "getCoordsAlongRoad (fromJunction)");

			if (!destinationOnRoad) {
				addToRoute(finalDestination, Road.nullRoad, 1, "setRoute final");
			}

			// Check that a route has actually been created
			checkListSizes();

			// If the algorithm was better no coordinates would have been duplicated
			// removePairs();

			// Check lists are still the same size.
			checkListSizes();

		} catch (RoutingException e) {
			LOGGER.log(Level.SEVERE, "Route.setRoute(): Problem creating route for " + this.agent.toString()
					+ " going from " + currentCoord.toString() + " to " + this.destination.toString() + "("
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString())
					+ ") See earlier messages error messages for more info.");
			throw e;
		}
		// Cache the route and route speeds
		// List<Coordinate> routeClone = Cloning.copy(theRoute);
		// LinkedHashMap<Coordinate, Double> routeSpeedsClone = Cloning.copy(this.routeSpeeds);
		// cachedRoute.setRoute(routeClone);
		// cachedRoute.setRouteSpeeds(routeSpeedsClone);

		// cachedRoute.setRoute(this.routeX, this.roadsX, this.routeSpeedsX, this.routeDescriptionX);
		// synchronized (Route.routeCache) {
		// // Same cached route is both value and key
		// Route.routeCache.put(cachedRoute, cachedRoute);
		// }
		// TempLogger.out("...Route cacheing new route with unique id " + cachedRoute.hashCode());

		LOGGER.log(Level.FINER, "Route Finished planning route for " + this.agent.toString() + "with "
				+ this.routeX.size() + " coords in " + (0.000001 * (System.nanoTime() - time)) + "ms.");

		// Finished, just check that the route arrays are all in sync
		assert this.roadsX.size() == this.routeX.size() && this.routeDescriptionX.size() == this.routeSpeedsX.size()
				&& this.roadsX.size() == this.routeDescriptionX.size();
	}

	private void checkListSizes() {
		assert this.roadsX.size() > 0 && this.roadsX.size() == this.routeX.size()
				&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
				&& this.roadsX.size() == this.routeDescriptionX.size() : this.routeX.size() + "," + this.roadsX.size()
				+ "," + this.routeDescriptionX.size() + "," + this.routeSpeedsX.size();

	}

	/**
	 * Convenience function that can be used to add details to the route. This should be used rather than updating
	 * individual lists because it makes sure that all lists stay in sync
	 * 閸欘垳鏁ゆ禍搴℃倻鐠侯垳鍤庡ǎ璇插鐠囷妇绮忔穱鈩冧紖閻ㄥ嫪绌堕崚鈺佸閼冲锟藉倽绻栨惔鏃囶嚉娴ｈ法鏁ら懓灞肩瑝閺勵垱娲块弬鏉垮礋娑擃亜鍨悰顭掔礉閸ョ姳璐熺�瑰啰鈥樻穱婵囧閺堝鍨悰銊ょ箽閹镐礁鎮撳锟�
	 * 
	 * @param coord
	 *            The coordinate to add to the route  鐟曚焦鍧婇崝鐘插煂鐠侯垰绶為惃鍕綏閺嶏拷
	 * @param road
	 *            The road that the coordinate is part of  閸ф劖鐖ｉ幍锟界仦鐐垫畱闁捁鐭�
	 * @param speed
	 *            The speed that the road can be travelled along  闁捁鐭鹃崣顖欎簰鐞涘矂鈹掗惃鍕拷鐔峰
	 * @param description
	 *            A description of why the coordinate has been added 閹诲繗鍫ǎ璇插閸ф劖鐖ｉ惃鍕斧閸ワ拷
	 */
	private void addToRoute(Coordinate coord, Road road, double speed, String description) {
		this.routeX.add(coord);
		this.roadsX.add(road);
		this.routeSpeedsX.add(speed);
		this.routeDescriptionX.add(description);
	}

	/**
	 * A convenience for adding to the route that will add a number of coordinates with the same description, road and
	 * speed.
	 * 
	 * @param coord
	 *            A list of coordinates to add to the route
	 * @param road
	 *            The road that the coordinates are part of
	 * @param speed
	 *            The speed that the road can be travelled along
	 * @param description
	 *            A description of why the coordinates have been added
	 */
	private void addToRoute(List<Coordinate> coords, Road road, double speed, String description) {
		for (Coordinate c : coords) {
			this.routeX.add(c);
			this.roadsX.add(road);
			this.routeSpeedsX.add(speed);
			this.routeDescriptionX.add(description);
		}
	}

	/**
	 * 閼惧嘲褰囪ぐ鎾冲閻ㄥ嫬娼楅弽锟�
	 * @return
	 */
	public Coordinate getCurrentCoord(){
		return this.routeX.get(this.currentPosition);
	}
	
	//濞ｈ濮為棁锟界憰浣稿灲閺傤厽瀚㈤崼鐢垫畱鐠猴拷
//	static{
//		TrafficJam [] jam=new TrafficJam[TRIFFIC_JAM_COUNT];
//		for(int i=0;i<TRIFFIC_JAM_COUNT;i++){
//			jam[i]=new TrafficJam(coorXY[2*i+1],coorXY[2*i]);
//			TrafficJam.list.add(jam[i]);
//		}
////		sleepNum=0;
////		for(int i=0;i<ContextManager.agentCount;i++){
////			isSleep[i]=false;
////		}
//	}
//	
	/**
	 * 延时出行
	 */
//	public double timeToGo(double speed,int agentID){
//		long unixtime=ThreadedAgentScheduler.getCurrentTime();
//		int num=0,firstDim=0,lastPart=0;
//		for(;num<agent.getID();){
//			num++;
//			if(num-lastPart>(Route.initInfo[firstDim][3]-1)){
//				lastPart=num;
//				firstDim++;
//			}
//		}
//		if(unixtime <= 1000*(initInfo[firstDim][2]))
//			return 0;
//		return speed;
//	}
	
//	public static int getSleepNum(){
//		return sleepNum;
//	}
//	
	//通过修改速度来控制时间跨度
	public double CtrlTimebyModSpeed(double speed,int agentID,double ratio) {
		
		return speed;
	}
	/**
	 * 閺嶈宓侀幏銉ョ壄閹懎鍠屾穱顔芥暭閺呴缚鍏樻担鎾崇安鐠囥儴顢戞潻娑氭畱闁喎瀹�.  閹枫儱鐗幆鍛枌閻ㄥ嫰锟界喎瀹崇拋鍓х枂
	 */
	public double changeSpeedInTrafficJam(double speed,Coordinate currentCoord,int agentID){
		for(int i = 0;i < TrafficJam.list.size(); i ++){
			TrafficJam next=TrafficJam.list.get(i);
			Coordinate jamcoor=new Coordinate(next.getX(),next.getY());
			//鐠侊紕鐣婚崚鏉垮綗娑擄拷娑擃亙缍呯純顔炬畱娴滃瞼娣▎褎鐨捄婵堫瀲閵嗭拷
			double dist=currentCoord.distance(jamcoor);
			
			//CROSS_MAX_DIST閻€劍娼电拋鍓х枂閺呴缚鍏樻担鎾茬瑢閹枫儱鐗崸鎰垼閻ㄥ嫯绐涚粋锟�
			if(dist<=CROSS_MAX_DIST){
				//isPass閻€劍娼甸崚銈嗘焽閺呴缚鍏樻担鎾存Ц閸氾箒鐭炬潻锟�
				if(isPass[next.getID()][agentID]==false){
					crossAgentNums[next.getID()]++;
					isPass[next.getID()][agentID]=true;
				}
				/*if(crossAgentNums[next.getID()]>=20) speed*=0.1;/*&&crossAgentNums[next.getID()]<=6){
					speed*=0.75;
				}else if(crossAgentNums[next.getID()]>6&&crossAgentNums[next.getID()]<=9){
					speed*=0.5;
				}else if(crossAgentNums[next.getID()]>9&&crossAgentNums[next.getID()]<=12){
					speed*=0.25;
				}else if(crossAgentNums[next.getID()]>12){
					speed*=0.05;
				}*/
				
				//濞村鐦�---------------------------------------------------------------
				//if(crossAgentNums[next.getID()]>=20) speed=9;
				
			}else if(dist>CROSS_MAX_DIST&&isPass[next.getID()][agentID]==true){
				crossAgentNums[next.getID()]--;
				isPass[next.getID()][agentID]=false;
			}
//			LOGGER.log(Level.SEVERE, "ID: " +agentID);
//			 Iterator it1 = Road.junctions.iterator();
//		     while(it1.hasNext()){
//		         LOGGER.log(Level.SEVERE, "X,y: " +it1.next().toString());
//		     }
//			
//			LOGGER.log(Level.SEVERE, "id " +next.getID());
		}
		return speed ;		
	}
	
	
	/**
	 * Travel towards our destination, as far as we can go this turn.
	 * <p>
	 * Also adds houses to the agent's cognitive environment. This is done by saving each coordinate the person passes,
	 * creating a polygon with a radius given by the "cognitive_map_search_radius" and adding all houses which touch the
	 * polygon.
	 * <p>
	 * Note: the agent might move their position many times depending on how far they are allowed to move each turn,
	 * this requires many calls to geometry.move(). This function could be improved (quite easily) by working out where
	 * the agent's final destination will be, then calling move() just once.
	 * 
	 * @param housesPassed
	 *            If not null then the buildings which the agent passed during their travels this iteration will be
	 *            calculated and stored in this array. This can be useful if a agent needs to know which houses it has
	 *            just passed and, therefore, which are possible victims. This isn't done by default because it's quite
	 *            an expensive operation (lots of geographic tests which must be carried out in each iteration). If the
	 *            array is null then the houses passed are not calculated.
	 * @return null or the buildings passed during this iteration if housesPassed boolean is true
	 * @throws Exception
	 */
	public void travel(AsymptomaticAgent agent) throws Exception {   		
//		agent.cishu++;
		//濞村鐦�   mr.wang
//		LOGGER.log(Level.INFO, "閺屻儳婀卆gent id"+ agent.getID() );
		
		// Check that the route has been created
		if (this.routeX == null) {
			this.setRoute();
			
		}
		try {
			if (this.atDestination()) {
				return ;
			}
			double time = System.nanoTime();
			
			// Store the roads the agent walks along (used to populate the awareness space)鐎涙ê鍋嶉弲楦垮厴娴ｆ挾绮℃潻鍥╂畱鐞涙浜�
			// List<Road> roadsPassed = new ArrayList<Road>();
			double distTravelled = 0; // The distance travelled so far 	閺呴缚鍏樻担鎾硅泲鏉╁洨娈戠捄婵堫瀲
			Coordinate currentCoord = null; // Current location 	瑜版挸澧犻崸鎰垼
			Coordinate target = null; // Target coordinate we're heading for (in route list)	閻╊喚娈戦崷鏉挎綏閺嶏拷
			boolean travelledMaxDist = false; // True when travelled maximum distance this iteration	閺勵垰鎯佺悰宀�绮￠張锟芥潻婊嗙獩缁傦拷
			double speed; // The speed to travel to next coord	閸掗绗呮稉锟芥稉顏勬綏閺嶅洨娈戦柅鐔峰   閿涘牊鐦℃稉顏勬綏閺嶅洨鍋ｉ柈鑺ユ箒閸欘垵鍏橀弨鐟板綁闁喎瀹抽敍锟�
			GeometryFactory geomFac = new GeometryFactory();	//閺屻儳婀呴惇瀣箹娑擃亞娈戦弸鍕拷鐘插毐閺侊拷
			while (!travelledMaxDist && !this.atDestination()) {
				target = this.routeX.get(this.currentPosition);
				speed = this.routeSpeedsX.get(this.currentPosition);     	
				// Work out the distance and angle to the next coordinate
				double[] distAndAngle = new double[2];
				Route.distance(currentCoord, target, distAndAngle);
				// divide by speed because distance might effectively be shorter

				double distToTarget = distAndAngle[0] / speed;//閻╃缍嬫禍锟� dist/speed
				// If we can get all the way to    the next coords on the route then just go there
				//婵″倹鐏夐幋鎴滄粦閸︺劏鐭剧痪澶哥瑐閼冲�熻泲閸掗绗呮稉锟芥稉顏勬綏閺嶅洤姘ㄩ崢濠氬亝闁诧拷
				if (distTravelled + distToTarget < GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					
					
					distTravelled += distToTarget;
					currentCoord = target;
//					
					// See if agent has reached the end of the route.
					
					if (this.currentPosition == (this.routeX.size() - 1)) {
						ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
						// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
						break; // Break out of while loop, have reached end of route.
					}
					// Haven't reached end of route, increment the counter
					this.currentPosition++;
				} // if can get all way to next coord

				// Check if dist to next coordinate is exactly same as maximum  濡拷閺屻儰绗呮稉锟芥稉顏勬綏閺嶅洨娈� dist 閺勵垰鎯佹稉搴㈡付婢堆冿拷鐓庣暚閸忋劎娴夐崥锟�
				// distance allowed to travel (unlikely but possible)閺勵垰鎯侀崚鎷屾彧閺堬拷鏉╂粏绐涚粋锟�
				else if (distTravelled + distToTarget == GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					travelledMaxDist = true;
					ContextManager.moveAgent(agent, geomFac.createPoint(target));
					// ContextManager.agentGeography.move(agent, geomFac.createPoint(target));
					this.currentPosition++;
					LOGGER.log(Level.WARNING, "Travel(): UNUSUAL CONDITION HAS OCCURED!");
				} else {
					// Otherwise move as far as we can towards the target along the road we're on.
					//閸氾箑鍨敍灞惧灉娴狀剙鏁栭崣顖濆厴鏉╂粌婀撮張婵堟絻閹存垳婊戦幍锟界挧鎵畱闁捁鐭炬稉濠勬畱閻╊喗鐖ｉ崜宥堢箻閵嗭拷
					// Move along the vector the maximum distance we're allowed this turn (take into account relative
					// speed)
					//濞岃法娼冮惌銏ゅ櫤缁夎濮╅幋鎴滄粦閸忎浇顔忛惃鍕付婢堆嗙獩缁備紮绱濇潻娆庨嚋閸ョ偛鎮庨懓鍐閸掓壆娴夌�靛綊锟界喎瀹抽敍锟�
					double distToTravel = (GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN - distTravelled) * speed;
					// Move the agent, first move them to the current coord (the first part of the while loop doesn't do
					// this for efficiency)
					//缁夎濮゛gent閿涳拷(妫ｆ牕鍘涚亸鍡楃暊娴狀剛些閸斻劌鍩岃ぐ鎾冲閸ф劖鐖ｉ敍瀵僪ile 瀵邦亞骞嗛惃鍕儑娑擄拷闁劌鍨庢稉宥勭窗娑撹桨绨￠弫鍫㈠芳閼板矁绻栭弽宄颁粵閿涳拷
					// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
					ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
					// Now move by vector towards target (calculated angle earlier).
					//閻滄澘婀柅姘崇箖閻垽鍣洪崥鎴犳窗閺嶅洨些閸旑煉绱欓崜宥夋桨鐠侊紕鐣婚惃鍕潡鎼达讣绱�
					ContextManager.moveAgentByVector(this.agent, distToTravel, distAndAngle[1]);
					// ContextManager.agentGeography.moveByVector(this.agent, distToTravel, distAndAngle[1]);

					travelledMaxDist = true;
				} // else
			} // while
			
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Route.trave(): Caught error travelling for " + this.agent.toString()
					+ " going to " + "destination "
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString() + ")"));
			throw e;
		} // catch exception
	}

	
	public void travel(InfectedAgent infectedAgent) throws Exception {
		
		


		
		// Check that the route has been created
		if (this.routeX == null) {
			this.setRoute();
			
		}
		try {
			if (this.atDestination()) {
				return ;
			}
			double time = System.nanoTime();
			
			// Store the roads the agent walks along (used to populate the awareness space)
			// List<Road> roadsPassed = new ArrayList<Road>();
			double distTravelled = 0; // The distance travelled so far 	
			Coordinate currentCoord = null; // Current location 	
			Coordinate target = null; // Target coordinate we're heading for (in route list)	
			boolean travelledMaxDist = false; // True when travelled maximum distance this iteration	
			double speed; // The speed to travel to next coord	
			GeometryFactory geomFac = new GeometryFactory();	
		

//			Geometry G = geomFac.toGeometry(null);	
			
			//获取agent经纬度
			currentCoord = ContextManager.getAgentGeometry(this.agent).getCoordinate();
			while (!travelledMaxDist && !this.atDestination()) {
				if (infectedAgent.getCustomSpeed()!=null)
				{
					this.currentSpeed=infectedAgent.getCustomSpeed();
				}
				target = this.routeX.get(this.currentPosition);
//				speed = this.routeSpeedsX.get(this.currentPosition)*infectedAgent.getCustomSpeed();
				speed=this.routeSpeedsX.get(this.currentPosition)*this.currentSpeed;
//				speed=timeToGo(speed,agent.getID()); 			
//				speed=changeSpeedInTrafficJam(speed,currentCoord,agent.getID());
				synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock)
				{
					//List humans = new CopyOnWriteArrayList();
					Geography geography = ContextManager.getAgentGeography();
					
					int yihu=(int)(ContextManager.infectedagentLatentCount*1/100+ContextManager.infectedagentOnsetCount*2/100+ContextManager.infectedagentSevereCount*6/100);
					if(yihu > ContextManager.MAssistanceThreshold && ContextManager.Medical == true) {
						ContextManager.Medical=false;
					}
					if(yihu <= ContextManager.MAssistanceThreshold && ContextManager.Medical == false) {
						ContextManager.Medical=true;
					}
//					double infectRadius = 5;
//					Geometry searchArea =  GeometryUtil.generateBuffer(geography, 
//								geography.getGeometry(infectedAgent), infectRadius);
//					Envelope searchEnvelope = searchArea.getEnvelopeInternal();
//					Iterable<MyAgent> nearObjects = geography.getObjectsWithin(searchEnvelope, MyAgent.class);
					Geometry firstCoordinate = null;
					
					double Count = 0;
					
					Geometry agentCoordinateGeometry = ContextManager.getAgentGeometry(this.agent);
					//buffer
					Geometry myBuffer = ContextManager.getAgentGeometry(this.agent).buffer(0.0001);
//					System.out.println("myBuffer"+"--"+myBuffer);
					
					synchronized(AgentContextLock) {
					Iterator<IAgent> iif;
//					Iterator<IAgent> i = ContextManager.getAllAgents().iterator();
					Iterator<IAgent> imy = ContextManager.getAllMyAgents().iterator();
					synchronized(ContextManager.infectedAgentContextLock) {
						 iif = ContextManager.getAllInfectedAgent().iterator();
					}
					
//					Iterator<IAgent> i = ContextManager.getAllAgents().iterator();
					
					ArrayList<MyAgent> nearObjectsMy=new ArrayList<>();
//					ArrayList<InfectedAgent> nearObjectsInfect=new ArrayList<>();

						
					System.out.println("imy.hasNext()"+""+imy.hasNext());
					while(imy.hasNext()){
						Count=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
//						System.out.println("imy中Count"+Count);
						MyAgent next = (MyAgent)imy.next();
						float ratio= 0;
						Geometry iAgentCoordinate = ContextManager.getAgentGeometry(next);
//						if(iAgentCoordinate == null) {
//							continue;
//						}
						if(myBuffer.contains(iAgentCoordinate)) {
											 
							if(next.getTickCount() == 0) {
								next.setTickCount(Count);
							}else if(Count-next.getTickCount() >= 50.0 ) {
									
								nearObjectsMy.add(next);
								 int infect = next.getInfectRatio() ;
//								 System.out.println("imy中有人进入范围的第一次感染率"+infect);
								 Random seed=new Random();
								 int ss=seed.nextInt(10);
								 if(next.getVaccine()) {
									 ratio=(float)ss/100*35;
								 }
								 if(next.getMasks())
								 {
									 ratio=(float)ss/100*20;
								 }
								 if(next.getAge()=="Young")
								 {
									 ratio=(float)ss/100*10;
								 }else {
									 ratio=(float)ss/100*80;
								 }
								 if(next.getRehabilitated())
								 {
									 ratio=(float)ss/100*70;
								 }
								 int retios = (int)ratio;
								if(!ContextManager.Medical)
								{
									retios=2*retios;
								}
//								System.out.println("MyAgent"+"--"+infect+retios);
								 next.setInfectRatio(infect+retios);
								}			
							}
						}
					System.out.println("iif.hasNext()"+""+iif.hasNext());
					while(iif.hasNext()){
						 Count=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
						InfectedAgent next = (InfectedAgent)iif.next();
						float ratio=0f;
						Geometry iAgentCoordinate = ContextManager.getAgentGeometry(next);
						if(iAgentCoordinate == null) {
							continue;
						}

						if(myBuffer.contains(iAgentCoordinate)) {
//							System.out.println("iif中有人进入范围");	
							if(next.getTickCount() == 0) {
								next.setTickCount(Count);
							}else if(Count-next.getTickCount() >= 50.0 ) {
								 int infect = next.getInfectRatio() ;
//								 System.out.println("iif中有人进入范围的第一次感染率"+infect);	
								 Random seed=new Random();
								 int ss=seed.nextInt(10);
								 if(next.getVaccine()) {
									 ratio=(float)ss/100*35;
								 }
								 if(next.getMasks())
								 {
									 ratio=(float)ss/100*20;
								 }
								 if(next.getAge()=="Young")
								 {
									 ratio=(float)ss/100*10;
								 }else {
									 ratio=(float)ss/100*80;
								 }
								 if(next.getRehabilitated())
								 {
									 ratio=(float)ss/100*70;
								 }
								 int retios = (int)ratio;
								if(!ContextManager.Medical)
								{
									retios=2*retios;
								}
//								System.out.println("InfectedAgent"+"--"+infect+retios);
								 next.setInfectRatio(infect+retios);

								}			
							}
						}
 
					
//					for (Object human : nearObjects){
//
//							humans.add(human);
//					}	
					

					if(nearObjectsMy.size() > 0) {

					for(int ii = 0;ii<nearObjectsMy.size();ii++){	
					MyAgent myAgent = nearObjectsMy.get(ii);	
					int infectRatio = myAgent.getInfectRatio();
					
//					if(ContextManager.Medical==true) {
//						infectRatio = infectRatio-30;
//					}
				
					Random r = new Random();
//					int i1 = r.nextInt(100);
//					if(i1<= infectRatio || i1>= 100) {
					if(infectRatio > 50) {
					
					if(myAgent.getRoute()!=null) {
					Building DestinationBuilding = myAgent.getRoute().getDestinationBuilding();
					Point JzPoint = ContextManager.getAgentGeometry(myAgent).getCentroid();
					String Age = myAgent.getAge();
					boolean Masks=myAgent.getMasks();
					boolean Vaccine=myAgent.getVaccine();
					boolean isRehabilitater=myAgent.getRehabilitated();
					
					Geometry myAgentGeo = geography.getGeometry(myAgent);
					
					synchronized(ContextManager.agentContext){
						myAgent.getRoute().forbid=true;
						ContextManager.agentContext.remove(myAgent);
						//如果正常人数>0就说明可以-1
						if (ContextManager.agentCount>0)
						{
							ContextManager.agentCount--;
						}
						ContextManager.agentnotAthome--;
						ContextManager.infectedagentnotAthome++;
						myAgent = null;
						
					}

					InfectedAgent infectAgent = new InfectedAgent();
					infectAgent.setAge(Age);
					infectAgent.setStage("latent period");
					ContextManager.infectedagentLatentCount++;//潜伏期+1
					infectAgent.setSwitchStageDay(r.nextInt(70));
					infectAgent.setstageTickCount(Count);
					infectAgent.setMasks(Masks);
					infectAgent.setVaccine(Vaccine);
					
					infectAgent.setInfectRatio(0);//
					//判断传染人数是否到达最大人数值
					if(ContextManager.infectedagentCount<ContextManager.AllAgentNumbers)
					{
						ContextManager.infectedagentCount++;	
					}
					Route JZroute = new Route(infectAgent, DestinationBuilding.getCoords(), DestinationBuilding);
					infectAgent.setRoute(JZroute);
					
					
					synchronized(ContextManager.agentContext){
//						ContextManager.agentContext.add(infectAgent);
						ContextManager.addAgentToContext(infectAgent);
					}
					
//							ContextManager.addAgentToContext(infectAgent);
					
//							geography.move(infectAgent, myAgentGeo);
					
					
					ContextManager.moveAgent(infectAgent,JzPoint);
//							context = ContextUtils.getContext(this);
//							geography = (Geography)context.getProjection("AgentGeography");
					}
					}
//					}
//					humans.clear();
				}
					}
				
					
				}//整出5.
			}
				
//				System.out.println(this.routeX.get(1)+"=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
//				System.out.println(this.routeX.get(0).getOrdinate(0)+"=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
				
//				濞村鐦�
//				for(int number = 0 ;number>=0;number++) {
//					System.out.println("agent娑撳绔村銉ㄨ泲閻ㄥ垕閸ф劖鐖�"+this.routeX.get(number).getOrdinate(0));
//					System.out.println("agent娑撳绔村銉ㄨ泲閻ㄥ墢閸ф劖鐖�"+this.routeX.get(number).getOrdinate(1));
//				    System.out.println(this.currentPosition);
					
//					NdPoint myPoint = agent.getSpace().getLocation(agent);
//					NdPoint otherPoint = new NdPoint(this.routeX.get(number).getOrdinate(0), this.routeX.get(number).getOrdinate(1));
//					double angle = SpatialMath.calcAngleFor2DMovement(agent.getSpace(), myPoint, otherPoint);
//					agent.getSpace().moveByVector(agent, 2, angle, 0);
//					myPoint = agent.getSpace().getLocation(agent);
//					agent.getGrid().moveTo(agent, (int)myPoint.getX(), (int)myPoint.getY());
//					System.out.println("agentID="+agent.getID());
//				}
				

				
				
				
				/*
				 * TODO Remember which roads have been passed, used to work out what should be added to cognitive map.
				 * Only add roads once the agent has moved all the way down them
				 */
				// roadsPassed.add(this.roads.get(this.previousRouteCoord()));
				// Work out the distance and angle to the next coordinate
//				double Count=RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				double[] distAndAngle = new double[2];
				Route.distance(currentCoord, target, distAndAngle);
				// divide by speed because distance might effectively be shorter
				double distToTarget = distAndAngle[0] / speed;//閻╃缍嬫禍锟� dist/speed
				
				// If we can get all the way to the next coords on the route then just go there
				//婵″倹鐏夐幋鎴滄粦閸︺劏鐭剧痪澶哥瑐閼冲�熻泲閸掗绗呮稉锟芥稉顏勬綏閺嶅洤姘ㄩ崢濠氬亝闁诧拷
				//distTravelled閺呴缚鍏樻担鎾硅泲鏉╁洨娈戠捄婵堫瀲
				if (distTravelled + distToTarget < GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					distTravelled += distToTarget;
					currentCoord = target;

					// See if agent has reached the end of the route.
					//閺屻儳婀卆gent閺勵垰鎯佸鎻掑煂鏉堟崘鐭鹃悽杈╂畱閺堫偄鐔�
					//routeX閺勵垰鐨㈢憰浣筋攽鐠ф壆娈戠捄顖溾柤閸ф劖鐖�
					if (this.currentPosition == (this.routeX.size() - 1)) {
						ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
						// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
//						System.out.println("infectedAgent"+infectedAgent.getID()+"瀹歌尙绮￠崚鎷屾彧閻╊喚娈戦崷锟�");
						break; // Break out of while loop, have reached end of route.
					}
					// Haven't reached end of route, increment the counter
					this.currentPosition++;
				} // if can get all way to next coord

				// Check if dist to next coordinate is exactly same as maximum  濡拷閺屻儰绗呮稉锟芥稉顏勬綏閺嶅洨娈� dist 閺勵垰鎯佹稉搴㈡付婢堆冿拷鐓庣暚閸忋劎娴夐崥锟�
				// distance allowed to travel (unlikely but possible)閺勵垰鎯侀崚鎷屾彧閺堬拷鏉╂粏绐涚粋锟�
				else if (distTravelled + distToTarget == GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					travelledMaxDist = true;//鐠囥儰璐焧rue鐞涖劎銇氬鑼病鐞涘矂鈹掗張锟芥潻婊嗙獩缁傦拷
					ContextManager.moveAgent(agent, geomFac.createPoint(target));
					// ContextManager.agentGeography.move(agent, geomFac.createPoint(target));
					this.currentPosition++;
					LOGGER.log(Level.WARNING, "Travel(): UNUSUAL CONDITION HAS OCCURED!");
				} else {	
					// Otherwise move as far as we can towards the target along the road we're on.
					//閸氾箑鍨敍灞惧灉娴狀剙鏁栭崣顖濆厴鏉╂粌婀撮張婵堟絻閹存垳婊戦幍锟界挧鎵畱闁捁鐭炬稉濠勬畱閻╊喗鐖ｉ崜宥堢箻閵嗭拷
					// Move along the vector the maximum distance we're allowed this turn (take into account relative
					// speed)
					//濞岃法娼冮惌銏ゅ櫤缁夎濮╅幋鎴滄粦閸忎浇顔忛惃鍕付婢堆嗙獩缁備紮绱濇潻娆庨嚋閸ョ偛鎮庨懓鍐閸掓壆娴夌�靛綊锟界喎瀹抽敍锟�
					double distToTravel = (GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN - distTravelled) * speed;
					// Move the agent, first move them to the current coord (the first part of the while loop doesn't do
					// this for efficiency)
					//缁夎濮゛gent閿涳拷(妫ｆ牕鍘涚亸鍡楃暊娴狀剛些閸斻劌鍩岃ぐ鎾冲閸ф劖鐖ｉ敍瀵僪ile 瀵邦亞骞嗛惃鍕儑娑擄拷闁劌鍨庢稉宥勭窗娑撹桨绨￠弫鍫㈠芳閼板矁绻栭弽宄颁粵閿涳拷
					// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
					ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
					// Now move by vector towards target (calculated angle earlier).
					//閻滄澘婀柅姘崇箖閻垽鍣洪崥鎴犳窗閺嶅洨些閸旑煉绱欓崜宥夋桨鐠侊紕鐣婚惃鍕潡鎼达讣绱�
					ContextManager.moveAgentByVector(this.agent, distToTravel, distAndAngle[1]);
					// ContextManager.agentGeography.moveByVector(this.agent, distToTravel, distAndAngle[1]);

					travelledMaxDist = true;
				} // else
			} // while
			
			//濞村鐦�
//			System.out.println("^^^^^^^^^^^^^^^");
//			this.printRoute();
//			System.out.println(this);
//			System.out.println(this.roadsX);
//			System.out.println(this.routeX);
//			System.out.println(this.routeSpeedsX);
//			System.out.println(this.currentPosition);
//			System.out.println("--------");

			/*
			 * TODO Agent has finished moving, now just add all the buildings and communities passed to their awareness
			 * space (unless they're on a transport route). Note also that if on a transport route without an associated
			 * road no roads are added to the 'roads' map so even if the check wasn't made here no buildings would be
			 * added anyway.
			 */
			// Community c = null;
			// if (!this.onTransportRoute) {
			// String outputString = "Route.travel() adding following to awareness space for '"
			// + this.agent.toString() + "':";
			// // roadsPassed will have duplicates, this is used to ignore them
			// Road current = roadsPassed.get(0);
			// // TODO The next stuff is a mess when it comes to adding communities to the memory. Need to go
			// // through and make sure communities aren't added too many times (i.e. more than once for each journey)
			// // and that they are always added when they should be.
			//
			// for (Road r : roadsPassed) { // last road in list is the one the
			// // agent finishes iteration on
			// if (r != null && roadsPassed.get(0) != null && !current.equals(r)) {
			// // Check road isn't null () and that buildings on road haven't already been added
			// // (road can be null when coords that aren't part of a road are added to the route)
			// current = r;
			// if (r.equals(this.previousRoad)) {
			// // The agent has just passed over this road, don't add the buildings or communities again
			// } else {
			// outputString += "\n\t" + r.toString() + ": ";
			// List<Building> passedBuildings = getBuildingsOnRoad(r);
			// List<Community> passedCommunities = new ArrayList<Community>();
			// if (passedBuildings != null) { // There might not be any buildings close to the road (unlikely)
			// outputString += passedBuildings.toString();
			// this.passedObjects(passedBuildings, Building.class);
			// // For efficiency just find one of the building's communities and hope no other
			// // communities were passed through - NO! I'VE CHANGED THIS BELOW!
			// c = passedBuildings.get(0).getCommunity();
			// // Check all buildings to make sure that if the agent has passed more than one community
			// // then they are all added.
			// for (Building b : passedBuildings) {
			// if (!passedCommunities.contains(b.getCommunity())) {
			// passedCommunities.add(b.getCommunity());
			// }
			// }
			// for (Community com : passedCommunities) {
			// if (com != null) {
			// this.passedObject(com, Community.class);
			// }
			// }
			//
			// } else { // Community won't have been added because no buildings passed, use slow method
			// c = GlobalVars.COMMUNITY_ENVIRONMENT.getObjectAt(Community.class, currentCoord);
			// if (c != null) {
			// this.passedObject(c, Community.class);
			// }
			// // TODO I think the following line is wrong, if the agent has made
			// // a long move they might have passed right through a community that doesn't
			// // have any buildings, perhaps this should check *all* the communities that touch
			// // the road, not just the community the agent finished the move in (i.e. currentCoord)
			// passedCommunities.add(GlobalVars.COMMUNITY_ENVIRONMENT.getObjectAt(Community.class,
			// currentCoord));
			// }
			// }
			// }
			// } // for roadsPassed
			// TempLogger.out(outputString + "\n");
			// } // if !onTransportRoute
			// else {
			// TempLogger.out("Route.travel() not adding to burglar '" + this.agent.toString()
			// + "' awareness space beecause on transport route: ");
			// }
			//
			// // Finally set the previousRoad and previousCommunity so that if these haven't changed in the next
			// iteration they're not added to
			// // the cognitive map again.
			// this.previousRoad = roadsPassed.get(roadsPassed.size() - 1);
			// // this.previousCommunity = c; // This was the most recent community passed over
			//
			// TempLogger.out("...Finished Travelling(" + (0.000001 * (System.nanoTime() - time)) + "ms)");
			// // } // synchronized GlobalVars.TRANSPORT_PARAMS.currentBurglar
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Route.trave(): Caught error travelling for " + this.agent.toString()
					+ " going to " + "destination "
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString() + ")"));
			throw e;
		} // catch exception
	}
	
	//鐞涘矁铔嬮弬瑙勭《    閸欏倹鏆烡efaultAgent閹广垺鍨歁yAgent 
	public void travel(MyAgent agent) throws Exception {   		
		
		// Check that the route has been created
		if (this.routeX == null) {
			this.setRoute();
			
		}
		try {
			if (this.atDestination()) {
				return ;
			}
			double time = System.nanoTime();
			
			// Store the roads the agent walks along (used to populate the awareness space)鐎涙ê鍋嶉弲楦垮厴娴ｆ挾绮℃潻鍥╂畱鐞涙浜�
			// List<Road> roadsPassed = new ArrayList<Road>();
			double distTravelled = 0; // The distance travelled so far 	閺呴缚鍏樻担鎾硅泲鏉╁洨娈戠捄婵堫瀲
			Coordinate currentCoord = null; // Current location 	瑜版挸澧犻崸鎰垼
			Coordinate target = null; // Target coordinate we're heading for (in route list)	閻╊喚娈戦崷鏉挎綏閺嶏拷
			boolean travelledMaxDist = false; // True when travelled maximum distance this iteration	閺勵垰鎯佺悰宀�绮￠張锟芥潻婊嗙獩缁傦拷
			double speed; // The speed to travel to next coord	閸掗绗呮稉锟芥稉顏勬綏閺嶅洨娈戦柅鐔峰   閿涘牊鐦℃稉顏勬綏閺嶅洨鍋ｉ柈鑺ユ箒閸欘垵鍏橀弨鐟板綁闁喎瀹抽敍锟�
			GeometryFactory geomFac = new GeometryFactory();	//閺屻儳婀呴惇瀣箹娑擃亞娈戦弸鍕拷鐘插毐閺侊拷
			
			//濞村鐦疓eometry  Mr.wang
//			Geometry G = geomFac.toGeometry(null);	
			//鏉╂瑦鐗遍惄瀛樺复鏉烆剙绨茬拠銉︽Ц閸欘垯浜掗惃锟�   鏉╂瑩鍣烽惃鍕棘閺佹壆绮伴惃鍕珶閻ｅ奔璐焠ull鎼存棁顕氱亸杈ㄦЦ閹靛墽娈戦弫缈犻嚋鐎电钖勯惃鍕敶鐎圭櫢绱濋幋鎴犳箙娑撳﹪娼伴懢宄板絿閻ㄥ嚕eometry鐎电钖勯柈鑺ユЦ閻╁瓨甯撮幏璺ㄦ畱   濞屸剝婀侀惇瀣煂new閸戠儤娼甸惃锟�
			
			currentCoord = ContextManager.getAgentGeometry(this.agent).getCoordinate();
			
			//濞村鐦�
//			System.out.println(geomFac.getCoordinateSequenceFactory());
//			System.out.println();
			
			//濞村鐦�   mr.wang
//			LOGGER.log(Level.INFO, "閺屻儳婀卆gent currentCoord"+ currentCoord );
			//
			while (!travelledMaxDist && !this.atDestination()) {
				if (agent.getCustomSpeed()!=null){
					this.currentSpeed=agent.getCustomSpeed();
				}
				target = this.routeX.get(this.currentPosition);
				speed = (this.routeSpeedsX.get(this.currentPosition))*this.currentSpeed;
				/*
				 * TODO Remember which roads have been passed, used to work out what should be added to cognitive map.
				 * Only add roads once the agent has moved all the way down them
				 */
				// roadsPassed.add(this.roads.get(this.previousRouteCoord()));
				// Work out the distance and angle to the next coordinate
				double[] distAndAngle = new double[2];
				//计算到下一个坐标的距离和角度
				//在本程序中distAndAngle[0]->是距离 distanceAndAngle[1]->是角度
				Route.distance(currentCoord, target, distAndAngle);
				// divide by speed because distance might effectively be shorter
				double distToTarget = distAndAngle[0] / speed;//閻╃缍嬫禍锟� dist/speed
				// If we can get all the way to the next coords on the route then just go there
				//婵″倹鐏夐幋鎴滄粦閸︺劏鐭剧痪澶哥瑐閼冲�熻泲閸掗绗呮稉锟芥稉顏勬綏閺嶅洤姘ㄩ崢濠氬亝闁诧拷
				if (distTravelled + distToTarget < GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					
					
					distTravelled += distToTarget;
					currentCoord = target;
//					LOGGER.log(Level.INFO, "閸欐垹鏁撶粔璇插З");
					// See if agent has reached the end of the route.
					//閺屻儳婀卆gent閺勵垰鎯佸鎻掑煂鏉堟崘鐭鹃悽杈╂畱閺堫偄鐔�
					if (this.currentPosition == (this.routeX.size() - 1)) {
						ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
						// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
						break; // Break out of while loop, have reached end of route.
					}
					// Haven't reached end of route, increment the counter
					this.currentPosition++;
				} // if can get all way to next coord

				// Check if dist to next coordinate is exactly same as maximum  濡拷閺屻儰绗呮稉锟芥稉顏勬綏閺嶅洨娈� dist 閺勵垰鎯佹稉搴㈡付婢堆冿拷鐓庣暚閸忋劎娴夐崥锟�
				// distance allowed to travel (unlikely but possible)閺勵垰鎯侀崚鎷屾彧閺堬拷鏉╂粏绐涚粋锟�
				else if (distTravelled + distToTarget == GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					travelledMaxDist = true;
					ContextManager.moveAgent(agent, geomFac.createPoint(target));
					// ContextManager.agentGeography.move(agent, geomFac.createPoint(target));
					this.currentPosition++;
					LOGGER.log(Level.WARNING, "Travel(): UNUSUAL CONDITION HAS OCCURED!");
				} else {
					// Otherwise move as far as we can towards the target along the road we're on.
					//閸氾箑鍨敍灞惧灉娴狀剙鏁栭崣顖濆厴鏉╂粌婀撮張婵堟絻閹存垳婊戦幍锟界挧鎵畱闁捁鐭炬稉濠勬畱閻╊喗鐖ｉ崜宥堢箻閵嗭拷
					// Move along the vector the maximum distance we're allowed this turn (take into account relative
					// speed)
					//濞岃法娼冮惌銏ゅ櫤缁夎濮╅幋鎴滄粦閸忎浇顔忛惃鍕付婢堆嗙獩缁備紮绱濇潻娆庨嚋閸ョ偛鎮庨懓鍐閸掓壆娴夌�靛綊锟界喎瀹抽敍锟�
					double distToTravel = (GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN - distTravelled) * speed;
					// Move the agent, first move them to the current coord (the first part of the while loop doesn't do
					// this for efficiency)
					//缁夎濮゛gent閿涳拷(妫ｆ牕鍘涚亸鍡楃暊娴狀剛些閸斻劌鍩岃ぐ鎾冲閸ф劖鐖ｉ敍瀵僪ile 瀵邦亞骞嗛惃鍕儑娑擄拷闁劌鍨庢稉宥勭窗娑撹桨绨￠弫鍫㈠芳閼板矁绻栭弽宄颁粵閿涳拷
					// ContextManager.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
					ContextManager.moveAgent(this.agent, geomFac.createPoint(currentCoord));
					// Now move by vector towards target (calculated angle earlier).
					//閻滄澘婀柅姘崇箖閻垽鍣洪崥鎴犳窗閺嶅洨些閸旑煉绱欓崜宥夋桨鐠侊紕鐣婚惃鍕潡鎼达讣绱�
					ContextManager.moveAgentByVector(this.agent, distToTravel, distAndAngle[1]);
					// ContextManager.agentGeography.moveByVector(this.agent, distToTravel, distAndAngle[1]);

					travelledMaxDist = true;
				} // else
			} // while
			

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Route.trave(): Caught error travelling for " + this.agent.toString()
					+ " going to " + "destination "
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString() + ")"));
			throw e;
		} // catch exception
	}

	private GeometryFactory Geometry(GeometryFactory geomFacSecond) {
		// TODO Auto-generated method stub
		return null;
	}

	private Coordinate Coordinate(double currentx, double currenty) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 閼惧嘲褰囩挧椋庡仯閸滃瞼娲伴惃鍕勾娑斿妫块惃鍕獩缁備紮绱欓崷銊х秹缂佹粈绗傞敍锟�
	 * Get the distance (on a network) between the origin and destination. Take into account the Burglar because they
	 * might be able to speed up the route by using different transport methods. Actually calculates the distance
	 * between the nearest Junctions between the source and destination. Note that the GRID environment doesn't have any
	 * transport routes in it so all distances will always be the same regardless of the agent.
	 * 
	 * @param agent
	 * @param destination
	 * @return
	 */
	public double getDistance(IAgent theBurglar, Coordinate origin, Coordinate destination) {

		// // See if this distance has already been calculated
		// if (Route.routeDistanceCache == null) {
		// Route.routeDistanceCache = new Hashtable<CachedRouteDistance, Double>();
		// }
		// CachedRouteDistance crd = new CachedRouteDistance(origin, destination, theBurglar.getTransportAvailable());
		//
		// synchronized (Route.routeDistanceCache) {
		// Double dist = Route.routeDistanceCache.get(crd);
		// if (dist != null) {
		// TempLogger.out("Route.ggetDistance, found a cached route distance from " + origin + " to "
		// + destination + " using available transport " + theBurglar.getTransportAvailable()
		// + ", returning it.");
		// return dist;
		// }
		// }
		// No distance in the cache, calculate it
		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			GlobalVars.TRANSPORT_PARAMS.currentAgent = theBurglar;
			// Find the closest Junctions to the origin and destination
			double minOriginDist = Double.MAX_VALUE;
			double minDestDist = Double.MAX_VALUE;
			double dist;
			Junction closestOriginJunc = null;
			Junction closestDestJunc = null;
			DistanceOp distOp = null;
			GeometryFactory geomFac = new GeometryFactory();
			// TODO EFFICIENCY: here could iterate over near junctions instead of all?
			for (Junction j : ContextManager.junctionContext.getObjects(Junction.class)) {
				// Check that the agent can actually get to the junction (if might be part of a transport route
				// that the agent doesn't have access to) 
				//濡拷閺屻儰鍞悶鍡樻Ц閸氾箑鐤勯梽鍛讲娴犮儱鍩屾潏鍙ユ唉濮瑰洨鍋ｉ敍鍫濐洤閺嬫粌褰查懗鑺ユЦ娴狅絿鎮婇弮鐘虫綀鐠佸潡妫堕惃鍕炊鏉堟捁鐭鹃悽杈╂畱娑擄拷闁劌鍨庨敍锟�
				boolean accessibleJunction = false; 
				accessibleJunc: for (RepastEdge<Junction> e : ContextManager.roadNetwork.getEdges(j)) {
					NetworkEdge<Junction> edge = (NetworkEdge<Junction>) e;
					for (String s : edge.getTypes()) {
						if (theBurglar.getTransportAvailable().contains(s)) {
							accessibleJunction = true;
							break accessibleJunc;
						}
					} // for types
				}// for edges
				if (!accessibleJunction) { // Agent can't get to the junction, ignore it
					continue;
				}
				Point juncPoint = geomFac.createPoint(j.getCoords());

				distOp = new DistanceOp(juncPoint, geomFac.createPoint(origin));
				dist = distOp.distance();
				if (dist < minOriginDist) {
					minOriginDist = dist;
					closestOriginJunc = j;
				}
				// Destination
				distOp = new DistanceOp(juncPoint, geomFac.createPoint(destination));
				dist = distOp.distance();
				if (dist < minDestDist) {
					minDestDist = dist;
					closestDestJunc = j;
				}
			} // for Junctions
				// Return the shortest path plus the distance from the origin/destination to their junctions
				// TODO NOTE: Bug in ShortestPath so have to make finalize is called, otherwise following lines are
				// neater
				// - MAYBE THIS HAS BEEN FIXED BY REPAST NOW.
				// return (new ShortestPath<Junction>(EnvironmentFactory.getRoadNetwork(),
				// closestOriginJunc)).getPathLength(closestDestJunc)+ minOriginDist + minDestDist ;
				// TODO : using non-deprecated methods don't work on NGS, probably need to update repast libraries
			ShortestPath<Junction> p = new ShortestPath<Junction>(ContextManager.roadNetwork, closestOriginJunc);
			double theDist = p.getPathLength(closestDestJunc);
			// ShortestPath<Junction> p = new
			// ShortestPath<Junction>(EnvironmentFactory.getRoadNetwork());
			// double theDist = p.getPathLength(closestOriginJunc,closestDestJunc);
			p.finalize();
			p = null;
			double finalDist = theDist + minOriginDist + minDestDist;
			// // Cache this distance
			// synchronized (Route.routeDistanceCache) {
			// Route.routeDistanceCache.put(crd, finalDist);
			// }
			return finalDist;
		} // synchronized

	}

	/**
	 * Find the nearest coordinate which is part of a Road. Returns the coordinate which is actually the closest to the
	 * given coord, not just the corner of the segment which is closest. Uses the DistanceOp class which finds the
	 * closest points between two geometries.
	 * <p>
	 * When first called, the function will populate the 'nearestRoadCoordCache' which calculates where the closest road
	 * coordinate is to each building. The agents will commonly start journeys from within buildings so this will
	 * improve efficiency.
	 * </p>
	 * 
	 * @param inCoord
	 *            The coordinate from which to find the nearest road coordinate
	 * @return the nearest road coordinate
	 * @throws Exception
	 */
	private synchronized Coordinate getNearestRoadCoord(Coordinate inCoord) throws Exception {
		// double time = System.nanoTime();

		synchronized (buildingsOnRoadCacheLock) {
			if (nearestRoadCoordCache == null) {
				LOGGER.log(Level.FINE, "Route.getNearestRoadCoord called for first time, "
						+ "creating cache of all roads and the buildings which are on them ...");
				// Create a new cache object, this will be read from disk if
				// possible (which is why the getInstance() method is used
				// instead of the constructor.
				String gisDir = ContextManager.getProperty(GlobalVars.GISDataDirectory);
				File buildingsFile = new File(gisDir + ContextManager.getProperty(GlobalVars.BuildingShapefile));
				File roadsFile = new File(gisDir + ContextManager.getProperty(GlobalVars.RoadShapefile));
				File serialisedLoc = new File(gisDir + ContextManager.getProperty(GlobalVars.BuildingsRoadsCoordsCache));

				nearestRoadCoordCache = NearestRoadCoordCache.getInstance(ContextManager.buildingProjection,
						buildingsFile, ContextManager.roadProjection, roadsFile, serialisedLoc, new GeometryFactory());
			} // if not cached
		} // synchronized
		return nearestRoadCoordCache.get(inCoord);
	}

	/**
	 * Finds the shortest route between multiple origin and destination junctions. Will return the shortest path and
	 * also, via two parameters, can return the origin and destination junctions which make up the shortest route.
	 * 
	 * 
	 * @param currentJunctions
	 *            An array of origin junctions
	 * @param destJunctions
	 *            An array of destination junctions
	 * @param routeEndpoints
	 *            An array of size 2 which can be used to store the origin (index 0) and destination (index 1) Junctions
	 *            which form the endpoints of the shortest route.
	 * @return the shortest route between the origin and destination junctions
	 * @throws Exception
	 */
	private List<RepastEdge<Junction>> getShortestRoute(List<Junction> currentJunctions, List<Junction> destJunctions,
			Junction[] routeEndpoints) throws Exception {
		double time = System.nanoTime();
		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			// This must be set so that NetworkEdge.getWeight() can adjust the weight depending on how this
			// particular agent is getting around the city
			GlobalVars.TRANSPORT_PARAMS.currentAgent = this.agent;
			double shortestPathLength = Double.MAX_VALUE;
			double pathLength = 0;
			ShortestPath<Junction> p;
			List<RepastEdge<Junction>> shortestPath = null;
			for (Junction o : currentJunctions) {
//				System.out.println("O Coord ToString = " + o.toString());
//				System.out.println ("id:"+""+o.getId() +"Coordinate"+ ""+o.getCoords()+"List<Road>"+""+o.getRoads().toString());//Mr.wang
				for (Junction d : destJunctions) {
//					System.out.println ("id:"+""+o.getId() +"Coordinate"+ ""+o.getCoords()+"List<Road>"+""+o.getRoads().toString());//Mr.wang
					if (o == null || d == null) {
						LOGGER.log(Level.WARNING, "Route.getShortestRoute() error: either the destination or origin "
								+ "junction is null. This can be caused by disconnected roads. It's probably OK"
								+ "to ignore this as a route should still be created anyway.");
						System.out.println("Road List hava a Null");
					} else {
						p = new ShortestPath<Junction>(ContextManager.roadNetwork);
						pathLength = p.getPathLength(o,d);	//OD点路网之间的长度
		
//						System.out.println("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
//						System.out.println("pahLengthOfOD = " + pathLength);
//						System.out.println("shortestPathLength = " + shortestPathLength);
						if (pathLength < shortestPathLength) {
							shortestPathLength = pathLength;
							shortestPath = p.getPath(o,d);		//最短路径的坐标点集合
//							System.out.println("currentJunction:"+o.toString()+" "+"destJuncation:"+d.toString());
//							System.out.println("shortestPath enter the if = " + shortestPath.toString());
//							ShortestPath<Junction> p2 = new ShortestPath<Junction>(ContextManager.roadNetwork);
//							shortestPath = p2.getPath(o, d);
//							p2.finalize();
//							p2 = null;
							// shortestPath = p1.getPath(o, d);
							// p1.finalize(); p1 = null;
							routeEndpoints[0] = o;
							routeEndpoints[1] = d;
						}
						// TODO See if the shortestpath bug has been fixed, would make this unnecessary
						p.finalize();
						p = null;
					} // if junc null
				} // for dest junctions
			} // for origin junctions
//			if(shortestPath == null)
//			{
////				shortestPath = currentJunctions.get(0);
//			}
			
			

			if (shortestPath == null) {
//				System.out.println("asd1111"+this.toString());
				String debugString = "Route.getShortestRoute() could not find a route. Looking for the shortest route between :\n";
				for (Junction j : currentJunctions)
					debugString += "\t" + j.toString() + ", roads: " + j.getRoads().toString() + "CurrentJunctions DebugString" + "\n";
				for (Junction j : destJunctions)
					debugString += "\t" + j.toString() + ", roads: " + j.getRoads().toString() + "DestJunctions DebugString" + "\n";
				throw new RoutingException(debugString);
			}
			LOGGER.log(Level.FINER, "Route.getShortestRoute (" + (0.000001 * (System.nanoTime() - time))
					+ "ms) found shortest path " + "(length: " + shortestPathLength + ") from "
					+ routeEndpoints[0].toString() + " to " + routeEndpoints[1].toString());
			
//			System.out.println("return shortestPath"+" "+shortestPath);
//			System.out.println("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
			return shortestPath;
		} // synchronized
	}

	/**
	 * 鐠侊紕鐣诲▽璺ㄦ絻缂佹瑥鐣剧捄顖氱窞鐏忓棙娅ら懗鎴掔秼娴犲骸缍嬮崜宥勭秴缂冾喚些閸斻劌鍩岄惄顔炬畱閸︾増澧嶉棁锟介惃鍕綏閺嶏拷
	 * Calculates the coordinates required to move an agent from their current position to the destination along a given
	 * road. The algorithm to do this is as follows:
	 * <ol>
	 * <li>Starting from the destination coordinate, record each vertex and check inside the booundary of each line
	 * segment until the destination point is found.娴犲海娲伴惃鍕勾閻ㄥ嫬娼楅弽鍥风礉鐠佹澘缍嶅В蹇庨嚋妞ゅ墎鍋ｉ崪灞绢梾閺屻儱婀В蹇庨嚋缁炬寧顔岄惃鍕珶閻ｅ瞼娲块崚鎵窗閻ㄥ嫬婀寸悮顐㈠絺閻滐拷</li>
	 * <li>Return all but the last vertex, this is the route to the destination.鏉╂柨娲栭梽銈勭啊閺堬拷閸氬簼绔存稉顏堛�婇悙鐧哥礉鏉╂瑦妲搁崚鎷屾彧閻╊喚娈戦崷鎵畱鐠侯垰绶�</li>
	 * </ol>
	 * A boolean allows for two cases: heading towards a junction (the endpoint of the line) or heading away from the
	 * endpoint of the line (this function can't be used to go to two midpoints on a line)
	 * 娑擄拷娑擃亜绔风亸鏂匡拷纭风礉閸掋倖鏌囨稉銈囶潚閹懎鍠岄敍姘宠泲閸氭垳绔存稉顏囩熅閸欙綇绱欓惄瀵稿殠閻ㄥ嫮顏悙鐧哥礆閹存牜顬囧锟介惄瀵稿殠閻ㄥ嫮顏悙鐧哥礄鏉╂瑤閲滈崙鑺ユ殶娑撳秷鍏樼悮顐″▏閻€劎鏁ら崢璁崇鐞涘奔琚辨稉顓犲仯閿涳拷
	 * 
	 * @param currentCoord
	 * @param destinationCoord
	 * @param road
	 * @param toJunction
	 *            whether or not we're travelling towards or away from a Junction閹存垳婊戦弰顖氭儊濮濓絽婀挧鏉挎倻閹存牞绻欑粋璁崇娑擃亣鐭鹃崣锟�
	 * @param coordList
	 *            A list which will be populated with the coordinates that the agent should follow to move along the
	 *            road.閺呴缚鍏樻担鎾圭熅缁捐儻銆�.
	 * @param roadList
	 *            A list of roads associated with each coordinate.
	 * @throws Exception
	 */
//	getCoordsAlongRoad(currentCoord, currentJunction.getCoords(), currentRoad, true, tempCoordList);
	private void getCoordsAlongRoad(Coordinate currentCoord, Coordinate destinationCoord, Road road,
			boolean toJunction, List<Coordinate> coordList) throws RoutingException {

		//閸掋倖鏌囬崣鍌涙殶閺勵垰鎯佸鍌氱埗
		Route.checkNotNull(currentCoord, destinationCoord, road, coordList);

		double time = System.nanoTime();
		Coordinate[] roadCoords = ContextManager.roadProjection.getGeometry(road).getCoordinates();

		// Check that the either the destination or current coordinate are actually part of the road濡拷閺屻儳娲伴惃鍕勾閹存牕缍嬮崜宥呮綏閺嶅洤鐤勯梽鍛瑐閺勵垶浜剧捄顖滄畱娑擄拷闁劌鍨�
		boolean currentCorrect = false, destinationCorrect = false;
		for (int i = 0; i < roadCoords.length; i++) {
			if (toJunction && destinationCoord.equals(roadCoords[i])) {
				destinationCorrect = true;
				break;
			} else if (!toJunction && currentCoord.equals(roadCoords[i])) {
				currentCorrect = true;
				break;
			}
		} // for

		if (!(destinationCorrect || currentCorrect)) {
			String roadCoordsString = "";
			for (Coordinate c : roadCoords)
				roadCoordsString += c.toString() + " - ";
			throw new RoutingException("Neigher the origin or destination nor the current"
					+ "coordinate are part of the road '" + road.toString() + "' (person '" + this.agent.toString()
					+ "').\n" + "Road coords: " + roadCoordsString + "\n" + "\tOrigin: " + currentCoord.toString()
					+ "\n" + "\tDestination: " + destinationCoord.toString() + " ( "
					+ this.destinationBuilding.toString() + " )\n " + "Heading " + (toJunction ? "to" : "away from")
					+ " a junction, so " + (toJunction ? "destination" : "origin")
					+ " should be part of a road segment");
		}

		// Might need to reverse the order of the road coordinates閸欘垵鍏橀棁锟界憰渚�顤呴崐鎺椾壕鐠侯垰娼楅弽鍥╂畱妞ゅ搫绨�
		if (toJunction && !destinationCoord.equals(roadCoords[roadCoords.length - 1])) {
			// If heading towards a junction, destination coordinate must be at end of road segment婵″倹鐏夐張婵嗘倻娑擄拷娑擃亣鐭鹃崣锝忕礉閻╊喚娈戦崷鏉挎綏閺嶅洤绻�妞よ婀柆鎾圭熅濞堢數娈戦張顐ゎ伂
			ArrayUtils.reverse(roadCoords);
		} else if (!toJunction && !currentCoord.equals(roadCoords[0])) {
			// If heading away form junction current coord must be at beginning of road segment婵″倹鐏夋潻婊咁瀲缂佹挾鍋ｈ箛鍛淬�忛崷銊ュ彆鐠侯垱顔岄惃鍕磻婵拷
			ArrayUtils.reverse(roadCoords);
		}
		GeometryFactory geomFac = new GeometryFactory();
		Point destinationPointGeom = geomFac.createPoint(destinationCoord);
		Point currentPointGeom = geomFac.createPoint(currentCoord);
		// If still false at end then algorithm hasn't worked婵″倹鐏夐崷銊︽付閸氬簼绮涢悞绂橝LSE閿涘瞼鐣诲▔鏇熺梾閺堝浼愭担锟�
		boolean foundAllCoords = false;
		search: for (int i = 0; i < roadCoords.length - 1; i++) {
			Coordinate[] segmentCoords = new Coordinate[] { roadCoords[i], roadCoords[i + 1] };
			// Draw a small buffer around the line segment and look for the coordinate within the buffer閸︺劎鍤庡▓闈涙噯閸ュ鏁炬稉锟芥稉顏勭毈缂傛挸鍟块崠鐚寸礉閺屻儲澹樼紓鎾冲暱閸栧搫鍞撮惃鍕綏閺嶏拷
//			createLineString閼惧嘲褰囨稉銈勯嚋閹存牕顦挎稉顏冪秴缂冾喖鑻熼崚娑樼紦閻╃绨查惃鍕攽鐎涙顑佹稉鑼剁箾閹恒儱鐣犳禒顒冪箲閸ユ看ineString(閺勵垯绔存稉顏冪缂佹潙顕挒鈽呯礉鐞涖劎銇氭稉锟界化璇插灙閻愮懓鎷版潻鐐村复鏉╂瑤绨洪悙鍦畱缁炬寧顔�)
//			buffer缂傛挸鍟块崠鍝勫瀻閺嬫劧绱濈拋锛勭暬閸戠姳缍嶇�电钖勯惃鍕处閸愭彃灏�,閸欏倹鏆熸稉鍝勫殤娴ｆ洖顕挒鈥虫嫲缂傛挸鍟块崠楦跨獩缁備紮绱濋崡鏇氱秴娑撳骸婀撮崶鎯у礋娴ｅ秶娴夐崥宀冪箲閸ョ偟绱﹂崘鎻掑隘閸戠姳缍嶇�电钖�
			
			//Mr.wang  createLineString閺傝纭堕崣鍌涙殶娣団晝顫掗幆鍛枌閿涘奔绔寸粔宀皁ordinate[]閸欙缚绔寸粔宀皁ordinateSequence
			//createLineString 娴ｈ法鏁ょ紒娆忕暰閸ф劖鐖ｉ崚娑樼紦缁惧じ瑕嗛妴鍌溾敄閺佹壆绮嶉幋鏍敄閺佹壆绮嶇亸鍡楀灡瀵よ櫣鈹栫悰灞界摟缁楋缚瑕嗛妴锟�
			Geometry buffer = geomFac.createLineString(segmentCoords).buffer(GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.SMALL.dist);

			//濞村鐦�  Mr.wang
//			System.out.println("----------------------------------------------------------------------------------------------------");
//			System.out.println("buffer"+buffer);
//			System.out.println("buffer.buffer"+buffer.buffer(30));
//			System.out.println("buffer.buffer"+buffer.getInteriorPoint());
//			System.out.println("buffer.buffer"+buffer.getUserData());

			if (!toJunction) {
				/* If heading away from a junction, keep adding road coords until we find the destination */
				coordList.add(roadCoords[i]);
				if (destinationPointGeom.within(buffer)) {
					coordList.add(destinationCoord);
					foundAllCoords = true;
					break search;
				}
			} else if (toJunction) {
				/*
				 * If heading towards a junction: find the curent coord, add it to the route, then add all the remaining
				 * coords which make up the road segment閸旂姴鍙嗛幍锟介張澶婂⒖娑撳娈戦崸鎰垼缂佸嫭鍨氱捄顖涱唽
				 */
				if (currentPointGeom.within(buffer)) {
					for (int j = i + 1; j < roadCoords.length; j++) {
						coordList.add(roadCoords[j]);
					}
					coordList.add(destinationCoord);
					foundAllCoords = true;
					break search;
				}
			}
		} // for
		if (foundAllCoords) {
			LOGGER.log(Level.FINER, "getCoordsAlongRoad (" + (0.000001 * (System.nanoTime() - time)) + "ms)");
			return;
		} else { // If we get here then the route hasn't been created
			// A load of debugging info
			String error = "Route: getCoordsAlongRoad: could not find destination coordinates "
					+ "along the road.\n\tHeading *" + (toJunction ? "towards" : "away from")
					+ "* a junction.\n\t Person: " + this.agent.toString() + ")\n\tDestination building: "
					+ destinationBuilding.toString() + "\n\tRoad causing problems: " + road.toString()
					+ "\n\tRoad vertex coordinates: " + Arrays.toString(roadCoords);
			throw new RoutingException(error);
			/*
			 * Hack: ignore the error, printing a message and just returning the origin destination and coordinates.
			 * This means agent will jump to/from the junction but I can't figure out why the fuck it occasionally
			 * doesn't work!! It's so rare that hopefully this isn't a problem.
			 */
			// TempLogger.err("Route: getCoordsAlongRoad: error... (not debugging).");
			// List<Coord> coords = new ArrayList<Coord>();
			// coords.add(currentCoord);
			// coords.add(destinationCoord);
			// for (Coord c : coords)
			// this.roads.put(c, road); // Remember the roads each coord is
			// // part of
			// return coords;

		}
	}

	private static void checkNotNull(Object... args) throws RoutingException {
		for (Object o : args) {
			if (o == null) {
				throw new RoutingException("An input argument is null");
			}
		}
		return;
	}

	/**
	 * Returns all the coordinates that describe how to travel along a path, restricted to road coordinates. In some
	 * cases the route wont have an associated road, this occurs if the route is part of a transport network. In this
	 * case just the origin and destination coordinates are added to the route.
	 * 
	 * 鏉╂柨娲栭幓蹇氬牚婵″倷缍嶅▽鑳熅瀵板嫯顢戞潻娑氭畱閹碉拷閺堝娼楅弽鍥风礉娴犲懘妾烘禍搴ㄤ壕鐠侯垰娼楅弽鍥ワ拷锟�
	 * 閸︺劋绔存禍娑橆洤閺嬫粏鐭剧痪鎸庣梾閺堝鍙ч懕鏃傛畱闁捁鐭鹃敍灞筋洤閺嬫粏鐭剧痪鎸庢Ц鏉╂劘绶純鎴犵捕閻ㄥ嫪绔撮柈銊ュ瀻閿涘苯鍨导姘絺閻㈢喕绻栫粔宥嗗剰閸愮偣锟斤拷
	 * 閸︺劍顒濇俊鍌涚亯閸欘亝妲哥亸鍡氭崳閻愮懓鎷伴惄顔炬畱閸︽澘娼楅弽鍥ㄥ潑閸旂姴鍩岀捄顖滃殠娑擃厹锟斤拷
	 * 
	 * @param shortestPath
	 * @param startingJunction
	 *            The junction the path starts from, this is required so that the algorithm knows which road coordinate
	 *            to add first (could be first or last depending on the order that the road coordinates are stored
	 *            internally).
	 * @return the coordinates as a mapping between the coord and its associated speed (i.e. how fast the agent can
	 *         travel to the next coord) which is dependent on the type of edge and the agent (e.g.
	 *         driving/walking/bus). LinkedHashMap is used to guarantee the insertion order of the coords is maintained.
	 * @throws RoutingException
	 */
	private void getRouteBetweenJunctions(List<RepastEdge<Junction>> shortestPath, Junction startingJunction)
			throws RoutingException {
		
//		LOGGER.log(Level.INFO, "shortestPath " + "(NetworkEdge<Junction>)shortestPath.get(1)");
//		System.out.println("shortestPath"+ (NetworkEdge<Junction>)shortestPath.get(1));
		
		double time = System.nanoTime();
		if (shortestPath.size() < 1) {
			// This could happen if the agent's destination is on the same road
			// as the origin
			return;
		}
		// Lock the currentAgent so that NetworkEdge obejcts know what speed to use (depends on transport available to
		// the specific agent).
		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			GlobalVars.TRANSPORT_PARAMS.currentAgent = this.agent;

			// Iterate over all edges in the route adding coords and weights as appropriate
			NetworkEdge<Junction> e;
			Road r;
			// Use sourceFirst to represent whether or not the edge's source does actually represent the start of the
			// edge (agent could be going 'forwards' or 'backwards' over edge
			boolean sourceFirst;
			for (int i = 0; i < shortestPath.size(); i++) {
				e = (NetworkEdge<Junction>) shortestPath.get(i);
				
				//濞村鐦�
//				System.out.println(e); //閹躲儵鏁� e.access缂傚搫銇�  瀹歌弓鎱ㄩ弨鐟般偨
				if (i == 0) {
					// No coords in route yet, compare the source to the starting junction
					sourceFirst = (e.getSource().equals(startingJunction)) ? true : false;
				} else {
					// Otherwise compare the source to the last coord added to the list
					sourceFirst = (e.getSource().getCoords().equals(this.routeX.get(this.routeX.size() - 1))) ? true
							: false;
				}
				

				/*
				 * Now add the coordinates describing how to move along the road. If there is no road associated with
				 * the edge (i.e. it is a transport route) then just add the source/dest coords. Note that the shared
				 * coordinates between two edges will be added twice, these must be removed later
				 */
				r = e.getRoad();
				//濞村鐦�
//				System.out.println(r);
//				System.out.println();
				/*
				 * Get the speed that the agent will be able to travel along this edge (depends on the transport
				 * available to the agent and the edge). Some speeds will be < 1 if the agent shouldn't be using this
				 * edge but doesn't have any other way of getting to the destination. in these cases set speed to 1
				 * (equivalent to walking).
				 */
				double speed = e.getSpeed();
				//濞村鐦�
//				System.out.println(speed);
				
				if (speed < 1)
					speed = 1;

				if (r == null) { // No road associated with this edge (it is a
									// transport link) so just add source
					if (sourceFirst) {
						this.addToRoute(e.getSource().getCoords(), r, speed, "getRouteBetweenJunctions - no road");
						this.addToRoute(e.getTarget().getCoords(), r, -1, "getRouteBetweenJunctions - no road");
						// (Note speed = -1 used because we don't know the weight to the next
						// coordinate - this can be removed later)
					} else {
						this.addToRoute(e.getTarget().getCoords(), r, speed, "getRouteBetweenJunctions - no road");
						this.addToRoute(e.getSource().getCoords(), r, -1, "getRouteBetweenJunctions - no road");
					}
				} else {
					// This edge is a road, add all the coords which make up its geometry
					Coordinate[] roadCoords = ContextManager.roadProjection.getGeometry(r).getCoordinates();
					if (roadCoords.length < 2)
						throw new RoutingException("Route.getRouteBetweenJunctions: for some reason road " + "'"
								+ r.toString() + "' doesn't have at least two coords as part of its geometry ("
								+ roadCoords.length + ")");
					// Make sure the coordinates of the road are added in the correct order
					if (!sourceFirst) {
						ArrayUtils.reverse(roadCoords);
					}
					// Add all the road geometry's coords
					for (int j = 0; j < roadCoords.length; j++) {
						this.addToRoute(roadCoords[j], r, speed, "getRouteBetweenJuctions - on road");
						// (Note that last coord will have wrong weight)
					} // for roadCoords.length
				} // if road!=null
			}
			// Check all lists are still the same size.
			assert this.roadsX.size() == this.routeX.size()
					&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
					&& this.roadsX.size() == this.routeDescriptionX.size();

			// Check all lists are still the same size.
			assert this.roadsX.size() == this.routeX.size()
					&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
					&& this.roadsX.size() == this.routeDescriptionX.size();

			// Finished!
			LOGGER.log(Level.FINER, "getRouteBetweenJunctions (" + (0.000001 * (System.nanoTime() - time)) + "ms");
			return;
		} // synchronized
	} // getRouteBetweenJunctions

	/**
	 * Determine whether or not the person associated with this Route is at their destination. Compares their current
	 * coordinates to the destination coordinates (must be an exact match).
	 * 
	 * @return True if the person is at their destination
	 */
	public boolean atDestination() {
//		try{
//		System.out.println("agent瑜版挸澧犻崸鎰垼= "+ContextManager.getAgentGeometry(this.agent).getCoordinate());
//		System.out.println("this.destination= "+this.destination);
//		
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		if(this.forbid ==true) { //娑撳瓨妞傚ǎ璇插 Mr.wang
		return  true;
		}
			
		
		return ContextManager.getAgentGeometry(this.agent).getCoordinate().equals(this.destination);	//閼惧嘲褰囬惄顔炬畱閸︽壆濮搁幀锟�
//		return ContextManager.getAgentGeometry(this.agent).getCoordinate().x == this.destination.x
//			&& ContextManager.getAgentGeometry(this.agent).getCoordinate().y == this.destination.y
//			&& ContextManager.getAgentGeometry(this.agent).getCoordinate().z == this.destination.z;
		
	}

	// /**
	// * Removes any duplicate coordinates from the curent route (coordinates which
	// * are the same *and* next to each other in the list).
	// * <p>
	// * If my route-generating algorithm was better this would't be necessary.
	// */
	// @Deprecated
	// private void removePairs() throws RoutingException {
	// if (this.routeX.size() < 1) {
	// // No coords to iterate over, probably something has gone wrong
	// throw new RoutingException("Route.removeDuplicateCoordinates(): WARNING an empty list has been "
	// + "passed to this function, something has probably gone wrong");
	// }
	// TempLogger.out("ROUTE BEFORE REMOVING PAIRS");
	// this.printRoute();
	//
	// // (setRoute() has already checked that lists are same size)
	//
	// // Iterate over the list, removing coordinates that are the same as their neighbours.
	// // (and associated objects in other lists)
	// Iterator<Road> roadIt = this.roadsX.iterator();
	// Iterator<Coordinate> routeIt = this.routeX.iterator();
	// Iterator<Double> routeSpeedIt = this.routeSpeedsX.iterator();
	// Iterator<String> routeDescIt = this.routeDescriptionX.iterator();
	// Coordinate c1, c2;
	// Road currentRoad = roadIt.next();
	// Road nextRoad = null;
	// routeIt.next(); routeSpeedIt.next(); routeDescIt.next();
	// while ( roadIt.hasNext() ) {
	// nextRoad = roadIt.next();
	// routeIt.next();
	// routeSpeedIt.next();
	// routeDescIt.next();
	//
	// c1 = currentRoad.getCoords();
	// c2 = nextRoad.getCoords();
	//
	// if (c1.equals(c2)) {
	// // Remove objects from the lists
	// roadIt.remove();
	// routeIt.remove();
	// routeSpeedIt.remove();
	// routeDescIt.remove();
	// }
	// else {
	// currentRoad = nextRoad;
	// }
	// }
	//
	// TempLogger.out("ROUTE AFTER REMOVING PAIRS");
	// this.printRoute();
	// }

	private void printRoute() {
		StringBuilder out = new StringBuilder();
		out.append("Printing route (" + this.agent.toString() + "). Current position in list is "
				+ this.currentPosition + " ('" + this.routeDescriptionX.get(this.currentPosition) + "')");
		for (int i = 0; i < this.routeX.size(); i++) {
			out.append("\t(" + this.agent.toString() + ") " + this.routeX.get(i).toString() + "\t"
					+ this.routeSpeedsX.get(i).toString() + "\t" + this.roadsX.get(i) + "\t"
					+ this.routeDescriptionX.get(i));
		}
		LOGGER.info(out.toString());
	}

	
	/**
	 * Find the nearest object in the given geography to the coordinate.
	 * 
	 * @param <T>
	 * @param x
	 *            The coordinate to search from
	 * @param geography
	 *            The given geography to look through
	 * @param closestPoints
	 *            An optional List that will be populated with the closest points to x (i.e. the results of
	 *            <code>distanceOp.closestPoints()</code>.
	 * @param searchDist
	 *            The maximum distance to search for objects in. Small distances are more efficient but larger ones are
	 *            less likely to find no objects.
	 * @return The nearest object.
	 * @throws RoutingException
	 *             If an object cannot be found.
	 */
//	Road destRoad = Route.findNearestObject(destCoord, ContextManager.roadProjection, null,GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.SMALL);
	public static synchronized <T> T findNearestObject(Coordinate x, Geography<T> geography,
			List<Coordinate> closestPoints, GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE searchDist)
			throws RoutingException {
		if (x == null) {
			throw new RoutingException("The input coordinate is null, cannot find the nearest object");
		}

		T nearestObject = SpatialIndexManager.findNearestObject(geography, x, closestPoints, searchDist);

		// Old way without using spatial index:
		//
		// GeometryFactory geomFac = new GeometryFactory();
		// Point point = geomFac.createPoint(x);
		// // TODO Use an expanding buffer that starts small but gets bigger if no object is found.
		//
		// Geometry buffer = point.buffer(searchDist.dist);
		// double minDist = Double.MAX_VALUE;
		// T nearestObject = null;
		// for (T t : geography.getObjectsWithin(buffer.getEnvelopeInternal())) {
		// DistanceOp distOp = new DistanceOp(point, geography.getGeometry(t));
		// double thisDist = distOp.distance();
		// if (thisDist < minDist) {
		// minDist = thisDist;
		// nearestObject = t;
		// // Optionally record the closest points
		// if (closestPoints != null) {
		// closestPoints.clear();
		// // TODO clean conversion of array to List (don't have access
		// // to internet!)
		// Coordinate[] crds = distOp.closestPoints();
		// List<Coordinate> temp = new ArrayList(crds.length);
		// for (Coordinate c : crds)
		// temp.add(c);
		// closestPoints.addAll(temp);
		// }
		// } // if thisDist < minDist
		// } // for nearRoads
		if (nearestObject == null) {
			throw new RoutingException("Couldn't find an object close to these coordinates:\n\t" + x.toString());
		} else {
			return nearestObject;
		}
	}

	/**
	 * Returns the angle of the vector from p0 to p1 relative to the x axis
	 * <p>
	 * The angle will be between -Pi and Pi. I got this directly from the JUMP program source.
	 * 
	 * @return the angle (in radians) that p0p1 makes with the positive x-axis.
	 */
	public static synchronized double angle(Coordinate p0, Coordinate p1) {
		double dx = p1.x - p0.x;
		double dy = p1.y - p0.y;

		return Math.atan2(dy, dx);
	}

	/**
	 * The building which this Route is targeting
	 * 
	 * @return the destinationHouse
	 */
	public Building getDestinationBuilding() {
		if (this.destinationBuilding == null) {
			LOGGER.log(Level.WARNING, "Route: getDestinationBuilding(), warning, no destination building has "
					+ "been set. This might be ok, the agent might be supposed to be heading to a coordinate "
					+ "not a particular building(?)");
			return null;
		}
		return destinationBuilding;
	}

	/**
	 * The coordinate the route is targeting
	 * 
	 * @return the destination
	 */
	public Coordinate getDestination() {
		return this.destination;
	}

	/**
	 * Maintain a cache of all coordinates which are part of a road segment. Store the coords and all the road(s) they
	 * are part of.
	 * 
	 * @param coord
	 *            The coordinate which should be part of a road geometry
	 * @return The road(s) which the coordinate is part of or null if the coordinate is not part of any road
	 */
	private List<Road> getRoadFromCoordCache(Coordinate coord) {

		populateCoordCache(); // Check the cache has been populated
		return coordCache.get(coord);
	}

	/**
	 * Test if a coordinate is part of a road segment.
	 * 
	 * @param coord
	 *            The coordinate which we want to test
	 * @return True if the coordinate is part of a road segment
	 */
	private boolean coordOnRoad(Coordinate coord) {
		populateCoordCache(); // check the cache has been populated 濡拷閺屻儳绱︾�涙ɑ妲搁崥锕�鍑＄紒蹇擄綖閸忥拷
		return coordCache.containsKey(coord);
	}

	private synchronized static void populateCoordCache() {

		double time = System.nanoTime();
		if (coordCache == null) { // Fist check cache has been created
			coordCache = new HashMap<Coordinate, List<Road>>();
			LOGGER.log(Level.FINER,
					"Route.populateCoordCache called for first time, creating new cache of all Road coordinates.");
		}
		if (coordCache.size() == 0) { // Now popualte it if it hasn't already
										// been populated
			LOGGER.log(Level.FINER, "Route.populateCoordCache: is empty, creating new cache of all Road coordinates.");

			for (Road r : ContextManager.roadContext.getObjects(Road.class)) {
//				System.out.println("Road鐎圭偘缍�"+ r.toString());//瑜般垹顩� Road鐎圭偘缍媟oad: 8true
				for (Coordinate c : ContextManager.roadProjection.getGeometry(r).getCoordinates()) {
//					System.out.println("Road闂嗗棗鎮�"+ c);//瑜般垹顩�  Road闂嗗棗鎮�(114.2627725, 30.5718991, NaN)
					if (coordCache.containsKey(c)) {
						coordCache.get(c).add(r);
					} else {
						List<Road> l = new ArrayList<Road>();
						l.add(r);
						// TODO Need to put *new* coordinate here? Not use
						// existing one in memory?
						coordCache.put(new Coordinate(c), l);
					}
				}
			}

			LOGGER.log(Level.FINER, "... finished caching all road coordinates (in " + 0.000001
					* (System.nanoTime() - time) + "ms)");
		}
	}

	/**
	 * Find the buildings which can be accessed from the given road (the given road is the closest to the buildings).
	 * Uses a separate cache object which can be serialised so that the cache doesn't need to be rebuilt every time.
	 * 
	 * @param road
	 * @return
	 * @throws Exception
	 */
	private List<Building> getBuildingsOnRoad(Road road) throws Exception {
		if (buildingsOnRoadCache == null) {
			LOGGER.log(Level.FINER, "Route.getBuildingsOnRoad called for first time, "
					+ "creating cache of all roads and the buildings which are on them ...");
			// Create a new cache object, this will be read from disk if possible (which is why the
			// getInstance() method is used instead of the constructor.
			String gisDir = GlobalVars.GISDataDirectory;
			File buildingsFile = new File(gisDir + GlobalVars.BuildingShapefile);
			File roadsFile = new File(gisDir + GlobalVars.RoadShapefile);
			File serialLoc = new File(gisDir + ContextManager.getProperty(GlobalVars.BuildingsRoadsCache));
			buildingsOnRoadCache = BuildingsOnRoadCache.getInstance(ContextManager.buildingProjection, buildingsFile,
					ContextManager.roadProjection, roadsFile, serialLoc, new GeometryFactory());
		} // if not cached
		return buildingsOnRoadCache.get(road);
	}

	private static double testDistance(double x1, double x2, double y1, double y2){
		double res = 0;
		if(x1 > x2)
			res += x1-x2;
		else
			res += x2-x1;
		if(y1 > y2)
			res += y1-y2;
		else
			res += y2-y1;
		return res;
		//return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
	}
	/**
	 * Calculate the distance (in meters) between two Coordinates, using the coordinate reference system that the
	 * roadGeography is using. For efficiency it can return the angle as well (in the range -0 to 2PI) if returnVals
	 * passed in as a double[2] (the distance is stored in index 0 and angle stored in index 1).
	 * 
	 * @param c1
	 * @param c2
	 * @param returnVals
	 *            Used to return both the distance and the angle between the two Coordinates. If null then the distance
	 *            is just returned, otherwise this array is populated with the distance at index 0 and the angle at
	 *            index 1.
	 * @return The distance between Coordinates c1 and c2.
	 */
	public static synchronized double distance(Coordinate c1, Coordinate c2, double[] returnVals) {
		// TODO check this now, might be different way of getting distance in new Simphony
		GeodeticCalculator calculator = new GeodeticCalculator(ContextManager.roadProjection.getCRS());
		calculator.setStartingGeographicPoint(c1.x, c1.y);
		calculator.setDestinationGeographicPoint(c2.x, c2.y);
//		Ellipsoid ellipsoid = calculator.getEllipsoid();
		double distance = 0;
		if(testDistance(c1.x,c2.x,c1.y,c2.y) < 0.00001)
			distance = 0;
		else if(c1.equals(c2))
			distance = 0;
		else{
			distance = calculator.getOrthodromicDistance();
		}
		if (returnVals != null && returnVals.length == 2) {
			returnVals[0] = distance;
			double angle = Math.toRadians(calculator.getAzimuth()); // Angle in range -PI to PI
			// Need to transform azimuth (in range -180 -> 180 and where 0 points north)
			// to standard mathematical (range 0 -> 360 and 90 points north)
			if (angle > 0 && angle < 0.5 * Math.PI) { // NE Quadrant
				angle = 0.5 * Math.PI - angle;
			} else if (angle >= 0.5 * Math.PI) { // SE Quadrant
				angle = (-angle) + 2.5 * Math.PI;
			} else if (angle < 0 && angle > -0.5 * Math.PI) { // NW Quadrant
				angle = (-1 * angle) + 0.5 * Math.PI;
			} else { // SW Quadrant
				angle = -angle + 0.5 * Math.PI;
			}
			returnVals[1] = angle;
		}
		return distance;
	}

	/**
	 * Converts a distance lat/long distance (e.g. returned by DistanceOp) to meters. The calculation isn't very
	 * accurate because (probably) it assumes that the distance is between two points that lie exactly on a line of
	 * longitude (i.e. one is exactly due north of the other). For this reason the value shouldn't be used in any
	 * calculations which is why it's returned as a String.
	 * 
	 * @param dist
	 *            The distance (as returned by DistanceOp) to convert to meters
	 * @return The approximate distance in meters as a String (to discourage using this approximate value in
	 *         calculations).
	 * @throws Exception
	 * @see com.vividsolutions.jts.operation.distance.DistanceOp
	 */
	public static synchronized String distanceToMeters(double dist) throws Exception {
		// Works by creating two coords (close to a randomly chosen object) which are a certain distance apart
		// then using similar method as other distance() function
		GeodeticCalculator calculator = new GeodeticCalculator(ContextManager.roadProjection.getCRS());
		Coordinate c1 = ContextManager.buildingContext.getRandomObject().getCoords();
		calculator.setStartingGeographicPoint(c1.x, c1.y);
		calculator.setDestinationGeographicPoint(c1.x, c1.y + dist);
		return String.valueOf(calculator.getOrthodromicDistance());
	}

	public void clearCaches() {
		if (coordCache != null)
			coordCache.clear();
		if (nearestRoadCoordCache != null) {
			nearestRoadCoordCache.clear();
			nearestRoadCoordCache = null;
		}
		if (buildingsOnRoadCache != null) {
			buildingsOnRoadCache.clear();
			buildingsOnRoadCache = null;
		}
		// if (routeCache != null) {
		// routeCache.clear();
		// routeCache = null;
		// }
		// if (routeDistanceCache != null) {
		// routeDistanceCache.clear();
		// routeDistanceCache = null;
		// }
	}

	// /**
	// * Will add the given buildings to the awareness space of the Burglar who is
	// * being controlled by this Route. Also tells the burglar which buildings
	// * have been passed if appropriate, this is needed for agents who are
	// * currently looking for a burglary target.
	// *
	// * @param buildings
	// * A list of buildings
	// */
	// @SuppressWarnings("unchecked")
	// protected <T> void passedObjects(List<T> objects, Class<T> clazz) {
	// this.agent.addToMemory(objects, clazz);
	// if (clazz.isAssignableFrom(Building.class)) {
	// // System.out.println("Route.passedObjects(): "+objects.toString());
	// this.agent.buildingsPassed((List<Building>) objects);
	// }
	// }

	/**
	 * Will add the given buildings to the awareness space of the Burglar who is being controlled by this Route.
	 * 
	 * @param buildings
	 *            A list of buildings
	 */
	protected <T> void passedObject(T object, Class<T> clazz) {
		List<T> list = new ArrayList<T>(1);
		list.add(object);
		this.agent.addToMemory(list, clazz);
	}

}

/* ************************************************************************ 娴犮儰绗傞柈鑺ユЦRoute*/

/**
 * Class can be used to store a cache of all roads and the buildings which can be accessed by them (a map of
 * Road<->List<Building>. Buildings are 'accessed' by travelling to the road which is nearest to them.
 * <p>
 * This class can be serialised so that if the GIS data doesn't change it doesn't have to be re-calculated each time.
 * However, the Roads and Buildings themselves cannot be serialised because if they are there will be two sets of Roads
 * and BUildings, the serialised ones and those that were created when the model was initialised. To get round this, an
 * array which contains the road and building ids is serialised and the cache is re-built using these caches ids after
 * reading the serialised cache. This means that the id's given to Buildings and Roads must not change (i.e.
 * auto-increment numbers are no good because if a simulation is restarted the static auto-increment variables will not
 * be reset to 0).
 * 
 * @author Nick Malleson
 */
class BuildingsOnRoadCache implements Serializable {

	private static Logger LOGGER = Logger.getLogger(BuildingsOnRoadCache.class.getName());

	private static final long serialVersionUID = 1L;
	// The actual cache, this isn't serialised
	private static transient Hashtable<Road, ArrayList<Building>> theCache;
	// The 'reference' cache, stores the building and road ids and can be
	// serialised
	private Hashtable<String, ArrayList<String>> referenceCache;

	// Check that the road/building data hasn't been changed since the cache was
	// last created
	private File buildingsFile;
	private File roadsFile;
	// The location that the serialised object might be found.
	private File serialisedLoc;
	// The time that this cache was created, can be used to check data hasn't
	// changed since
	private long createdTime;

	// Private constructor because getInstance() should be used
	private BuildingsOnRoadCache(Geography<Building> buildingEnvironment, File buildingsFile,
			Geography<Road> roadEnvironment, File roadsFile, File serialisedLoc, GeometryFactory geomFac)
			throws Exception {
		// this.buildingEnvironment = buildingEnvironment;
		// this.roadEnvironment = roadEnvironment;
		this.buildingsFile = buildingsFile;
		this.roadsFile = roadsFile;
		this.serialisedLoc = serialisedLoc;
		theCache = new Hashtable<Road, ArrayList<Building>>();
		this.referenceCache = new Hashtable<String, ArrayList<String>>();

		LOGGER.log(Level.FINE, "BuildingsOnRoadCache() creating new cache with data (and modification date):\n\t"
				+ this.buildingsFile.getAbsolutePath() + " (" + new Date(this.buildingsFile.lastModified()) + ")\n\t"
				+ this.roadsFile.getAbsolutePath() + " (" + new Date(this.roadsFile.lastModified()) + ")\n\t"
				+ this.serialisedLoc.getAbsolutePath());

		populateCache(buildingEnvironment, roadEnvironment, geomFac);
		this.createdTime = new Date().getTime();
		serialise();
	}

	public void clear() {
		theCache.clear();
		this.referenceCache.clear();

	}

	private void populateCache(Geography<Building> buildingEnvironment, Geography<Road> roadEnvironment,
			GeometryFactory geomFac) throws Exception {
		double time = System.nanoTime();
		for (Building b : buildingEnvironment.getAllObjects()) {
			// Find the closest road to this building
			Geometry buildingPoint = geomFac.createPoint(b.getCoords());
			double minDistance = Double.MAX_VALUE;
			Road closestRoad = null;
			double distance;
			Envelope e = buildingPoint.buffer(GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.LARGE.dist)
					.getEnvelopeInternal();
			for (Road r : roadEnvironment.getObjectsWithin(e)) {
				distance = DistanceOp.distance(buildingPoint, ContextManager.roadProjection.getGeometry(r));
				if (distance < minDistance) {
					minDistance = distance;
					closestRoad = r;
				}
			} // for roads
				// Found the closest road, add the information to the cache
			if (theCache.containsKey(closestRoad)) {
				theCache.get(closestRoad).add(b);
				this.referenceCache.get(closestRoad.getIdentifier()).add(b.getIdentifier());
			} else {
				ArrayList<Building> l = new ArrayList<Building>();
				l.add(b);
				theCache.put(closestRoad, l);
				ArrayList<String> l2 = new ArrayList<String>();
				l2.add(b.getIdentifier());
				this.referenceCache.put(closestRoad.getIdentifier(), l2);
			}
		} // for buildings
		int numRoads = theCache.keySet().size();
		int numBuildings = 0;
		for (List<Building> l : theCache.values())
			numBuildings += l.size();
		LOGGER.log(Level.FINER, "Finished caching roads and buildings. Cached " + numRoads + " roads and "
				+ numBuildings + " buildings in " + 0.000001 * (System.nanoTime() - time) + "ms");
	}

	public List<Building> get(Road r) {
		return theCache.get(r);
	}

	private void serialise() throws IOException {
		double time = System.nanoTime();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			if (!this.serialisedLoc.exists())
				this.serialisedLoc.createNewFile();
			fos = new FileOutputStream(this.serialisedLoc);
			out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		} catch (IOException ex) {
			if (serialisedLoc.exists())
				serialisedLoc.delete(); // delete to stop problems loading incomplete file next time
			throw ex;
		}
		LOGGER.log(Level.FINER, "Serialised BuildingsOnRoadCache to " + this.serialisedLoc.getAbsolutePath() + " in ("
				+ 0.000001 * (System.nanoTime() - time) + "ms)");
	}

	/**
	 * Used to create a new BuildingsOnRoadCache object. This function is used instead of the constructor directly so
	 * that the class can check if there is a serialised version on disk already. If not then a new one is created and
	 * returned.
	 * 
	 * @param buildingEnv
	 * @param buildingsFile
	 * @param roadEnv
	 * @param roadsFile
	 * @param serialisedLoc
	 * @param geomFac
	 * @return
	 * @throws Exception
	 */
	public synchronized static BuildingsOnRoadCache getInstance(Geography<Building> buildingEnv, File buildingsFile,
			Geography<Road> roadEnv, File roadsFile, File serialisedLoc, GeometryFactory geomFac) throws Exception {
		double time = System.nanoTime();
		// See if there is a cache object on disk.
		if (serialisedLoc.exists()) {
			FileInputStream fis = null;
			ObjectInputStream in = null;
			BuildingsOnRoadCache bc = null;
			try {
				fis = new FileInputStream(serialisedLoc);
				in = new ObjectInputStream(fis);
				bc = (BuildingsOnRoadCache) in.readObject();
				in.close();

				// Check that the cache is representing the correct data and the
				// modification dates are ok
				// (WARNING, if this class is re-compiled the serialised object
				// will still be read in).
				if (!buildingsFile.getAbsolutePath().equals(bc.buildingsFile.getAbsolutePath())
						|| !roadsFile.getAbsolutePath().equals(bc.roadsFile.getAbsolutePath())
						|| buildingsFile.lastModified() > bc.createdTime || roadsFile.lastModified() > bc.createdTime) {
					LOGGER.log(Level.FINER, "BuildingsOnRoadCache, found serialised object but it doesn't match the "
							+ "data (or could have different modification dates), will create a new cache.");
				} else {
					// Have found a useable serialised cache. Now use the cached
					// list of id's to construct a
					// new cache of buildings and roads.
					// First need to buld list of existing roads and buildings
					Hashtable<String, Road> allRoads = new Hashtable<String, Road>();
					for (Road r : roadEnv.getAllObjects())
						allRoads.put(r.getIdentifier(), r);
					Hashtable<String, Building> allBuildings = new Hashtable<String, Building>();
					for (Building b : buildingEnv.getAllObjects())
						allBuildings.put(b.getIdentifier(), b);

					// Now create the new cache
					theCache = new Hashtable<Road, ArrayList<Building>>();

					for (String roadId : bc.referenceCache.keySet()) {
						ArrayList<Building> buildings = new ArrayList<Building>();
						for (String buildingId : bc.referenceCache.get(roadId)) {
							buildings.add(allBuildings.get(buildingId));
						}
						theCache.put(allRoads.get(roadId), buildings);
					}
					LOGGER.log(Level.FINER, "BuildingsOnRoadCache, found serialised cache, returning it (in "
							+ 0.000001 * (System.nanoTime() - time) + "ms)");
					return bc;
				}
			} catch (IOException ex) {
				if (serialisedLoc.exists())
					serialisedLoc.delete(); // delete to stop problems loading incomplete file next tinme
				throw ex;
			} catch (ClassNotFoundException ex) {
				if (serialisedLoc.exists())
					serialisedLoc.delete();
				throw ex;
			}

		}

		// No serialised object, or got an error when opening it, just create a
		// new one
		return new BuildingsOnRoadCache(buildingEnv, buildingsFile, roadEnv, roadsFile, serialisedLoc, geomFac);
	}
}

/* ************************************************************************ */

/**
 * Caches the nearest road Coordinate to every building for efficiency (agents usually/always need to get from the
 * centroids of houses to/from the nearest road).
 * <p>
 * This class can be serialised so that if the GIS data doesn't change it doesn't have to be re-calculated each time.
 * 
 * @author Nick Malleson
 */
class NearestRoadCoordCache implements Serializable {

	private static Logger LOGGER = Logger.getLogger(NearestRoadCoordCache.class.getName());

	private static final long serialVersionUID = 1L;
	private Hashtable<Coordinate, Coordinate> theCache; // The actual cache
	// Check that the road/building data hasn't been changed since the cache was
	// last created
	private File buildingsFile;
	private File roadsFile;
	// The location that the serialised object might be found.
	private File serialisedLoc;
	// The time that this cache was created, can be used to check data hasn't
	// changed since
	private long createdTime;

	private GeometryFactory geomFac;

	private NearestRoadCoordCache(Geography<Building> buildingEnvironment, File buildingsFile,
			Geography<Road> roadEnvironment, File roadsFile, File serialisedLoc, GeometryFactory geomFac)
			throws Exception {

		this.buildingsFile = buildingsFile;
		this.roadsFile = roadsFile;
		this.serialisedLoc = serialisedLoc;
		this.theCache = new Hashtable<Coordinate, Coordinate>();
		this.geomFac = geomFac;

		LOGGER.log(Level.FINE, "NearestRoadCoordCache() creating new cache with data (and modification date):\n\t"
				+ this.buildingsFile.getAbsolutePath() + " (" + new Date(this.buildingsFile.lastModified()) + ") \n\t"
				+ this.roadsFile.getAbsolutePath() + " (" + new Date(this.roadsFile.lastModified()) + "):\n\t"
				+ this.serialisedLoc.getAbsolutePath());

		populateCache(buildingEnvironment, roadEnvironment);
		this.createdTime = new Date().getTime();
		serialise();
	}

	public void clear() {
		this.theCache.clear();
	}

	private void populateCache(Geography<Building> buildingEnvironment, Geography<Road> roadEnvironment)
			throws Exception {
		double time = System.nanoTime();
		theCache = new Hashtable<Coordinate, Coordinate>();
		// Iterate over every building and find the nearest road point
		for (Building b : buildingEnvironment.getAllObjects()) {
			List<Coordinate> nearestCoords = new ArrayList<Coordinate>();
			Route.findNearestObject(b.getCoords(), roadEnvironment, nearestCoords,
					GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.LARGE);
			// Two coordinates returned by closestPoints(), need to find the one
			// which isn't the building coord
			Coordinate nearestPoint = null;
			for (Coordinate c : nearestCoords) {
				if (!c.equals(b.getCoords())) {
					nearestPoint = c;
					break;
				}
			} // for nearestCoords
			if (nearestPoint == null) {
				throw new Exception("Route.getNearestRoadCoord() error: couldn't find a road coordinate which "
						+ "is close to building " + b.toString());
			}
			theCache.put(b.getCoords(), nearestPoint);
		}// for Buildings
		LOGGER.log(Level.FINER, "Finished caching nearest roads (" + (0.000001 * (System.nanoTime() - time)) + "ms)");
	} // if nearestRoadCoordCache = null;

	/**
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public Coordinate get(Coordinate c) throws Exception {
		if (c == null) {
			throw new Exception("Route.NearestRoadCoordCache.get() error: the given coordinate is null.");
		}
		double time = System.nanoTime();
		Coordinate nearestCoord = this.theCache.get(c);
		if (nearestCoord != null) {
			LOGGER.log(Level.FINER, "NearestRoadCoordCache.get() (using cache) - ("
					+ (0.000001 * (System.nanoTime() - time)) + "ms)");
			return nearestCoord;
		}
		// If get here then the coord is not in the cache, agent not starting their journey from a house, search for
		// it manually. Search all roads in the vicinity, looking for the point which is nearest the person
		double minDist = Double.MAX_VALUE;
		Coordinate nearestPoint = null;
		Point coordGeom = this.geomFac.createPoint(c);

		// Note: could use an expanding envelope that starts small and gets bigger
		double bufferDist = GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.LARGE.dist;
		double bufferMultiplier = 1.0;
		Envelope searchEnvelope = coordGeom.buffer(bufferDist * bufferMultiplier).getEnvelopeInternal();
		StringBuilder debug = new StringBuilder(); // incase the operation fails

		for (Road r : ContextManager.roadProjection.getObjectsWithin(searchEnvelope)) {

			DistanceOp distOp = new DistanceOp(coordGeom, ContextManager.roadProjection.getGeometry(r));
			double thisDist = distOp.distance();
			// BUG?: if an agent is on a really long road, the long road will not be found by getObjectsWithin because
			// it is not within the buffer
			debug.append("\troad ").append(r.toString()).append(" is ").append(thisDist).append(
					" distance away (at closest point). ");

			if (thisDist < minDist) {
				minDist = thisDist;
				Coordinate[] closestPoints = distOp.closestPoints();
				// Two coordinates returned by closestPoints(), need to find the
				// one which isn''t the coord parameter
				debug.append("Closest points (").append(closestPoints.length).append(") are: ").append(
						Arrays.toString(closestPoints));
				nearestPoint = (c.equals(closestPoints[0])) ? closestPoints[1] : closestPoints[0];
				debug.append("Nearest point is ").append(nearestPoint.toString());
				nearestPoint = (c.equals(closestPoints[0])) ? closestPoints[1] : closestPoints[0];
			} // if thisDist < minDist
			debug.append("\n");

		} // for nearRoads

		if (nearestPoint != null) {
			LOGGER.log(Level.FINER, "NearestRoadCoordCache.get() (not using cache) - ("
					+ (0.000001 * (System.nanoTime() - time)) + "ms)");
			return nearestPoint;
		}
		/* IF HERE THEN ERROR, PRINT DEBUGGING INFO */
		StringBuilder debugIntro = new StringBuilder(); // Some extra info for debugging
		debugIntro.append("Route.NearestRoadCoordCache.get() error: couldn't find a coordinate to return.\n");
		Iterable<Road> roads = ContextManager.roadProjection.getObjectsWithin(searchEnvelope);
		debugIntro.append("Looking for nearest road coordinate around ").append(c.toString()).append(".\n");
		debugIntro.append("RoadEnvironment.getObjectsWithin() returned ").append(
				ContextManager.sizeOfIterable(roads) + " roads, printing debugging info:\n");
		debugIntro.append(debug);
		throw new Exception(debugIntro.toString());

	}

	private void serialise() throws IOException {
		double time = System.nanoTime();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			if (!this.serialisedLoc.exists())
				this.serialisedLoc.createNewFile();
			fos = new FileOutputStream(this.serialisedLoc);
			out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		} catch (IOException ex) {
			if (serialisedLoc.exists()) {
				// delete to stop problems loading incomplete file next time
				serialisedLoc.delete();
			}
			throw ex;
		}
		LOGGER.log(Level.FINE, "... serialised NearestRoadCoordCache to " + this.serialisedLoc.getAbsolutePath()
				+ " in (" + 0.000001 * (System.nanoTime() - time) + "ms)");
	}

	/**
	 * Used to create a new BuildingsOnRoadCache object. This function is used instead of the constructor directly so
	 * that the class can check if there is a serialised version on disk already. If not then a new one is created and
	 * returned.
	 * 
	 * @param buildingEnv
	 * @param buildingsFile
	 * @param roadEnv
	 * @param roadsFile
	 * @param serialisedLoc
	 * @param geomFac
	 * @return
	 * @throws Exception
	 */
	public synchronized static NearestRoadCoordCache getInstance(Geography<Building> buildingEnv, File buildingsFile,
			Geography<Road> roadEnv, File roadsFile, File serialisedLoc, GeometryFactory geomFac) throws Exception {
		double time = System.nanoTime();
		// See if there is a cache object on disk.
		if (serialisedLoc.exists()) {
			FileInputStream fis = null;
			ObjectInputStream in = null;
			NearestRoadCoordCache ncc = null;
			try {

				fis = new FileInputStream(serialisedLoc);
				in = new ObjectInputStream(fis);
				ncc = (NearestRoadCoordCache) in.readObject();
				in.close();

				// Check that the cache is representing the correct data and the
				// modification dates are ok
				if (!buildingsFile.getAbsolutePath().equals(ncc.buildingsFile.getAbsolutePath())
						|| !roadsFile.getAbsolutePath().equals(ncc.roadsFile.getAbsolutePath())
						|| buildingsFile.lastModified() > ncc.createdTime || roadsFile.lastModified() > ncc.createdTime) {
					LOGGER.log(Level.FINE, "BuildingsOnRoadCache, found serialised object but it doesn't match the "
							+ "data (or could have different modification dates), will create a new cache.");
				} else {
					LOGGER.log(Level.FINER, "NearestRoadCoordCache, found serialised cache, returning it (in "
							+ 0.000001 * (System.nanoTime() - time) + "ms)");
					return ncc;
				}
			} catch (IOException ex) {
				if (serialisedLoc.exists())
					serialisedLoc.delete(); // delete to stop problems loading incomplete file next tinme
				throw ex;
			} catch (ClassNotFoundException ex) {
				if (serialisedLoc.exists())
					serialisedLoc.delete();
				throw ex;
			}

		}

		// No serialised object, or got an error when opening it, just create a new one
		return new NearestRoadCoordCache(buildingEnv, buildingsFile, roadEnv, roadsFile, serialisedLoc, geomFac);
	}

}

/**
 * Used to cache routes. Saves the origin and destination coords and the transport available to the agent (if transport
 * changes then the agent might have to create a new route.
 * 
 * @author Nick Malleson
 */
class CachedRoute {
	private List<Coordinate> theRoute;
	private List<Double> routeSpeeds;
	private List<String> routeDescriptions;
	private List<Road> roads;
	private Coordinate origin;
	private Coordinate destination;
	private List<String> transportAvailable;
	// Used to generate hash codes (each route must have unique ID)
	private static int uniqueRouteCacheID;
	private int uniqueID;

	public CachedRoute(Coordinate origin, Coordinate destination, List<String> transportAvailable) {
		this.origin = origin;
		this.destination = destination;
		this.transportAvailable = transportAvailable;
		this.uniqueID = CachedRoute.uniqueRouteCacheID++;
	}

	public void setRoute(List<Coordinate> theRoute, List<Road> roads, List<Double> routeSpeeds,
			List<String> routeDescriptions) {
		this.theRoute = theRoute;
		this.roads = roads;
		this.routeSpeeds = routeSpeeds;
		this.routeDescriptions = routeDescriptions;
	}

	public List<Coordinate> getRoute() {
		return this.theRoute;
	}

	public List<Double> getRouteSpeeds() {
		return this.routeSpeeds;
	}

	public List<Road> getRoads() {
		return this.roads;
	}

	public List<String> getDescriptions() {
		return this.routeDescriptions;
	}

	@Override
	public String toString() {
		return "CachedRoute " + this.uniqueID;
	}

	/**
	 * Returns true if input object is a CachedRoute and the the origin, destination and transport available are the
	 * same as this CachedRoute
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CachedRoute) {
			CachedRoute r = (CachedRoute) obj;
			return (r.origin.equals(this.origin)) && (r.destination.equals(this.destination))
					&& (r.transportAvailable.equals(this.transportAvailable));
		} else {
			return false;
		}
	}

	/**
	 * Returns: <code>Float.floatToIntBits((float)(this.origin.getX()+this.origin.getY()))</code>
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits((float) (this.origin.x + this.origin.y));
	}
}

/**
 * Used to cache route distances. Saves the origin and destination coords and the transport available to the agent (if
 * transport changes then the agent might have to create a new route).
 * 
 * @author Nick Malleson
 */
class CachedRouteDistance {
	private Coordinate origin;
	private Coordinate destination;
	private List<String> transportAvailable;
	private static int uniqueRouteCacheID; // Used to generate hash codes (each
											// route must have unique ID)
	private int uniqueID;

	// private List<Coord> theRoute; // The actual route (a list of coords)

	public CachedRouteDistance(Coordinate origin, Coordinate destination, List<String> transportAvailable) {
		this.origin = origin;
		this.destination = destination;
		this.transportAvailable = transportAvailable;
		this.uniqueID = CachedRouteDistance.uniqueRouteCacheID++;
	}

	@Override
	public String toString() {
		return "CachedRouteDistance " + this.uniqueID;
	}

	/**
	 * Returns true if input object is a CachedRoute and the the origin, destination and transport available are the
	 * same as this CachedRoute. Because routes are non-directional the origins and destinations are interchangeable.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CachedRouteDistance) {
			CachedRouteDistance r = (CachedRouteDistance) obj;
			return ((r.origin.equals(this.origin) && r.destination.equals(this.destination)) || (r.origin
					.equals(this.destination) && r.destination.equals(this.origin)))
					&& r.transportAvailable.equals(this.transportAvailable);
		} else {
			return false;
		}
	}

	/**
	 * Returns: <code>Float.floatToIntBits((float)(this.origin.getX()+this.origin.getY()))</code>
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits((float) (this.origin.x + this.origin.y));
	}
}

/**
 * Convenience class for creating deep copies of lists/maps (copies the values stored as well). Haven't made this
 * generic because need access to constructors to create new objects (e.g. new Coord(c))
 */
final class Cloning {

	public static List<Coordinate> copy(List<Coordinate> in) {

		List<Coordinate> out = new ArrayList<Coordinate>(in.size());
		for (Coordinate c : in) {
			// TODO Check this Coordinate constructor does what I expect it to
			out.add(new Coordinate(c));
		}
		return out;
	}

	// Not used now that route speeds are a list, not a map
	// public static LinkedHashMap<Coordinate, Double>
	// copy(LinkedHashMap<Coordinate, Double> in) {
	//
	// LinkedHashMap<Coordinate, Double> out = new LinkedHashMap<Coordinate,
	// Double>(in.size());
	// for (Coordinate c : in.keySet()) {
	// out.put(c, in.get(c));
	// }
	// return out;
	// }

}
