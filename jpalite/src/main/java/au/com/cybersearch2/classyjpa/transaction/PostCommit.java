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
package au.com.cybersearch2.classyjpa.transaction;

import java.util.concurrent.Callable;

import javax.persistence.PersistenceException;

/**
 * PostCommit
 * Executes Callable object and records success and any Exception or expected RuntimeException thrown. 
 * Expected: PersistenceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
 * Only non-important code should to be executed such as for auditing, logging and other cross-cutting concerns 
 * following transaction commit/rollback. 
 * @author Andrew Bowley
 * 18/07/2014
 */
public class PostCommit
{
    /** Caught Exception or RuntimeException declared in EntityManager interface  */
    protected Throwable postCommitException;
    /** Error message for unexpected RuntimeException, ie. not declared in EntityManager interface. */
    protected String error;
    /** The Callable object */
    protected Callable<Boolean> onPostCommit;
   
    /**
     * Create PostCommit object
     * @param onPostCommit Callable
     * @see java.util.concurrent.Callable
     */
    public PostCommit(Callable<Boolean> onPostCommit)
    {
        this.onPostCommit = onPostCommit;
    }
    
    /**
     * Execute call
     */
    public void doPostCommit()
    {
        boolean success = false; // Use flag to identify RuntimeExceptions
        try
        {
            onPostCommit.call();
            success = true;
        }
        catch (PersistenceException e)
        {
            postCommitException = e;
        }
        catch (IllegalArgumentException e)
        {
            postCommitException = e;
        }
        catch (IllegalStateException e)
        {
            postCommitException = e;
        }
        catch (UnsupportedOperationException e)
        {
            postCommitException = e;
         }
        catch (RuntimeException e)
        {
            throw(e);
        }
        catch (Exception e)
        {
            postCommitException = e;
        }
        finally
        {
            if (!success)
                error = "postCommit operation failed due to unexpected exception";
        }
    }

    /**
     * Returns caught Exception, if thrown
     * @return Throwable or null if no exception thrown
     */
    public Throwable getPostCommitException() 
    {
        return postCommitException;
    }

    /**
     * Returns error message for unexpected exception
     * @return String or null if unexpected exception did not occur
     */
    public String getError() 
    {
        return error;
    }
}
