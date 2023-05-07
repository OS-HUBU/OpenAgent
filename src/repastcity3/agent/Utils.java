package repastcity3.agent;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import repastcity3.main.ContextManager;
import repastcity3.agent.IAgent;
import repastcity3.agent.MyAgent;


public class Utils {
	public static List<?> getObjectsWithinDistance(Object source, Class clazz,
			double searchDistance){
		Context context = ContextUtils.getContext(source);
		Geography geography = (Geography)context.getProjection("AgentGeography");
		
		Geometry searchArea =  GeometryUtil.generateBuffer(geography, 
				geography.getGeometry(source), searchDistance);
//		double xLength =searchArea.getBoundary().getEnvelopeInternal().getMaxX()-searchArea.getBoundary().getEnvelopeInternal().getMinX();
//		double yLength =searchArea.getBoundary().getEnvelopeInternal().getMaxY()-searchArea.getBoundary().getEnvelopeInternal().getMinY();
//		System.out.println("boundary"+xLength);
//		System.out.println("boundary"+yLength);
//		clazz = IAgent.class; 
//		Geometry searchArea = ContextManager.getAgentGeometry((MyAgent)clazz.cast(MyAgent.class)).buffer(0.000001);
//		System.out.println("Geometry形状"+searchArea.toString());
		Envelope searchEnvelope = searchArea.getEnvelopeInternal();
		
		System.out.println("Geometry形状"+searchArea.toString());
		System.out.println("矩形面积"+searchEnvelope.getArea());
		System.out.println("矩形高度"+searchEnvelope.getHeight());
		System.out.println("矩形宽度"+searchEnvelope.getWidth());
		System.out.println("矩形最大X"+searchEnvelope.getMaxX());
		System.out.println("矩形最小X"+searchEnvelope.getMinX());
		System.out.println("矩形最大Y"+searchEnvelope.getMaxY());
		System.out.println("矩形最小Y"+searchEnvelope.getMinY());
		System.out.println("矩形toString"+searchEnvelope.toString());
	
		
		
		//getObjectsWithin获取对指定信封中属于指定类型且仅具有指定类型的所有对象的可迭代性。
		Iterable<?> nearObjects = geography.getObjectsWithin(searchEnvelope, clazz);	
		List nearObjectList = new ArrayList();
		
		for (Object o : nearObjects){
			nearObjectList.add(o);
		}	
		return nearObjectList;
	}
	//判断数组中是否包含该元素
	public static <T> boolean contain(T y,T x[])
	{
		int k=x.length;
		for (int i=0;i<k;i++)
		{
			if(y.equals(x[i]))
			{
				return true;
			}
		}
		return false;
	}
	public static void main(String[] args)
	{
		System.out.println(Utils.contain(250,ContextManager.leftBottomArea));
	}
}
