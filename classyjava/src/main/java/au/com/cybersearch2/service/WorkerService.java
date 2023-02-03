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
package au.com.cybersearch2.service;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import au.com.cybersearch2.classyjpa.global.Singleton;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * Maintains a pool of work threads scaled according to available processors 
 */
public class WorkerService {

	private static int MAX_THREADS = Runtime.getRuntime().availableProcessors() + 2;

    /** Semaphore to throttle work submissions */
    private final Semaphore semaphore;
	/** Execution service */
    private ExecutorService executorService;
	private volatile boolean isActive;

 
	public WorkerService() {
		semaphore = new Semaphore(MAX_THREADS);
	}

    public static WorkStatus submitWork(Callable<WorkStatus> worker) throws InterruptedException, ExecutionException {
    	return getSingleton().submit(worker);
    }

	public static void await() throws InterruptedException {
		WorkerService singleton = (WorkerService)Singleton.worker_service.getObject();
		if (singleton.isActive) {
			singleton.semaphore.acquire(MAX_THREADS);
			singleton.semaphore.release(MAX_THREADS);
		}
	}
	
	private static WorkerService getSingleton() {
		WorkerService singleton = (WorkerService)Singleton.worker_service.getObject();
		if (!singleton.isActive) 
			singleton.activate();
		return singleton;
	}
	
	synchronized private void  activate() {
    	if ((executorService == null) || executorService.isShutdown()) {
		    executorService = Executors.newFixedThreadPool(MAX_THREADS, Executors.defaultThreadFactory());	
			Runtime.getRuntime().addShutdownHook(new Thread() {
			      public void run() {
			    	  shutdown(2L);
			      }
			    });
			isActive = true;
   	    }
    }
    
    private WorkStatus submit(Callable<WorkStatus> worker) throws InterruptedException, ExecutionException {
    	try {
    		semaphore.acquire();
    	    return executorService.submit(worker).get();
    	} finally {
    		semaphore.release();
    	}
    }
    
    private void shutdown(long timeout) {
    	if (timeout < 2L)
    		timeout = 20L;
    	executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
        	    executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
             }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
         }
     }

}
