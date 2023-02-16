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
package au.com.cybersearch2.pp.v2;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.container.JpaContainer;
import au.com.cybersearch2.container.WorkStatus;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.PeopleAndPetsMain;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;
import au.com.cybersearch2.pp.jpa.PeopleUpdate;
import au.com.cybersearch2.pp.jpa.PetsUpdate;


/**
 * PeopleAndPetsTestV2
 * Performs integration tests to verify version 2 application behavior.
 * @author Andrew Bowley
 */
public class PeopleAndPetsTestV2
{
     private static ObjectsStore objectsStoreV2 = new ObjectsStore() {

		@Override
		public Person personInstance(String name) {
			return new PersonDataV2(name, QuoteSource.getQuote());
		}

		@Override
		public Pet petInstance(String name) {
			return new PetDataV2(name, QuoteSource.getQuote());
		}};

    @Before
    public void setUp() throws Exception 
    {
		File resourcePath = new File("src/main/resources");
		System.setProperty(JpaContainer.RESOURCE_PATH_NAME, resourcePath.getAbsolutePath());
    } 

    @Test 
    public void test_serial_jpa_on_drive() throws Exception
    {
		deleteDatabaseFile();
    	String context = "test_serial_jpa_on_drive";
    	doIntegrationTest(context, ConnectionType.file, true);
    	// Do second time for start at JPA version 1 instead of 0
		Thread.sleep(1000);
    	doIntegrationTest(context, ConnectionType.file, true);
    }
    
    @Test 
    public void test_parallel_jpa_on_drive() throws Exception
    {
		deleteDatabaseFile();
    	String context = "test_parallel_jpa_on_drive";
    	doIntegrationTest(context, ConnectionType.file, true);
    	// Do second time for start at JPA version 1 instead of 0
    	doIntegrationTest(context, ConnectionType.file, true);
    }
    
    private void doIntegrationTest(String context, ConnectionType connectionType, boolean isSerial) throws Exception
    {
	   PeopleAndPetsV2 peopleAndPets = null;
	   try {
		   peopleAndPets = new PeopleAndPetsV2();
	    	//if (connectionType == ConnectionType.file)
	        //	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "true");
	    	//else
	    	//	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "false");
	        assertThat(peopleAndPets.setUp()).isTrue();
	        assertThat(peopleAndPets.getVersion(PeopleAndPets.PETS_PU)).isEqualTo(2);
	        assertThat(peopleAndPets.getVersion(PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
	        PetsUpdate petsUpdate = new PetsUpdate(objectsStoreV2, context);
	        WorkStatus workStatus = WorkStatus.PENDING;
	        if (isSerial)
	        {
	            workStatus = 
	        	    peopleAndPets.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate);
	            assertThat(workStatus == WorkStatus.FINISHED);
	        }
	        PeopleUpdate peopleUpdate = new PeopleUpdate(objectsStoreV2, context);
	        if (isSerial)
	        {
	            workStatus = 
	                peopleAndPets.performPersistenceWork(PeopleAndPets.PEOPLE_PU, peopleUpdate);
	            assertThat(workStatus == WorkStatus.FINISHED);
	        } else {
	            WorkStatus[] finalStates = new WorkStatus[2];
	            PeopleAndPetsV2 pp = peopleAndPets;
	            Thread thread1 = new Thread(() -> 
	            	finalStates[0] = 
	            		pp.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate));
	            Thread thread2 = new Thread(() -> 
	                finalStates[1] = 
	                    pp.performPersistenceWork(PeopleAndPets.PEOPLE_PU, peopleUpdate));
	            thread1.start();
	            thread2.start();
	            thread1.join(5000);
	            thread1.join(5000);
	            assertThat(finalStates[0] == WorkStatus.FINISHED);
	            assertThat(finalStates[1] == WorkStatus.FINISHED);
	        }
	        assertThat(peopleAndPets.getVersion(PeopleAndPets.PETS_PU)).isEqualTo(2);
	        assertThat(peopleAndPets.getVersion(PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
	        PeopleAndPetsMain.logInfo("Test completed successfully");
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
	        peopleAndPets.displayMessage(sb
					.append(PeopleAndPets.SEPARATOR_LINE)
					.append(petsUpdate.getMessage())
					.append(PeopleAndPets.SEPARATOR_LINE)
					.append(peopleUpdate.getMessage())
					.toString());
	   	} finally {
			if (peopleAndPets != null)
			    peopleAndPets.close();
		}
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
}

