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
package eu.xworlds.nukkit.test.sample;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeAllExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionContext.Store;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.UseClassPathAdjuster;
import org.powermock.core.spi.PowerMockPolicy;
import org.powermock.core.transformers.MockTransformer;
import org.powermock.core.transformers.impl.MainMockTransformer;
import org.powermock.reflect.Whitebox;
import org.powermock.reflect.proxyframework.RegisterProxyFramework;
import org.powermock.tests.utils.TestClassesExtractor;
import org.powermock.tests.utils.impl.ArrayMergerImpl;
import org.powermock.tests.utils.impl.MockPolicyInitializerImpl;
import org.powermock.tests.utils.impl.PowerMockIgnorePackagesExtractorImpl;
import org.powermock.tests.utils.impl.PrepareForTestExtractorImpl;

/**
 * Sample code for powermock extension
 * 
 * @author mepeisen
 */
public class PowermockExtension implements InstancePostProcessor, AfterEachExtensionPoint, BeforeEachExtensionPoint, BeforeAllExtensionPoint
{
    
    /** the extension namespace */
    private static final Namespace NS = Namespace.of(PowermockExtension.class);

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception
    {
        final PowermockState state = getState(context);
        state.setOrigClassLoader(Thread.currentThread().getContextClassLoader()); // JUnit4TestSuiteChunkerImpl:103
        Thread.currentThread().setContextClassLoader(context.getTestClass().getClassLoader());
        new MockPolicyInitializerImpl(context.getTestClass()).initialize(context.getTestClass().getClassLoader());
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception
    {
        final PowermockState state = getState(context);
        if (state.getOrigClassLoader() != null)
        {
            Thread.currentThread().setContextClassLoader(state.getOrigClassLoader()); // JUnit4TestSuiteChunkerImpl:108
            state.setOrigClassLoader(null);
        }
    }

    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception
    {
        MockRepository.clear(); // AbstractCommonPowerMockRunner->constructor
    }
    
    /**
     * Returns the current powermock state; could be used by other extensions to manipulate classesToPrepare
     * @param context
     * @return powermock state
     */
    public static PowermockState getState(ExtensionContext context)
    {
        final Store store = context.getStore(NS);
        return (PowermockState) store.getOrComputeIfAbsent(PowermockState.class, (type) -> new PowermockState());
    }

    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception
    {
        final PowermockState state = getState(context);
        
        // fetch original test object/method/class
        final Object origTestInstance = context.getTestInstance();
        final Class<?> origClass = context.getTestClass();
        final Method origMethod = context.getTestMethod();
        
        // powermock class loader and clone
        final TestClassesExtractor prepareForTestExtractor = new PrepareForTestExtractorImpl();
        final String[] prepareForTestClasses = prepareForTestExtractor.getTestClasses(origClass); // AbstractTestSuiteChunkerImpl:176
        final String[] stateTestClasses = state.getClassesToPrepareAsString(); // classes to prepare, that can be injected by other extensions
        final String[] stateIgnorePackages = state.getPackagesToIgnoreAsArray(); // packages to ignore, that can be injected by other extensions
        final String[] ignorePackages = new PowerMockIgnorePackagesExtractorImpl().getPackagesToIgnore(origClass);
        final ClassLoader defaultMockLoader = createNewClassloader(
                origClass,
                new ArrayMergerImpl().mergeArrays(String.class, prepareForTestClasses, stateTestClasses),
                new ArrayMergerImpl().mergeArrays(String.class, ignorePackages, stateIgnorePackages),
                new MockTransformer[0]); // AbstractTestSuiteChunkerImpl:178
        
        registerProxyframework(defaultMockLoader); // AbstractTestSuiteChunkerImpl:181
        
        // create new instance etc. from new class loader
        final Class<?> newTestClass = defaultMockLoader.loadClass(origClass.getName());
        final Object newTestInstance = newTestClass.newInstance();
        // find test method
        Method newTestMethod = null;
        final String origSignature = origMethod.toGenericString();
        for (final Method mth : newTestClass.getDeclaredMethods())
        {
            if (mth.getName().equals(origMethod.getName()) && mth.toGenericString().equals(origSignature))
            {
                newTestMethod = mth;
                break;
            }
        }
        if (newTestMethod == null) throw new IllegalStateException(); // should never happen as long as defaultMockLoader works correctly

        // override
        final Object testDescriptor = Whitebox.getInternalState(context, "testDescriptor");
        Whitebox.setInternalState(context, "testInstance", newTestInstance);
        Whitebox.setInternalState(testDescriptor, "testClass", (Object) newTestClass);
        Whitebox.setInternalState(testDescriptor, "testMethod", newTestMethod);
    }
    


    /**
     * @return <code>true</code> if there are some mock policies that
     *         contributes with classes that should be loaded by the mock
     *         classloader, <code>false</code> otherwise.
     */
    private boolean hasMockPolicyProvidedClasses(Class<?> testClass) {
        boolean hasMockPolicyProvidedClasses = false;
        if (testClass.isAnnotationPresent(MockPolicy.class)) {
            MockPolicy annotation = testClass.getAnnotation(MockPolicy.class);
            Class<? extends PowerMockPolicy>[] value = annotation.value();
            hasMockPolicyProvidedClasses = new MockPolicyInitializerImpl(value).needsInitialization();
        }
        return hasMockPolicyProvidedClasses;
    }
    
    // taken from AbstractTestChunkerImpl
    private void registerProxyframework(ClassLoader classLoader) {
        Class<?> proxyFrameworkClass = null;
        try {
            proxyFrameworkClass = Class.forName("org.powermock.api.extension.proxyframework.ProxyFrameworkImpl", false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Extension API internal error: org.powermock.api.extension.proxyframework.ProxyFrameworkImpl could not be located in classpath.");
        }

        Class<?> proxyFrameworkRegistrar = null;
        try {
            proxyFrameworkRegistrar = Class.forName(RegisterProxyFramework.class.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
        try {
            Whitebox.invokeMethod(proxyFrameworkRegistrar, "registerProxyFramework", Whitebox.newInstance(proxyFrameworkClass));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // taken from AbstractTestChunkerImpl
    private ClassLoader createNewClassloader(
            Class<?> testClass,
            String[] preliminaryClassesToLoadByMockClassloader,
            final String[] packagesToIgnore,
            MockTransformer... extraMockTransformers) {
        ClassLoader mockLoader = null;
        final String[] classesToLoadByMockClassloader = makeSureArrayContainsTestClassName(
                preliminaryClassesToLoadByMockClassloader, testClass.getName());
        if ((classesToLoadByMockClassloader == null || classesToLoadByMockClassloader.length == 0) && !hasMockPolicyProvidedClasses(testClass)) {
            mockLoader = Thread.currentThread().getContextClassLoader();
        } else {
            List<MockTransformer> mockTransformerChain = new ArrayList<MockTransformer>();
            final MainMockTransformer mainMockTransformer = new MainMockTransformer();
            mockTransformerChain.add(mainMockTransformer);
            Collections.addAll(mockTransformerChain, extraMockTransformers);
            final UseClassPathAdjuster useClassPathAdjuster = testClass.getAnnotation(UseClassPathAdjuster.class);
            mockLoader = AccessController.doPrivileged(new PrivilegedAction<MockClassLoader>() {
                public MockClassLoader run() {
                    return new MockClassLoader(classesToLoadByMockClassloader, packagesToIgnore, useClassPathAdjuster);
                }
            });
            MockClassLoader mockClassLoader = (MockClassLoader) mockLoader;
            mockClassLoader.setMockTransformerChain(mockTransformerChain);
            new MockPolicyInitializerImpl(testClass).initialize(mockLoader);
        }
        return mockLoader;
    }

    // taken from AbstractTestChunkerImpl
    private String[] makeSureArrayContainsTestClassName(
            String[] arrayOfClassNames, String testClassName) {
        if (null == arrayOfClassNames || 0 == arrayOfClassNames.length) {
            return new String[] {testClassName};

        } else {
            List<String> modifiedArrayOfClassNames = new ArrayList<String>(
                    arrayOfClassNames.length + 1);
            modifiedArrayOfClassNames.add(testClassName);
            for (String className : arrayOfClassNames) {
                if (testClassName.equals(className)) {
                    return arrayOfClassNames;
                } else {
                    modifiedArrayOfClassNames.add(className);
                }
            }
            return modifiedArrayOfClassNames.toArray(
                    new String[arrayOfClassNames.length + 1]);
        }
    }
    
}
