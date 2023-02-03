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

import com.j256.ormlite.logger.Logger;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classylog.LogManager;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.container.JpaContainer;
import au.com.cybersearch2.container.JpaProcess;
import au.com.cybersearch2.container.PersistenceUnit;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import au.com.cybersearch2.pp.jpa.PeopleUpdate;
import au.com.cybersearch2.pp.jpa.PetsUpdate;


public class PeopleAndPets {

    static public final String PETS_PU = "pets";
    static public final String PEOPLE_PU = "people";

    static public final String ALL_PET_DATA = "all_pet_data";

    static public final String ALL_PERSON_DATA = "all_person_data";
    
	public static final Object SEPARATOR_LINE = "------------------------------------------\n"; 
	
	private static Logger logger = LogManager.getLogger(PeopleAndPets.class);

	protected JpaContainer jpaContainer;
    
	public int performTasks(ObjectsStore objectsStore) {
        int status = 0;
        // Run tasks serially to exercise databases
        PetsUpdate petsUpdate = new PetsUpdate(objectsStore, "main");
        performPersistenceWork(PETS_PU, petsUpdate);
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
        PeopleUpdate peopleUpdate = new PeopleUpdate(objectsStore, "main");
        performPersistenceWork(PEOPLE_PU, peopleUpdate);
        jpaContainer.forEach(unit -> displayVersion(unit.getPersistenceUnitName()));
        PeopleAndPetsMain.logInfo("Tasks completed successfully");
        displayMessage(sb
				.append(SEPARATOR_LINE)
				.append(petsUpdate.getMessage())
				.append(SEPARATOR_LINE)
				.append(peopleUpdate.getMessage())
				.toString());
        return status;
	}
	
    /**
     * Initialize application and database and return flag set true if successful
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @return boolean
     */
    public boolean setUp()
    {
		try {
			jpaContainer = new JpaContainer();
			jpaContainer.initialize(getJpaVersion());
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		try {
			initializeDatabase();
		} catch (InterruptedException e) {
			logger.error("Database initialization interrupted");
			return false;
		} catch (Throwable e) {
			logger.error("Database initialization failed", e);
			return false;
		}
		return true;
     }

	public void close() throws InterruptedException {
     	jpaContainer.close();
    }
 
    /**
     * Launch persistence work to run in background thread
     * @param puName PersistenceUnitAdmin Unit name
     * @param persistenceTask PersistenceTask object
     * @return Executable object to signal completion of task
     */
    public WorkStatus performPersistenceWork(final String puName, final PersistenceTask persistenceTask)
    {
        // There will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork persistenceWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
            	persistenceTask.doTask(entityManager);
                // Database updates committed upon exit
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
    	JpaProcess process = jpaContainer.execute(puName, persistenceWork);
		return process.exitValue();
    }

	/**
	 * Display the current JPA schema version of a persistence unit
	 * @param unitName Persistence unit name
	 */
   public int getVersion(String unitName) {
	    PersistenceUnit unit = jpaContainer.getUnit(unitName);
		return unit.getPersistenceAdmin().getVersion();
   }
   
	/**
	 * Display the current JPA schema version of a persistence unit
	 * @param unit Persistence unit
	 */
   public void displayVersion(String unitName) {
	    int unitVersion = getVersion(unitName);
		System.out.println(String.format("%s version = %s", unitName,  unitVersion));
    }
    
    protected boolean initializeDatabase() throws InterruptedException
    {
    	PersistenceUnit petsUnit = jpaContainer.getUnit(PETS_PU);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = petsUnit.getPersistenceAdmin();
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator<PetData> allPetDataObjects = 
                new QueryForAllGenerator<PetData>(PetData.class, persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(PetData.class, ALL_PET_DATA, allPetDataObjects);
    	PersistenceUnit peopleUnit = jpaContainer.getUnit(PEOPLE_PU);
       // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = peopleUnit.getPersistenceAdmin();
        QueryForAllGenerator<PersonData> allPersonDataObjects = 
                new QueryForAllGenerator<PersonData>(PersonData.class, persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(PersonData.class, ALL_PERSON_DATA, allPersonDataObjects);
       	return populateDatabases();
    }
    
	/**
	 * Display message to user
	 * @param message Message
	 */
    public void displayMessage(String message)
	{
		System.out.println(message);
	}

    protected int getJpaVersion() {
		return 1;
	}

	/**
	 * Populate databases with initial sample data
	 * @throws InterruptedException if interrupted
	 */
    private boolean populateDatabases() throws InterruptedException
	{
         // PersistenceUnitAdmin task adds 2 PetData entity objects to the helloTwoDb1.db database using JPA. 
		WorkStatus status = performPersistenceWork(PETS_PU, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				// create some entries in the onCreate
				Pet pet1 = new PetData("Fido");
                entityManager.persist(pet1);
                Pet pet2 = new PetData("Bruiser");
                entityManager.persist(pet2);
                PeopleAndPetsMain.logInfo(PETS_PU, "Created new PetData entries");
			}});
		if (status != WorkStatus.FINISHED)
			return false;
    	// PersistenceUnitAdmin task adds 2 PersonData entity objects to the helloTwoDb2.db database using JPA.
		status = performPersistenceWork(PEOPLE_PU, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				// create some entries in the onCreate
				Person person1 = new PersonData("Alice");
				entityManager.persist(person1);
				Person person2 = new PersonData("Robert");
				entityManager.persist(person2);
				PeopleAndPetsMain.logInfo(PEOPLE_PU, "Created new PersonData entries");
			}});
		return status == WorkStatus.FINISHED;
	}

}
