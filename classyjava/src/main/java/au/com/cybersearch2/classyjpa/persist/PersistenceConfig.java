/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
// Derived from OrmLite com.j256.ormlite.misc.JavaxPersistence and 
// com.j256.ormlite.android.apptools.OrmLiteConfigUtil
// Original copyright license:
/*
Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
granted, provided that this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
USE OR PERFORMANCE OF THIS SOFTWARE.

The author may be contacted via http://ormlite.com/ 
*/
package au.com.cybersearch2.classyjpa.persist;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;
import javax.persistence.spi.PersistenceUnitInfo;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperFactory;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyser.ClassRegistry;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * PersistenceConfig
 * Configuration information for one PersistenceUnitAdmin Unit
 * @author Andrew Bowley
 * 05/05/2014
 */
public class PersistenceConfig
{
    public static final String TAG = "PersistenceConfig";
    protected static Log log = JavaLogger.getLogger(TAG);

    private static final String NAME_EXISTS_MESSAGE  = "Query name already exists: ";
    
    /** Maps ORM query to name of query */ 
    Map<String,NamedDaoQuery<? extends OrmEntity>> namedQueryMap;
    /** Maps native query to name of query */
    Map<String,NamedSqlQuery> nativeQueryMap;
    /** Maps ORM DAO helper factory object to entity class name */
    private final Map<String,OrmDaoHelperFactory<? extends OrmEntity>> helperFactoryMap;
    /** PU info from persistence.xml */
    private PersistenceUnitInfo puInfo;
    /** Database type */
    private DatabaseType databaseType;
    /** Class loader to instantiate entity classes (optional) */
    private EntityClassLoader entityClassLoader;

    /**
     * Construct a PersistenceConfig instance
     * @param databaseType Database type
     */
    public PersistenceConfig(DatabaseType databaseType)
    {
        this.databaseType = databaseType;
        namedQueryMap = new HashMap<>();
        nativeQueryMap = new HashMap<>();
        helperFactoryMap = new HashMap<>();
    }

	/**
     * Create a named query and store it in namedQueryMap. The query is implemented using OrmLite rather than JPA query language.
     * @param entityClass entityClass Entity class. This must be included in persistence.xml entity class list.
     * @param name Query name
     * @param daoQueryFactory Query generator which uses supplied DAO for entity class
     */
    public <T extends OrmEntity> void addNamedQuery(Class<T> entityClass, String name, DaoQueryFactory<T> daoQueryFactory) 
    {
        if (existsName(name))
            log.warn(TAG, NAME_EXISTS_MESSAGE + name);
        else
            namedQueryMap.put(name, new NamedDaoQuery<T>(entityClass, name, daoQueryFactory));
    }

    /**
     * Add native named query to persistence unit context
     * @param name Query name
     * @param queryInfo Native query information
     * @param queryGenerator Native query generator
     */
    public void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator)
    {
        if (existsName(name))
            log.warn(TAG, NAME_EXISTS_MESSAGE + name);
        else
            nativeQueryMap.put(name, new NamedSqlQuery(name, queryInfo, queryGenerator));
    }

    /**
     * Returns DAO for given entity class
     * @param entityClass Entity class
     * @param connectionSource Open connection source
     * @return PersistenceDao object
     */
	public <T extends OrmEntity> PersistenceDao<T> getDao(Class<T> entityClass, ConnectionSource connectionSource) {
		return getOrmDaoHelperFactoryForClass(entityClass).getDao(connectionSource);
	}

    /**
     * Returns NamedDaoQuery objects mapped by name
     * @return Map&lt;String, NamedDaoQuery&gt;
     */
    public NamedDaoQuery<? extends OrmEntity> getNamedQuery(String name) 
    {
        return namedQueryMap.get(name);
    }

    /**
     * Returns NamedSqlQuery objects mapped by name
     * @return Map&lt;String, NamedSqlQuery&gt;
     */
    public NamedSqlQuery getNativeQuery(String name) 
    {
        return nativeQueryMap.get(name);
    }

    /**
     * Returns OrmDaoHelperFactory objects mapped by entity class name
     * @return Map&lt;String, OrmDaoHelperFactory&gt;
     */
    public Map<String, OrmDaoHelperFactory<? extends OrmEntity>> getHelperFactoryMap() 
    {
        return Collections.unmodifiableMap(helperFactoryMap);
    }

    /**
     * Returns OrmDaoHelperFactory objects mapped by entity class
     * @return Map&lt;String, OrmDaoHelperFactory&gt;
     */
    @SuppressWarnings("unchecked")
	public <T extends OrmEntity> OrmDaoHelperFactory<T> getHelperFactory(Class<T> clazz) 
    {
        return (OrmDaoHelperFactory<T>) helperFactoryMap.get(clazz.getName());
    }

    /**
     * Returns PersistenceUnitInfo object unmarshalled from persistence.xml
     * @return PersistenceUnitInfo
     */
    public PersistenceUnitInfo getPuInfo() 
    {
        return puInfo;
    }

    /**
     * Set PersistenceUnitInfo object unmarshalled from persistence.xml and prepare entity class DAOs
     * @param puInfo PersistenceUnitInfo object
     */
    public void setPuInfo(PersistenceUnitInfo puInfo) 
    {
        this.puInfo = puInfo;
        List<String> managedClassNames = puInfo.getManagedClassNames();
        if (!managedClassNames.isEmpty())
        	registerClasses(managedClassNames);
    }

    public void setEntityClassLoader(EntityClassLoader entityClassLoader) 
    {
    	this.entityClassLoader = entityClassLoader;
    }
    
    public void checkEntityTablesExist(ConnectionSource connectionSource)
    {
    	for (Map.Entry<String,OrmDaoHelperFactory<? extends OrmEntity>> entry: helperFactoryMap.entrySet())
    	{
    		if (!entry.getValue().checkTableExists(connectionSource))
                log.warn(TAG, "Created Entity table for class: " + entry.getKey());
    	}
    }

	@SuppressWarnings("unchecked")
	public <X> TypedQuery<X> createNamedQuery(String name, Class<X> resultClass, ConnectionSource connectionSource) {
        NamedDaoQuery<? extends OrmEntity> namedDaoQuery = getNamedQuery(name);
        if (namedDaoQuery != null) {
        	Class<? extends OrmEntity> entityClass = namedDaoQuery.getEntityClass();
        	if (!resultClass.isAssignableFrom(entityClass))
                throw new IllegalArgumentException(
                	String.format("Named query \"%s\" result class %s is not assignable from entity class %s", 
                			      name, resultClass.getSimpleName(), entityClass.getSimpleName()));
            return (TypedQuery<X>) namedDaoQuery.createQuery(connectionSource);
        } else {
            NamedSqlQuery namedSqlQuery = getNativeQuery(name);
            if (namedSqlQuery == null)
                throw new IllegalArgumentException("Named query '" + name + "' not found");
            return (TypedQuery<X>) namedSqlQuery.createQuery(resultClass);
        }
	}

    /**
     * Returns true in query of specified name exists
     * @param name Query name
     * @return boolean
     */
    protected boolean existsName(String name)
    {
        return namedQueryMap.containsKey(name) || nativeQueryMap.containsKey(name);
    }
    
    protected void registerClasses(List<String> managedClassNames)
    {
        ClassRegistry classRegistry = new ClassRegistry(){

            @Override
            public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) 
            {
                String key = entityClass.getName();
                helperFactoryMap.put(key, new OrmDaoHelperFactory<T>(entityClass));
           }};
        ClassAnalyser classAnlyser = new ClassAnalyser(databaseType, classRegistry, entityClassLoader);
        List<DatabaseTableConfig<?>> configs = classAnlyser.getDatabaseTableConfigList(managedClassNames);
        if (!configs.isEmpty())
            DaoManager.addCachedDatabaseConfigs(configs);
    }

    /**
     * Returns ORMLite DAO helper for specified class 
     * @param entityClass Entity class
     * @return OrmDaoHelper
     * @throws IllegalStateException if class is unknown to the current PersistenceUnitAdmin Unit.
     */
    private <T extends OrmEntity> OrmDaoHelperFactory<T> getOrmDaoHelperFactoryForClass(Class<T> entityClass)
    {
    	OrmDaoHelperFactory<T> ormDaoHelperFactory = getHelperFactory(entityClass);
        if (ormDaoHelperFactory == null)
            throw new IllegalArgumentException("Class " + entityClass.getName() + " not an entity in this persistence context");
        return ormDaoHelperFactory;
    }

}
