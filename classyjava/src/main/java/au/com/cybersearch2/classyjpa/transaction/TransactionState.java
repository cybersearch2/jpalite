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

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * TransactionState
 * Database connection management to enable transaction begin(), commit() and rollback()
 * @author Andrew Bowley
 * 09/05/2014
 */
public class TransactionState
{
    private static final String TAG = "TransactionState";
	private static final String DATABASE_INFO_NAME = "";
    protected static Log log = JavaLogger.getLogger(TAG);

    protected static final String SAVE_POINT_PREFIX = "ORMLITE";
    protected static AtomicInteger savePointCounter;

    protected ConnectionSource connectionSource;
    protected DatabaseConnection connection;
    protected Boolean autoCommitAtStart;
    protected Boolean hasSavePoint;
    protected boolean savedSpecialConnection;
    protected Savepoint savePoint;
    protected String savePointName;
    protected int transactionId;

    static
    {   // Use counter to generate unique savepoint identifiers
        // Note SQLite does not support nested save points
        savePointCounter = new AtomicInteger();
    }
    
 /**
  * Construct a TransactionState instance   
  * @param connectionSource ConnectionSource to be used for database operations
  * @throws SQLException if ConnectionSource error occurs
  */
    public TransactionState(ConnectionSource connectionSource) throws SQLException
    {
        this.connectionSource = connectionSource;
        boolean success = false;
        try
        {
            setup();
            success = true;
        }
        finally
        {   // Clean up if connection was not established
            if (!success)
                release();
        }
    }

    public DatabaseConnection getDatabaseConnection()
    {
        return connection;
    }
    
    protected void setup() throws SQLException
    {
        /*
         * Save this connection and return it for all calls to connectionSource.getReadOnlyConnection() and
         * connectionSource.getReadWriteConnection() 
         * unless the connectionSource.clearSpecialConnection(DatabaseConnection) method is
         * called. This is used by the transaction mechanism since since all operations within a transaction must
         * operate on the same connection. It is also used by the Android code during initialization.
         * 
         * <p>
         * <b> NOTE: </b> This should be a read-write connection since transactions and Android need it to be so.
         * </p>
         * 
         * <p>
         * <b> NOTE: </b> Saving a connection is usually accomplished using ThreadLocals so multiple threads should not be
         * using connections in this scenario.
         * </p>
         * 
         */
    	connection = connectionSource.getReadWriteConnection(DATABASE_INFO_NAME);
    	savedSpecialConnection = connectionSource.saveSpecialConnection(connection);
    	transactionId = savePointCounter.incrementAndGet();
    	if (savedSpecialConnection || connectionSource.getDatabaseType().isNestedSavePointsSupported())
    	{
            setAutoCommit();
            setSavePoint();
    	}
    }
    
    /**
     * Reset everything to initial state
     */
    public void release()
    {
        if (connection != null)
        {
            if (autoCommitAtStart != null)
                resetAutoCommit();
            clearSpecialConnection();
            savePoint = null;
            hasSavePoint = null;
            connection = null;
    	}
    }
  
    /**
     * Commit
     * @throws SQLException if database error occurs
     */
    public void doCommit() throws SQLException 
    {
        // Perform check for release state. 
        if (!isValid() || connection.isAutoCommit())
        {
            if (log.isLoggable(TAG, Level.WARNING) &&
                (savedSpecialConnection || connectionSource.getDatabaseType().isNestedSavePointsSupported()))
                log.warn(TAG, "doCommit() called while invalid");
            return;
        }
        try
        {
	    	//System.out.println("Transaction " + transactionId + " about to commit");
            connection.commit(savePoint);
            if (log.isLoggable(TAG, Level.FINE))
                log.debug(TAG, "committed savePoint transaction " + savePointName);
        }
	    catch (SQLException e) 
        {
		    if (hasSavePoint) 
		    {
			    try 
			    {
			    	doRollback();
			    } 
			    catch (SQLException e2) 
			    {
				    log.error(TAG, "After commit exception, rolling back to save-point also threw exception", e);
				    // we continue to throw the commit exception
			    }
		    }
		    throw e;
        }
        finally
        {
            release();
        }
    }

    /**
     * Rollback
     * @throws SQLException if database error occurs
     */
    public void doRollback() throws SQLException 
    {
        // Perform check for release state. 
        if (!isValid() || connection.isAutoCommit())
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "doRollback() called while invalid");
            return;
        }
        try
        {
            connection.rollback(savePoint);
            if (log.isLoggable(TAG, Level.FINE))
                log.debug(TAG, "rolled back savePoint transaction " + savePointName);
        }
        finally
        {
            release();
        }
    }

    /**
     * Check for release state
     * @return boolean true for is valid
     */
    protected boolean isValid()
    {
        return (connection != null) && (hasSavePoint != null) && (autoCommitAtStart != null);
    }

    /**
     * Turn off auto commit
     * @throws SQLException if database error occurs
     */
    protected void setAutoCommit() throws SQLException
    {
        if (connection.isAutoCommitSupported()) 
        {
            autoCommitAtStart = Boolean.valueOf(connection.isAutoCommit());
            if (autoCommitAtStart) 
            {
                // Disable auto-commit mode if supported and enabled at start
                connection.setAutoCommit(false);
                if (log.isLoggable(TAG, Level.FINE))
                    log.debug(TAG, "Had to set auto-commit to false");
            }
        }

    }

    /**
     * Store save point
     * @throws SQLException if database error occurs
     */
    protected void setSavePoint() throws SQLException
    {
    	savePointName = SAVE_POINT_PREFIX + transactionId;
        savePoint = connection.setSavePoint(savePointName);
        if (log.isLoggable(TAG, Level.FINE))
            log.debug(TAG, "Started savePoint transaction " + savePointName);
        hasSavePoint = Boolean.TRUE;
    }
 
    /**
     * Restore auto commit if required
     */
    protected void resetAutoCommit()
    {
        // try to restore if we are in auto-commit mode
        if (autoCommitAtStart) 
            try
            {
                connection.setAutoCommit(true);
                if (log.isLoggable(TAG, Level.FINE))
                    log.debug(TAG, "restored auto-commit to true");
            }
            catch (SQLException e)
            {
                if (log.isLoggable(TAG, Level.WARNING))
                    log.warn(TAG, "setAutoCommit() failed");
            }
            finally
            {
                autoCommitAtStart = null;
            }
    }
    
    /**
     * Clear arrangement to use a single connection for the transaction
     */
    protected void clearSpecialConnection()
    {
        connectionSource.clearSpecialConnection(connection);
        try
        {
            connectionSource.releaseConnection(connection);
        }
        catch (SQLException e)
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "releaseConnection() failed");
        }
    }
}
