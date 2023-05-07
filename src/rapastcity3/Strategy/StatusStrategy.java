package rapastcity3.Strategy;

import repastcity3.agent.IAgent;
import repastcity3.agent.InfectedAgent;

/**
 * 感染者状况的策略接口
 * @author hzw
 *
 */
public interface StatusStrategy {
	//处理方法
	public void handler(InfectedAgent agent,Integer i1);
	//判断方法
	public boolean choose(InfectedAgent agent);
}
