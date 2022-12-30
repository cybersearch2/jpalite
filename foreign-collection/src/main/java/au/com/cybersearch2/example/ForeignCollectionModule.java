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
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

/**
 * ForeignCollectionModule
 * H2 database allows multiple connections, so PersistenceContainer runs 
 * the requested task in a background thread. This means WorkerRunnable 
 * must be supported.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class ForeignCollectionModule
{
    private ResourceEnvironment resourceEnvironment;
    private H2DatabaseSupport h2DatabaseSupport;
   	private PersistenceContext persistenceContext;
    
    public ForeignCollectionModule(ResourceEnvironment resourceEnvironment)
    {
        this.resourceEnvironment = resourceEnvironment;
    }
    
    public  ResourceEnvironment provideResourceEnvironment()
    {
        return resourceEnvironment;
    }

    public  DatabaseSupport provideDatabaseSupport()
    {
        h2DatabaseSupport = new H2DatabaseSupport(ConnectionType.memory); 
        return h2DatabaseSupport;     
    }
    
    public  PersistenceFactory providePersistenceFactory()
    {
        return new PersistenceFactory(provideDatabaseSupport(), resourceEnvironment);
    }

    public  ConnectionSourceFactory provideConnectionSourceFactory()
    {
        return (ConnectionSourceFactory) provideDatabaseSupport();
    }

    public  PersistenceContext providePersistenceContext()
    {
    	if (persistenceContext == null)
    	{
    		ConnectionSourceFactory connectionSourceFactory = provideConnectionSourceFactory();
    		persistenceContext = new PersistenceContext(providePersistenceFactory(), connectionSourceFactory);
    	}
    	return persistenceContext;
    }
}
