package au.com.cybersearch2.pp.v2;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.PeopleAndPetsMain;
import au.com.cybersearch2.pp.PersonData;
import au.com.cybersearch2.pp.PetData;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import au.com.cybersearch2.pp.jpa.PeopleAndPetsModule;
import au.com.cybersearch2.pp.jpa.PetsUpdate;

public class PP_UpdateTest {

    private static TaskExecutor taskExecutor;
    private static ObjectsStore objectsStoreV2 = new ObjectsStore() {

		@Override
		public Person personInstance(String name) {
			return new PersonDataV2(name, QuoteSource.getQuote());
		}

		@Override
		public Pet petInstance(String name) {
			return new PetDataV2(name, QuoteSource.getQuote());
		}};

	@BeforeClass
	public static void beforeClass() {
     	taskExecutor = new DefaultTaskExecutor();
	}

    @AfterClass
    public static void afterClass() {
    	taskExecutor.shutdown();
    }
    
    @Test 
    public void test_upgrade() throws Exception
    {
		deleteDatabaseFile();
    	doPeoplePetsV1Test("Version 1");
    	DaoManager.clearCache();

    	String context = "test_upgrade";
    	doSingleTest(context);
    	// Do second time for start at JPA version 2
    	doSingleTest(context);
    }

    /**
     * Run people-and-pets version 1 in separate thread
     * @param context Logging context
     * @throws Exception
     */
    private void doPeoplePetsV1Test(String context) throws Exception
    {
 	   final Semaphore semaphore = new Semaphore(1);
 	   semaphore.acquireUninterruptibly(1);
 	   Thread thread = new Thread(new Runnable() {

 		@Override
 		public void run() {
 			PeopleAndPets peopleAndPetsV1 = null;
 			try {
 		       	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "true");
 		        peopleAndPetsV1 = new PeopleAndPets(taskExecutor);
 		        assertThat(peopleAndPetsV1.setUp()).isTrue();
 		        PersistenceContext persistenceContext = peopleAndPetsV1.getPersistenceContext();
 		        assertThat(getVersion(persistenceContext, PeopleAndPets.PEOPLE_PU)).isEqualTo(1);
 		        assertThat(getVersion(persistenceContext, PeopleAndPets.PETS_PU)).isEqualTo(1);
 		        PetsUpdate petsUpdate = new PetsUpdate(new ObjectsStore() {
 		
 		    		@Override
 		    		public Person personInstance(String name) {
 		    			return new PersonData(name);
 		    		}
 		
 		    		@Override
 		    		public Pet petInstance(String name) {
 		    			return new PetData(name);
 		    		}}, context);
 		        WorkStatus workStatus = peopleAndPetsV1.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate);
 		        assertThat(workStatus == WorkStatus.FINISHED);
 		        PeopleAndPetsMain.logInfo("Version 1 test completed successfully");
 				// Our string builder for building the content-view
 				StringBuilder sb = new StringBuilder();
 				peopleAndPetsV1.displayMessage(sb
 						.append(PeopleAndPets.SEPARATOR_LINE)
 						.append(petsUpdate.getMessage())
 						.append(PeopleAndPets.SEPARATOR_LINE)
 						.append(petsUpdate.getMessage())
 						.toString());
 			    } catch (Throwable t) {
 			    	t.printStackTrace();
 			    } finally {
 			    	peopleAndPetsV1.shutdown();
 			    	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "");
 			    	semaphore.release();
 			    }
 		    }
 	   });
 	   thread.start();
 	   semaphore.acquireUninterruptibly(1);
     }

    /**
     * Run people-and-pets version 2 in separate thread
     * @param context Logging context
     * @throws Exception
     */
    private void doSingleTest(String context) throws Exception
    {
  	   final Semaphore semaphore = new Semaphore(1);
 	   semaphore.acquireUninterruptibly(1);
  	   Thread thread = new Thread(new Runnable() {

  		@Override
  		public void run() {
  			PeopleAndPetsV2 peopleAndPets = null;
  			try {
		         System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "true");
		         peopleAndPets = new PeopleAndPetsV2(taskExecutor);
		         assertThat(peopleAndPets.setUp()).isTrue();
		         PersistenceContext persistenceContext = peopleAndPets.getPersistenceContext();
		         assertThat(getVersion(persistenceContext, PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
		         assertThat(getVersion(persistenceContext, PeopleAndPets.PETS_PU)).isEqualTo(2);
		         PetsUpdate petsUpdate = new PetsUpdate(objectsStoreV2, context);
		         WorkStatus workStatus = peopleAndPets.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate);
		         assertThat(workStatus == WorkStatus.FINISHED);
		         assertThat(getVersion(persistenceContext, PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
		         assertThat(getVersion(persistenceContext, PeopleAndPets.PETS_PU)).isEqualTo(2);
		         PeopleAndPetsMain.logInfo("Test completed successfully");
		 		// Our string builder for building the content-view
		 		StringBuilder sb = new StringBuilder();
		         peopleAndPets.displayMessage(sb
		 				.append(PeopleAndPets.SEPARATOR_LINE)
		 				.append(petsUpdate.getMessage())
		 				.append(PeopleAndPets.SEPARATOR_LINE)
		 				.append(petsUpdate.getMessage())
		 				.toString());
			    } catch (Throwable t) {
 			    	t.printStackTrace();
 			    } finally {
 			    	peopleAndPets.shutdown();
 			    	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "");
 			    	semaphore.release();
 			    }
 		    }
 	   });
 	   thread.start();
 	   semaphore.acquireUninterruptibly(1);
    }
    
    private void deleteDatabaseFile() throws InterruptedException {
    	
    	File defaultDatabaseFile = new File(DatabaseSupportBase.DEFAULT_FILE_LOCATION);
    	FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("people-and-pets");
			}
    		
    	};
    	File[] files = defaultDatabaseFile.listFiles(filter);
    	Arrays.asList(files).forEach(file -> {
    		try {
    			file.delete();
    		} catch (Throwable t) {
    			t.printStackTrace();
    		}
    	});
    }
    
    private static int getVersion(PersistenceContext persistenceContext, String persistenceUnit) 
    {
 		PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(persistenceUnit);
 		DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
 		ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
 		return databaseSupport.getVersion(connectionSource, persistenceAdmin.getProperties());
    }
    
}
