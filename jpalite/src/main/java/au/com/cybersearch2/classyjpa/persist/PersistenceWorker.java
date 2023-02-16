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
package au.com.cybersearch2.classyjpa.persist;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.container.JpaContainer;

/**
 * PersistenceWorker
 * Base class for execution of persistence tasks. Aggregates error count for reporting success of multiple executions.
  */
public abstract class PersistenceWorker
{
    /** Application persistence interface */
	protected JpaContainer jpaContainer;
    /** JPA container to execute named query */
	protected String persistenceUnit;
	/** Aggregate count of errors to track asynchronous work progress */
	protected int errorCount;

	/**
	 * Construct PersistenceWorker object
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
	 * @param jpaContainer Application persistence interface
	 */
	public PersistenceWorker(String persistenceUnit, JpaContainer jpaContainer)
	{
		this.persistenceUnit = persistenceUnit;
		this.jpaContainer = jpaContainer;
	}

    /**
     * Return persistence context
     * @return JpaContainer object
     */ 	
    public JpaContainer getPersistenceContext() 
    {
		return jpaContainer;
	}

    /**
     * Return persistence unit name
     * @return String
     */
	public String getPersistenceUnit() 
	{
		return persistenceUnit;
	}

    /**
     * Execute task
     * @param persistenceWork Task to execute
     */
    public abstract void doWork(PersistenceWork persistenceWork);

	/**
	 * Reset error count
	 */
	public void resetErrorCount()
	{
		errorCount = 0;
	}

	/**
	 * Returns aggregate error count
	 * @return int
	 */
	int getErrorCount()
	{
		return errorCount;
	}

}
