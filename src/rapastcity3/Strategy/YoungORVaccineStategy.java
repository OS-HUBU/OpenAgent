package rapastcity3.Strategy;

import repastcity3.agent.InfectedAgent;
import repastcity3.main.ContextManager;

public class YoungORVaccineStategy implements StatusStrategy{

	@Override
	public void handler(InfectedAgent agent, Integer i1) {
		// TODO Auto-generated method stub
		if(i1>50)
		{
			agent.SwitchInfectedAgent(agent);
		}
		else {
			agent.changeInfectedAgent(agent);
		}
	}

	@Override
	public boolean choose(InfectedAgent agent) {
		return agent.getVaccine() || agent.getAge()=="young";
	}

}
