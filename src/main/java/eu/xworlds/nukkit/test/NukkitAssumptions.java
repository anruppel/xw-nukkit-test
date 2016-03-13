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

import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.function.Supplier;

import cn.nukkit.utils.LogLevel;

/**
 * Assumptions for nukkit
 * 
 * @author mepeisen
 */
public class NukkitAssumptions
{
    
    /**
     * Hidden constructor
     */
    private NukkitAssumptions()
    {
        // empty
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     */
    public static void assumeLogMessage(NukkitTestSession session, LogLevel level, String logMessage)
    {
        assumeLogMessage(session, () -> level, () -> logMessage, () -> null);
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     */
    public static void assumeLogMessage(NukkitTestSession session, LogLevel level, String logMessage, String message)
    {
        assumeLogMessage(session, () -> level, () -> logMessage, () -> message);
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     */
    public static void assumeLogMessage(NukkitTestSession session, Supplier<LogLevel> level, Supplier<String> logMessage, Supplier<String> message)
    {
        final String msg = logMessage.get();
        final LogLevel lvl = level.get();
        assumeLogEvent(session, (event) -> event.getLevel() == lvl && event.getMessage().equals(msg), message);
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param check
     */
    public static void assumeLogEvent(NukkitTestSession session, LogEventCheck check)
    {
        assumeLogEvent(session, check, () -> null);
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     */
    public static void assumeLogEvent(NukkitTestSession session, LogEventCheck check, String message)
    {
        assumeLogEvent(session, check, () -> message);
    }
    
    /**
     * assume that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     */
    public static void assumeLogEvent(NukkitTestSession session, LogEventCheck check, Supplier<String> message)
    {
        assumeTrue(session.checkLog(check), message);
    }
    
}
