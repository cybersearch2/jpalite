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
package au.com.cybersearch2.example;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseType;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

/**
 * ManyToManyModule
 * Dependency injection data object. @see ManyToManyMain.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class ManyToManyModule
{
    private ResourceEnvironment resourceEnvironment;
   	private PersistenceContext persistenceContext;
    
    public ManyToManyModule(ResourceEnvironment resourceEnvironment)
    {
        this.resourceEnvironment = resourceEnvironment;
    }
    
    public  ResourceEnvironment provideResourceEnvironment()
    {
         return resourceEnvironment;
    }

    public  PersistenceContext providePersistenceContext()
    {
    	if (persistenceContext == null)
    		persistenceContext = 
    	        new PersistenceFactory(DatabaseType.SQLite, ConnectionType.memory, resourceEnvironment)
    	        .persistenceContextInstance();
    	return persistenceContext;
    }
    
}
