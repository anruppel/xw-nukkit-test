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
package eu.xworlds.nukkit.servertests;

import static eu.xworlds.nukkit.test.Criterias.selectLangAndWaitStarted;

import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

import eu.xworlds.nukkit.test.NukkitExtension;
import eu.xworlds.nukkit.test.NukkitTestSession;
import eu.xworlds.nukkit.test.sample.PowermockExtension;

/**
 * A simple test case that stops the server via console
 * 
 * @author mepeisen
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({Server.class, CommandReader.class, ConsoleReader.class, ServerScheduler.class})
@RunWith(JUnit5.class)
@ExtendWith(NukkitExtension.class)
@ExtendWith(PowermockExtension.class)
public class StopCommandTest
{
    
    /**
     * Tests selecting the language and starting the server, stopping via console
     */
//     @org.junit.Test
    @org.junit.gen5.api.Test
    public void testConsoleStop()
    {
        final NukkitTestSession session = new NukkitTestSession();
        session.startWith(
            selectLangAndWaitStarted("eng") //$NON-NLS-1$
        );
        
        session.sendConsoleString("stop").waitShutdownComplete(5000); //$NON-NLS-1$
    }
    
}
