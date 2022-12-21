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

import java.util.concurrent.TimeUnit;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * H2ManyToManyMain
 * @author Andrew Bowley
 * 16 May 2015
 * 
 * H2 database version uses a connection source which allows multiple connections.
 * 
 * ORIGINAL COMMENTS:
 * Main sample routine to show how to do many-to-many type relationships. It also demonstrates how we user inner queries
 * as well foreign objects.
 * 
 * <p>
 * <b>NOTE:</b> We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 * </p>
 * <p>
 * CLASSYTOOLS COMMENTS:
 * </p>
 * <p>
 * This example shows JPA in action. The application code exempifies use of a standard persistence interface. 
 * The OrmLite implemention is mostly hidden in library code, but does show up in named queries where, to keep things
 * lightweight, OrmLite QueryBuilder is employed in place of a JQL implementation @see ManyToManyGenerator.
 * </p>
 * <p>
 * Also notable is dependency injection using Dagger @see ManyToManyModule. If one studies the details, what the
 * dependency inject allows is flexibility in 3 ways:
 * </p>
 * <ol>
 * <li>Choice of database - the PersistenceContext binding</li>
 * <li>Location of resource files such as persistence.xml (and Locale too) - the ResourceEnvironment binding</li>
 * <li>How to reduce background thread priority - the ThreadHelper binding</li>
 * </ol>
 */
public class H2ManyToManyMain extends ManyToManyMain 
{
    private static TaskExecutor taskExecutor;

    protected H2ManyToManyFactory h2ManyToManyFactory;
    
    /**
     * Create H2ManyToManyMain object
     * This creates and populates the database using JPA, provides verification logic and runs a test from main().
     */
	public H2ManyToManyMain() 
	{
		super();
	}

    /**
     * Test ManyToMany association
     * @param args Not used
     */
	public static void main(String[] args)
	{
     	taskExecutor = new DefaultTaskExecutor();
     	try {
            new H2ManyToManyMain().runApplication();
     	} catch (Throwable e) {
     		e.printStackTrace();
     	}	finally {
     		taskExecutor.shutdown();
     		System.exit(0);
     	}
	}
	

	/**
	 * Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object.
	 * Override to run with different database and/or platform. 
	 * Refer au.com.cybersearch2.example.AndroidManyToMany in classyandroid module for Android example.
	 */
	@Override
    protected PersistenceContext createFactory()
    {
        h2ManyToManyFactory = new H2ManyToManyFactory(taskExecutor, taskMessenger);
        return h2ManyToManyFactory.getPersistenceContext();
    }
    
    @Override
    protected WorkStatus execute(PersistenceWork persistenceWork) throws InterruptedException
    {
        TaskStatus taskStatus = h2ManyToManyFactory.doTask(persistenceWork);
        taskStatus.await(5, TimeUnit.SECONDS);
        return taskStatus.getWorkStatus();
    }


}
