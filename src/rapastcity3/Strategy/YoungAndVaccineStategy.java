package rapastcity3.Strategy;

import repastcity3.agent.IAgent;
import repastcity3.agent.InfectedAgent;

public class YoungAndVaccineStategy implements StatusStrategy{
	
	@Override
	public void handler(InfectedAgent agent,Integer i1) {
		// TODO Auto-generated method stub
		if (i1>80) {
			
			agent.SwitchInfectedAgent(agent);
		}else {
			
			agent.changeInfectedAgent(agent);
			
		}
		}

	@Override
	public boolean choose(InfectedAgent agent) {
		// TODO Auto-generated method stub
		return agent.getVaccine()&& agent.getAge()=="young";
	}


}
