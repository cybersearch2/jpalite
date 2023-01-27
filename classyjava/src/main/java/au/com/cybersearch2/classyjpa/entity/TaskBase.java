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
package au.com.cybersearch2.classyjpa.entity;

import au.com.cybersearch2.classyjpa.transaction.TransactionInfo;
import au.com.cybersearch2.classytask.TaskMonitor;

/**
 * Implements most of the details to execute a persistence WorkerTask. To
 * complete, override base class method executeInBackground().
 */
public abstract class TaskBase<Progress> extends TaskMonitor<Progress, Boolean> {
	/** Task to be performed */
	private final JavaPersistenceContext persistenceContext;

	/**
	 * Constructor
	 * 
	 * @param persistenceContext Object which creates a persistence context and
	 *                           executes a task in that context
	 */
	public TaskBase(final JavaPersistenceContext persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	/**
	 * Process signaled result after task has run
	 * 
	 * @param success Boolean TRUE or FALSE or null if task cancelled before result
	 *                available
	 */
	@Override
	public void onPostExecute(Boolean success) {
		persistenceContext.setExecutionException(executionException);
		persistenceContext.onPostExecute(success);
	}

	/**
	 * Process signaled result after task has been cancelled. NOTE: Interruption of
	 * the running thread is not permitted, so same as non-cancel case.
	 * 
	 * @param success Boolean TRUE or FALSE or null if task cancelled before result
	 *                available
	 */
	@Override
	public void onCancelled(Boolean success) {
		persistenceContext.onPostExecute(success);
	}

	/**
	 * Returns transaction information
	 * 
	 * @return TransactionInfo
	 */
	public TransactionInfo getTransactionInfo() {
		return persistenceContext.getTransactionInfo();
	}

}
