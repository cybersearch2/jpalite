package au.com.cybersearch2.pp.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.logger.Logger;

import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.classylog.LogManager;
import au.com.cybersearch2.container.JpaContainer;
import au.com.cybersearch2.container.WorkStatus;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.PeopleAndPetsMain;
import au.com.cybersearch2.pp.PersonData;
import au.com.cybersearch2.pp.PetData;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import au.com.cybersearch2.pp.jpa.PetsUpdate;

public class PP_UpdateTest {

     private static ObjectsStore objectsStoreV2 = new ObjectsStore() {

		@Override
		public Person personInstance(String name) {
			return new PersonDataV2(name, QuoteSource.getQuote());
		}

		@Override
		public Pet petInstance(String name) {
			return new PetDataV2(name, QuoteSource.getQuote());
		}};

	static LogRecordHandler logRecordHandler;

	static Logger logger;
	
	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
		logger = LogManager.getLogger(PP_UpdateTest.class);
	}

	@Before
	public void setUp() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
        File resourcePath = new File("src/main/resources");
	    System.setProperty(JpaContainer.RESOURCE_PATH_NAME, resourcePath.getAbsolutePath());
	}		
	
    @Test 
    public void test_upgrade() throws Exception
    {
		deleteDatabaseFile();
    	doPeoplePetsV1Test("Version 1");
    	DaoManager.clearCache();

    	String context = "test_upgrade";
    	doSingleTest(context);
    	//logRecordHandler.printAll();
    	//Thread.sleep(1000);
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
 	   boolean[] success = new boolean[] {false};
	   Thread thread = new Thread(new Runnable() {

 		@Override
 		public void run() {
 			PeopleAndPets peopleAndPetsV1 = null;
 			try {
 		        peopleAndPetsV1 = new PeopleAndPets();
 		        assertThat(peopleAndPetsV1.setUp()).isTrue();
 		        assertThat(peopleAndPetsV1.getVersion(PeopleAndPets.PETS_PU)).isEqualTo(1);
 		        assertThat(peopleAndPetsV1.getVersion(PeopleAndPets.PEOPLE_PU)).isEqualTo(1);
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
 		        logger.debug("Work status = " + workStatus.name());
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
		         success[0] = true;
 			    } catch (Throwable t) {
 			    	t.printStackTrace();
 			    } finally {
 			    	try {
						peopleAndPetsV1.close();
					} catch (InterruptedException e) {
					}
 			    	semaphore.release();
 			    }
 		    }
 	   });
 	   thread.start();
 	   semaphore.acquireUninterruptibly(1);
 	   if (!success[0])
 		   fail();
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
 	   boolean[] success = new boolean[] {false};
  	   Thread thread = new Thread(new Runnable() {

  		@Override
  		public void run() {
  			PeopleAndPetsV2 peopleAndPets = null;
  			try {
		         peopleAndPets = new PeopleAndPetsV2();
		         assertThat(peopleAndPets.setUp()).isTrue();
			     assertThat(peopleAndPets.getVersion(PeopleAndPets.PETS_PU)).isEqualTo(2);
			     assertThat(peopleAndPets.getVersion(PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
		         PetsUpdate petsUpdate = new PetsUpdate(objectsStoreV2, context);
		         WorkStatus workStatus = peopleAndPets.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate);
		         assertThat(workStatus == WorkStatus.FINISHED);
			     assertThat(peopleAndPets.getVersion(PeopleAndPets.PETS_PU)).isEqualTo(2);
			     assertThat(peopleAndPets.getVersion(PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
		         PeopleAndPetsMain.logInfo("Test completed successfully");
		 		 //Our string builder for building the content-view
		 		 StringBuilder sb = new StringBuilder();
		         peopleAndPets.displayMessage(sb
		 				.append(PeopleAndPets.SEPARATOR_LINE)
		 				.append(petsUpdate.getMessage())
		 				.append(PeopleAndPets.SEPARATOR_LINE)
		 				.append(petsUpdate.getMessage())
		 				.toString());
		         success[0] = true;
			    } catch (Throwable t) {
 			    	t.printStackTrace();
 			    } finally {
 			    	try {
						peopleAndPets.close();
					} catch (InterruptedException e) {
					}
 			    	semaphore.release();
 			    }
 		    }
 	   });
 	   thread.start();
 	   semaphore.acquireUninterruptibly(1);
 	   if (!success[0])
 		   fail();
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
    	if (files != null)
	    	Arrays.asList(files).forEach(file -> {
	    		try {
	    			file.delete();
	    		} catch (Throwable t) {
	    			t.printStackTrace();
	    		}
	    	});
    }
    
}
