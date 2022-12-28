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

import java.util.List;
import java.util.Properties;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;

/**
 * PersistenceAdmin
 * Persistence unit interface
 * @author Andrew Bowley
 * 05/07/2014
 */
public interface PersistenceAdmin extends ConnectionSourceFactory
{
    /**
     * Add named query to persistence unit context
     * @param entityClass Entity class
     * @param name Query name
     * @param daoQueryFactory Query generator
     */
     <T extends OrmEntity> void addNamedQuery(Class<T> entityClass, String name, DaoQueryFactory<T> daoQueryFactory);
    
    /**
     * Add native named query to persistence unit context
     * @param name Query name
     * @param queryInfo Native query information
     * @param queryGenerator Native query generator
     */
    void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator);
    
    /**
     * Returns EntityManager Factory for this persistence unit
     * @return EntityManagerLiteFactory
     */
    EntityManagerLiteFactory getEntityManagerFactory();
    
    /**
     * Returns list of objects from executing a native query
     * @param queryInfo Native query details
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return List&lt;Object&gt;
     */
    List<Object> getResultList(QueryInfo queryInfo, int startPosition, int maxResults);
    
    /**
     * Returns object from executing a native query
     * @param queryInfo Native query details
     * @return Object or null if nothing returned by query
     */
    Object getSingleResult(QueryInfo queryInfo);

    String getPuName();
    
    /**
     * Returns database name
     * @return String
     */
    String getDatabaseName();
    
    /**
     * Close all database connections
     */
    void close();
    
    /**
     * Returns database type
     * @return com.j256.ormlite.db.DatabaseType
     */
    DatabaseType getDatabaseType();

    /**
     * Returns database version
     * @return int
     */
    int getDatabaseVersion();
    
    /**
     * Returns PU properties
     * @return java.util.Properties
     */
    Properties getProperties();
 
    /**
     * Register specified classes
     * @param managedClassNames Class name list
     */
    void registerClasses(List<String> managedClassNames);

    /**
     * Create a EntityManager bound to an existing connectionSource. Use only for special case of database creation or update.
     * @param connectionSource The existing ConnectionSource object 
     * @return Entity manager instance
     */
    EntityManagerLite createEntityManager(ConnectionSource connectionSource);
    
    /** 
     * Returns flag set true if connection source is for a single connection 
     * @return boolean
     */
    boolean isSingleConnection();

    /**
     * Returns DAO for given entity class
     * @param entityClass Entity class
     * @param connectionSource Open connection source
     * @return PersistenceDao object
     */
    //PersistenceDao<?> getDao(Class<? extends OrmEntity> entityClass, ConnectionSource connectionSource);
    <T extends OrmEntity> PersistenceDao<T> getDao(Class<T> entityClass, ConnectionSource connectionSource);

    /**
     * Returns DAO for given entity class
     * @param entityClass Entity class
     * @return PersistenceDao object
     */
    //PersistenceDao<?> getDao(Class<? extends OrmEntity> entityClass, ConnectionSource connectionSource);
    <T extends OrmEntity> PersistenceDao<T> getDao(Class<T> entityClass);

}
