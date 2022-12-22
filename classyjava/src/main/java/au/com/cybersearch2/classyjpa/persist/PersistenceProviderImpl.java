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

import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.PersistenceProvider;

/**
 * PersistenceProviderImpl
 * Implementation of PersistenceProvider interface
 * @author Andrew Bowley
 * 28/05/2014
 */
public class PersistenceProviderImpl implements PersistenceProvider
{
    /** PersistenceUnitAdmin Unit configuration information */
    protected PersistenceConfig persistenceConfig;
    /** Connection source factory */
    protected ConnectionSourceFactory connectionSourceFactory;
    /** PersistenceUnitAdmin Unit name */
    public final String puName;

    /**
     * Create PersistenceProviderImpl object
     * @param puName PersistenceUnitAdmin unit name
     * @param persistenceConfig PersistenceUnitAdmin configuration
     * @param connectionSourceFactory Database connection provider object
     */
    public PersistenceProviderImpl(
            String puName, 
            PersistenceConfig persistenceConfig, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        this.puName = puName;
        this.persistenceConfig = persistenceConfig;
        this.connectionSourceFactory = connectionSourceFactory;
    }

    /**
     * Called by PersistenceUnitAdmin class when an EntityManagerFactory is to be created.
     * 
     * @param emName
     *            The name of the persistence unit
     * @param map
     *            Not used - set to null
     * @return EntityManagerFactory for the persistence unit, or null if the provider is not the right provider
     */
    @Override
    public EntityManagerLiteFactory createEntityManagerFactory(String emName,
            @SuppressWarnings("rawtypes") Map map) 
    {
        if (puName.equals(emName))
            return new EntityManagerFactoryImpl(connectionSourceFactory.getConnectionSource(), persistenceConfig);
        return null;
    }

    /**
     * Called by the container when an EntityManagerFactory is to be created.
     * 
     * @param info
     *            Metadata for use by the persistence provider
     * @param map
     *            Not used - set to null
     * @return EntityManagerFactory for the persistence unit specified by the metadata, or null if the provider is not the right provider
     */
    @Override
    public EntityManagerLiteFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
        if ((puName).equals(info.getPersistenceUnitName()))
            return new EntityManagerFactoryImpl(connectionSourceFactory.getConnectionSource(), persistenceConfig);
        return null;
    }

}
