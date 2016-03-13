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

import static org.junit.gen5.api.Assertions.assertFalse;

import java.util.function.Supplier;

import cn.nukkit.Server;

/**
 * Static class holding server criterias
 * 
 * @author mepeisen
 */
public class Criterias
{
    
    /**
     * Hidden constructor
     */
    private Criterias()
    {
        // empty
    }
    
    /**
     * Sets the server folder
     * 
     * @param folder
     *            server folder
     * @return the criteria to set the server folder
     */
    public static NukkitServerCriteria serverFolder(String folder)
    {
        return serverFolder(() -> folder);
    }
    
    /**
     * Sets the server folder parent (the folder a temporary folder is created in)
     * 
     * @param folder
     *            server folder
     * @return the criteria to set the server folder
     */
    public static NukkitServerCriteria serverFolderParent(String folder)
    {
        return serverFolderParent(() -> folder);
    }
    
    /**
     * Sets the data folder
     * 
     * @param folder
     *            data folder
     * @return the criteria to set the data folder
     */
    public static NukkitServerCriteria dataFolder(String folder)
    {
        return dataFolder(() -> folder);
    }
    
    /**
     * Sets the plugin folder
     * 
     * @param folder
     *            plugin folder
     * @return the criteria to set the data folder
     */
    public static NukkitServerCriteria pluginFolder(String folder)
    {
        return pluginFolder(() -> folder);
    }
    
    /**
     * Selects a language and waits for the server being started
     * 
     * @param lang
     * @return the criteria to select the lang
     */
    public static NukkitServerCriteria selectLangAndWaitStarted(String lang)
    {
        return selectLangAndWaitStarted(() -> lang);
    }
    
    /**
     * Sets the server folder
     * 
     * @param supplier
     *            server folder (lambda)
     * @return the criteria to set the server folder
     */
    public static NukkitServerCriteria serverFolder(Supplier<String> supplier)
    {
        return new NukkitServerBeforeStartupCriteria() {
            @Override
            public void beforeStartup(NukkitTestSession session)
            {
                session.setServerFolder(supplier.get());
            }
        };
    }
    
    /**
     * Sets the server folders parent (the folder a temporary folder is created in)
     * 
     * @param supplier
     *            server folder parent (lambda)
     * @return the criteria to set the server folder
     */
    public static NukkitServerCriteria serverFolderParent(Supplier<String> supplier)
    {
        return new NukkitServerBeforeStartupCriteria() {
            @Override
            public void beforeStartup(NukkitTestSession session)
            {
                session.setServerFolderParent(supplier.get());
            }
        };
    }
    
    /**
     * Sets the server data folder
     * 
     * @param supplier
     *            data folder (lambda)
     * @return the criteria to set the data folder
     */
    public static NukkitServerCriteria dataFolder(Supplier<String> supplier)
    {
        return new NukkitServerBeforeStartupCriteria() {
            @Override
            public void beforeStartup(NukkitTestSession session)
            {
                session.setDataFolder(supplier.get());
            }
        };
    }
    
    /**
     * Sets the server plugin folder
     * 
     * @param supplier
     *            plugin folder (lambda)
     * @return the criteria to set the plugin folder
     */
    public static NukkitServerCriteria pluginFolder(Supplier<String> supplier)
    {
        return new NukkitServerBeforeStartupCriteria() {
            @Override
            public void beforeStartup(NukkitTestSession session)
            {
                session.setPluginFolder(supplier.get());
            }
        };
    }
    
    /**
     * Selects a language and waits for the server being started
     * 
     * @param lang
     * @return the criteria to select the lang
     */
    public static NukkitServerCriteria selectLangAndWaitStarted(Supplier<String> lang)
    {
        return new NukkitServerAfterStartupCriteria() {
            
            @Override
            public void afterStartup(NukkitTestSession session, Server server)
            {
                // check if we are in correct state
                assertFalse(session.isConfigured());
                
                // select lang
                session.sendConsoleString(lang);
                
                // wait for finished startup
                session.waitMainLoop(10000); // wait for at least 10 seconds
            }
        };
    }
    
}
