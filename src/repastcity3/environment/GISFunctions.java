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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.ShapefileLoader;
import repast.simphony.space.graph.Network;

/**
 * Class with useful GIS functions for configuring the GIS model environment.
 * 具有用于配置 GIS 模型环境的有用 GIS 函数的类
 * 
 * @author Nick Malleson
 * 
 */
public class GISFunctions {

	private static Logger LOGGER = Logger.getLogger(Route.class.getName());

	/**
	 * Create the road network. Runs through the roads in the <code>roadGeography</code> and, for each one, will create
	 * <code>Junction</code> objects at their end points and an edge linking them. The <code>Junction</code> objects are
	 * added to the given <code>junctionGeography</code> (so that we know where they are spatially) and they are also
	 * added, along with the edge between them, to the <code>junctionNetwork</code> so that topographical relationships
	 * can be established. (The <code>junctionNetwork</code> is part of the <code>junctionContext</code>
	 * 
	 * @param roadGeography
	 * @param junctionContext
	 * @param junctionGeography
	 * @param roadNetwork
	 */
	public static void buildGISRoadNetwork(Geography<Road> roadGeography, Context<Junction> junctionContext,
			Geography<Junction> junctionGeography, Network<Junction> roadNetwork) {

		// Create a GeometryFactory so we can create points/lines from the junctions and roads 创建几何工厂，以便我们可以从交汇点和道路创建点/线
		// (this is so they can be displayed on the same display to check if the network has been created successfully)这样它们就可以显示在同一显示器上，以检查网络是否已成功创建
		GeometryFactory geomFac = new GeometryFactory();

		// Create a cache of all Junctions and coordinates so we know if a junction has already been created at a
		// particular coordinate
		Map<Coordinate, Junction> coordMap = new HashMap<Coordinate, Junction>();

		// Iterate through all roads 遍历所有道路
		Iterable<Road> roadIt = roadGeography.getAllObjects();
		for (Road road : roadIt) {
			
			//
//			System.out.println(road.toString());
			//初始化处理数据  Mr.wang
			road.initialiseFirst();
			//输出access 和 Accessibility(测试) ma.wang
//			System.out.println(road.getAccessibility());
//			System.out.println(road.toString());
//			System.out.println(road.getAccess());
			
			// Create a LineString from the road so we can extract coordinates 
			//从道路上创建线串，以便我们可以提取坐标
			Geometry roadGeom = roadGeography.getGeometry(road);
			Coordinate c1 = roadGeom.getCoordinates()[0]; // First coord
			Coordinate c2 = roadGeom.getCoordinates()[roadGeom.getNumPoints() - 1]; // Last coord
			
			//测试 Mr.wang
//			System.out.println(roadGeom + "全坐标");
//			System.out.println(c1 + "第一个坐标");
//			System.out.println(c2 + "最后一个坐标");
			
			// Create Junctions from these coordinates and add them to the JunctionGeography 
			//(if they haven't been created already)
			//从这些坐标创建交汇点并将其添加到交汇点地理（如果尚未创建）
			Junction junc1, junc2;
			if (coordMap.containsKey(c1)) {
				// A Junction with those coordinates (c1) has been created, 
				//get it so we can add an edge to it 
				//已创建具有这些坐标 （c1） 的交汇点，获取它，
				//以便我们可以向其添加边
				junc1 = coordMap.get(c1);
			} else { // Junction does not exit
				junc1 = new Junction();
				junc1.setCoords(c1);//交汇点处添加坐标
				junctionContext.add(junc1);
				coordMap.put(c1, junc1);//添加
				Point p1 = geomFac.createPoint(c1);
				junctionGeography.move(junc1, p1);
			}
			if (coordMap.containsKey(c2)) {
				junc2 = coordMap.get(c2);
			} else { // Junction does not exit
				junc2 = new Junction();
				junc2.setCoords(c2);
				junctionContext.add(junc2);
				coordMap.put(c2, junc2);
				Point p2 = geomFac.createPoint(c2);
				junctionGeography.move(junc2, p2);
			}
			// Tell the road object who it's junctions are
			road.addJunction(junc1);
			road.addJunction(junc2);
			// Tell the junctions about this road
			junc1.addRoad(road);
			junc2.addRoad(road);

			// Create an edge between the two junctions, assigning a weight equal to it's length
			NetworkEdge<Junction> edge = new NetworkEdge<Junction>(junc1, junc2, false, roadGeom.getLength(), road
					.getAccessibility());
			// Set whether or not the edge represents a major road (gives extra benefit to car drivers).
			if (road.isMajorRoad())
				edge.setMajorRoad(true);
			// // Store the road's TOID in a dictionary (one with edges as keys, one with id's as keys)
			// try {
			// // edgeIDs_KeyEdge.put(edge, (String) road.getIdentifier());
			// // edgeIDs_KeyID.put((String) road.getIdentifier(), edge);
			// edges_roads.put(edge, road);
			// roads_edges.put(road, edge);
			// } catch (Exception e) {
			// Outputter.errorln("EnvironmentFactory: buildGISRoadNetwork error, here's the message:\n"+e.getMessage());
			// }
			// Tell the Road and the Edge about each other
			road.setEdge(edge);
			edge.setRoad(road);
			if (!roadNetwork.containsEdge(edge)) {
				roadNetwork.addEdge(edge);
			} else {
				LOGGER.severe("CityContext: buildRoadNetwork: for some reason this edge that has just been created "
						+ "already exists in the RoadNetwork!");
			}

		} // for road:
	}

	/**
	 * Nice generic function :-) that reads in objects from shapefiles.
	 * <p>
	 * The objects (agents) created must extend FixedGeography to guarantee that they will have a setCoords() method.
	 * This is necessary because, for simplicity, geographical objects which don't move store their coordinates
	 * alongside the projection which stores them as well. So the coordinates must be set manually by this function once
	 * the shapefile has been read and the objects have been given coordinates in their projection.
	 * 
	 * @param <T>
	 *            The type of object to be read (e.g. PecsHouse). Must exted
	 * @param cl
	 *            The class of the building being read (e.g. PecsHouse.class). 正在读取的建筑物的类
	 * @param shapefileLocation
	 *            The location of the shapefile containing the objects. 包含对象的 shapefile 的位置。
	 * @param geog
	 *            A geography to add the objects to.  要将对象添加到的地理位置。
	 * @param context
	 *            A context to add the objects to. 要将对象添加到的上下文
	 * @throws MalformedURLException
	 *             If the location of the shapefile cannot be converted into a URL
	 * @throws FileNotFoundException
	 *             if the shapefile does not exist.
	 * @see FixedGeography
	 */
	public static <T extends FixedGeography> void readShapefile(Class<T> cl, String shapefileLocation,
			Geography<T> geog, Context<T> context) throws MalformedURLException, FileNotFoundException {
		File shapefile = null;
		ShapefileLoader<T> loader = null;
		shapefile = new File(shapefileLocation);
		if (!shapefile.exists()) {
			throw new FileNotFoundException("Could not find the given shapefile: " + shapefile.getAbsolutePath());
		}
		loader = new ShapefileLoader<T>(cl, shapefile.toURI().toURL(), geog, context);
		
//		System.out.println("--------------------------------------------------------------------------");
//		System.out.println(geog);
		
		
//		try {		
//			while (loader.hasNext()) {   
//			loader.next();
//		}
//			
//		}catch(Exception e) {
//			System.out.println(e);
//		}
		
		//ERROR [AWT-EventQueue-0] 10:16:00,866 repast.simphony.space.gis.ShapefileLoader - Error transforming feature geometry to geography's CRS
		while (loader.hasNext()) {   
			loader.next();
		}
		
		for (T obj : context.getObjects(cl)) {
//			System.out.println(obj);
//			System.out.println(geog);
//			System.out.println(geog.getGeometry(obj));
//			System.out.println(geog.getGeometry(obj).getCentroid().getCoordinate());
			obj.setCoords(geog.getGeometry(obj).getCentroid().getCoordinate());

			 
		}
	}

	/**
	 * An alternative to <code>readShapefile()</code> that does not require objects to implement
	 * <code>FixedGeography<code>. Hence it can be used by objects that don't store their coordinates internally (such 
	 * as agents).
	 * 
	 * @param <T>
	 *            The type of object to be read (e.g. PecsHouse). Must exted
	 * @param cl
	 *            The class of the building being read (e.g. PecsHouse.class).
	 * @param shapefileLocation
	 *            The location of the shapefile containing the objects.
	 * @param geog
	 *            A geography to add the objects to.
	 * @param context
	 *            A context to add the objects to.
	 * @throws MalformedURLException
	 *             If the location of the shapefile cannot be converted into a URL
	 * @throws FileNotFoundException
	 *             if the shapefile does not exist.
	 * @see FixedGeography
	 */
	public static <T> void readAgentShapefile(Class<T> cl, String shapefileLocation, Geography<T> geog,
			Context<T> context) throws MalformedURLException, FileNotFoundException {
		
		File shapefile = null;
		ShapefileLoader<T> loader = null;
		shapefile = new File(shapefileLocation);
		if (!shapefile.exists()) {
			throw new FileNotFoundException("Could not find the given shapefile: " + shapefile.getAbsolutePath());
		}
		loader = new ShapefileLoader<T>(cl, shapefile.toURI().toURL(), geog, context);
		while (loader.hasNext()) {
			loader.next();
		}
	}

}
