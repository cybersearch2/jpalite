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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * ObjectMonitor
 * Delegated by ClassyEntityManager to manage entity objects.
 * Note: This class is not thread safe. It assumes that the owning EntityManager runs in a single thread 
 * @author Andrew Bowley
 * 06/05/2014
 */
public class ObjectMonitor
{
    /** Map managed entity objects by key */
    protected Map<EntityKey, OrmEntity> managedObjects;
    /** Map removed entity objects by key */
    protected Map<EntityKey, OrmEntity> removedObjects;

    /**
     * Create ObjectMonitor object
     */
    public ObjectMonitor()
    {
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
        /*
        if (primaryKey == null)
        {
            if ((persistOp == PersistOp.persist) || (persistOp == PersistOp.contains))
                return null; // New entity will not match to any existing entity and cannot be managed
            throw new IllegalArgumentException(persistOp.toString() + " entity has null primary key");
        } */
        EntityKey key = new EntityKey(entity.getClass(), primaryKey);
        // Check if this is a removed object. Throw an exception if attempting to merge or refresh a removed object.
        if ((removedObjects != null) && removedObjects.containsKey(key))
        {
            if ((persistOp == PersistOp.merge) || (persistOp == PersistOp.refresh))
                throw new IllegalArgumentException("Entity of class " + entity.getClass().getName() + ", primary key " + primaryKey + " is removed");
            else if (persistOp == PersistOp.persist) // Unexpected. Unlikely a primary key will be recycled.
                removedObjects.remove(key);
            else if (persistOp == PersistOp.contains) // Do removed objects qualify as "belongs to the current persistence context"?
                return (T)removedObjects.get(key);
        }
        // Map of mangaged objects is lazily created
        if (managedObjects == null)
            managedObjects = new HashMap<>();
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
        if (postCreateKey == null)
            return false;
        if (preCreateKey == null)
            applyPostCreateKey = true;
        else if (!preCreateKey.equals(postCreateKey))
        {
            // Remove pre-create key from monitored objects
            EntityKey key = new EntityKey(entity.getClass(), preCreateKey);
            managedObjects.remove(key);
            applyPostCreateKey = true;
        }
        if (applyPostCreateKey && (startManagingEntity(entity, postCreateKey, PersistOp.persist) != null))
            return false; // Unexpected. DAO created same key as existing managed entity
        return true; // True primary key from start or valid primary key created by DAO
    }

    /**
     * Mark a managed object, identified by class and primaryKey, for removal
     * @param clazz Class of entity
     * @param primaryKey Primary key of entity to remove
     * @throws IllegalArgumentException if no managed object is matched to specified primary key
     */
    public void markForRemoval(Class<? extends OrmEntity> clazz, int primaryKey) 
    {
        if (clazz == null)
            throw new IllegalArgumentException("Parameter \"clazz\" is null");
        //if (primaryKey == null)
        //    throw new IllegalArgumentException("remove failed due entity of class " + clazz.getName() + " does not have primary key");
        EntityKey key = new EntityKey(clazz, primaryKey);
        if ((managedObjects == null) || !managedObjects.containsKey(key))
            throw new IllegalArgumentException("remove failed because entity of class " + clazz.getName() + " with primary key " + primaryKey + " is detached");
        if (removedObjects == null)
            removedObjects = new HashMap<>();
        removedObjects.put(key, managedObjects.get(key));
        managedObjects.remove(key);
    }

    /**
     * Remove references to all managed objects
     */
    public void release() 
    {
        if (removedObjects != null)
            removedObjects.clear();
        if (managedObjects != null) 
            managedObjects.clear();
    }
 
    /**
     * Returns a list of objects which need to be updated
     * @return List&lt;Object&gt;
     */
    public List<OrmEntity> getObjectsToUpdate()
    {
        List<OrmEntity> result = new ArrayList<>();
        if (managedObjects != null)
            for (EntityKey key: managedObjects.keySet())
            {
                if (key.isDirty())
                {
                	OrmEntity entity = managedObjects.get(key);
                    key.setDirty(false);
                    result.add(entity);
                }
            }
        return result;
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
     * Utilty method to throw PersistenceException for reflection error
     *@param method Name of method to report
     *@param details Error details
     *@return PersistenceException
     */
    private static PersistenceException createReflectionErrorException(String method, String details)
    {
        throw new PersistenceException(method + " failed due to Java Reflection error: " + details);
    }

}
