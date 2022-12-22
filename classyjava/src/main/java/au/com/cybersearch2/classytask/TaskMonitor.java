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
package au.com.cybersearch2.classytask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors task through start to completion and post execution
 *
 * @param <Progress> Progress data type
 * @param <Result> Result type
 */
public abstract class TaskMonitor<Progress,Result> {

	/** Enumerates tupes of monitor message */
    protected enum MonitorType {
        result,
        progress
    }

    /**
     * Monitor message with handler to process payload
     */
    public static class EventHandler {
    	
    	private MonitorType monitorType;
    	private TaskMonitor<?,?> taskMonitor;
    	/** Payload is Progress or Result object according to monitor type */
    	private Object payload;
    	
    	public EventHandler(TaskMonitor<?,?> taskMonitor, MonitorType monitorType) {
    		this.taskMonitor = taskMonitor;
    		this.monitorType = monitorType;
    	}
  
    	public MonitorType getMonitorType() {
			return monitorType;
		}

		public Object getPayload() {
			return payload;
		}

		public void setPayload(Object payload) {
			this.payload = payload;
		}

		public boolean isCancelled() {
    		return taskMonitor.isCancelled();
    	}

		public boolean cancel() {
			return taskMonitor.cancel(true);
		}
		
        public void handleMessage(Object payload) {
            switch (monitorType) {
                case result:
                    taskMonitor.postPayload(payload);
                    break;
                case progress:
                    taskMonitor.onProgressPayload(payload);
                    break;
            }
        }
    }

    /** Execution cancelled flag */
    protected final AtomicBoolean cancelled;
    /** Exception thrown when the worker task fails due to a uncaught RuntimeException */
    protected ExecutionException executionException;
    /** Future to return result and perform cancel operation - set only for async execution only */
    private volatile Future<Result> futureResult;

    /** Work status PENDING, RUNNING, FINISHED, FAILED */
    protected volatile WorkStatus workStatus;
 
    /**
     * Construct TaskMonitor object
     */
    public TaskMonitor() {
        cancelled = new AtomicBoolean();
        workStatus = WorkStatus.PENDING;
    }

    /**
     * Post result.  Implementation will be specific to execution details 
     * @param result Result returned on completion of execution
     * @throws InterruptedException if interrupted
     */
    abstract protected void postResult(Result result) throws InterruptedException;

    /**
     * Publish progress update. Applicable only to async execution.
     * @param progress Progress data
     * @throws InterruptedException if interrupted
     */
    abstract protected void publishProgress(Progress progress) throws InterruptedException;


    /**
     * Attempts cancellation with option to allow task to complete if already executing. 
     * Applicable only to async execution. If the task has already started,
     * then the <code>mayInterruptIfRunning</code> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     * 
     * Note that the execution may continue for some time after a successful cancel
     * attempt is indicated.
     *
     * @param mayInterruptIfRunning <code>true</code> if the thread executing this
     *        task should be interrupted; otherwise, in-progress tasks are allowed
     *        to complete.
     * @return <code>false</code> if the task could not be cancelled,
     *         typically because it has already completed normally;
     *         <code>true</code> otherwise
     *
     * @see #isCancelled()
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        boolean canBeCancelled = !isCancelled();
        cancelled.set(true);
        if (futureResult != null) {
            canBeCancelled = futureResult.cancel(mayInterruptIfRunning);
            if (canBeCancelled) {
            	new Thread(() ->
            	{
                    try {
                        futureResult.get();
                    } catch (Throwable e) {
                    } finally {
                        try {
							postResult(null);
						} catch (InterruptedException e) {
						}
                    }
            	}).start();
            }
        }
        else
            canBeCancelled = false;
        return canBeCancelled;
    }

    /**
     * Returns <code>true</code> if this task was cancelled before it completed normally.
     * @return boolean
     *
     * @see #cancel(boolean)
     */
    public final boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * exception
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    public final Result get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (futureResult != null)
            return futureResult.get(timeout, unit);
        return null;
    }

    /**
     * Returns current work status
     * @return WorkStatus enum
     */
    public WorkStatus getWorkStatus() {
		return workStatus;
	}

    /**
     * Returns task monitor listener
     * @return TaskListener object
     */
	public TaskListener<Result> getTaskListener() {
    	return new TaskListener<Result>() {

    		/**
    		 * Handle execution has commenced. Sets work status to RUNNING
    		 * and calls pre-execute hook.
    		 */
			@Override
    	    public void onStart() {
    	        WorkStatus currentStatus = workStatus;
    	        if (currentStatus != WorkStatus.PENDING) {
    	            switch (currentStatus) {
    	                case RUNNING:
    	                    throw new IllegalStateException("Cannot execute task:"
    	                            + " the task is already running.");
    	                case FINISHED:
    	                case FAILED:
    	                    throw new IllegalStateException("Cannot execute task:"
    	                            + " the task has already been executed "
    	                            + "(a task can be executed only once)");
    	                default:
    	            }
    	         }
    	         workStatus = WorkStatus.RUNNING;
    	         onPreExecute();
    	    }

			@Override
			public void setFutureResult(Future<Result> futureFesult) {
				TaskMonitor.this.setFutureResult(futureResult);
				
			}
			
		    /**
		     * Post result to calling thread
		     * @param result Result object
		     * @return Object of generic type Result or null if task cancelled before result available
		     */
    	    public void postResult(Result result) {
    	    	try {
					TaskMonitor.this.postResult(result);
				} catch (InterruptedException e) {
					TaskMonitor.this.finish(result);
				}
    	    }

		    /**
		     * Set Exception thrown when the worker task fails due to a uncaught RuntimeException  
		     * @param executionException ExecutionException
		     */
			@Override
		    public void setExecutionException(ExecutionException executionException)
		    {
				TaskMonitor.this.executionException = executionException;
		    }

       	};
    }

	/**
	 * Returns Progress listener
	 * @return ProgressListener object
	 */
	public ProgressListener<Progress> getBackgroundListener() {
    	return new ProgressListener<Progress>() {

			@Override
			public void publishProgress(Progress progress) throws InterruptedException {
    	    	TaskMonitor.this.publishProgress(progress);
			}

    	};
    }
 
    /**
     * Process signaled result after task has run
     * @param result Object of generic type Result or null if task cancelled before result available
     */
    public void finish(Result result) {
        if (isCancelled()) {
            onCancelled(result);
            workStatus = WorkStatus.FAILED;
        } else if ((result == null) || (workStatus == WorkStatus.FAILED)) {
            onPostExecute(null);
        } else {
            onPostExecute(result);
            workStatus = WorkStatus.FINISHED;
         }
    }


	/**
     * Called on the monitor thread before the background task starts
     */
    protected void onPreExecute() {}

    /**
     * Called on the monitor thread after the background task ends. The
     * specified result is the value returned by the background task
     * or null if the task was cancelled or an exception occurred.
     *
     * @param result The value returned by the background task
     */
    protected void onPostExecute(Result result) {}

    /**
     * Called on separate thread after the background task is invoked. 
     *
     * @param result Expected result value is null but this cannot be guaranteed
     */
    protected void onCancelled(Result result) {
        onCancelled();
    }

    /**
     * Called from task to report progress
     * @param progress Progress data
     */
    protected void onProgressUpdate(Progress progress) {
    	
    }

    /**
     * Called on separate thread after the background task is invoked. 
     * Provided as alternative to above method for when 'don't care' applies to possible return value.
     */
    protected void onCancelled() {}

    /**
     * Returns event handler for given monitor type
     * @param monitorType Monitor type enum
     * @return EventHandler object
     */
    protected EventHandler getEventHandler(MonitorType monitorType) {
    	return new EventHandler(this, monitorType);
    }

    /**
     * Process payload from progress message
     * @param payload Object to be cast to Payload type
     */
    @SuppressWarnings("unchecked")
	private void onProgressPayload(Object payload) {
    	onProgressUpdate((Progress)payload);
    }

    /**
     * Process signaled result after task has run
     * @param result Object to be cast to Result type
     */
    @SuppressWarnings("unchecked")
    private void postPayload(Object payload) {
    	finish((Result)payload);
    }

    private void setFutureResult(Future<Result> futureResult) {
    	this.futureResult = futureResult;
    }


}
