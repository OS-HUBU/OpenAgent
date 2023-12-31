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

/**
 * Used by any class which has static cached objects. Static caches must be cleared at the start of
 * each simulation or they will persist over multiple simulation runs unless Simphony is restarted. 
 * 
 * 由具有静态缓存对象的任何类使用。
 * 静态缓存必须在每个模拟或它们将在多个模拟运行中持续存在，除非重新启动 Simphony。
 * @author Nick Malleson
 *
 */
public interface Cacheable {
	
	void clearCaches();

}
