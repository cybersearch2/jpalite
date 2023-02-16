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

import java.util.concurrent.CountDownLatch;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * TestPersistenceWork
 * @author Andrew Bowley
 * 28/06/2014
 */
public class TestPersistenceWork implements PersistenceWork
{
    public interface Callable
    {
        Boolean call(EntityManagerLite entityManager) throws Exception;
    }

    private Callable doInBackgroundCallback;
    private Transcript transcript;
    private CountDownLatch latch;
    
    public TestPersistenceWork(Transcript transcript)
    {
        this.transcript = transcript;
        latch = new CountDownLatch(1);
    }
    
    public TestPersistenceWork(Transcript transcript, Callable doInBackgroundCallback)
    {
        this(transcript);
        this.doInBackgroundCallback = doInBackgroundCallback;
    }
    
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        transcript.add("background task");
        if (doInBackgroundCallback != null)
            try
            {
                doInBackgroundCallback.call(entityManager);
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new PersistenceException("Exception thrown in doInBackground", e);
            }
    }

    @Override
    public void onPostExecute(boolean success) 
    {
        transcript.add("onPostExecute " + success);
        latch.countDown();
   }

    @Override
    public void onRollback(Throwable rollbackException) 
    {
        transcript.add("onRollback " + rollbackException.toString());
        latch.countDown();
   }

    public void setCallable(Callable doInBackgroundCallback) 
    {
        this.doInBackgroundCallback = doInBackgroundCallback;
    }
    
    public void await() throws InterruptedException {
    	latch.await();
    }
}
