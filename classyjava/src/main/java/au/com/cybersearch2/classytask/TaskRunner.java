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

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Executes work units asynchronously using provided executor service
 * @author Andrew Bowley
 */
public class TaskRunner<Progress>
{
    /** Manages the worker thread pool */
	private final TaskExecutor taskExecutor; 
	/** Executor service which returns a ListenableFuture when a task is submitted.
	    This provides an effective cancel capability. Recommended by Jesper de Jong */
    private final ListeningExecutorService executorService;

    /**
     * Construct TaskRunner object
     * @param taskExecutor Executor service with thread pool
    */
    public TaskRunner(TaskExecutor taskExecutor)
    {
        this.taskExecutor = taskExecutor;
        executorService = MoreExecutors.listeningDecorator(taskExecutor.getExecutorService());
    }

    /**
     * Executes the work unit. Any required parameters will need to be provided as fields 
     * belonging to the sub class. 
     * @param workUnit Task to run
     * @param taskListener Task executor to call back to task monitor
     * @param progressListener Progress listener
     * @throws IllegalStateException If status is either
     *         {@link WorkStatus#RUNNING} or {@link WorkStatus#FINISHED}.
     */
    public final void execute(WorkUnit<Progress> workUnit, 
	                            TaskListener<Boolean> taskListener, 
	                            ProgressListener<Progress> progressListener) 
    {
        taskListener.onStart();
        ListenableFuture<Boolean> futureResult =
        	executorService.submit(() -> { 
        		return workUnit.execute(progressListener); 
        	});
        Futures.addCallback(futureResult, new FutureCallback<Boolean>() {
        	           public void onSuccess(Boolean result) {
 							taskListener.postResult(result);
        		       }
        		       public void onFailure(Throwable t) {
        		            taskListener.setExecutionException(new ExecutionException(t));
							taskListener.postResult(null);
        		       }}, taskExecutor.getExecutorService()); 
        taskListener.setFutureResult(futureResult);
    }

    public void shutdownService() {
        taskExecutor.shutdown();
    }

}
