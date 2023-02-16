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

import javax.persistence.EntityTransaction;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classyjpa.transaction.TransactionStateFactory;
import au.com.cybersearch2.classylog.LogManager;
import au.com.cybersearch2.container.JpaSetting;
import au.com.cybersearch2.container.SettingsMap;

/**
 * DatabaseAdminImpl
 * Handler of database create and upgrade events

 * @author Andrew Bowley
 * 29/07/2014
 */
public class DatabaseAdminImpl implements DatabaseAdmin
{
	private static Logger logger = LogManager.getLogger(DatabaseAdminImpl.class);
	
    /** PersistenceUnitAdmin control and configuration implementation */
    private final PersistenceAdmin persistenceAdmin;
    /** Resource environment provides system-specific file open method. */
    private final ResourceEnvironment resourceEnvironment;
    
    /** Open helper callbacks */
    private OpenHelper openHelper;
    
    /**
     * Construct a DatabaseAdminImpl object
     * @param persistenceAdmin The persistence unit connectionSource and properties provider  
     * @param resourceEnvironment Resource environment
     */
    public DatabaseAdminImpl(
            PersistenceAdmin persistenceAdmin, 
            ResourceEnvironment resourceEnvironment)
    {
        this.persistenceAdmin = persistenceAdmin;
        this.resourceEnvironment = resourceEnvironment;
    }

    public void setOpenHelper(OpenHelper openHelper) {
        this.openHelper = openHelper;
    }
    
    @Override
    public OpenHelper getOpenHelper()
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
    	SettingsMap settingsMap = persistenceAdmin.getPuInfo().getSettingsMap();
    	boolean hasSchemaFilename = settingsMap.hasSetting(JpaSetting.schema_filename);
    	boolean hasDataFilename = settingsMap.hasSetting(JpaSetting.data_filename);
        if (hasSchemaFilename || hasDataFilename)
        {
        	// Database work is executed as background task
        	String schemaFilename = settingsMap.get(JpaSetting.schema_filename);
        	String dataFilename = settingsMap.get(JpaSetting.data_filename);
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
        SettingsMap settingsMap = persistenceAdmin.getPuInfo().getSettingsMap();
        String filename = null;
         boolean upgradeSupported = settingsMap.hasSetting(JpaSetting.upgrade_filename);
         if (upgradeSupported) {
            filename = settingsMap.get(JpaSetting.upgrade_filename);
         	upgradeSupported = false;
	        InputStream instream = null;
	        try
	        {
	            instream = resourceEnvironment.openResource(filename);
	            upgradeSupported = instream != null;
	        } 
	        catch (IOException e) 
	        {
	        	logger.error("Error opening \"" + filename + "\" for database upgrade", e);
			}
	        finally
	        {
	            close(instream, filename);
	            if (logger.isLevelEnabled(Level.INFO))
	                logger.info("Upgrade file \"" + filename + "\" exists: " + upgradeSupported);
	        }
        }
        if (upgradeSupported) {
        	// Database work is executed in a transaction
        	TransactionCallable processFilesCallable = new NativeScriptDatabaseWork(resourceEnvironment, filename);    
        	executeTask(connectionSource, processFilesCallable);
        }
    }

	/**
	 * Open database and handle create/upgrade events
	 * @param persistenceConfig PersistenceUnitAdmin Unit Configuration
	 * @param databaseSupport Database Support for specific database type 
	 */
	@Override
    public void initializeDatabase(PersistenceConfig persistenceConfig, DatabaseSupport databaseSupport)
    {
        // Ensure database version is up to date.
    	String puName = persistenceAdmin.getPuName();
		SettingsMap settingsMap = persistenceConfig.getPuInfo().getSettingsMap();
   	    int currentDatabaseVersion = PersistenceAdminImpl.getDatabaseVersion(settingsMap);
        // Get a connection to open the database and possibly trigger a create or upgrade event (eg. AndroidSQLite)
        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
        boolean dropSchema = false;
        int reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, puName);
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
        		reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, puName);
        		if (reportedDatabaseVersion != currentDatabaseVersion)
        			persistenceAdmin.setVersion(currentDatabaseVersion);
        	}
        	else
        	{
        		dropSchema = currentDatabaseVersion < reportedDatabaseVersion;
        		if (dropSchema) {
        			dropSchema = settingsMap.hasSetting(JpaSetting.drop_schema_filename);
        			if (dropSchema) {
        				dropSchema= dropSchema(settingsMap.get(JpaSetting.drop_schema_filename), connectionSource);
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
        		persistenceAdmin.setVersion(currentDatabaseVersion);
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
    	TransactionStateFactory transStateFactory = new TransactionStateFactory(connectionSource);
    	EntityTransaction transaction = new EntityTransactionImpl(transStateFactory, processFilesCallable);
        transaction.begin();
        transaction.commit();
    }
   
    /**
     * Database drop schema handler.
     * @param filename Name of file with SQL to drop the schema
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     */
    private boolean dropSchema(String filename, ConnectionSource connectionSource)
    {
        boolean downgradeSupported = false;
        InputStream instream = null;
        try {
            instream = resourceEnvironment.openResource(filename);
            downgradeSupported = instream != null;
        } catch (IOException e) {
        	logger.error("Error opening \"" + filename + "\" for database upgrade", e);
		}
        finally {
            close(instream, filename);
        }
        //	throw new PersistenceException("\"" + puName + "\" database upgrade from v" + oldVersion + " to v" + newVersion + " is not possible");
        if (logger.isLevelEnabled(Level.INFO))
            logger.info("Downgrade file \"" + filename + "\" exists: " + downgradeSupported);
        if (downgradeSupported) {
        	// Database work is executed in a transaction
        	TransactionCallable processFilesCallable = new NativeScriptDatabaseWork(resourceEnvironment, filename);    
        	executeTask(connectionSource, processFilesCallable);
        }
        return downgradeSupported;
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
                logger.error("Error closing file " + filename, e);
            }
    }



}
