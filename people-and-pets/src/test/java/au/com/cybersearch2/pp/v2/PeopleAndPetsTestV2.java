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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
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
import au.com.cybersearch2.pp.jpa.PeopleUpdate;
import au.com.cybersearch2.pp.jpa.PetsUpdate;


/**
 * PeopleAndPetsTestV2
 * Performs integration tests to verify version 2 application behavior.
 * @author Andrew Bowley
 */
public class PeopleAndPetsTestV2
{
    private static PeopleAndPetsV2 peopleAndPets;
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
		
    @Before
    public void setUp() throws Exception 
    {
        if (peopleAndPets == null)
        {
            peopleAndPets = new PeopleAndPetsV2(taskExecutor);
        }
    } 

    @AfterClass
    public static void afterClass() {
    	taskExecutor.shutdown();
    }
    
    @After
    public void shutdown() throws Exception
    {
    	peopleAndPets.shutdown();
    	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "");
   }
    
    @Test 
    public void test_serial_jpa_in_memory() throws Exception
    {
    	String context = "test_serial_jpa_in_memory";
    	doIntegrationTest(context, ConnectionType.memory, true);
    }
    
    @Test 
    public void test_serial_jpa_on_drive() throws Exception
    {
		deleteDatabaseFile();
    	String context = "test_serial_jpa_on_drive";
    	doIntegrationTest(context, ConnectionType.file, true);
    	// Do second time for start at JPA version 1 instead of 0
    	peopleAndPets.shutdown();
		Thread.sleep(1000);
    	doIntegrationTest(context, ConnectionType.file, true);
    }
    
    @Test 
    public void test_parallel_jpa_in_memory() throws Exception
    {
    	String context = "test_parallel_jpa_in_memory";
    	doIntegrationTest(context, ConnectionType.memory, true);
    }
    
    @Test 
    public void test_parallel_jpa_on_drive() throws Exception
    {
		deleteDatabaseFile();
    	String context = "test_parallel_jpa_on_drive";
    	doIntegrationTest(context, ConnectionType.file, true);
    	// Do second time for start at JPA version 1 instead of 0
    	peopleAndPets.shutdown();
    	doIntegrationTest(context, ConnectionType.file, true);
    }
    
   private void doIntegrationTest(String context, ConnectionType connectionType, boolean isSerial) throws Exception
   {
    	if (connectionType == ConnectionType.file)
        	System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "true");
    	else
    		System.setProperty(PeopleAndPetsModule.PERSIST_ON_DISK, "false");
        assertThat(peopleAndPets.setUp()).isTrue();
        PersistenceContext persistenceContext = peopleAndPets.getPersistenceContext();
        assertThat(getVersion(persistenceContext, PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
        assertThat(getVersion(persistenceContext, PeopleAndPets.PETS_PU)).isEqualTo(2);
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

            Thread thread1 = new Thread(() -> 
            	finalStates[0] = 
            		peopleAndPets.performPersistenceWork(PeopleAndPets.PETS_PU, petsUpdate));
            Thread thread2 = new Thread(() -> 
                finalStates[1] = 
                    peopleAndPets.performPersistenceWork(PeopleAndPets.PEOPLE_PU, peopleUpdate));
            thread1.start();
            thread2.start();
            thread1.join(5000);
            thread1.join(5000);
            assertThat(finalStates[0] == WorkStatus.FINISHED);
            assertThat(finalStates[1] == WorkStatus.FINISHED);
        }
        assertThat(getVersion(persistenceContext, PeopleAndPets.PEOPLE_PU)).isEqualTo(2);
        assertThat(getVersion(persistenceContext, PeopleAndPets.PETS_PU)).isEqualTo(2);
        PeopleAndPetsMain.logInfo("Test completed successfully");
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
        peopleAndPets.displayMessage(sb
				.append(PeopleAndPets.SEPARATOR_LINE)
				.append(petsUpdate.getMessage())
				.append(PeopleAndPets.SEPARATOR_LINE)
				.append(peopleUpdate.getMessage())
				.toString());
    }

    private static int getVersion(PersistenceContext persistenceContext, String persistenceUnit) 
    {
 		PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(persistenceUnit);
 		DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
 		ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
 		return databaseSupport.getVersion(connectionSource, persistenceAdmin.getProperties());
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
    
    /*
     * Copyright (C) 2014 The Android Open Source Project
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    
    /**
     * Get the stderr/stdout outputs of a process and return when the process is done.
     * Both <b>must</b> be read or the process will block on windows.
     *
     * @param process the process to get the output from
     * @param output the {@link ProcessOutput} where to send the output; note that on Windows
     * capturing the output is not optional
     * @return a future with the the process return code
     */
    private static ListenableFuture<Integer> grabProcessOutput(final Process process) {
        final SettableFuture<Integer> result = SettableFuture.create();
        final AtomicReference<Throwable> exceptionHolder = new AtomicReference<>();

        /*
         * It looks like on windows process#waitFor() can return before the thread have filled the
         * arrays, so we wait for both threads and the process itself.
         *
         * To make sure everything is complete before setting the future, the thread handling
         * "out" will wait for all its input to be read, will wait for the "err" thread to finish
         * and will wait for the process to finish. Only after all three are done will it set
         * the future and terminate.
         *
         * This means that the future will be set while the "out" thread is still running, but
         * no output is pending and the process has already finished.
         */
        final Thread threadErr = new Thread("stderr") {
            @Override
            public void run() {
                InputStream stderr = process.getErrorStream();
                 try {
                    ByteStreams.copy(stderr, System.err);
                    System.err.flush();
                } catch (IOException e) {
                    exceptionHolder.compareAndSet(null, e);
                }
            }
        };

        Thread threadOut = new Thread("stdout") {
            @Override
            public void run() {
                InputStream stdout = process.getInputStream();
 
                try {
                    ByteStreams.copy(stdout, System.out);
                    System.out.flush();
                } catch (Throwable e) {
                    exceptionHolder.compareAndSet(null, e);
                }

                try {
                    threadErr.join();
                    int processResult = process.waitFor();
                    if (exceptionHolder.get() != null) {
                        result.setException(exceptionHolder.get());
                    }

                    result.set(processResult);
                } catch (Throwable e) {
                    result.setException(e);
                }
            }
        };

        threadErr.start();
        threadOut.start();

        return result;
    }

    String[] getEnv(int length, String... keys) {
    	String[] params = new String[length];
    	int index = 0;
    	for (String key: keys) {
    		params[index++] = System.getProperty(key);
    	}
        return params;
    }
}

