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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.cybersearch2.container.JpaContainer;

/**
 * PersistenceService
 * @author Andrew Bowley
 * 25 Jan 2016
 */
public abstract class PersistenceService<E> extends PersistenceWorker
{
    public static int MAX_QUEUE_LENGTH = 16;
    
    private BlockingQueue<E> entityQueue;
    private Thread consumeThread;

    /**
     * Construct PersistenceService object
     * @param persistenceUnit Persistence unit
     * @param persistenceContext persistence context
     */
    public PersistenceService(String persistenceUnit, JpaContainer jpaContainer)
    {
        super(persistenceUnit, jpaContainer);
        entityQueue = new LinkedBlockingQueue<E>(MAX_QUEUE_LENGTH);
        runConsumer();
    }

    /**
     * Handle entity submission
     * @param entity Entity object
     */
    public abstract void onEntityReceived(E entity);

    /**
     * Inserts the specified element into the service queue, waiting if necessary
     * for space to become available.
     *
     * @param element the element to add
     * @throws InterruptedException if interrupted while waiting
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    public void put(E element) throws InterruptedException
    {
        entityQueue.put(element);
    }

    /**
     * Shutdown service. Note: does not block pending completion.
     */
    public void shutdown()
    {
        consumeThread.interrupt();
    }
    
    private void runConsumer() 
    {
        Runnable comsumeTask = new Runnable()
        {
            @Override
            public void run() 
            {
                while (true)
                {
                    try 
                    {
                        onEntityReceived(entityQueue.take());
                    } 
                    catch (InterruptedException e) 
                    {
                        break;
                    }
                }
            }
        };
        consumeThread = new Thread(comsumeTask, "PersistenceWorker");
        consumeThread.start();
    }

}
