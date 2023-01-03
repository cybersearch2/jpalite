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
import java.util.Properties;
import java.util.logging.Level;

import javax.persistence.EntityTransaction;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * DatabaseAdminImpl
 * Handler of database create and upgrade events

 * @author Andrew Bowley
 * 29/07/2014
 */
public class DatabaseAdminImpl implements DatabaseAdmin
{
    private static final String TAG = "DatabaseAdminImpl";
    private static Log log = JavaLogger.getLogger(TAG);
    /** PersistenceUnitAdmin unit name*/
    protected String puName;
    /** PersistenceUnitAdmin control and configuration implementation */
    protected PersistenceAdmin persistenceAdmin;
    /** Resource environment provides system-specific file open method. */
    protected ResourceEnvironment resourceEnvironment;
    protected OpenHelper openHelper;
    
    /**
     * Construct a DatabaseAdminImpl object
     * @param puName The persistence unit name
     * @param persistenceAdmin The persistence unit connectionSource and properties provider  
     * @param resourceEnvironment Resource environment
     * @param openHelper Open helper callbacks
     */
    public DatabaseAdminImpl(
            String puName, 
            PersistenceAdmin persistenceAdmin, 
            ResourceEnvironment resourceEnvironment,
            OpenHelper openHelper)
    {
        this.puName = puName;
        this.persistenceAdmin = persistenceAdmin;
        this.resourceEnvironment = resourceEnvironment;
        if (openHelper != null)
        {
            openHelper.setDatabaseAdmin(this);
            openHelper.setPersistenceAdmin(persistenceAdmin);
            this.openHelper = openHelper;
        }
    }

    @Override
    public OpenHelper getCustomOpenHelperCallbacks()
    {
        return openHelper;
    }

    /**
     * Database create handler.
     * Optionally runs native script to create schema and populate the database with data.
     * Note that because ORMLite uses a ThreadLocal variable for a special connection, this 
     * executes in a single thread.
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script file names from persistence.xml properties
        // A filename may be null if operation not supported
         String schemaFilename = properties.getProperty(DatabaseSupport.JTA_PREFIX + DatabaseAdmin.SCHEMA_FILENAME);
        String dataFilename = properties.getProperty(DatabaseSupport.JTA_PREFIX + DatabaseAdmin.DATA_FILENAME);
        if (!((schemaFilename == null) && (dataFilename == null)))
        {
        	// Database work is executed as background task
        	TransactionCallable processFilesCallable = 
                new NativeScriptDatabaseWork(resourceEnvironment, schemaFilename, dataFilename);    
        	executeTask(connectionSource, processFilesCallable);
        }
    }

    /**
     * Database upgrade handler.
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        Properties properties = persistenceAdmin.getProperties();
        String filename = properties.getProperty(DatabaseSupport.JTA_PREFIX + DatabaseAdmin.UPGRADE_FILENAME);
        boolean upgradeSupported = filename != null;
        if (upgradeSupported) {
        	upgradeSupported = false;
	        InputStream instream = null;
	        try
	        {
	            instream = resourceEnvironment.openResource(filename);
	            upgradeSupported = instream != null;
	        } 
	        catch (IOException e) 
	        {
	        	log.error(TAG, "Error opening \"" + filename + "\" for database upgrade", e);
			}
	        finally
	        {
	            close(instream, filename);
	            if (log.isLoggable(TAG, Level.INFO))
	                log.info(TAG, "Upgrade file \"" + filename + "\" exists: " + upgradeSupported);
	        }
        }
        if (upgradeSupported) {
        	// Database work is executed in a transaction
        	TransactionCallable processFilesCallable = new NativeScriptDatabaseWork(resourceEnvironment, filename);    
        	executeTask(connectionSource, processFilesCallable);
        }
    }

    /**
     * Database drop schema handler.
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     */
    private boolean dropSchema(ConnectionSource connectionSource)
    {
        Properties properties = persistenceAdmin.getProperties();
        String filename = properties.getProperty(DatabaseSupport.JTA_PREFIX + DatabaseAdmin.DROP_SCHEMA_FILENAME);
        boolean downgradeSupported = false;
        InputStream instream = null;
        try {
            instream = resourceEnvironment.openResource(filename);
            downgradeSupported = instream != null;
        } catch (IOException e) {
        	log.error(TAG, "Error opening \"" + filename + "\" for database upgrade", e);
		}
        finally {
            close(instream, filename);
        }
        //	throw new PersistenceException("\"" + puName + "\" database upgrade from v" + oldVersion + " to v" + newVersion + " is not possible");
        if (log.isLoggable(TAG, Level.INFO))
            log.info(TAG, "Downgrade file \"" + filename + "\" exists: " + downgradeSupported);
        if (downgradeSupported) {
        	// Database work is executed in a transaction
        	TransactionCallable processFilesCallable = new NativeScriptDatabaseWork(resourceEnvironment, filename);    
        	executeTask(connectionSource, processFilesCallable);
        }
        return downgradeSupported;
    }
    
	/**
	 * Open database and handle create/upgrade events
	 * @param persistenceConfig PersistenceUnitAdmin Unit Configuration
	 * @param databaseSupport Database Support for specific database type 
	 */
    public void initializeDatabase(PersistenceConfig persistenceConfig, DatabaseSupport databaseSupport)
    {
        // Ensure database version is up to date.
    	Properties properties = persistenceConfig.getPuInfo().getProperties();
    	int currentDatabaseVersion = PersistenceAdminImpl.getDatabaseVersion(properties);
        // Get a connection to open the database and possibly trigger a create or upgrade event (eg. AndroidSQLite)
        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
        boolean dropSchema = false;
        int reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, properties);
        if (reportedDatabaseVersion != currentDatabaseVersion)
        {   // No assistance provided by helper to trigger create/upgrade event
            // Allow custom create/upgrade handler
        	if (reportedDatabaseVersion == 0)
        	{
        		if (openHelper == null)
        			onCreate(connectionSource);
        		else
        			openHelper.onCreate(connectionSource);
        		// Get database version again in case onCreate() set it
        		reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, properties);
        		if (reportedDatabaseVersion != currentDatabaseVersion)
        		    databaseSupport.setVersion(currentDatabaseVersion, properties, connectionSource);
        	}
        	else
        	{
        		dropSchema = currentDatabaseVersion < reportedDatabaseVersion;
        		if (dropSchema) {
        			dropSchema = properties.containsKey(DatabaseSupport.JTA_PREFIX + DatabaseAdmin.DROP_SCHEMA_FILENAME);
        			if (dropSchema) {
        				dropSchema= dropSchema(connectionSource);
            			if (dropSchema) {
		            		if (openHelper == null)
		            			onCreate(connectionSource);
		            		else
		            			openHelper.onCreate(connectionSource);
            			}
        			}
        		} else {
	        		if (openHelper == null)
	        			onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
	        		else
	        			openHelper.onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
        		}
    		    databaseSupport.setVersion(currentDatabaseVersion, properties, connectionSource);
       	    }
        }
        persistenceConfig.checkEntityTablesExist(connectionSource);
    }

    /**
     * Execute database work
     * @param connectionSource Open ConnectionSource
     * @param processFilesCallable TransactionCallable Object containing unit of work to perform
     */
    protected void executeTask(ConnectionSource connectionSource, TransactionCallable processFilesCallable)
    {
        // Execute task on transaction commit using Callable
    	EntityTransaction transaction = new EntityTransactionImpl(connectionSource, processFilesCallable);
        transaction.begin();
        transaction.commit();
    }
   
    /**
     * Closes input stream quietly
     * @param instream InputStream
     * @param filename Name of file being closed
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
