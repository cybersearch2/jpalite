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
package au.com.cybersearch2.pp.jpa;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseType;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

/**
 * PeopleAndPetsModule
 * Constructs application object graph. 
 * Allows for upgrades by having a version passed as a parameter.
 * This is used to determine the resources location containing the 
 * persistence.xml JPA configuration file. It also allows the
 * default persistence in memory to be changed to disk by using
 * the "persist-on-disk" environment parameter.
 * @author Andrew Bowley
 */
public class PeopleAndPetsModule
{
	public static String PERSIST_ON_DISK = "persist-on-disk";

	private final PersistenceFactory persistenceFactory;
	
	private ConnectionType connectionType;
   	private PersistenceContext persistenceContext;

    public PeopleAndPetsModule(ResourceEnvironment resourceEnvironment) {
    	if (isPersistOnDisk())
    		connectionType = ConnectionType.file;
    	else
       		connectionType = ConnectionType.memory;
    	persistenceFactory = new PersistenceFactory(DatabaseType.H2, connectionType, resourceEnvironment);
    }
    
    public  PersistenceContext providePersistenceContext()
    {
    	if (persistenceContext == null)
    		persistenceContext = persistenceFactory.persistenceContextInstance();
    	return persistenceContext;
    }
    
    private boolean isPersistOnDisk() 
    {
    	String peristOnDisk = System.getProperty(PERSIST_ON_DISK);
    	if ("true".equals(peristOnDisk))
    		return true;
    	if ("false".equals(peristOnDisk))
    		return false;
    	peristOnDisk = System.getenv(PERSIST_ON_DISK);
    	return ("true".equals(peristOnDisk));
    }

	public DatabaseSupport getDatabaseSupport() {
		return persistenceFactory.getDatabaseSupport();
	}
}
