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
package au.com.cybersearch2.classyjpa.entity;

import java.util.Map;

import javax.persistence.EntityTransaction;

import com.j256.ormlite.support.ConnectionSource;

/**
 * EntityManagerDelegate
 * Object returned by ClassyFyEntityManager getDelegate() method to give caller access to OrmLite DAOs
 * @author Andrew Bowley
 * 03/05/2014
 */
public class EntityManagerDelegate
{
    /** Connection Source to use for all database connections */
    protected final ConnectionSource connectionSource;
    /** Enclosing transaction object */ 
    protected final EntityTransaction entityTransaction;
    /** Maps entity class name to ORMLite DAO helper */
    protected final Map<String,OrmDaoHelperFactory<? extends OrmEntity>> helperFactoryMap;
    
    /**
     * Constructor.
     * @param connectionSource Connection Source to use for all database connection
     * @param entityTransaction Enclosing transaction object
     * @param helperFactoryMap Maps entity class name to ORMLite DAO helper
     */
    public EntityManagerDelegate(ConnectionSource connectionSource, EntityTransaction entityTransaction, Map<String,OrmDaoHelperFactory<? extends OrmEntity>> helperFactoryMap)
    {
        this.connectionSource = connectionSource;
        this.entityTransaction = entityTransaction;
        this.helperFactoryMap = helperFactoryMap;
    }

    /**
     * Returns ORMLite DAO for specified entity class
     * @param clazz  Entity class
     * @return PersistenceDao
     * @throws UnsupportedOperationException if class is unknown for this persistence unit
     */
    public PersistenceDao<?> getDaoForClass(Class<?> clazz)
    {
        OrmDaoHelperFactory<? extends OrmEntity> ormDaoHelperFactory = (OrmDaoHelperFactory<? extends OrmEntity>) helperFactoryMap.get(clazz.getName());
        if (ormDaoHelperFactory == null)
            throw new UnsupportedOperationException("DAO for entity class " + clazz.getName() + " not supported because ormDaoHelper is not set");
        return ormDaoHelperFactory.getDao(connectionSource);
    }

    /**
     * Returns enclosing transaction. If in user transaction mode, this will be the actual transaction object, 
     * otherwise, it will be a proxy which only supports setRollbackOnly()
     * @return EntityTransaction
     */
    public EntityTransaction getTransaction() 
    {
        return entityTransaction;
    }

}
