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
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.classylog.LogManager;
import au.com.cybersearch2.container.JpaSetting;
import au.com.cybersearch2.container.SettingsMap;

/**
 * PersistenceAdminImpl Persistence unit implementation
 * 
 * @author Andrew Bowley 29/07/2014
 */
public class PersistenceAdminImpl implements PersistenceAdmin {
	private static final String DATABASE_INFO_NAME = "";
	private static Logger logger = LogManager.getLogger(PersistenceAdminImpl.class);

	/** Persistence unit name */
	private final String puName;
	/** Persistence unit configuration */
	private final PersistenceConfig config;
	/** Implementation of javax.persistence.PersistenceUnitInfo */
	private final PersistenceUnitInfo puInfo;
	/** Direct database access to allow native operations to be performed */
	private final DatabaseSupport databaseSupport;
	/** Database name */
	private final String databaseName;
	
	/** Flag set true if connection source uses a single connection */
	private Boolean singleConnection;
	/** Persistence unit connection source */
	private ConnectionSource connectionSource;

	/**
	 * Create PersistenceAdminImpl object
	 * 
	 * @param puName          Persistence Unit (PU) name
	 * @param databaseSupport Native support
	 * @param config          Persistence unit configuration
	 * @param connectionSource Connection source
	 */
	public PersistenceAdminImpl(String puName,
			                    DatabaseSupport databaseSupport, 
			                    PersistenceConfig config) {
		this.puName = puName;
		this.config = config;
		this.puInfo = config.getPuInfo();
		this.databaseSupport = databaseSupport;
		databaseName = getDatabaseName(puInfo);
		singleConnection = Boolean.valueOf(getConnectionSource().isSingleConnection(DATABASE_INFO_NAME));
	}

	/**
	 * Returns connection source
	 * 
	 * @return com.j256.ormlite.support.ConnectionSource
	 */
	@Override
	public ConnectionSource getConnectionSource() {
		if ((connectionSource == null) || !connectionSource.isOpen(""))
			connectionSource = 
			    ((ConnectionSourceFactory)databaseSupport).getConnectionSource(puName, databaseName, puInfo.getProperties());
		return connectionSource;
	}

	/**
	 * Add named query to persistence unit context
	 * 
	 * @param entityClass     Entity class
	 * @param name            Query name
	 * @param daoQueryFactory Query generator
	 */
	@Override
	public <T extends OrmEntity> void addNamedQuery(Class<T> entityClass, String name,
			DaoQueryFactory<T> daoQueryFactory) {
		config.addNamedQuery(entityClass, name, daoQueryFactory);
	}

	/**
	 * Add native named query to persistence unit context
	 * 
	 * @param name           Query name
	 * @param queryInfo      Native query information
	 * @param queryGenerator Native query generator
	 */
	@Override
	public void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator) {
		config.addNamedQuery(name, queryInfo, queryGenerator);
	}

	/**
	 * Returns list of objects from executing a native query
	 * 
	 * @param queryInfo     Native query details
	 * @param startPosition The start position of the first result, numbered from 0
	 * @param maxResults    Maximum number of results to retrieve, or 0 for no limit
	 * @return Object list
	 */
	@Override
	public List<Object> getResultList(QueryInfo queryInfo, int startPosition, int maxResults) {
		return databaseSupport.getResultList(connectionSource, queryInfo, startPosition, maxResults);
	}

	/**
	 * Returns object from executing a native query
	 * 
	 * @param queryInfo Native query details
	 * @return Object or null if nothing returned by query
	 */
	@Override
	public Object getSingleResult(QueryInfo queryInfo) {
		return databaseSupport.getSingleResult(connectionSource, queryInfo);
	}

	@Override
	public String getPuName() {
		return puName;
	}

	/**
	 * Returns database name, which is defined as PU property "database-name"
	 * 
	 * @return String
	 */
	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public int getDatabaseVersion() {
		return getDatabaseVersion(puInfo.getSettingsMap());
	}

	/**
	 * Close all database connections
	 */
	@Override
	public void close() {
		databaseSupport.close();
	}

	/**
	 * Returns database type
	 * 
	 * @return com.j256.ormlite.db.DatabaseType
	 */
	@Override
	public DatabaseType getDatabaseType() {
		return databaseSupport.getDatabaseType();
	}

	/**
	 * Returns PU properties
	 * 
	 * @return java.util.Properties
	 */
	@Override
	public Properties getProperties() {
		return puInfo.getProperties();
	}

	@Override
	public PersistenceUnitInfo getPuInfo() {
		return puInfo;
	}

	@Override
	public PersistenceConfig getConfig() {
		return config;
	}

	@Override
	public boolean isSingleConnection() {
		return singleConnection;
	}

	/**
	 * Gets the database version.
	 * 
	 * @return the database version
	 */
	@Override
    public int getVersion() {
    	return databaseSupport.getVersion(connectionSource, puName);
    }

	/**
	 * Sets the database version.
	 * 
	 * @param version          the new database version
	 */
	@Override
    public void setVersion(int version) {
		databaseSupport.setVersion(version, puName, connectionSource);
    }

	/**
	 * Returns DAO for given entity class
	 * 
	 * @param <T>              Entity type
	 * @param entityClass      Entity class
	 * @param connectionSource Open connection source
	 * @return PersistenceDao object
	 */
	@Override
	public <T extends OrmEntity> PersistenceDao<T> getDao(Class<T> entityClass, ConnectionSource connectionSource) {

		return config.getDao(entityClass, connectionSource);
	}

	/**
	 * Returns DAO for given entity class
	 * 
	 * @param <T>         Entity type
	 * @param entityClass Entity class
	 * @return PersistenceDao object
	 */
	@Override
	public <T extends OrmEntity> PersistenceDao<T> getDao(Class<T> entityClass) {

		return config.getDao(entityClass, getConnectionSource());
	}

	@Override
	public String getSetting(JpaSetting key) {
		return puInfo.getSettingsMap().get(key);
	}

	@Override
	public boolean hasSetting(JpaSetting key) {
		return puInfo.getSettingsMap().hasSetting(key);
	}

	/**
	 * Returns database version, which is defined as PU property "database-version".
	 * Defaults to 1 if not defined
	 * 
	 * @param settingsMap Settings map
	 * @return int
	 */
	public static int getDatabaseVersion(SettingsMap settingsMap) {
		// Database version defaults to 1
		int databaseVersion = 1;
			try {
				if (settingsMap.hasSetting(JpaSetting.database_version))
					databaseVersion = Integer.parseInt(settingsMap.get(JpaSetting.database_version));
			} catch (NumberFormatException e) {
				logger.error(JpaSetting.database_version.getDescription() + " is invalid ");
			}
		return databaseVersion;
	}

	public static String getDatabaseName(PersistenceUnitInfo puInfo) {
		SettingsMap settingsMap = puInfo.getSettingsMap();
		String databaseName = null;
		if (settingsMap.hasSetting(JpaSetting.database_name)) 
			databaseName = settingsMap.get(JpaSetting.database_name);
		if (databaseName == null)
		{
			logger.warn(String.format("\"%s\" not set in unit \"%s\"",
				JpaSetting.database_name.getDescription(), puInfo.getPersistenceUnitName()));
			databaseName = puInfo.getPersistenceUnitName();
		}
		return databaseName;
	}

}
