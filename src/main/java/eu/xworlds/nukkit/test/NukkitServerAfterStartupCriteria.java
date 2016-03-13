/*
    This file is part of "nukkit xWorlds test tools".

    "nukkit xWorlds test tools" is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    "nukkit xWorlds test tools" is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with "nukkit xWorlds test tools". If not, see <http://www.gnu.org/licenses/>.

 */
package eu.xworlds.nukkit.test;

import cn.nukkit.Server;

/**
 * Criteria used for configuration after startup
 * 
 * @author mepeisen
 */
public abstract class NukkitServerAfterStartupCriteria extends NukkitServerCriteria
{
    
    /**
     * Hidden constructor
     */
    protected NukkitServerAfterStartupCriteria()
    {
        // empty
    }
    
    /**
     * Configures nukkit after startup
     * @param session
     * @param server
     */
    public abstract void afterStartup(NukkitTestSession session, Server server);
    
}
