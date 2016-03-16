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

import static eu.xworlds.nukkit.test.NukkitAssertions.assertLogEvent;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Charsets;

import cn.nukkit.Server;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.LogLevel;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import jline.console.ConsoleReader;

/**
 * A sample test session to handle a nukkit test server.
 * 
 * <p>
 * If no server path is set it will use the environment variable {@code NUKKIT_TEST_PATH}.
 * </p>
 * 
 * @author mepeisen
 */
public final class NukkitTestSession
{
    
    /** the thread of the nukkit server */
    private Thread     serverThread;
    
    /** the server folder */
    private String     serverFolder;
    
    /** the server folder parent */
    private String     serverFolderParent;
    
    /** the data folder */
    private String     dataFolder;
    
    /** the plugin folder */
    private String     pluginFolder;
    
    /** {@code true} if the files are deleted on server shutdown */
    private boolean    deletingOnFinish;
    
    /** {@code true} if the files are deleted on vm exit */
    private boolean    deletingOnVmExit;
    
    /** the log events that were caught by intercepting main logger */
    List<LogEvent>     logEvents        = new ArrayList<>();
    
    /** the console spy for injecting commands */
    // ConsoleReader consoleSpy;
    
    /** injected console commands */
    LinkedList<String> injectedCommands = new LinkedList<>();
    
    /** the scheduler spy */
    ServerScheduler    schedulerSpy;
    
    /** the flag for indicated a running nukkit server */
    boolean            finishedStart;
    
    /** the main logger */
    MainLogger         logger;
    
    /** the flag to indicate a stopped nukkit server */
    boolean            finishedStop;
    
    /**
     * Constructor
     */
    public NukkitTestSession()
    {
        // empty
    }
    
    /**
     * Checks if the nukkit server is running
     * 
     * @return {@code true} if the server is running
     */
    public boolean isRunning()
    {
        return Server.getInstance() != null && Server.getInstance().isRunning();
    }
    
    /**
     * Starts the nukkit server
     * 
     * @return this test session instance
     */
    public NukkitTestSession start()
    {
        return startWith();
    }
    
    /**
     * Starts the nukkit server with given criteria
     * 
     * @param criteria
     *            one or more criterias
     * @return this test session instance
     */
    public NukkitTestSession startWith(NukkitServerCriteria... criteria)
    {
        this.finishedStart = false;
        this.finishedStop = false;
        
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        if (criteria != null)
        {
            for (final NukkitServerCriteria c : criteria)
            {
                if (c instanceof NukkitServerBeforeStartupCriteria)
                {
                    ((NukkitServerBeforeStartupCriteria) c).beforeStartup(this);
                }
            }
        }
        
        this.createMainLogger();
        final String filePath = this.createFilePath();
        final String dataPath = this.createDataPath();
        final String pluginPath = this.createPluginPath();
        this.applyConfig(filePath, dataPath, pluginPath);
        this.serverThread = new Thread(new Runnable() {
            
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                // mock console reader
                try
                {
                    final ConsoleReader reader = new ConsoleReader(new InputStream() {
                        
                        /** the buffer */
                        private int[] buffer = new int[0];
                        /** the pos in buffer */
                        private int   pos;
                        
                        @Override
                        public int read() throws IOException
                        {
                            while (this.buffer.length > this.pos)
                            {
                                return this.buffer[this.pos++];
                            }
                            while (true)
                            {
                                synchronized (NukkitTestSession.this.injectedCommands)
                                {
                                    if (!NukkitTestSession.this.injectedCommands.isEmpty())
                                    {
                                        final String cmd = NukkitTestSession.this.injectedCommands.poll() + "\r\n"; //$NON-NLS-1$
                                        System.out.print(cmd);
                                        final byte[] bytes = cmd.getBytes(Charsets.UTF_8);
                                        this.buffer = new int[bytes.length];
                                        this.pos = 0;
                                        for (int i = 0; i < this.buffer.length; i++)
                                        {
                                            this.buffer[i] = bytes[i] & 0xFF;
                                        }
                                        return this.buffer[this.pos++];
                                    }
                                    if (!Server.getInstance().isRunning())
                                    {
                                        return -1;
                                    }
                                    try
                                    {
                                        NukkitTestSession.this.injectedCommands.wait(500);
                                    }
                                    catch (InterruptedException ex)
                                    {
                                        // silently ignore
                                    }
                                }
                            }
                        }
                        
                    }, System.out);
                    whenNew(ConsoleReader.class).withAnyArguments().thenReturn(reader);
                    
                    final ServerScheduler scheduler = new ServerScheduler();
                    NukkitTestSession.this.schedulerSpy = spy(scheduler);
                    mock(ServerScheduler.class);
                    whenNew(ServerScheduler.class).withNoArguments().thenReturn(NukkitTestSession.this.schedulerSpy);
                    
                    doAnswer(new Answer<Void>() {
                        
                        @Override
                        public Void answer(InvocationOnMock invocation) throws Throwable
                        {
                            scheduler.mainThreadHeartbeat(1);
                            synchronized (NukkitTestSession.this)
                            {
                                NukkitTestSession.this.finishedStart = true;
                                NukkitTestSession.this.notifyAll();
                            }
                            return null;
                        }
                    }).when(NukkitTestSession.this.schedulerSpy).mainThreadHeartbeat(1);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    // should never happen
                }
                
                new Server(NukkitTestSession.this.logger, filePath, dataPath, pluginPath);
                synchronized (NukkitTestSession.this)
                {
                    NukkitTestSession.this.finishedStop = true;
                    NukkitTestSession.this.notifyAll();
                }
            }
            
        });
        this.serverThread.start();
        if (this.isConfigured())
        {
            // wait for initialization phase
            assertLogEvent(this, (event) -> event.getMessage().startsWith("Loading"), "missing Loading log message", 1000); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            // wait for the language question
            assertLogEvent(this, (event) -> event.getMessage().startsWith(TextFormat.GREEN + "Welcome!"), "missing welcome log message", 1000); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (criteria != null)
        {
            for (final NukkitServerCriteria c : criteria)
            {
                if (c instanceof NukkitServerAfterStartupCriteria)
                {
                    ((NukkitServerAfterStartupCriteria) c).afterStartup(this, Server.getInstance());
                }
            }
        }
        
        return this;
    }
    
    /**
     * Creates the config and prepares the files
     * 
     * @param filePath
     *            file path
     * @param dataPath
     *            data path
     * @param pluginPath
     *            plugin path
     */
    private void applyConfig(String filePath, String dataPath, String pluginPath)
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Checks if the server is well configured to directly start minecraft.
     * 
     * <p>
     * This method is only meaningful if the server is already started.
     * </p>
     * 
     * @return {@code true} if the server is well configured
     */
    public boolean isConfigured()
    {
        final String path = this.createDataPath();
        final File ymlConfig = new File(path, "nukkit.yml"); //$NON-NLS-1$
        return ymlConfig.exists();
    }
    
    /**
     * Checks if the files will be deleted after finish
     * 
     * @return {@code true} if the files are deleted on finish
     */
    public boolean isDeletingOnFinish()
    {
        return this.deletingOnFinish;
    }
    
    /**
     * Checks if the files will be deleted after vm exit
     * 
     * @return {@code true} if the files are deleted on vm exit
     */
    public boolean isDeletingOnVmExit()
    {
        return this.deletingOnVmExit;
    }
    
    /**
     * Creates the plugin path
     * 
     * @return plugin path
     */
    private String createPluginPath()
    {
        if (this.pluginFolder != null)
        {
            return this.pluginFolder;
        }
        
        final File tmp = new File(this.createFilePath() + File.separator + "plugins"); //$NON-NLS-1$
        tmp.delete();
        tmp.mkdirs();
        tmp.deleteOnExit();
        return tmp.getAbsolutePath();
    }
    
    /**
     * Returns the data folder
     * 
     * @return data folder
     */
    private String createDataPath()
    {
        if (this.dataFolder != null)
        {
            return this.dataFolder;
        }
        
        final File tmp = new File(this.createFilePath() + File.separator + "data"); //$NON-NLS-1$
        tmp.delete();
        tmp.mkdirs();
        tmp.deleteOnExit();
        return tmp.getAbsolutePath();
    }
    
    /**
     * Creates the file path to be used
     * 
     * @return file path
     */
    private String createFilePath()
    {
        if (this.serverFolder != null)
        {
            return this.serverFolder;
        }
        
        try
        {
            if (this.serverFolderParent == null)
            {
                this.serverFolderParent = System.getenv("NUKKIT_TEST_PATH"); //$NON-NLS-1$
            }
            final File temp = File.createTempFile("junit", "", this.serverFolderParent == null ? null : new File(this.serverFolderParent)); //$NON-NLS-1$ //$NON-NLS-2$
            temp.delete();
            temp.mkdirs();
            temp.deleteOnExit();
            this.serverFolder = temp.getAbsolutePath();
            return this.serverFolder;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * inject a private static field
     * 
     * @param clazz
     * @param field
     * @param value
     */
    static void injectPrivateStaticField(Class<?> clazz, String field, Object value)
    {
        try
        {
            final Field f = clazz.getDeclaredField(field);
            final Field modifiersField = Field.class.getDeclaredField("modifiers"); //$NON-NLS-1$
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.setAccessible(true);
            f.set(null, value);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }
    
    /**
     * Returns the main logger instance
     * 
     * @return main logger
     */
    private MainLogger createMainLogger()
    {
        if (this.logger == null)
        {
            final MainLogger orig = new MainLogger(this.createFilePath() + File.separator + "nukkit.log"); //$NON-NLS-1$
            this.logger = spy(orig);
            // inject spy into static instance
            NukkitTestSession.injectPrivateStaticField(MainLogger.class, "logger", this.logger); //$NON-NLS-1$
            
            // mock methods
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.ALERT, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.alert(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).alert(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.CRITICAL, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.critical(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).critical(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.DEBUG, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.debug(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).debug(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.EMERGENCY, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.emergency(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).emergency(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.ERROR, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.error(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).error(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.INFO, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.info(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).info(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.NOTICE, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.notice(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).notice(anyString());
            
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final String msg = invocation.getArgumentAt(0, String.class);
                    synchronized (NukkitTestSession.this.logEvents)
                    {
                        NukkitTestSession.this.logEvents.notifyAll();
                        NukkitTestSession.this.logEvents.add(new LogEvent(LogLevel.WARNING, msg));
                    }
                    if (NukkitTestSession.this.getServer().isRunning())
                    {
                        orig.warning(msg);
                    }
                    else
                    {
                        System.out.println(msg);
                    }
                    return null;
                }
            }).when(this.logger).warning(anyString());
        }
        return this.logger;
    }
    
    /**
     * Stops the nukkit Server
     * 
     * @return this test session instance
     */
    public NukkitTestSession stop()
    {
        assertTrue(this.isRunning(), "Server not started"); //$NON-NLS-1$
        Server.getInstance().shutdown();
        return this;
    }
    
    /**
     * Sets the server folder to be used
     * 
     * @param string
     * @return this test session instance
     */
    public NukkitTestSession setServerFolder(String string)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.serverFolder = string;
        return this;
    }
    
    /**
     * Sets the server folder parent to be used (the folder where a temporary folder is created in)
     * 
     * @param string
     * @return this test session instance
     */
    public NukkitTestSession setServerFolderParent(String string)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.serverFolderParent = string;
        return this;
    }
    
    /**
     * Sets the data folder to be used
     * 
     * @param string
     * @return this test session instance
     */
    public NukkitTestSession setDataFolder(String string)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.dataFolder = string;
        return this;
    }
    
    /**
     * Sets the plugin folder to be used
     * 
     * @param string
     * @return this test session instance
     */
    public NukkitTestSession setPluginFolder(String string)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.pluginFolder = string;
        return this;
    }
    
    /**
     * Sets the deleting on server finish flag
     * 
     * @param deletingOnFinish
     *            the deletingOnVmFinish to set
     * @return this test session instance
     */
    public NukkitTestSession setDeletingOnFinish(boolean deletingOnFinish)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.deletingOnFinish = deletingOnFinish;
        return this;
    }
    
    /**
     * Sets the deleting on vm exit flag
     * 
     * @param deletingOnVmExit
     *            the deletingOnVmExit to set
     * @return this test session instance
     */
    public NukkitTestSession setDeletingOnVmExit(boolean deletingOnVmExit)
    {
        assertFalse(this.isRunning(), "Server already started"); //$NON-NLS-1$
        
        this.deletingOnVmExit = deletingOnVmExit;
        return this;
    }
    
    /**
     * Returns the log events fetched from nukkit server till start or last call of {@link #clearLogEvents}
     * 
     * @return log events
     */
    public LogEvent[] getLogEvents()
    {
        synchronized (this.logEvents)
        {
            return this.logEvents.toArray(new LogEvent[0]);
        }
    }
    
    /**
     * Clears all log events
     */
    public void clearLogEvents()
    {
        synchronized (this.logEvents)
        {
            this.logEvents.clear();
        }
    }
    
    /**
     * Helper class for caught log events
     */
    public final static class LogEvent
    {
        /** the log level */
        private final LogLevel level;
        
        /** the message */
        private final String   message;
        
        /**
         * @param level
         * @param msg
         */
        public LogEvent(LogLevel level, String msg)
        {
            this.level = level;
            this.message = msg;
        }
        
        /**
         * @return the level
         */
        public LogLevel getLevel()
        {
            return this.level;
        }
        
        /**
         * @return the message
         */
        public String getMessage()
        {
            return this.message;
        }
    }
    
    /**
     * Waits for given log message to arrive within given amout of millis
     * 
     * @param check
     * @param millis
     * @return {@code true} if the log message was present
     */
    public boolean waitForLog(LogEventCheck check, int millis)
    {
        final long start = System.currentTimeMillis();
        final long max = start + millis;
        if (checkLog(check))
            return true;
        while (System.currentTimeMillis() < max)
        {
            long count = Math.min(max - System.currentTimeMillis(), 100);
            synchronized (this.logEvents)
            {
                try
                {
                    this.logEvents.wait(count);
                }
                catch (InterruptedException ex)
                {
                    // silently ignore
                }
            }
            if (checkLog(check))
                return true;
        }
        return false;
    }
    
    /**
     * Checks if given log message is present
     * 
     * @param check
     * @return {@code true} if the log message was present
     */
    public boolean checkLog(LogEventCheck check)
    {
        LogEvent[] events;
        synchronized (this.logEvents)
        {
            events = this.logEvents.toArray(new LogEvent[this.logEvents.size()]);
        }
        for (final LogEvent event : events)
        {
            if (check.check(event))
                return true;
        }
        return false;
    }
    
    /**
     * Returns the nukkit server
     * 
     * @return nukkit server
     */
    public Server getServer()
    {
        return Server.getInstance();
    }
    
    /**
     * Waits for entering the main loop; waits for finished startup
     * 
     * @param millis
     *            milliseconds to wait
     * @return this test session
     */
    public NukkitTestSession waitMainLoop(int millis)
    {
        synchronized (this)
        {
            if (!this.finishedStart)
            {
                try
                {
                    this.wait(millis);
                }
                catch (InterruptedException ex)
                {
                    // silently ignore
                }
            }
        }
        
        assertTrue(this.finishedStart, "Nukkit did not start within " + millis + " milliseconds"); //$NON-NLS-1$ //$NON-NLS-2$
        
        return this;
    }
    
    /**
     * Waits for clean shutdown
     * @param millis
     * @return this test session
     */
    public NukkitTestSession waitShutdownComplete(int millis)
    {
        synchronized (this)
        {
            if (!this.finishedStop)
            {
                try
                {
                    this.wait(millis);
                }
                catch (InterruptedException ex)
                {
                    // silently ignore
                }
            }
        }
        
        assertTrue(this.finishedStop, "Nukkit did not stop within " + millis + " milliseconds"); //$NON-NLS-1$ //$NON-NLS-2$
        
        return this;
    }
    
    /**
     * Semds given string to console
     * 
     * @param msg
     * @return this test session
     */
    public NukkitTestSession sendConsoleString(String msg)
    {
        return sendConsoleString(() -> msg);
    }
    
    /**
     * Semds given string to console
     * 
     * @param msg
     * @return this test session
     */
    public NukkitTestSession sendConsoleString(Supplier<String> msg)
    {
        assertTrue(this.isRunning(), "Server not started"); //$NON-NLS-1$
        
        synchronized (this.injectedCommands)
        {
            this.injectedCommands.add(msg.get());
            this.injectedCommands.notifyAll();
        }
        return this;
    }
    
}
