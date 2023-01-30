/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.container;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * Launches JpaProcess
 */
public class JpaRunner {

	/** Persistence unit to provide the context in which the process runs */
	private final PersistenceUnit unit;

	/**
	 * Constructs JpaRunner object
	 * @param unit Persistence unit to provide the context in which the process runs
	 */
	JpaRunner(PersistenceUnit unit) {
		this.unit = unit;
	}
	
	public JpaProcess execute(PersistenceWork persistenceWork) {
		JpaProcess process = new JpaProcess(unit, persistenceWork);
	    CompletableFuture<JpaProcess> processFuture = process.onExit();
	    try {
			return processFuture.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
	    	throw new PersistenceException("Jpa process failed to terminate normally", e);
		}
	}
}
