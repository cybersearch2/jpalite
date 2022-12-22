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
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.support.ConnectionSource;

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
    /** Default filename template for upgrade */
    protected String DEFAULT_FILENAME_TEMPLATE = "{2}-upgrade-v{0}-v{1}.sql";
    /** PersistenceUnitAdmin unit name*/
    protected String puName;
    /** PersistenceUnitAdmin control and configuration implementation */
    protected PersistenceAdmin persistenceAdmin;
    /** Resource environment provides system-specific file open method. */
    protected ResourceEnvironment resourceEnvironment;
    protected OpenHelperCallbacks openHelperCallbacks;
    
    /**
     * Construct a DatabaseAdminImpl object
     * @param puName The persistence unit name
     * @param persistenceAdmin The persistence unit connectionSource and properties provider  
     * @param resourceEnvironment Resource environment
     * @param openHelperCallbacks Open helper callbacks
     */
    public DatabaseAdminImpl(
            String puName, 
            PersistenceAdmin persistenceAdmin, 
            ResourceEnvironment resourceEnvironment,
            OpenHelperCallbacks openHelperCallbacks)
    {
        this.puName = puName;
        this.persistenceAdmin = persistenceAdmin;
        this.resourceEnvironment = resourceEnvironment;
        if (openHelperCallbacks != null)
        {
            openHelperCallbacks.setDatabaseAdmin(this);
            openHelperCallbacks.setPersistenceAdmin(persistenceAdmin);
            this.openHelperCallbacks = openHelperCallbacks;
        }
    }

    @Override
    public OpenHelperCallbacks getCustomOpenHelperCallbacks()
    {
        return openHelperCallbacks;
    }

    /**
     * Database create handler.
     * Optionaly runs native scripts to drop and create schema and populate the database with data.
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
        String schemaFilename = properties.getProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME);
        String dropSchemaFilename = properties.getProperty(DatabaseAdmin.SCHEMA_FILENAME);
        String dataFilename = properties.getProperty(DatabaseAdmin.DATA_FILENAME);
        if (!((schemaFilename == null) && (dropSchemaFilename == null) && (dataFilename == null)))
        {
        	// Database work is executed as background task
        	TransactionCallable processFilesCallable = 
                new NativeScriptDatabaseWork(resourceEnvironment, schemaFilename, dropSchemaFilename, dataFilename);    
        	executeTask(connectionSource, processFilesCallable);
        }
    }

    /**
     * Database upgrade handler.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p>  
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script upgrade file name format from persistence.xml properties
        boolean upgradeSupported = false;
        String upgradeFilenameFormat = properties.getProperty(DatabaseAdmin.UPGRADE_FILENAME_FORMAT);
        String filename = null;
        // Default filename format: "{puName}-upgrade-v{old-version}-v{new-version}.sql" 
        if (upgradeFilenameFormat == null)
        {
         	MessageFormat messageFormat = new MessageFormat(DEFAULT_FILENAME_TEMPLATE, resourceEnvironment.getLocale());
        	filename = messageFormat.format(new String[] { Integer.toString(oldVersion), Integer.toString(newVersion), puName }).toString();
        }
        // Custom format can be defined in PU properties. [0] and [1] substitute for old-version and new-version respectively
        else
        {
        	MessageFormat messageFormat = new MessageFormat(upgradeFilenameFormat, resourceEnvironment.getLocale());
      	    filename = messageFormat.format(new String[] { Integer.toString(oldVersion), Integer.toString(newVersion) }).toString();
        }
        InputStream instream = null;
        try
        {
            instream = resourceEnvironment.openResource(filename);
            upgradeSupported = true;
        } 
        catch (IOException e) 
        {
        	log.error(TAG, "Error opening \"" + filename + "\" for database upgrade", e);
		}
        finally
        {
            close(instream, filename);
        }
        if (!upgradeSupported)
        	throw new PersistenceException("\"" + puName + "\" database upgrade from v" + oldVersion + " to v" + newVersion + " is not possible");
        if (log.isLoggable(TAG, Level.INFO))
            log.info(TAG, "Upgrade file \"" + filename + "\" exists: " + upgradeSupported);
    	// Database work is executed in a transaction
        TransactionCallable processFilesCallable = new NativeScriptDatabaseWork(resourceEnvironment, filename);    
    	executeTask(connectionSource, processFilesCallable);
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
        int reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, properties);
        if (reportedDatabaseVersion != currentDatabaseVersion)
        {   // No assistance provided by helper to trigger create/upgrade event
            // Allow custom creat/upgrade handler
        	if (reportedDatabaseVersion == 0)
        	{
        		if (openHelperCallbacks == null)
        			onCreate(connectionSource);
        		else
        			openHelperCallbacks.onCreate(connectionSource);
        		// Get database version again in case onCreate() set it
        		reportedDatabaseVersion = databaseSupport.getVersion(connectionSource, properties);
        		if (reportedDatabaseVersion != currentDatabaseVersion)
        		    databaseSupport.setVersion(currentDatabaseVersion, properties, connectionSource);
        	}
        	else
        	{
        		if (openHelperCallbacks == null)
        			onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
        		else
        			openHelperCallbacks.onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
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
     * Cloes input stream quietly
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
