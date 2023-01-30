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

import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;

/**
 * PersistenceContext
 * Application persistence interface
 * @author Andrew Bowley
 * 05/07/2014
 */
public class PersistenceContext
{
    protected PersistenceFactory persistenceFactory;
    protected ConnectionSourceFactory connectionSourceFactory;
    protected boolean isInitialized;
   
    /**
     * Create PersistenceContext object
     * @param persistenceFactory Persistence factory
     * @param connectionSourceFactory ConnectionSource factory
     */
    public PersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        this(persistenceFactory, connectionSourceFactory, true);
    }

    /**
     * Create PersistenceContext object
     * @param persistenceFactory Persistence factory
     * @param connectionSourceFactory ConnectionSource factory
     * @param initialize Flag set true if all databases are to be initialized too
     */
    public PersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory, boolean initialize)
    {
        this.persistenceFactory = persistenceFactory;
        this.connectionSourceFactory = connectionSourceFactory;
        persistenceFactory.initializeAllConnectionSources(connectionSourceFactory);
        if (initialize)
            initializeAllDatabases();
    }

    /**
     * Returns persistence unit implementation, specified by name
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceUnitAdmin
     */
    public PersistenceUnitAdmin getPersistenceUnit(String puName)
    {
        return persistenceFactory.getPersistenceUnit(puName);
    }

    /**
     * Returns Database-specific admin object for specified PersistenceUnitAdmin unit name
     * @param puName PersistenceUnitAdmin unit name
     * @return DatabaseAdmin
     */
    public DatabaseAdmin getDatabaseAdmin(String puName) 
    {
        return getPersistenceUnit(puName).getDatabaseAdmin();
    }

    /**
     * Returns JPA admin object for specified PersistenceUnitAdmin unit name
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceAdmin
     */
    public PersistenceAdmin getPersistenceAdmin(String puName) 
    {
        return getPersistenceUnit(puName).getPersistenceAdmin();
    }

    /**
     * Register given Persistence unit name
     * @param puName PersistenceUnitAdmin unit name
     * @param managedClassNames Class name list
     */
    public void registerClasses(String puName, Set<String> managedClassNames)
    {
    	getPersistenceAdmin(puName).registerClasses(managedClassNames);
    }
  
    /**
     * Add given properties to those of the named persistence unit
     * @param puName PersistenceUnitAdmin unit name
     * @param properties Properties
     */
    public void putProperties(String puName, Properties properties)
    {
    	getPersistenceAdmin(puName).getProperties().putAll(properties);
    }
 
    /**
     * Close persistence context
     */
    public void close()
    {
        persistenceFactory.getDatabaseSupport().close();
    }

    /**
     * Initialize all databases, if not already initialized
     */
    public void initializeAllDatabases()
    {
        if (isInitialized)
            return;
        persistenceFactory.initializeAllDatabases(connectionSourceFactory);
        isInitialized = true;
    }

    /**
     * Upgrade all databases
     */
    public void upgradeAllDatabases()
    {
        if (!isInitialized)
            throw new PersistenceException("PersistenceContext upgrade request while uninitialized");
        persistenceFactory.initializeAllDatabases(connectionSourceFactory);
    }

    /**
     * Returns database support
     * @return DatabaseSupport object
     */
    public DatabaseSupport getDatabaseSupport()
    {
        return persistenceFactory.getDatabaseSupport();
    }
}
