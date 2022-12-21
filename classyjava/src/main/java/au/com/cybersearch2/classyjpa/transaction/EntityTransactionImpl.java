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

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * EntityTransactionImpl
 * Implements javax.persistence.EntityTransaction. 
 * Database connection management is delegated to TransactionState class.
 * @author Andrew Bowley
 * 03/05/2014
 * Code originates from com.j256.ormlite.misc.TransactionManager by graywatson
   Note on OrmLite autocommit
         * Sqlite does not support auto-commit. The various JDBC drivers seem to implement it with the use of a
         * transaction. 
  Therefore there will be no attempt to save and restore auto-commit state as it just interferes with the
  transaction in progress.
*/
public class EntityTransactionImpl implements EntityTransaction
{
    public static final String TAG = "ClassyEntityTransaction";
    private static final Log log = JavaLogger.getLogger(TAG);

    
    protected volatile boolean isActive;
    protected volatile boolean rollbackOnly;
    protected TransactionCallable onPreCommit;
    protected Callable<Boolean> onPostCommit;
    protected ConnectionSource connectionSource;
    protected TransactionState transactionState;

    /**
     * Construct a ClassyEntityTransaction instance
     * @param connectionSource ConnectionSource to be used for database operations
     */
    public EntityTransactionImpl(ConnectionSource connectionSource)
    {
        this(connectionSource, null, null);
    }
    
    /**
     * Construct a ClassyEntityTransaction instance
     * @param connectionSource ConnectionSource to be used for database operations
     * @param onPreCommit Callable of return type Boolean to invoke on commit() called
     */
    public EntityTransactionImpl(ConnectionSource connectionSource, TransactionCallable onPreCommit)
    {
        this(connectionSource, onPreCommit, null);
    }

    /**
     * Construct a ClassyEntityTransaction instance
     * @param connectionSource ConnectionSource to be used for database operations
     * @param onPreCommit Callable of return type Boolean to invoke on commit() called
     * @param onPostCommit Callable of return type Boolean to invoke after commit() called and transaction committed.
     * OnPostCommit should just do clean up, auditing and other non-essential operations.
     * NOTE: The post-commit call will not occur if onPreCommit is called and fails. The onPreCommit parameter may be null.  
     */
    public EntityTransactionImpl(ConnectionSource connectionSource, TransactionCallable onPreCommit, Callable<Boolean> onPostCommit)
    {
        this.connectionSource = connectionSource;
        this.onPreCommit = onPreCommit;
        this.onPostCommit = onPostCommit;
    }

    /**
     * Start the resource transaction.
     * @throws IllegalStateException if {@link #isActive()} is true.
     */
    @Override
    public void begin() 
    {
        if (isActive)
            throw new IllegalStateException("begin() called while active");
        try
        {
            transactionState = getTransactionState();
            isActive = true;
        }
        catch (SQLException e)
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "begin() failed");
            throw new PersistenceException("begin transaction error " + e.getMessage(), e);
        }
    }

    /**
     * Returns new TransactionState instance.
     * @return TransactionState
     * @throws SQLException if ConnectionSource error occurs
     */
    protected TransactionState getTransactionState() throws SQLException 
    {
        return new TransactionState(connectionSource);
    }

    /**
     * Commit the current transaction, writing any unflushed changes to the database.
     * @throws IllegalStateException if {@link #isActive()} is false.
     * @throws RollbackException if the commit fails.
     */
    @Override
    public void commit() throws RollbackException
    {
        if (!isActive)
            throw new IllegalStateException("commit() called while not active");
        // Work on local TransactionState to allow isActive to be cleared
        TransactionState commitTransactionState = transactionState;
        transactionState = null;
        boolean doRollback = rollbackOnly;
        rollbackOnly = false;
        isActive = false;
        DatabaseConnection connection = commitTransactionState.getDatabaseConnection();
        if (!doRollback && (onPreCommit != null))
        {
            // Delegate pre-commit call to PreCommit class. This will capture error details if the call fails.
            PreCommit preCommit = new PreCommit(onPreCommit);
            try
            {
                preCommit.doPreCommit(connection);
            }
            finally
            {
                if (preCommit.isDoRollback())
                {   // Pre-commit error requires rollback
                    try
                    {
                        commitTransactionState.doRollback();
                    }
                    catch (SQLException e)
                    {    // Only log rollback execption as an exception will be throw anyway.
                        log.error(TAG, "rollback() failed", e);
                    }
                }
            }
            if (preCommit.getPreCommitException() != null)
                throw new PersistenceException("Pre commit operation failed", preCommit.getPreCommitException());
            if (preCommit.isDoRollback())
                // Case where onPreCommit() returned false rather than throwing an exception
                throw new PersistenceException("Pre commit failure caused rollback");
        }
        SQLException sqlException = null;
        try
        {
            if (!doRollback)
                commitTransactionState.doCommit();
            else
                commitTransactionState.doRollback();
        }
        catch (SQLException e)
        {   // Delay handling this exception to after post commit called, if required.
            sqlException = e;
        }
        // PostCommit operation is not allowed to to throw exceptions (except for unexpected RuntimeExceptions)
        if (onPostCommit != null)
        {
            PostCommit postCommit = new PostCommit(onPostCommit);
            postCommit.doPostCommit();
            if (postCommit.getPostCommitException() != null)
                log.error(TAG, "Post commit failed", postCommit.getPostCommitException());
            else if (postCommit.getError() != null)
                log.error(TAG, postCommit.getError());
        }
        if (sqlException != null)
            throw new PersistenceException("Exception on commit/rollback: " + sqlException.getMessage(), sqlException);
        /* Alternative
        {    // Only log commit/rollback exception as transaction outcome is uncertain.
            log.error(TAG, "Error on commit/rollback", sqlException);
        } */
    }


    /**
     * Determine whether the current transaction has been marked
     * for rollback.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public boolean getRollbackOnly() 
    {
        if (!isActive)
            throw new IllegalStateException("getRollbackOnly() called while not active");
        return rollbackOnly;
    }

    /**
     * Indicate whether a transaction is in progress.
     * @throws PersistenceException if an unexpected error condition is encountered.
     */
    @Override
    public boolean isActive() 
    {
        return isActive;
    }

    /**
     * Roll back the current transaction
     * @throws IllegalStateException if {@link #isActive()} is false.
     * @throws PersistenceException if an unexpected error
     * condition is encountered.
     */
    @Override
    public void rollback() 
    {
        if (!isActive)
            throw new IllegalStateException("rollback() called while not active");
        // Work on local TransactionState to allow isActive to be cleared
        TransactionState rollbackTransactionState = transactionState;
        transactionState = null;
        rollbackOnly = false;
        isActive = false;
        SQLException exception = null;
        try
        {
            rollbackTransactionState.doRollback();
        }
        catch (SQLException e)
        {
            exception = e;
        }
        if (exception != null) // Do not throw exception from rollback so it can be called from a finally clause
            log.error(TAG, "rollback() failed", exception);
    }

    /**
     * Mark the current transaction so that the only possible
     * outcome of the transaction is for the transaction to be
     * rolled back.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public void setRollbackOnly() 
    {
        if (!isActive)
            throw new IllegalStateException("setRollbackOnly() called while not active");
        rollbackOnly = true;
    }

}
