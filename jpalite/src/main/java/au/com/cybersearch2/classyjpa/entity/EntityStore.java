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
package au.com.cybersearch2.classyjpa.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

/**
 * Retains entity objects during persistence phases
 */
public class EntityStore {

    /** Map managed entity objects by key. Created only when first object is to be stored. */
    private Map<EntityKey, OrmEntity> managedObjects;

    /**
     * Returns flag set true if given key identifies a contained object
     * @param key Entity key
     * @return boolean
     */
	public boolean containsKey(EntityKey key) {
		 if (managedObjects != null)
			 return managedObjects.containsKey(key);
		 return false;
	}

	/**
	 * Removes object identified by given key and returns it
	 * @param key Entity key
	 * @return OrmEntity object
	 * @throws PersistenceException if object not found
	 */
	public OrmEntity remove(EntityKey key) {
		OrmEntity entity = null;
        if (managedObjects != null)
 		    entity = managedObjects.remove(key);
		if (entity == null)
			throw new PersistenceException(String.format("Entity object not found with profile %s", key.toString()));
		return entity;
	}

	public OrmEntity get(EntityKey key) {
		if (managedObjects != null)
			return managedObjects.get(key);
		return null;
	}

	public void put(EntityKey key, OrmEntity ormEntity) {
		if (managedObjects == null)
			managedObjects = new HashMap<>();
		managedObjects.put(key, ormEntity);
	}

    /**
     * Returns a list of objects which need to be updated
     * @return OrmEntity list
     */
    protected List<OrmEntity> getObjectsToUpdate()
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
     * Remove references to all managed objects
     */
    public void release() 
    {
        if (managedObjects != null) 
            managedObjects.clear();
    }
}
