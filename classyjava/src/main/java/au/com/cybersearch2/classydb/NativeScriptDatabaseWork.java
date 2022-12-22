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
package au.com.cybersearch2.classydb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.SqlParser.StatementCallback;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.support.DatabaseConnection;

/**
 * NativeScriptDatabaseWork
 * Implementation of TransactionCallable interface to be executed upon transaction commit.
 * Executes SQL statements contained in a script file. Each statement must be delimited with a semi-colon ';'.
 * @author Andrew Bowley
 * 31/07/2014
 */
public class NativeScriptDatabaseWork implements TransactionCallable
{
    private static final String TAG = "NativeScriptDatabaseWork";
    private static Log log = JavaLogger.getLogger(TAG);
    
    final String[] filenames;
    /** Resource environment provides system-specific file open method. */
    protected ResourceEnvironment resourceEnvironment;
    
    /**
     * Create NativeScriptDatabaseWork object
     * @param resourceEnvironment Resource environment
     * @param filenames SQL script file names 
     */
    public NativeScriptDatabaseWork(ResourceEnvironment resourceEnvironment, String... filenames)
    {
        this.resourceEnvironment = resourceEnvironment;
        this.filenames = filenames == null ? new String[]{} : filenames;
    }

	/**
	 * Execute SQL statements contained in a script file
	 * @see au.com.cybersearch2.classyjpa.transaction.TransactionCallable#call(com.j256.ormlite.support.DatabaseConnection)
	 */
	@Override
	public Boolean call(final DatabaseConnection databaseConnection) throws Exception 
	{   // Execute SQL statement in SqlParser callback
        StatementCallback callback = new StatementCallback(){
            
            @Override
            public void onStatement(String statement) throws SQLException {
                databaseConnection.executeStatement(statement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
            }};
        boolean success = false;
        for (String filename: filenames)
        {
            if ((filename == null) || (filename.length() == 0))
                continue;
            success = false;
            InputStream instream = null;
            try
            {
                instream = resourceEnvironment.openResource(filename);
                SqlParser sqlParser = new SqlParser();
                sqlParser.parseStream(instream, callback);
                success = true;
                if (log.isLoggable(TAG, Level.FINE))
                    log.debug(TAG, "Executed " + sqlParser.getCount() + " statements from " + filename);
            }
            catch(SQLException e)
            {
            	throw new PersistenceException("Error executing native script " + filename, e);
            }
            finally
            {
                close(instream, filename);
            }
        }
        return success;
	}

	/**
	 * Quietly close file stream
	 * @param instream InputStream
	 * @param filename
	 */
    private void close(InputStream instream, String filename) 
    {
        if (instream != null)
            try
            {
                instream.close();
            }
            catch (IOException e)
            {
                log.warn(TAG, "Error closing file " + filename, e);
            }
    }

}
