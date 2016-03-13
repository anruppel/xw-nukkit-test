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

import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.function.Supplier;

import cn.nukkit.utils.LogLevel;

/**
 * Asserts for nukkit
 * 
 * @author mepeisen
 */
public class NukkitAssertions
{
    
    /**
     * Hidden constructor
     */
    private NukkitAssertions()
    {
        // empty
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     */
    public static void assertLogMessage(NukkitTestSession session, LogLevel level, String logMessage)
    {
        assertLogMessage(session, () -> level, () -> logMessage, () -> null);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     */
    public static void assertLogMessage(NukkitTestSession session, LogLevel level, String logMessage, String message)
    {
        assertLogMessage(session, () -> level, () -> logMessage, () -> message);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     */
    public static void assertLogMessage(NukkitTestSession session, Supplier<LogLevel> level, Supplier<String> logMessage, Supplier<String> message)
    {
        final String msg = logMessage.get();
        final LogLevel lvl = level.get();
        assertLogEvent(session, (event) -> event.getLevel() == lvl && event.getMessage().equals(msg), message);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check)
    {
        assertLogEvent(session, check, () -> null);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check, String message)
    {
        assertLogEvent(session, check, () -> message);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check, Supplier<String> message)
    {
        assertTrue(session.checkLog(check), message);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogMessage(NukkitTestSession session, LogLevel level, String logMessage, int millis)
    {
        assertLogMessage(session, () -> level, () -> logMessage, () -> null, millis);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogMessage(NukkitTestSession session, LogLevel level, String logMessage, String message, int millis)
    {
        assertLogMessage(session, () -> level, () -> logMessage, () -> message, millis);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param level
     * @param logMessage
     * @param message
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogMessage(NukkitTestSession session, Supplier<LogLevel> level, Supplier<String> logMessage, Supplier<String> message, int millis)
    {
        final String msg = logMessage.get();
        final LogLevel lvl = level.get();
        assertLogEvent(session, (event) -> event.getLevel() == lvl && event.getMessage().equals(msg), message, millis);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check, int millis)
    {
        assertLogEvent(session, check, () -> null, millis);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check, String message, int millis)
    {
        assertLogEvent(session, check, () -> message, millis);
    }
    
    /**
     * Assert that a given log message was present on nukkit logger
     * @param session
     * @param check
     * @param message
     * @param millis milliseconds to wait for the message
     */
    public static void assertLogEvent(NukkitTestSession session, LogEventCheck check, Supplier<String> message, int millis)
    {
        assertTrue(session.waitForLog(check, millis), message);
    }
    
}
