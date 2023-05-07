/*�Copyright 2012 Nick Malleson
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
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.*/

package repastcity3.environment;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

import repastcity3.agent.IAgent;
import repastcity3.exceptions.NoIdentifierException;

public class Building implements FixedGeography {

	/** A list of agents who live here */
	private List<IAgent> agents;

	/**
	 * A unique identifier for buildings, usually set from the 'identifier' column in a shapefile
	 */
	private String identifier;

	/**
	 * The coordinates of the Building. This is also stored by the projection that contains this Building but it is
	 * useful to have it here too. As they will never change (buildings don't move) we don't need to worry about keeping
	 * them in sync with the projection.
	 * 
	 * 大楼的坐标。这也由包含此建筑物的投影存储，但将其放在这里也很有用。
	 * 由于它们永远不会改变（建筑物不会移动），因此我们无需担心如何使它们与投影保持同步。
	 */
	private Coordinate coords;
	
	/** 最多可以容纳多少人 */
	private Integer limit = 1;
	
	//是否已经被限制进去building
//	private boolean limited = false;
	
	/** 已经容纳了多少人了 */
	private Integer occupation = 0;

	public Building() {
		this.agents = new ArrayList<IAgent>();
	}
//	//测试
//	public Building(String identifier) {
//		this.agents = new ArrayList<IAgent>();
//		this.identifier = identifier;
//	}
//	//添加方法
//	public void setLimited(String a) {
//		if(a == "true") {
//			this.limited = (0 != 1) ;
//		}else if(a == "flase") {
//			this.limited = (0==1) ;
//		}
//		
//	}
//	public boolean getLimited () {
//		return this.limited;
//	}

	@Override
	public Coordinate getCoords() {
		return this.coords;
	}

	@Override
	public void setCoords(Coordinate c) {
		this.coords = c;

	}

	public String getIdentifier() throws NoIdentifierException {
		if (this.identifier == null) {
			throw new NoIdentifierException("This building has no identifier. This can happen "
					+ "when roads are not initialised correctly (e.g. there is no attribute "
					+ "called 'identifier' present in the shapefile used to create this Road)");
		} else {
			return identifier;
		}
	}
	
	public void addOccuption(Integer value){
		this.occupation += value;
	}

	public void setIdentifier(String id) {
		this.identifier = id;
	}

	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOccupation() {
		return occupation;
	}

	public void setOccupation(Integer occupation) {
		this.occupation = occupation;
	}

	@Override
	public String toString() {
		return "building: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Building))
			return false;
		Building b = (Building) obj;
		return this.identifier.equals(b.identifier);
	}

	/**
	 * Return this buildings unique id number.
	 */
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}

}
