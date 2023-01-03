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
package au.com.cybersearch2.classyjpa.persist;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseType;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classydb.OpenHelper;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;

/**
 * PersistenceFactory
 * Creates Persistence Unit implementations based on persistence.xml configuration
 * @author Andrew Bowley
 * 05/07/2014
 */
public class PersistenceFactory
{
    /** Persist double values as bit-encode long values */
    public static boolean useDoubleLongBits = false;

    /** Native support. */
    protected DatabaseSupport databaseSupport;
    /** Maps PersistenceAdmin implementation to persistence unit name */
    protected Map<String, PersistenceAdminImpl> persistenceImplMap;
    /** Maps DatabaseAdmin implementation to persistence unit name */
    protected Map<String, DatabaseAdminImpl> databaseAdminImplMap;
    /** Interface for access to persistence.xml */
    private final ResourceEnvironment resourceEnvironment;
    /** Flag set true if initializePersistenceContext() called */
    private boolean isContextInitialized;
    
    /**
     * Create PersistenceFactory object
     * @param databaseType Database type
     * @param connectionType Connection type
     * @param resourceEnvironment Resource environment
     * @throws PersistenceException for error opening or parsing persistence.xml
     */
    public PersistenceFactory(DatabaseType databaseType, ConnectionType connectionType, ResourceEnvironment resourceEnvironment)
    {
        this.resourceEnvironment = resourceEnvironment;
        databaseSupport = getDatabaseSupport(databaseType, connectionType);
        persistenceImplMap = new HashMap<>();
        databaseAdminImplMap = new HashMap<>();
        if (useDoubleLongBits)
        	setUseDoubleLongBits();
    }

    /**
     * Returns a PersistenceContext instance
     * @return PersistenceContext object
     */
    public PersistenceContext persistenceContextInstance() {
    	if (!isContextInitialized) {
            initializePersistenceContext();
            isContextInitialized = true;
    	}
    	return new PersistenceContext(this, (ConnectionSourceFactory) databaseSupport, true);
    }
    
	/**
     * Returns native support
     * @return DatabaseSupport
     */
    public DatabaseSupport getDatabaseSupport()
    {
        return databaseSupport;
    }
 
    /**
     * Returns persistence unit implementation, specified by name
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceUnitAdmin
     */
    public PersistenceUnitAdmin getPersistenceUnit(final String puName)
    {
    	if (!persistenceImplMap.containsKey(puName))
    		throw new PersistenceException("Persistence Unit named \"" + puName + "\" not found");
        return new PersistenceUnitAdmin(){

            /**
             * Returns Database-specific admin object
             * @return DatabaseAdmin
             */
            @Override
            public DatabaseAdmin getDatabaseAdmin() 
            {
                return databaseAdminImplMap.get(puName);
            }

            /**
             * Returns JPA admin object
             * @return PersistenceAdmin
             */
            @Override
            public PersistenceAdmin getPersistenceAdmin() 
            {
                return persistenceImplMap.get(puName);
            }

            /**
             * Returns persistence unit name
             * @return String
             */
            @Override
            public String getPersistenceUnitName() 
            {
                return puName;
            }
        };
    }

    public void initializeAllDatabases(ConnectionSourceFactory connectionSourceFactory)
    {
        //Initialize PU implementations
        for (Map.Entry<String, DatabaseAdminImpl> entry: databaseAdminImplMap.entrySet())
        {
            PersistenceAdminImpl persistenceAdmin = persistenceImplMap.get(entry.getKey());
        	DatabaseAdminImpl databaseAdmin = entry.getValue();
        	databaseAdmin.initializeDatabase(persistenceAdmin.getConfig(), databaseSupport);
        }
    }
    
    public void initializeAllConnectionSources(ConnectionSourceFactory connectionSourceFactory)
    {
        //Initialize PU implementations
        for (Map.Entry<String, DatabaseAdminImpl> entry: databaseAdminImplMap.entrySet())
        {
            PersistenceAdminImpl persistenceAdmin = persistenceImplMap.get(entry.getKey());
            PersistenceUnitInfo puInfo = persistenceAdmin.getConfig().getPuInfo();
            String databaseName = PersistenceAdminImpl.getDatabaseName(puInfo);
            ConnectionSource connectionSource = 
                    connectionSourceFactory.getConnectionSource(databaseName, puInfo.getProperties());
            persistenceAdmin.setConnectionSource(connectionSource);
            persistenceAdmin.setSingleConnection();
        }
    }
    
    /**
     * Returns object to which persistence.xml is unmarshalled
     * @param  resourceEnv Resource environment
     * @return Map&lt;String, PersistenceUnitInfo&gt; - maps each peristence unit data to it's name
     * @throws IOException for error reading persistence.xml
     * @throws XmlPullParserException for error parsing persistence.xml
     */
    public static Map<String, PersistenceUnitInfo> getPersistenceUnitInfo(ResourceEnvironment resourceEnv) throws IOException, XmlPullParserException
    {
        InputStream inputStream = null;
        PersistenceXmlParser parser = null;
        Map<String, PersistenceUnitInfo> persistenceUnitInfoMap = null;
        try
        {
            inputStream = resourceEnv.openResource(PersistenceUnitInfoImpl.PERSISTENCE_CONFIG_FILENAME);
            parser = new PersistenceXmlParser();
            persistenceUnitInfoMap = parser.parsePersistenceXml(inputStream);
        }
        finally
        {
            if (inputStream != null)
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                }
        }
        return persistenceUnitInfoMap;
    }

    /**
     * Initialize persistence unit implementations based on persistence.xml configuration
     * @throws PersistenceException for error opening or parsing persistence.xml
     */
    protected synchronized void initializePersistenceContext()
    {
        // Input persistence.xml
        Map<String, PersistenceUnitInfo> puMap = readPersistenceConfigFile(resourceEnvironment);
        // Set up PU implementations
        for (String name: puMap.keySet())
        {
            // Create configuration object and initialize it according to PU info read from persistence.xml
            // This includes setting up DAOs for all entity classes
            PersistenceConfig persistenceConfig = new PersistenceConfig(databaseSupport.getDatabaseType());
            persistenceConfig.setEntityClassLoader(resourceEnvironment.getEntityClassLoader(name));
            persistenceConfig.setPuInfo(puMap.get(name));
            // Create objects for JPA and native support which are accessed using PersistenceFactory
            PersistenceAdminImpl persistenceAdmin = new PersistenceAdminImpl(name, databaseSupport, persistenceConfig);
            persistenceImplMap.put(name, persistenceAdmin);
            OpenHelper openHelper = getOpenHelperCallbacks(persistenceConfig.getPuInfo().getProperties());
            DatabaseAdminImpl databaseAdmin = new DatabaseAdminImpl(name, persistenceAdmin, resourceEnvironment, openHelper);
            databaseAdminImplMap.put(name, databaseAdmin);
        }
        databaseSupport.initialize();
    }

    /**
     * Returns OpenHelperCallbacks object, if defined in the PU properties
     * @param properties Properties object
     * @return OpenHelperCallbacks or null if not defined
     */
    protected OpenHelper getOpenHelperCallbacks(Properties properties)
    {
        // Property "open-helper-callbacks-classname"
        String openHelperCallbacksClassname = properties.getProperty(DatabaseSupport.JTA_PREFIX + PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY);
        if (openHelperCallbacksClassname != null)
        {
            // Custom
            for (OpenHelper openHelper: databaseSupport.getOpenHelperCallbacksList())
                if (openHelper.getClass().getName().equals(openHelperCallbacksClassname))
                    return openHelper;
            throw new PersistenceException(openHelperCallbacksClassname + " object not registered");
        }
        // Mo match
        return null;
    }
    

    protected Map<String, PersistenceUnitInfo> readPersistenceConfigFile(ResourceEnvironment resourceEnv)
    {
        // Input persistence.xml
        Map<String, PersistenceUnitInfo> puMap = null;
        try
        {
            puMap  = getPersistenceUnitInfo(resourceEnv);
        }
        catch (IOException e)
        {
            throw new PersistenceException("Error opening persistence configuration file", e);
        }
        catch (XmlPullParserException e)
        {
            throw new PersistenceException("Error parsing persistence configuration file", e);
        }
        return puMap;
    }
    
    private static void setUseDoubleLongBits() {
        DataPersisterManager.registerDataPersisters(
    		JavaDoubleType.getSingleton(), PrimitiveJavaDoubleType.getSingleton());
	}

    private DatabaseSupport getDatabaseSupport(DatabaseType databaseType, ConnectionType connectionType) {
		switch (databaseType) {
		case H2: return new H2DatabaseSupport(connectionType);
		case SQLite: return new SQLiteDatabaseSupport(connectionType);
		default:
		}
		throw new PersistenceException(String.format("Unsupported database type %s", databaseType.name()));
	}

}
