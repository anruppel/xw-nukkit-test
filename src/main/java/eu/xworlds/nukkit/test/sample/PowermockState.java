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

import java.util.HashSet;
import java.util.Set;

/**
 * The internal powermock state
 * 
 * @author mepeisen
 */
public class PowermockState
{
    
    /**
     * Original class loader
     */
    private ClassLoader origClassLoader;
    
    /**
     * the classes that will be prepared
     */
    private Set<Class<?>> classesToPrepare = new HashSet<>();
    
    /**
     * packages to ignore
     */
    private Set<String> packagesToIgnore = new HashSet<>();

    /**
     * @return the origClassLoader
     */
    public ClassLoader getOrigClassLoader()
    {
        return this.origClassLoader;
    }

    /**
     * @param origClassLoader the origClassLoader to set
     */
    void setOrigClassLoader(ClassLoader origClassLoader)
    {
        this.origClassLoader = origClassLoader;
    }

    /**
     * @return the classesToPrepare
     */
    public Set<Class<?>> getClassesToPrepare()
    {
        return this.classesToPrepare;
    }

    /**
     * @return
     */
    public String[] getClassesToPrepareAsString()
    {
        final String[] result = new String[this.classesToPrepare.size()];
        final Class[] classes = this.classesToPrepare.toArray(new Class[this.classesToPrepare.size()]);
        for (int i = 0; i < result.length; i++)
        {
            result[i] = classes[i].getName();
        }
        return result;
    }

    /**
     * @return the packagesToIgnore
     */
    public Set<String> getPackagesToIgnore()
    {
        return this.packagesToIgnore;
    }

    /**
     * @return
     */
    public String[] getPackagesToIgnoreAsArray()
    {
        final String[] result = this.packagesToIgnore.toArray(new String[this.packagesToIgnore.size()]);
        return result;
    }
    
}
