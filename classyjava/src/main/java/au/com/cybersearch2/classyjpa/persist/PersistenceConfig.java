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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperFactory;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyser.ClassRegistry;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

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
    
 
    Map<String,NamedDaoQuery> namedQueryMap;

    Map<String,NamedSqlQuery> nativeQueryMap;

    private final Map<String,OrmDaoHelperFactory<?,?>> helperFactoryMap;

    private PersistenceUnitInfo puInfo;

    private DatabaseType databaseType;

    private EntityClassLoader entityClassLoader;

    /**
     * Construct a PersistenceConfig instance
     * @param databaseType Database type
     */
    public PersistenceConfig(DatabaseType databaseType)
    {
        this.databaseType = databaseType;
        namedQueryMap = new HashMap<String,NamedDaoQuery>();
        nativeQueryMap = new HashMap<String,NamedSqlQuery>();
        helperFactoryMap = new HashMap<String,OrmDaoHelperFactory<?,?>>();
    }

	/**
     * Create a named query and store it in namedQueryMap. The query is implemented using OrmLite rather than JPA query language.
     * @param clazz Class&lt;?&gt; class of entity to which the query applies. This must be included in persistence.xml PersistenceUnitAdmin Unit class list.
     * @param name Query name
     * @param daoQueryFactory Query generator which uses supplied DAO for entity class
     */
    public void addNamedQuery(Class<?> clazz, String name, DaoQueryFactory daoQueryFactory)
    {
        if (existsName(name))
            log.warn(TAG, NAME_EXISTS_MESSAGE + name);
        else
            namedQueryMap.put(name, new NamedDaoQuery(clazz, name, daoQueryFactory));
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
     * Returns true in query of specified name exists
     * @param name Query name
     * @return boolean
     */
    protected boolean existsName(String name)
    {
        return namedQueryMap.containsKey(name) || nativeQueryMap.containsKey(name);
    }
    
    /**
     * Returns NamedDaoQuery objects mapped by name
     * @return Map&lt;String, NamedDaoQuery&gt;
     */
    public Map<String, NamedDaoQuery> getNamedQueryMap() 
    {
        return Collections.unmodifiableMap(namedQueryMap);
    }

    /**
     * Returns NamedSqlQuery objects mapped by name
     * @return Map&lt;String, NamedSqlQuery&gt;
     */
    public Map<String, NamedSqlQuery> getNativeQueryMap() 
    {
        return Collections.unmodifiableMap(nativeQueryMap);
    }

    /**
     * Returns OrmDaoHelperFactory objects mapped by entity class name
     * @return Map&lt;String, OrmDaoHelperFactory&gt;
     */
    public Map<String, OrmDaoHelperFactory<?, ?>> getHelperFactoryMap() 
    {
        return Collections.unmodifiableMap(helperFactoryMap);
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
    	for (Map.Entry<String,OrmDaoHelperFactory<?,?>> entry: helperFactoryMap.entrySet())
    	{
    		if (!entry.getValue().checkTableExists(connectionSource))
                log.warn(TAG, "Created Entity table for class: " + entry.getKey());
    	}
    }

    protected void registerClasses(List<String> managedClassNames)
    {
        ClassRegistry classRegistry = new ClassRegistry(){

            @Override
            public <T, ID> void registerEntityClass(Class<T> entityClass,
                    Class<ID> primaryKeyClass) 
            {
                String key = entityClass.getName();
                helperFactoryMap.put(key, new OrmDaoHelperFactory<T,ID>(entityClass));
           }};
        ClassAnalyser classAnlyser = new ClassAnalyser(databaseType, classRegistry, entityClassLoader);
        List<DatabaseTableConfig<?>> configs = classAnlyser.getDatabaseTableConfigList(managedClassNames);
        if (!configs.isEmpty())
            DaoManager.addCachedDatabaseConfigs(configs);
    }

}
