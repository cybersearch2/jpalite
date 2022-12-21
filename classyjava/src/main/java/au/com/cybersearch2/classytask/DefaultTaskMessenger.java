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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import au.com.cybersearch2.classytask.TaskMonitor.EventHandler;
import au.com.cybersearch2.classytask.TaskMonitor.MonitorType;

/**
 * Runs a task monitor thread to service messages originating from worker threads.
 * The message contains a payload object specific to message type.  
 *
 * @param <Progress> Progress reporting type
 */
public class DefaultTaskMessenger<Progress> implements TaskMessenger<Progress,Boolean> {

    public static int MAX_QUEUE_LENGTH = 16;


    private final Class<Progress> progressClass;

    private BlockingQueue<EventHandler> messageQueue;

    private Thread consumeThread;

    /**
     * Construct DefaultTaskMessenger object
     * @param progressClass Progress class for runtime type check
     */
	public DefaultTaskMessenger(Class<Progress> progressClass) {
		this.progressClass = progressClass;
		messageQueue = new LinkedBlockingQueue<EventHandler>(MAX_QUEUE_LENGTH);
        runConsumer();
	}

    /**
     * Post task success flag
     * @param taskMonitor Task monitor
     * @param result Success flag
     */
	@Override
	public void sendResult(TaskMonitor<Progress, Boolean> taskMonitor, Boolean result) throws InterruptedException {
		EventHandler element = taskMonitor.getEventHandler(MonitorType.result);
		element.setPayload(result);
		put(element);
	}

	/**
	 * Post progress update
     * @param taskMonitor Task monitor
     * @param progress Progress object
	 */
	@Override
	public void sendProgress(TaskMonitor<Progress, Boolean> taskMonitor, Progress progress) throws InterruptedException {
		EventHandler element = taskMonitor.getEventHandler(MonitorType.progress);
		element.setPayload(progress);
		put(element);
	}

	/**
	 * Shutdown
	 */
	@Override
	public void shutdown() {
        consumeThread.interrupt();
	}

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
    public void put(EventHandler element) throws InterruptedException
    {
        messageQueue.put(element);
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
                    	EventHandler message = messageQueue.take();
                        onMessageReceived(message);
                    } 
                    catch (InterruptedException e) 
                    {
                    	messageQueue.forEach(message -> cancel(message));
                    	messageQueue.clear();
                        break;
                    }
                }
            }

			private void cancel(EventHandler message) {
				try {
					message.cancel();
				} catch (Throwable e) {
					
				}
			}
        };
        consumeThread = new Thread(comsumeTask, "DefaultTaskMessenger");
        consumeThread.start();
    }

	protected void onMessageReceived(EventHandler message) {
		MonitorType monitorType = message.getMonitorType();
		Object payload = message.getPayload();
		boolean skip = false;
        switch (monitorType) {
        case result:
            skip = !(payload instanceof Boolean);
            break;
        case progress:
            skip = message.isCancelled() || !progressClass.isAssignableFrom(payload.getClass());
            break;
        }
		if (!skip)
			message.handleMessage(message.getPayload());
	}
}
