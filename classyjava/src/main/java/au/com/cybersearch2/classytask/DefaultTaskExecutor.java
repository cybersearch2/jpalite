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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains a thread pool
 */
public class DefaultTaskExecutor implements TaskExecutor
{
	/** Number of threads to keep in the pool, even if they are idle */
    private static final int CORE_POOL_SIZE = 1;
    /** Maximum number of threads to allow in the pool */
    private static final int MAXIMUM_POOL_SIZE = 10;
    /** Maximum time that excess idle threads
        will wait for new tasks before terminating */
    private static final int KEEP_ALIVE = 10;
    /** An {@link ExecutorService} that executes each submitted task using
        one of possibly several pooled threads */
    private final ThreadPoolExecutor executorService;

    /**
     * Create DefaultTaskExecutor object
     */
    public DefaultTaskExecutor()
    {
    	executorService = 
                new ThreadPoolExecutor(CORE_POOL_SIZE,
                                       MAXIMUM_POOL_SIZE, 
                                       KEEP_ALIVE, 
                                       TimeUnit.SECONDS, 
                                       new LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE), 
                                       new ThreadFactory() 
                                       {
                                           private final AtomicInteger count = new AtomicInteger(1);

                                           public Thread newThread(Runnable r) 
                                           {
                                               return new Thread(r, "Worker #" + count.getAndIncrement());
                                           }
                                       },
                                       new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    /**
     * Returns thread pool executor. Pool parameters statically defined in this class - CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE
     * @return ThreadPoolExecutor
     */
	@Override
    public ExecutorService getExecutorService() 
    {
        return executorService;
    }

	@Override
	public void shutdown() {
		executorService.shutdownNow();
	}
}
