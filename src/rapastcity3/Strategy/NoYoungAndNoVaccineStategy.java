 package rapastcity3.Strategy;

import repastcity3.agent.InfectedAgent;

public class NoYoungAndNoVaccineStategy implements StatusStrategy{

	@Override
	public void handler(InfectedAgent agent, Integer i1) {
		// TODO Auto-generated method stub
		if(i1>20)
		{
			agent.SwitchInfectedAgent(agent);
		}
		else {
			agent.changeInfectedAgent(agent);
		}
		
	}

	@Override
	public boolean choose(InfectedAgent agent) {
		// TODO Auto-generated method stub
		return !(agent.getVaccine() && agent.getAge()=="young");
	}

}
