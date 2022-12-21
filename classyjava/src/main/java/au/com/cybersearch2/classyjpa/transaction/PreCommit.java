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

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.DatabaseConnection;

/**
 * PreCommit
 * Prior to transaction commmit, executes Callable object and records success and any Exception or expected RuntimeException thrown. 
 * Expected: PersistenceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
 * Flags rollback required if exception is thrown or call returns false, indicating a fatal error has occured. 
 * @author Andrew Bowley
 * 18/07/2014
 */
public class PreCommit
{

    protected Throwable preCommitException;

    protected boolean doRollback;

    protected TransactionCallable onPreCommit;
    
    /**
     * Create PreCommit object
     * @param onPreCommit Callable
     * @see java.util.concurrent.Callable
     */
    public PreCommit(TransactionCallable onPreCommit)
    {
        this.onPreCommit = onPreCommit;
    }
    
    /**
     * Execute call
     * @param databaseConnection Open database connection on which transaction is active
     */
    public void doPreCommit(DatabaseConnection databaseConnection)
    {
        boolean success = false; // Use flag to identify RuntimeExceptions
        try
        {
            doRollback = !onPreCommit.call(databaseConnection);
            success = true;
        }
        catch (PersistenceException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (IllegalArgumentException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (IllegalStateException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (UnsupportedOperationException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (RuntimeException e)
        {
            throw(e);
        }
        catch (Exception e)
        {
            preCommitException = e;
            doRollback = true;
        }
        finally
        {
            if (!success)
                doRollback = true;
        }
    }

    /**
     * Returns caught Exception, if thrown
     * @return Throwable or null if no exception thrown
     */
    public Throwable getPreCommitException() 
    {
        return preCommitException;
    }

    /**
     * Returns rollback flag
     * @return boolean - true if rollback only
     */
    public boolean isDoRollback() 
    {
        return doRollback;
    }
}
