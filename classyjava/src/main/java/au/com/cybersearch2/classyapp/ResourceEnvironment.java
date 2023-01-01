/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classyapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * ResourceEnvironment
 * Adapts access to resources according to platform and locale
 * @author Andrew Bowley
 * 16/06/2014
 */
public interface ResourceEnvironment
{
    /**
     * Provides read access to a resource stream such as a file.
     * @param resourceName Resource name
     * @return InputStream object
     * @throws IOException if IO error occurs
     */
    InputStream openResource(String resourceName) throws IOException;

    /**
     * Provides read access to a resource stream such as a file.
     * @param resourceFile File object
     * @return InputStream object
     * @throws IOException if IO error occurs
     */
    default InputStream openFile(File resourceFile) throws IOException {
        if (!resourceFile.exists())
            throw new FileNotFoundException(resourceFile.toString());
        InputStream instream = new FileInputStream(resourceFile);
        return instream;
    }
    
    /**
     * Get locale. 
     * Android lint complains if Locale is omitted where it can be specified as an optional parameter.
     * @return Locale object
     */
    default Locale getLocale() {
    	return Locale.getDefault();
    }
  
    /**
     * Returns database location when ConnectionType = "file"
     * @return File object for a directory location
     */
    default File getDatabaseDirectory() {
    	return new File(DatabaseSupport.DEFAULT_FILE_LOCATION);
    }

    /**
     * Returns Class Loader for instantiating entity classes
     * @param puName Persistence unit name - allows class loader to be specific to persistence unit
     * @return EntityClassLoader object
     */
    default EntityClassLoader getEntityClassLoader(String puName) {
    	return new EntityClassLoader() {

			@SuppressWarnings("unchecked")
			@Override
			public Class<? extends OrmEntity> loadClass(String name) throws ClassNotFoundException {
				Class<?> entityClass = Class.forName(name);
				if (OrmEntity.class.isAssignableFrom(entityClass))
				    return (Class<? extends OrmEntity>) Class.forName(name);
				else
					throw new IllegalArgumentException(String.format("%s down not implement interface OrmEntity", entityClass.getName()));
			}};
    }
}
