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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.transaction.SetRollbackTransaction;
import au.com.cybersearch2.classyjpa.transaction.UserTransactionSupport;

/**
 * EntityManagerImpl
 * Implementation of EntityManager interface
 * Transaction scoped. Entity objects are managed only while a transaction is active.
 * Except for when the EntityManger has been closed, a new transaction will begin whenever an object needs to be managed and the transaction is not active. 
 * @author Andrew Bowley
 * 01/05/2014
 */
public class EntityManagerImpl implements EntityManagerLite, UserTransactionSupport
{
    /** PersistenceUnitAdmin Unit configuration */
    private final PersistenceConfig persistenceConfig;
    /** Retrieving DaoHelper for an entity class is delegated to OrmEmtityMonitor  */
    private final DaoHelperForClass daoHelperForClass;
    
    /** Enclosing transaction object */ 
    protected MonitoredTransaction transaction;
   /** Flag for user transaction mode. If true, getTransaction() returns rollbackonly transaction 
    * otherwise it returns the actual transaction */
    private boolean isUserTransaction; 
    /** Flag set when close() is called */
    private volatile boolean isOpen;

    /**
     * Create EntityManagerImpl object
     * @param transaction Manages transactions and performs all pending entity updates before commit
     * @param persistenceConfig PersistenceUnitAdmin Unit configuration
     */
    public EntityManagerImpl(
    		MonitoredTransaction transaction, 
            PersistenceConfig persistenceConfig)
    {
        this.transaction = transaction;
        this.persistenceConfig = persistenceConfig;
        daoHelperForClass = transaction.getEntityMonitor();
        isOpen = true;
    }

    /**
     * setUserTransaction
     * Determines if getTransaction() returns null (false) or transaction (true)
     * @param value boolean
     */
    @Override
    public void setUserTransaction(boolean value)
    {
        isUserTransaction = value;
    }
    
   /**
     * Make an entity instance managed and persistent.
     * @param entity The entity instance
     * @throws IllegalArgumentException if not an entity
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public void persist(OrmEntity entity) 
    {
        if (entity == null)
            throw new IllegalArgumentException("Parameter \"entity\" is null");
        checkEntityManagerClosed("persist()");
        OrmDaoHelper<?> ormDaoHelper = 
        		daoHelperForClass.getOrmDaoHelperForClass(entity.getClass());
        int primaryKey = ormDaoHelper.extractId(entity);
        OrmEntityMonitor entityMonitor = transaction.getEntityMonitor();
        OrmEntity alreadyManaged = entityMonitor.startManagingEntity(entity, primaryKey, PersistOp.persist);
        if ((alreadyManaged != null) || 
            ((primaryKey > 0) && ormDaoHelper.entityExists(entity)))
            throw new EntityExistsException("Entity of class " + entity.getClass() + ", primary key " + primaryKey + " already exists");
        if (!transaction.isActive())
            transaction.begin(); // Transaction commit/rollback triggers refresh
        if (ormDaoHelper.create(entity) == 0)
            throw new PersistenceException("persist operation returned result count 0");
        ormDaoHelper.setForeignCollections(entity);
        // DAO may update primary key value on entity during create operation
        if (!entityMonitor.monitorNewEntity(entity, primaryKey, ormDaoHelper.extractId(entity)))
        {
            // No Primary key or matches one belonging to existing managed entity
            transaction.rollback();
            throw new PersistenceException("Error persisting entity class " + 
                                            entity.getClass().getName() + 
                                            ": No Primary key or matches one belonging to managed entity");
        }
    }
    
    /**
     * Merge the state of the given entity into the
     * current persistence context.
     * @param entity The entity instance
     * @return The instance that the state was merged to
     * @throws IllegalArgumentException if instance is not an entity or is a removed entity
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public <T extends OrmEntity> T merge(T entity) 
    {
        checkEntityManagerClosed("merge()");
        OrmDaoHelper<?> ormDaoHelper = 
        	daoHelperForClass.getOrmDaoHelperForClass(entity.getClass());
        int primaryKey = ormDaoHelper.extractId(entity);
        T managed = transaction.getEntityMonitor().startManagingEntity(entity, primaryKey, PersistOp.merge);
        if (!transaction.isActive())
            transaction.begin(); // Transaction commit triggers update and refresh
        return (T) managed;
    }

    /** 
     * Refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any.
     * @param entity The entity instance
     * @throws IllegalArgumentException if not an entity or entity is not managed
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public void refresh(OrmEntity entity) 
    {
        checkEntityManagerClosed("refresh()");
        OrmDaoHelper<? extends OrmEntity> ormDaoHelper = 
        	daoHelperForClass.getOrmDaoHelperForClass(entity.getClass());
        int primaryKey = ormDaoHelper.extractId(entity);
        // For refresh, the returned object is the entity, not the former managed object
        Object managed = transaction.getEntityMonitor().startManagingEntity(entity, primaryKey, PersistOp.refresh);
        if (managed == null)
            throw new IllegalArgumentException("Entity of class " + entity.getClass() + ", primary key " + primaryKey + " is not managed");
        if (!transaction.isActive())
            transaction.begin(); // Transaction commit/rollback triggers refresh
        if (ormDaoHelper.refresh(managed) == 0)
            throw new PersistenceException("refresh operation returned result count 0");
    }


    /**
     * Remove the entity instance.
     * @param entity The entity instance
     * @throws IllegalArgumentException if not an entity or if a detached entity
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public void remove(OrmEntity entity) 
    {
        checkEntityManagerClosed("remove()");
        OrmDaoHelper<?> ormDaoHelper = 
        	daoHelperForClass.getOrmDaoHelperForClass(entity.getClass());
        int primaryKey = ormDaoHelper.extractId(entity);
        transaction.getEntityMonitor().markForRemoval(entity.getClass(), primaryKey);
        if (!transaction.isActive())
            transaction.begin();
        if (ormDaoHelper.delete(entity) == 0)
            throw new PersistenceException("remove operation returned result count 0");
    }

    /**
     * Find by primary key.
     * Does not require transaction.
     * @param entityClass The class of the entity
     * @param primaryKey The primary key as Object
     * @return the found entity instance or null if the entity does not exist
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if the first argument does
     *    not denote an entity type or the second
     *    argument is not a valid type for that
     *    entity's primary key
     */
    @Override
    public <T extends OrmEntity> T find(Class<T> entityClass, int primaryKey) 
    {
        checkEntityManagerClosed("find()");
        OrmDaoHelper<T> ormDaoHelper = 
        	daoHelperForClass.getOrmDaoHelperForClass(entityClass);
        return ormDaoHelper.queryForId((Integer)primaryKey);
    }

    /**
     * Javax PersistenceUnitAdmin: "Get an instance, whose state may be lazily fetched".
     * This implementation is just an alias for find()
     * If the requested instance does not exist in the database,
     * throws {@link EntityNotFoundException} when the instance state is
     * first accessed.
     *
     * The application should not expect that the instance state will
     * be available upon detachment, unless it was accessed by the
     * application while the entity manager was open.
     * @param entityClass The class of the entity
     * @param primaryKey The primary key as Object
     * @return the found entity instance
     * @throws IllegalArgumentException if the first argument does
     *    not denote an entity type or the second
     *    argument is not a valid type for that
     *    entity's primary key
     * @throws EntityNotFoundException if the entity state
     *    cannot be accessed
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public <T extends OrmEntity> T getReference(Class<T> entityClass, int primaryKey) 
    {
        checkEntityManagerClosed("getReference()");
        T entity = find(entityClass, primaryKey);
        if (entity == null)
            throw new EntityNotFoundException("Not found: class " + entityClass.getName() + ", primary key " + primaryKey);
        return entity;
    }

    /**
     * Synchronize the persistence context to the underlying database.
     * @throws PersistenceException if the flush fails
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public void flush() 
    {
        checkEntityManagerClosed("flush()");
        if (transaction.isActive())
            transaction.commit();
        transaction.begin();
    }

    /**
    * Set the flush mode that applies to all objects contained
    * in the persistence context.
    * @param flushMode  The flush mode
    * @throws IllegalStateException if this EntityManager has been closed.
    */
    @Override
    public void setFlushMode(FlushModeType flushMode) 
    {
        checkEntityManagerClosed("setFlushMode()");
        if (FlushModeType.AUTO.equals(flushMode))
            throw new UnsupportedOperationException("FlushModeType.AUTO not supported");
    }

    /**
    * Get the flush mode that applies to all objects contained
    * in the persistence context.
    * @return flush mode - always FlushModeType.COMMIT
     * @throws IllegalStateException if this EntityManager has been closed.
    */
    @Override
    public FlushModeType getFlushMode() 
    {
        checkEntityManagerClosed("getFlushMode()");
        return FlushModeType.COMMIT;
    }

    /**
    * NOT SUPPORTED. 
    * Set the lock mode for an entity object contained in the persistence context.
    * @param entity Entity
    * @param lockMode Lock mode
    * @throws UnsupportedOperationException unconditionally
    */
    @Override
    public void lock(Object entity, LockModeType lockMode) 
    {
       throw new UnsupportedOperationException("lock() not available");
    }

    /**
    * Clear the persistence context, causing all managed
    * entities to become detached. Changes made to entities that
    * have not been flushed to the database will not be persisted.
    * @throws IllegalStateException if this EntityManager has been closed.
    */
    @Override
    public void clear() 
    {
        checkEntityManagerClosed("clear()");
        if (transaction.isActive())
            transaction.rollback();
        transaction.getEntityMonitor().release();
        transaction.begin();
    }

    /**
     * Check if the instance belongs to the current persistence
     * context.
     * @param entity The entity instance
     * @return <code>true</code> if the instance belongs to 
     * the current persistence context.
     * @throws IllegalArgumentException if not an entity
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public boolean contains(OrmEntity entity) 
    {
        checkEntityManagerClosed("contains()");
        OrmDaoHelper<?> ormDaoHelper = daoHelperForClass.getOrmDaoHelperForClass(entity.getClass());
        int primaryKey = ormDaoHelper.extractId(entity);
        Object alreadyManaged = transaction.getEntityMonitor().startManagingEntity(entity, primaryKey, PersistOp.contains);
        return (alreadyManaged != null) || 
                ((primaryKey > 0l) && ormDaoHelper.entityExists(entity));
    }


    /**
     * Create an instance of Query for executing a
     * named query (executed using OrmLite QueryBuilder or in native SQL).
     * @param name The name of a query 
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     * defined with the given name
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    @Override
    public TypedQuery<?> createNamedQuery(String name) 
    {
    	return createNamedQuery(name, Object.class);
    }

	/**
	 * Create an instance of TypedQuery for executing a named query using a Custom Statement Builder or in native SQL).
	 * 
	 * @param name The name of a query defined in metadata
	 * @param resultClass The type of the query result 
	 * @return the new query instance
	 * @throws IllegalArgumentException
	 *             if a query has not been defined with the given name or if the query string is found to be invalid
	 */
	public <X> TypedQuery<X> createNamedQuery(String name, Class<X> resultClass) {
        checkEntityManagerClosed("createNamedQuery()");
        return persistenceConfig.createNamedQuery(name, resultClass, transaction.getConnectionSource());
	}

    /**
     * NOT SUPPORTED
     * Indicate to the EntityManager that a JTA transaction is
     * active. This method should be called on a JTA application
     * managed EntityManager that was created outside the scope
     * of the active transaction to associate it with the current
     * JTA transaction.
     * @throws UnsupportedOperationException unconditionally
     */
    @Override
    public void joinTransaction() 
    {
        throw new UnsupportedOperationException("joinTransaction() not available");
    }

    /**
    * Returns EntityManagerDelegate object which provides access to OrmLite DAOs.
    * Return type of Object complies with PersistenceUnitAdmin API. Use cast to access the returned object as EntityManagerDelegate type.
    * @return Object 
    * @throws IllegalStateException if this EntityManager has been closed.
    */
    @Override
    public Object getDelegate() 
    {
        checkEntityManagerClosed("getDelegate()");
        return new EntityManagerDelegate(transaction.getConnectionSource(), transaction, persistenceConfig.getHelperFactoryMap());
    }

    /**
	 * DO NOT CALL close() as the container will do this automatically.
     * Close an application-managed EntityManager.
     * After the close method has been invoked, all methods
     * on the EntityManager instance and any Query objects obtained
     * from it will throw the IllegalStateException except
     * for getTransaction and isOpen (which will return false).
     * If this method is called when the EntityManager is
     * associated with an active transaction, the persistence
     * context remains managed until the transaction completes.
     * @throws IllegalStateException if the EntityManager
     * is container-managed or has been already closed.
     */
    @Override
    public void close() 
    {   
        checkEntityManagerClosed("close()");
        isOpen = false;
        if (transaction.isActive())
             transaction.commit();
        transaction.getEntityMonitor().release();
        // Do not close connection. This is managed by the EntityManagerFactory
    }

	/**
	 * Determine whether the entity manager is open.
	 * 
	 * @return true until the entity manager has been closed
	 */
    @Override
    public boolean isOpen() 
    {
        return isOpen;
    }

    /**
     * Returns the resource-level transaction object, if User Transactions selected,  otherwise proxy object returned for which only setRollbackOnly() is active.
     * In User Transaction mode, the EntityTransaction instance may be used serially to begin and commit multiple transactions.
     * @return EntityTransaction instance if in User Transaction mode, otherwise null
     */
    @Override
    public EntityTransaction getTransaction() 
    {
        if (!isUserTransaction)
            return new SetRollbackTransaction(transaction);
        return transaction;
    }

	protected void setOpen(boolean value) {
		isOpen = value;
	}
    
    /**
     * Confirm this Entity Manager is open
     * @param method Name of method being invoked
     * @throws IllegalStateException if this Entity Manager is closed.
     */
    private void checkEntityManagerClosed(String method)
    {
        if (!isOpen)
            throw new IllegalStateException(method + " called after EntityManager has been closed");
    }

}
