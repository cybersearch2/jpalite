/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.container;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.persist.EntityManagerFactoryImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;

/**
 * JPA persistence unit which provides a context for executing persistence work
 */
public class PersistenceUnit {

	private final String persistenceUnitName;
	private final DatabaseAdmin databaseAdmin;
	private final PersistenceAdmin persistenceAdmin;
	private final PersistenceConfig persistenceConfig;
	private final EntityManagerLiteFactory entityManagerFactory;
	
	/**
	 * Construct PersistenceUnit object
	 * @param persistenceUnitName Name of unit
	 * @param databaseAdmin Database administration object 
	 * @param persistenceAdmin JPA administration object
	 * @param persistenceConfig Persistence configuration
	 */
	public PersistenceUnit(String persistenceUnitName,
                           DatabaseAdmin databaseAdmin, 
                           PersistenceAdmin persistenceAdmin,
                           PersistenceConfig persistenceConfig) {
		this.persistenceUnitName = persistenceUnitName;
		this.databaseAdmin = databaseAdmin;
		this.persistenceAdmin = persistenceAdmin;
		this.persistenceConfig = persistenceConfig;
		entityManagerFactory = new EntityManagerFactoryImpl(persistenceAdmin);
	}

    /**
     * Returns persistence unit name
     * @return String
     */
    String getPersistenceUnitName() {
    	return persistenceUnitName;
    }
    
    /**
     * Returns Database-specific admin object
     * @return DatabaseAdmin
     */
    DatabaseAdmin getDatabaseAdmin() {
    	return databaseAdmin;
    }
    
    /**
     * Returns JPA administration object
     * @return PersistenceAdmin
     */
    PersistenceAdmin getPersistenceAdmin() {
    	return persistenceAdmin;
    }

	/**
	 * Create a EntityManager bound to an existing connectionSource. Use only for
	 * special case of database creation or update.
	 * 
	 * @param connectionSource The existing ConnectionSource object
	 * @return Entity manager instance
	 */
	public EntityManagerLite createEntityManager() {
         return entityManagerFactory.createEntityManager();
	}
	
	/**
	 * Create a EntityManager bound to an existing connectionSource. Use only for
	 * special case of database creation or update.
	 * 
	 * @param connectionSource The existing ConnectionSource object
	 * @return Entity manager instance
	 */
	public EntityManagerLite createEntityManager(ConnectionSource connectionSource) {
        return entityManagerFactory.createEntityManager(connectionSource);
	}
	
	protected PersistenceConfig getPersistenceConfig() {
		return persistenceConfig;
	}
}
