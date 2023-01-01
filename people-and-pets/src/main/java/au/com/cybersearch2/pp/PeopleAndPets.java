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

import java.util.concurrent.TimeUnit;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classytask.DefaultTaskMessenger;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import au.com.cybersearch2.pp.jpa.PeopleAndPetsFactory;
import au.com.cybersearch2.pp.jpa.PeopleUpdate;
import au.com.cybersearch2.pp.jpa.PetsUpdate;


public class PeopleAndPets {

    static public final int JPA_VERSION = 1;
    static public final String PETS_PU = "pets";
    static public final String PEOPLE_PU = "people";

    static public final String ALL_PET_DATA = "all_pet_data";

    static public final String ALL_PERSON_DATA = "all_person_data";
    
	public static final Object SEPARATOR_LINE = "------------------------------------------\n"; 
	
	private static JavaLogger logger = JavaLogger.getLogger(PeopleAndPets.class);


    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    protected PersistenceContext persistenceContext;
    private TaskExecutor taskExecutor;
    private final TaskMessenger<Void,Boolean> taskMessenger;
    
    protected class ApplicationComponent {

    	private PeopleAndPetsFactory peopleAndPetsFactory;
    	
    	public ApplicationComponent(int version) {
    		peopleAndPetsFactory = new PeopleAndPetsFactory(version);
    		prepareDatabaseSupport(peopleAndPetsFactory.getDatabaseSupport());
    	}

		public PersistenceContext persistenceContext() {
			return peopleAndPetsFactory.getPersistenceContext();
		}

		public WorkStatus execute(PersistenceWorkModule persistenceWorkModule) {
			TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext());
			try {
				taskStatus.await(5, TimeUnit.SECONDS);
				return taskStatus.getWorkStatus();
			} catch (InterruptedException e) {
				return WorkStatus.FAILED;
			}
		}
		
		protected void prepareDatabaseSupport(DatabaseSupport databaseSupport) {
			
		}
	}

    public PeopleAndPets(TaskExecutor taskExecutor) 
    {
    	this.taskExecutor = taskExecutor;
        taskMessenger = new DefaultTaskMessenger<Void>(Void.class);
    }
    
	public PersistenceContext getPersistenceContext()
	{
	    return persistenceContext;
	}
	
	public int performTasks(ObjectsStore objectsStore) {
        int status = 0;
        try
        {
            // Run tasks serially to exercise databases
            PetsUpdate petsUpdate = new PetsUpdate(objectsStore, "main");
            performPersistenceWork(PETS_PU, petsUpdate);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
            PeopleUpdate peopleUpdate = new PeopleUpdate(objectsStore, "main");
            performPersistenceWork(PEOPLE_PU, peopleUpdate);
            PeopleAndPetsMain.displayVersions(getPersistenceContext());
            PeopleAndPetsMain.logInfo("Tasks completed successfully");
            displayMessage(sb
					.append(SEPARATOR_LINE)
					.append(petsUpdate.getMessage())
					.append(SEPARATOR_LINE)
					.append(peopleUpdate.getMessage())
					.toString());
        }
        catch (Throwable  e)
        {
            e.printStackTrace();
            status = 1;
        }
        finally
        {
        	shutdown();
        }
        return status;
	}
	
    /**
     * Initialize application and database and return flag set true if successful
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @return boolean
     */
    public boolean setUp()
    {
    	persistenceContext = initializeApplication();
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

    public void shutdown()
    {
    	taskMessenger.shutdown();
    	if (persistenceContext != null)
    	{
	        String[] puNames = { PETS_PU, PEOPLE_PU }; 
	        {
	            for (String puName: puNames)
	                persistenceContext.getPersistenceAdmin(puName).close();
	        }
	        persistenceContext = null;
    	}
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
        persistenceWorkModule = new PersistenceWorkModule(puName, persistenceWork, taskMessenger, taskExecutor);
        return component.execute(persistenceWorkModule);
    }

    protected PersistenceContext initializeApplication()
    {
        // Set up dependency injection, which creates an ObjectGraph from a PeopleAndPetsModule configuration object
        component = new ApplicationComponent(JPA_VERSION);
        return component.persistenceContext();
    }
    
    protected void initializeDatabase() throws InterruptedException
    {
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistenceContext.getPersistenceAdmin(PETS_PU);
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator<PetData> allPetDataObjects = 
                new QueryForAllGenerator<PetData>(PetData.class, persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(PetData.class, ALL_PET_DATA, allPetDataObjects);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistenceContext.getPersistenceAdmin(PEOPLE_PU);
        QueryForAllGenerator<PersonData> allPersonDataObjects = 
                new QueryForAllGenerator<PersonData>(PersonData.class, persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(PersonData.class, ALL_PERSON_DATA, allPersonDataObjects);
       	populateDatabases();
    }
    
	/**
	 * Display message to user
	 * @param message Message
	 */
    public void displayMessage(String message)
	{
		System.out.println(message);
	}

	/**
	 * Populate databases with initial sample data
	 * @throws InterruptedException if interrupted
	 */
    private void populateDatabases() throws InterruptedException
	{
         // PersistenceUnitAdmin task adds 2 PetData entity objects to the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PETS_PU, new PersistenceTask(){

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
    	// PersistenceUnitAdmin task adds 2 PersonData entity objects to the helloTwoDb2.db database using JPA.
		performPersistenceWork(PEOPLE_PU, new PersistenceTask(){

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
	}


}
