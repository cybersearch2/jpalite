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

import java.util.Map;

import javax.persistence.PersistenceException;

import com.google.common.base.Throwables;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;

/**
 * TestClassyApplicationModule
 * @author Andrew Bowley
 * 13/06/2014
 */
public class TestClassyApplicationModule
{
    private ResourceEnvironment resourceEnvironment;
    //private SQLiteDatabaseSupport sqliteDatabaseSupport;
    private H2DatabaseSupport h2DatabaseSupport;
    private PersistenceContext persistenceContext;
    private PersistenceFactory persistenceFactory;
    
    public TestClassyApplicationModule(ResourceEnvironment resourceEnvironment)
    {
        this.resourceEnvironment = resourceEnvironment;
    }
    
    public DefaultTaskExecutor provideTaskManager()
    {
        return new DefaultTaskExecutor();
    }

    public DatabaseSupport provideDatabaseSupport()
    {
        //sqliteDatabaseSupport = new SQLiteDatabaseSupport(ConnectionType.memory);
        //return sqliteDatabaseSupport;    
        h2DatabaseSupport = new H2DatabaseSupport(ConnectionType.memory); 
        return h2DatabaseSupport;     
    }
    
    public ResourceEnvironment provideResourceEnvironment()
    {
         return resourceEnvironment;
    }

    public PersistenceFactory providePersistenceFactory()
    {
    	if (persistenceFactory == null)
    	{
    		persistenceFactory =  new PersistenceFactory(provideDatabaseSupport(), resourceEnvironment) {
    			
	        	@Override
	            public void initializeAllDatabases(ConnectionSourceFactory connectionSourceFactory) {
	                //Initialize PU implementations
	                for (Map.Entry<String, DatabaseAdminImpl> entry: databaseAdminImplMap.entrySet())
	                {
	                    PersistenceAdminImpl persistenceAdmin = persistenceImplMap.get(entry.getKey());
	                    ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
	                    try {
	                    	databaseSupport.setVersion(0, persistenceAdmin.getProperties(), connectionSource);
	                    	System.out.println("Set user_info version to 0");
						} catch (PersistenceException e) {
							System.out.println(Throwables.getStackTraceAsString(e));
						}
	                    super.initializeAllDatabases(connectionSourceFactory);
	                }
	            }
	        };
        }
    	return persistenceFactory;
    }

    public ConnectionSourceFactory provideConnectionSourceFactory()
    {
        //return sqliteDatabaseSupport;
    	return h2DatabaseSupport;
    }
    
    public PersistenceContext providePersistenceContext()
    {
    	if (persistenceContext == null)
    	{
    		persistenceContext = new PersistenceContext(providePersistenceFactory(), provideConnectionSourceFactory(), true);
    	}
        return persistenceContext;
    }
    
}


