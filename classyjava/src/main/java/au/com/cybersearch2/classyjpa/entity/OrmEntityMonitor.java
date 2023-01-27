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

import java.lang.reflect.InvocationTargetException;

import javax.persistence.PersistenceException;

import org.apache.commons.beanutils.PropertyUtils;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;

/**
 * ObjectMonitor
 * Delegated by Entity Manager to manage entity objects.
 * Note: This class is not thread safe. It assumes that the owning EntityManager runs in a single thread 
 * @author Andrew Bowley
 * 06/05/2014
 */
public class OrmEntityMonitor implements DaoHelperForClass
{
    /** PersistenceUnitAdmin Unit configuration */
    private final PersistenceConfig persistenceConfig;
    /** Open connection source */
    private final ConnectionSource connectionSource;
    
    /** Map managed entity objects by key */
    private final EntityStore managedObjects;
    /** Map removed entity objects by key */
    private final EntityStore removedObjects;

    /**
     * Create OrmEntityMonitor object
     * @param connectionSource Open connection source
     * @param persistenceConfig PersistenceUnitAdmin Unit configuration
     */
    public OrmEntityMonitor(ConnectionSource connectionSource, 
    		                PersistenceConfig persistenceConfig)
    {
    	this.connectionSource = connectionSource;
    	this.persistenceConfig = persistenceConfig;
    	managedObjects = new EntityStore();
    	removedObjects = new EntityStore();
   }

    /**
     * OrmEntityMonitor dependency injection constructor
     */
    protected OrmEntityMonitor(ConnectionSource connectionSource, 
    		                   PersistenceConfig persistenceConfig,
       		                   EntityStore managedObjects,
       		                   EntityStore removedObjects)
    {
    	this.connectionSource = connectionSource;
    	this.persistenceConfig = persistenceConfig;
    	this.managedObjects = managedObjects;
    	this.removedObjects = removedObjects;
   }

    /**
     * Start Managing entity
     * @param entity Object to be managed
     * @param primaryKey Entity primary key
     * @param persistOp persist, merge, refresh or contains
     * @param <T> Entity type
     * @return null if first time this method is called for this entity, otherwise, the existing managed object
     * @throws IllegalArgumentException if any parameter is invalid
     * @see #monitorNewEntity
     */
    @SuppressWarnings("unchecked") 
    public <T extends OrmEntity> T startManagingEntity(OrmEntity entity, int primaryKey, PersistOp persistOp)
    {
        if (entity == null)
            throw new IllegalArgumentException("Parameter \"entity\" is null");
        if (persistOp == null)
            throw new IllegalArgumentException("Parameter \"persistOp\" is null");
        EntityKey key = new EntityKey(entity.getClass(), primaryKey);
        // Check if this is a removed object. Throw an exception if attempting to merge or refresh a removed object.
        if (removedObjects.containsKey(key))
        {
            if ((persistOp == PersistOp.merge) || (persistOp == PersistOp.refresh))
                throw new PersistenceException("Entity of class " + entity.getClass().getName() + ", primary key " + primaryKey + " is removed");
            else if (persistOp == PersistOp.persist) // Unexpected. Unlikely a primary key will be recycled.
                removedObjects.remove(key);
            else if (persistOp == PersistOp.contains) // Do removed objects qualify as "belongs to the current persistence context"?
                return (T)removedObjects.get(key);
        }
        // Map of managed objects is lazily created
        else if (managedObjects.containsKey(key))
        {   // This is an existing managed object
            if ((persistOp == PersistOp.persist) || (persistOp == PersistOp.contains))
                return (T) managedObjects.get(key);
            // persistOp == PersistOp.merge) || (persistOp == PersistOp.refresh)
            if (persistOp == PersistOp.merge)
            { // Update previously managed object before detaching it
                T toDetach = (T) managedObjects.get(key);
                mergeObjects(toDetach, entity);
            }
            // Must set/clear dirty flag. Quickest way is with a swap
            managedObjects.remove(key);
            if (persistOp == PersistOp.merge)
                key.setDirty(true);
            managedObjects.put(key, entity);
            return (T)entity;
        }
        if (persistOp == PersistOp.merge) 
        {
            key.setDirty(true);
            // merge allows previously unmanaged objects to be managed
            managedObjects.put(key, entity);
        } 
        else if (persistOp == PersistOp.persist)
            // persist objects are managed
            managedObjects.put(key, entity);
        // Returning null indicates this is a previously unmanaged object
        return (T)null;
    }

    /**
     * Update monitor state after database operation to persist a new entity
     * @param entity  Object being managed
     * @param preCreateKey Specified primary key or null
     * @param postCreateKey Primary key returned by DAO after persisting entity 
     * @return true if preCreateKey and postCreateKey match or preCreateKey is null 
     *          and postCreateKey does not match existing managed entity
     */
    public boolean monitorNewEntity(OrmEntity entity, Integer preCreateKey, Integer postCreateKey)
    {
        boolean applyPostCreateKey = false;
        if (postCreateKey != null) {
	        EntityKey key = new EntityKey(entity.getClass(), postCreateKey);
	    	if (!managedObjects.containsKey(key)) {
	    		// DAO has not created same key as existing managed entity
		        if (preCreateKey == null) {
		            applyPostCreateKey = true;
		        } else if (!preCreateKey.equals(postCreateKey)) {
		            // Remove pre-create key from monitored objects
		            key = new EntityKey(entity.getClass(), preCreateKey);
		            if (managedObjects.containsKey(key))
		                managedObjects.remove(key);
		            applyPostCreateKey = true;
		        }
	    	}
	        if (applyPostCreateKey) 
	        	startManagingEntity(entity, postCreateKey, PersistOp.persist);
        }
        return applyPostCreateKey; // True primary key from start or valid primary key created by DAO
    }

    /**
     * Mark a managed object, identified by class and primaryKey, for removal
     * @param clazz Class of entity
     * @param primaryKey Primary key of entity to remove
     * @throws PersistenceException if no managed object is matched to specified primary key
     */
    public void markForRemoval(Class<? extends OrmEntity> clazz, int primaryKey) 
    {
        if (clazz == null)
            throw new IllegalArgumentException("Parameter \"clazz\" is null");
        EntityKey key = new EntityKey(clazz, primaryKey);
        if (!managedObjects.containsKey(key))
            throw new PersistenceException("remove failed because entity of class " + clazz.getName() + " with primary key " + primaryKey + " is detached");
        removedObjects.put(key, managedObjects.remove(key));
    }

    /**
     * Perform outstanding updates on all managed objects
     */
    public void updateAllManagedObjects()
    {
        for (OrmEntity entity: managedObjects.getObjectsToUpdate())
        {
            OrmDaoHelper<? extends OrmEntity> ormDaoHelper = getOrmDaoHelperForClass(entity.getClass());
            if (ormDaoHelper.update(entity) == 0)
                throw new PersistenceException("update operation returned result count 0");
        }
    }
    
    /**
     * Remove references to all managed objects
     */
    public void release() 
    {
        removedObjects.release();
        managedObjects.release();
    }
 
    /**
     * Returns ORMLite DAO helper for specified class 
     * @param clazz Entity class
     * @return OrmDaoHelper
     * @throws IllegalStateException if class is unknown to the current PersistenceUnitAdmin Unit.
     */
    public <T extends OrmEntity> OrmDaoHelper<T> getOrmDaoHelperForClass(Class<T> clazz)
    {
        return getOrmDaoHelperFactoryForClass(clazz).getOrmDaoHelper(connectionSource);
    }

    /**
     * Returns ORMLite DAO helper for specified class 
     * @param clazz Entity class
     * @return OrmDaoHelper
     * @throws PersistenceException if class is unknown to the current PersistenceUnitAdmin Unit.
     */
    private <T extends OrmEntity> OrmDaoHelperFactory<T> getOrmDaoHelperFactoryForClass(Class<T> clazz)
    {
        OrmDaoHelperFactory<T> ormDaoHelperFactory = persistenceConfig.getHelperFactory(clazz);
        if (ormDaoHelperFactory == null)
            throw new PersistenceException("Class " + clazz.getName() + " not an entity in this persistence context");
        return ormDaoHelperFactory;
    }

    /**
     * Merge entity objects. Performs copy using reflection.
     * @param dest Entity to be updated
     * @param orig Source entity
     */
    private static <T extends OrmEntity> void mergeObjects(T dest, T orig)
    {
        try
        {
            PropertyUtils.copyProperties(dest, orig);
        }
        catch (IllegalAccessException e)
        {
            throw createReflectionErrorException("refresh", e.toString());
        }
        catch (InvocationTargetException e)
        {
            throw createReflectionErrorException("refresh", e.getCause() == null ? e.toString() : e.getCause().toString());
        }
        catch (NoSuchMethodException e)
        {
            throw createReflectionErrorException("refresh", e.toString());
        }
    }

    /**
     * Utility method to throw PersistenceException for reflection error
     *@param method Name of method to report
     *@param details Error details
     *@return PersistenceException
     */
    private static PersistenceException createReflectionErrorException(String method, String details)
    {
        throw new PersistenceException(method + " failed due to Java Reflection error: " + details);
    }

}
