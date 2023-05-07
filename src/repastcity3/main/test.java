package repastcity3.main;

import java.util.Random;

import repast.simphony.context.Context;
import repastcity3.agent.AgentFactory;
import repastcity3.agent.IAgent;
import repastcity3.environment.contexts.AgentContext;

public class test {
	public static void test() throws Exception{
       Random random=new Random();
       int ss=random.nextInt(10);
       float ss1=(float)ss/10;
       System.out.println(ss1);
	}
	public static void main(String[] args) {
		try {
			test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
