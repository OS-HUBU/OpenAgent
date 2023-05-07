package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;

public class TrafficJam {
	private int trafficjamID;
	private double X;
	private double Y;
	private static int IDcount=0;
	public static List<TrafficJam> list=new  ArrayList<TrafficJam>();
	
	public TrafficJam(double X,double Y){
		this.X=X;
		this.Y=Y;
		this.trafficjamID=IDcount++;
	}
	public double getX(){
		return X;
	}
	public double getY(){
		return Y;
	}
	public int getID(){
		return trafficjamID;
	}

}
