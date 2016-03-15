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
package eu.xworlds.nukkit.spowermock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.xworlds.nukkit.test.sample.ATestable;
import eu.xworlds.nukkit.test.sample.ToTest;

/**
 * @author mepeisen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ATestable.class, ToTest.class})
public class Junit4Test
{
    
    @Test
    public void testMe() throws Exception
    {
        final ATestable mocked = mock(ATestable.class);
        when(mocked.getString()).thenReturn("mocked");
        whenNew(ATestable.class).withNoArguments().thenReturn(mocked);
        
        final ToTest toTest = new ToTest();
        assertEquals("mocked", toTest.getSomeString());
    }
    
}
