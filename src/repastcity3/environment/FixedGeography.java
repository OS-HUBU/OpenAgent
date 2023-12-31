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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Implemented by all objects which hold their own Coord objects in conjunction with those
 * held by any projections they exist in. For example, a Road object has road coordinates stored
 * by the ROAD_ENVIRONMENT, but these coordinates can also be found (for simplicity) by calling
 * road.getCoords().<br>
 * 
 * 由所有对象实现，这些对象与它们存在的任何投影所持有的对象一起保存自己的坐标对象。
 * 例如，道路对象存储了道路坐标通过ROAD_ENVIRONMENT，但这些坐标也可以通过调用 road.getCoords（） 找到（为简单起见）。
 * 
 * Used by EnvironmentFactory.readShapeFile().<br>
 * Must not be used by objects which will move such as People.
 * @author Nick Malleson
 *
 */
public interface FixedGeography {
	
	Coordinate getCoords();
	void setCoords(Coordinate c);

}
