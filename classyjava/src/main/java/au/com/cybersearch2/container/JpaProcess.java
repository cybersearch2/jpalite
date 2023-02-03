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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.service.WorkerService;

 /**
  * Modeled on java.lang.Process, executes persistence work in an Executor thread
  */
public class JpaProcess {

	/** Callable to pass to the Executor */
	private class JpaProcessWorker implements Callable<WorkStatus> {

		@Override
		public WorkStatus call() throws Exception {
			persistenceContext.onPostExecute(persistenceContext.doTask());
			return persistenceContext.getWorkStatus();
		}
	}
	
	/** Flag set true if operating in synchronous mode */
	private final boolean isSynchronous;
	/**  Executes a task in a persistence context */
	private final JavaPersistenceContext persistenceContext;
	/** Final work status */
	private WorkStatus workStatus;
	
	/**
	 * Construct JpaProcess object
	 * @param unit Persistence unit selected to provide the context
	 * @param persistenceWork Persistence work to be performed
	 */
	public JpaProcess(PersistenceUnit unit, PersistenceWork persistenceWork) {
		this(unit, persistenceWork, false);
	}
	
	/**
	 * Construct JpaProcess object
	 * @param unit Persistence unit selected to provide the context
	 * @param persistenceWork Persistence work to be performed
	 * @param isSynchronous Flag set true if operating in synchronous mode
	 */
	public JpaProcess(PersistenceUnit unit, PersistenceWork persistenceWork, boolean isSynchronous) {
		this.isSynchronous = isSynchronous;
		persistenceContext = new JavaPersistenceContext(persistenceWork, unit);
		workStatus = WorkStatus.PENDING;
	}

    /**
     * Causes the current thread to wait until the
     * process represented by this {@code Process} object has
     * terminated.  The calling thread will be blocked until the
     * process exits.
     *
     * @return the exit value of the process represented by this
     *         {@code JpaProcess} object which is the final status value {@link au.com.cybersearch2.classytask.WorkStatus}
     */
    public JpaProcess waitFor() {
    	if (!isSynchronous)
    		throw new UnsupportedOperationException();
		persistenceContext.onPostExecute(persistenceContext.doTask());
		workStatus = persistenceContext.getWorkStatus();
		return this;
    }
    
    /**
     * Returns future which waits for process termination
     * @return CompletableFuture object
     */
    public CompletableFuture<JpaProcess> onExit() {
    	if (isSynchronous)
    		throw new UnsupportedOperationException();
        return CompletableFuture.supplyAsync(this::waitForInternal);
    }

    /**
     * Wait for the process to exit by calling {@code waitFor}.
     * If the thread is interrupted, remember the interrupted state to
     * be restored before returning. Use ForkJoinPool.ManagedBlocker
     * so that the number of workers in case ForkJoinPool is used is
     * compensated when the thread blocks in waitFor().
     *
     * @return the Process
     */
    private JpaProcess waitForInternal() {
        boolean interrupted = false;
        while (true) {
            try {
                ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                    @Override
                    public boolean block() throws InterruptedException {
                		try {
                			workStatus = WorkerService.submitWork(new JpaProcessWorker());
                		} catch (ExecutionException e) {
                			throw new JpaliteException("Persistence work terminated with an error", e.getCause());
                		}
                        return true;
                    }

                    @Override
                    public boolean isReleasable() {
                        return !isAlive();
                    }
                });
                break;
            } catch (InterruptedException x) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    /**
     * Tests whether the process represented by this {@code Process} is
     * alive.
     *
     * @return {@code true} if the process represented by this
     *         {@code Process} object has not yet terminated.
     * @since 1.8
     */
    public boolean isAlive() {
        return !hasExited();
    }

    /**
     * Returns the exit value for the process.
     *
     * @return the exit value of the process represented by this
     *         {@code Process} object.  By convention, the value
     *         {@code 0} indicates normal termination.
     * @throws IllegalThreadStateException if the process represented
     *         by this {@code Process} object has not yet terminated
     */
    public WorkStatus exitValue() {
    	if (workStatus == WorkStatus.PENDING || workStatus == WorkStatus.RUNNING)
    		throw new IllegalThreadStateException("Jpa process exitValue() requested while process is alive ");
    	return workStatus;
    }

    /**
     * This is called from the default implementation of
     * {@code waitFor(long, TimeUnit)}, which is specified to poll
     * {@code exitValue()}.
     */
    private boolean hasExited() {
        try {
            exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }

    /**
     * Set user transactions flag
     * @param value boolean
     */
	public void setUserTransactions(boolean value) {
		persistenceContext.getTransactionInfo().setUserTransaction(value);
	}

}
