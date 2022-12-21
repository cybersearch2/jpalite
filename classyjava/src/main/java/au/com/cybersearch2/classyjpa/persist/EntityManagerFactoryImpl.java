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

import java.util.Collections;
import java.util.Map;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImpl;

/**
 * EntityManagerFactoryImpl
 * Implementation of EntityManagerFactory interface
 * @author Andrew Bowley
 * 25/08/2014
 */
public class EntityManagerFactoryImpl implements EntityManagerLiteFactory
{

    protected ConnectionSource connectionSource;

    protected PersistenceConfig persistenceConfig; 

    private volatile boolean isOpen;

    /**
     * Create an EntityManagerFactoryImpl object
     * @param connectionSource Database connection provider
     * @param persistenceConfig PersistenceUnitAdmin Unit configuration information
     */
    public EntityManagerFactoryImpl(ConnectionSource connectionSource, PersistenceConfig persistenceConfig)
    {
        this.connectionSource = connectionSource; //
        this.persistenceConfig = persistenceConfig;
        isOpen = true;
    }
    
    /**
     * Create a new application-managed EntityManager. This method returns a new EntityManager instance each time it is invoked. The isOpen
     * method will return true on the returned instance.
     * 
     * @return entity manager instance
     * @throws IllegalStateException
     *             if the entity manager factory has been closed
     */
    @Override
    public EntityManagerLite createEntityManager() 
    {
        checkEntityManagerFactoryClosed("createEntityManager");
        return new EntityManagerImpl(
                connectionSource, 
                persistenceConfig);
    }

    /**
     * Create a new application-managed EntityManager with the specified Map of properties. This method returns a new EntityManager instance
     * each time it is invoked. The isOpen method will return true on the returned instance.
     * 
     * @param map
     *            properties for entity manager
     * @return entity manager instance
     * @throws IllegalStateException
     *             if the entity manager factory has been closed
     */
    @Override
    public EntityManagerLite createEntityManager(Map<String, Object> map) 
    {
        return createEntityManager();
    }

    /**
     * Close the factory, releasing any resources that it holds. After a factory instance has been closed, all methods invoked on it will
     * throw the IllegalStateException, except for isOpen, which will return false. Once an EntityManagerFactory has been closed, all its
     * entity managers are considered to be in the closed state.
     * 
     * @throws IllegalStateException
     *             if the entity manager factory has been closed
     */
    @Override
    public void close() 
    {
        checkEntityManagerFactoryClosed("close");
        isOpen = false;
    }

    /**
     * Indicates whether the factory is open. Returns true until the factory has been closed.
     * 
     * @return boolean indicating whether the factory is open
     */
    @Override
    public boolean isOpen() 
    {
        return isOpen;
    }
    
    /**
     * Get the properties and associated values that are in effect for the entity manager factory. Changing the contents of the map does not
     * change the configuration in effect.
     * 
     * @return properties
     * @throws IllegalStateException
     *             if the entity manager factory has been closed
     */
    @Override
    public Map<String, Object> getProperties() 
    {
        checkEntityManagerFactoryClosed("getProperties");
        return Collections.emptyMap();
    }

    /**
     * Confirm this Entity Manager is open
     * @param method Name of method being invoked
     * @throws IllegalStateException if this Entity Manager is closed.
     */
    private void checkEntityManagerFactoryClosed(String method)
    {
        if (!isOpen)
            throw new IllegalStateException(method + " called after EntityManagerFactory has been closed");
    }
}
