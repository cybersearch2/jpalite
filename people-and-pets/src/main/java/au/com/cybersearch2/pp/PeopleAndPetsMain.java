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
package au.com.cybersearch2.pp;

import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classylog.LogManager;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import pu.People;
import pu.Pets;

/**
 * This application demonstrates how Jpalite provides support for in-situ database schema updates.
 * It also show how two persistence units can share a single database which is an alternative to placing
 * each persistence unit in it's own database. 
 */
public class PeopleAndPetsMain 
{
	/** Custom log tags are supported. Along with this default, the persistence unit names are also used */
    static public final String TAG = "PeopleAndPets";
    /** Entity objects are hidden behind interfaces to allow working with different versions */
    private static final ObjectsStore objectsStoreV1;
    /** Map to implement custom log tags */
    private final static Map<String, Logger> logMap;
    private static TaskExecutor taskExecutor;

    static {
    	/** Create and populate log map */
    	logMap = new HashMap<>(2); 
    	logMap.put(TAG, LogManager.getLogger(PeopleAndPets.class));
    	logMap.put(PeopleAndPets.PETS_PU, LogManager.getLogger(Pets.class));
    	logMap.put(PeopleAndPets.PEOPLE_PU, LogManager.getLogger(People.class));


    	objectsStoreV1 = new ObjectsStore() {

    		@Override
    		public Person personInstance(String name) {
    			return new PersonData(name);
    		}

    		@Override
    		public Pet petInstance(String name) {
    			return new PetData(name);
    		}};
    }


    /**
     * Java main entry point
     * @param args Not used
     */
	public static void main(String[] args)
	{
     	taskExecutor = new DefaultTaskExecutor();
     	int returnCode = 1;
     	try {
	        PeopleAndPets peopleAndPets = new PeopleAndPets(taskExecutor);
	        if (peopleAndPets.setUp())
	        	returnCode = peopleAndPets.performTasks(objectsStoreV1);
     	} catch (Throwable t) {
     		t.printStackTrace();
     	} finally {
     		taskExecutor.shutdown();
     		System.exit(returnCode);
     	}
	}
	
	/**
	 * Log message with default tag
	 * @param message Message
	 */
	public static void logInfo(String message) 
	{
		logInfo(TAG, message);
	}

	/**
	 * Log message with given tag
	 * @param tag Log tag
	 * @param message Message
	 */
	public static void logInfo(String tag, String message) 
	{
        Logger log = logMap.get(tag);
        if ((log != null) && log.isLevelEnabled(Level.INFO))
        {
            log.info(tag, message);
        }
	}

	/**
	 * Display the current JPA schema version of both persistence units
	 * @param persistenceContext Persistence context of database in opened state
	 */
   public static void displayVersions(PersistenceContext persistenceContext) {
		PersistenceAdmin petAdmin = persistenceContext.getPersistenceAdmin(PeopleAndPets.PETS_PU);
		PersistenceAdmin personAdmin = persistenceContext.getPersistenceAdmin(PeopleAndPets.PEOPLE_PU);
		DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
		ConnectionSource connectionSource1 = petAdmin.getConnectionSource();
		int petVersion = databaseSupport.getVersion(connectionSource1, personAdmin.getProperties());
		ConnectionSource connectionSource2 = personAdmin.getConnectionSource();
		int personVersion = databaseSupport.getVersion(connectionSource2, personAdmin.getProperties());
		System.out.println(String.format("Pets version = %s, People version = %s", petVersion, personVersion));
    }
    
}
