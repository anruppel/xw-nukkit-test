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

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionContext.Store;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import cn.nukkit.Server;
import cn.nukkit.command.CommandReader;
import cn.nukkit.scheduler.ServerScheduler;
import eu.xworlds.nukkit.test.api.NukkitInject;
import jline.console.ConsoleReader;

/**
 * JUnit5 Extension for nukkit
 * 
 * @author mepeisen
 */
public class NukkitExtension implements InstancePostProcessor, MethodParameterResolver, AfterEachExtensionPoint, BeforeEachExtensionPoint
{
    
    /** the extension namespace */
    private static final Namespace NS = Namespace.of(NukkitExtension.class);
    
    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception
    {
        // mokito support
        MockitoAnnotations.initMocks(context.getTestInstance());
        PowerMockito.mock(Server.class);
        PowerMockito.mock(CommandReader.class);
        PowerMockito.mock(ConsoleReader.class);
        PowerMockito.mock(ServerScheduler.class);
    }
    
    @Override
    public Object resolve(Parameter param, MethodInvocationContext methodInvocationContext, ExtensionContext extensionContext) throws ParameterResolutionException
    {
        final Store mocks = extensionContext.getStore(NS);
        return getMockWithoutCast(extensionContext, param.getType(), mocks);
    }
    
    @Override
    public boolean supports(Parameter param, MethodInvocationContext methodInvocationContext, ExtensionContext extensionContext) throws ParameterResolutionException
    {
        return param.isAnnotationPresent(NukkitInject.class);
    }
    
//    /**
//     * Calculates the mocking class
//     * 
//     * @param extensionContext
//     * @param mockType
//     * @param mocks
//     * @return mocking class
//     */
//    private <T> T getMock(ExtensionContext extensionContext, Class<T> mockType, Store mocks)
//    {
//        return mockType.cast(mocks.getOrComputeIfAbsent(mockType, type -> mock(extensionContext, mockType, mocks)));
//    }
    
    /**
     * Calculates the mocking class
     * 
     * @param extensionContext
     * @param mockType
     * @param mocks
     * @return mocking class
     */
    private Object getMockWithoutCast(ExtensionContext extensionContext, Class<?> mockType, Store mocks)
    {
        return mocks.getOrComputeIfAbsent(mockType, type -> mock(extensionContext, mockType, mocks));
    }
    
    /**
     * Creates a mock
     * 
     * @param extensionContext
     * @param mockType
     * @param mocks
     * @return mocking class
     */
    private Object mock(ExtensionContext extensionContext, Class<?> mockType, Store mocks)
    {
        if (mockType.getName().equals(NukkitTestSession.class.getName()))
        {
            return new NukkitTestSession();
        }
        // TODO support additional nukkit classes
        
        // fall back to mockito
        return Mockito.mock(mockType);
    }
    
    /**
     * @see org.junit.gen5.api.extension.AfterEachExtensionPoint#afterEach(org.junit.gen5.api.extension.TestExtensionContext)
     */
    @Override
    public void afterEach(TestExtensionContext context) throws Exception
    {
        // currently empty
    }
    
    /**
     * @see org.junit.gen5.api.extension.BeforeEachExtensionPoint#beforeEach(org.junit.gen5.api.extension.TestExtensionContext)
     */
    @Override
    public void beforeEach(TestExtensionContext context) throws Exception
    {
        // currently empty
    }
    
}
