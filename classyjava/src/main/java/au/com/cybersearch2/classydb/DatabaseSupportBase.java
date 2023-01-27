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
package au.com.cybersearch2.classydb;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.h2.jdbcx.JdbcDataSource;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.ResultRow;
import com.j256.ormlite.logger.Logger;

/**
 * DatabaseSupportBase
 * 
 * @author Andrew Bowley 16 May 2015
 */
public abstract class DatabaseSupportBase implements DatabaseSupport, ConnectionSourceFactory {
	/** A connection is created when a ConnectionSource is created */
	public static final class ConnectionPair {

		private final ConnectionSource connectionSource;
		/** Connection created on datasource creation */
		private final DatabaseConnection databaseConnection;

		public ConnectionPair(ConnectionSource connectionSource, DatabaseConnection databaseConnection) {
			this.connectionSource = connectionSource;
			this.databaseConnection = databaseConnection;
		}

		public ConnectionSource getCnnectionSource() {
			return connectionSource;
		}

		public DatabaseConnection getDatabaseConnection() {
			return databaseConnection;
		}

		public void close() throws Exception {
			connectionSource.releaseConnection(getDatabaseConnection());
			connectionSource.close();
		}
	}

	/** Table to hold database version */
	public static final String INFO_SUFFIX = "_info";
	/** Limit clause validation */
	protected static final Pattern LIMIT_PATTERN = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
	public static final boolean CACHE_STORE = true;

	/** ORMLite databaseType */
	protected final DatabaseType databaseType;
	/** Connection type: memory, file or pooled */
	protected final ConnectionType connectionType;
	/** Derived logger */
	private final Logger logger;
	/** Map connectionSource to database name */
	protected Map<String, ConnectionPair> connectionSourceMap;
	protected List<OpenHelper> openHelperCallbacksList;

	/**
	 * Construct DatabaseSupportBase object
	 * 
	 * @param databaseType   Database type
	 * @param connectionType ConnectionType - memory, file or pooled
	 * @param logger         Logger Derived logger
	 */
	protected DatabaseSupportBase(DatabaseType databaseType, ConnectionType connectionType, Logger logger) {
		this.connectionType = connectionType;
		this.databaseType = databaseType;
		this.logger = logger;
		connectionSourceMap = new HashMap<>();
		openHelperCallbacksList = Collections.emptyList();
	}

	abstract protected File getDatabaseLocation();

	abstract protected String getVersionUpdateStatement(String infoTableName, int version);

	abstract protected String getVersionCreateStatement(String infoTableName);

	abstract protected String getVersionInsertStatement(String infoTableName, int version);

	abstract protected ConnectionSource getConnectionSourceForType(String databaseName, Properties properties)
			throws SQLException;

	/**
	 * Perform any initialization required prior to creating first database
	 * connection
	 */
	public void initialize() {
		if (connectionType != ConnectionType.memory) { // Create database directory
			File dbDir = getDatabaseLocation();
			if (!dbDir.exists() && !dbDir.mkdirs())
				throw new PersistenceException("Failed to create database location: " + dbDir);
		}
	}

	/**
	 * Returns ConnectionSource object
	 * 
	 * @param databaseName Database name
	 * @param properties   Properties defined in persistence.xml
	 * @return ConnectionSource
	 */
	@Override
	public ConnectionSource getConnectionSource(String databaseName, Properties properties) {
		ConnectionSource connectionSource = null;
		ConnectionPair connectionPair = connectionSourceMap.get(databaseName);
		if (connectionPair != null)
			connectionSource = connectionPair.getCnnectionSource();
		else {
			try {
				connectionSource = getConnectionSourceForType(databaseName, properties);
				DatabaseConnection databaseConnection = connectionSource
						.getReadWriteConnection(getInfoTable(properties));
				connectionSourceMap.put(databaseName, new ConnectionPair(connectionSource, databaseConnection));
			} catch (SQLException e) {
				throw new PersistenceException("Cannot create connectionSource for database " + databaseName, e);
			}
		}
		return connectionSource;
	}

	/**
	 * Perform any clean up required on database shutdown
	 */
	@Override
	public synchronized void close() { // Close all ConnectionSource objects and clear ConnectionSource map
		for (Entry<String, ConnectionPair> entry : connectionSourceMap.entrySet()) {
			try {
				entry.getValue().close();
			} catch (Exception e) {
				logger.warn("Error closing connection for database " + entry.getKey(), e);
			}
		}
		connectionSourceMap.clear();
	}

	/**
	 * Returns database type
	 * 
	 * @return DatabaseType
	 */
	@Override
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	@Override
	public void registerOpenHelperCallbacks(OpenHelper openHelper) {
		if (openHelperCallbacksList.isEmpty())
			openHelperCallbacksList = new ArrayList<>();
		openHelperCallbacksList.add(openHelper);
	}

	@Override
	public List<OpenHelper> getOpenHelperCallbacksList() {
		return openHelperCallbacksList;
	}

	/**
	 * Gets the database version.
	 * 
	 * @param connectionSource Open ConnectionSource object of database.
	 * @param properties       Properties defined in persistence.xml
	 * @return the database version
	 */
	@Override
	public int getVersion(ConnectionSource connectionSource, Properties properties) {
		int databaseVersion = 0;
		boolean tableExists = false;
		DatabaseConnection connection = null;
		String infoTableName = getInfoTable(properties);
		try {
			connection = connectionSource.getReadOnlyConnection(infoTableName);
			tableExists = connection.isTableExists(infoTableName);
			if (tableExists)
				databaseVersion = ((Long) connection.queryForLong("select version from " + infoTableName)).intValue();
		} catch (SQLException e) {
			throw new PersistenceException(e);
		} finally {
			try {
				connectionSource.releaseConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return databaseVersion;
	}

	/**
	 * Sets the database version.
	 * 
	 * @param connectionSource Open ConnectionSource object of database. Can be null
	 *                         for Android SQLite.
	 * @param properties       Properties defined in persistence.xml
	 * @param version          the new database version
	 */
	@Override
	public void setVersion(int version, Properties properties, ConnectionSource connectionSource) {
		boolean tableExists = false;
		DatabaseConnection connection = null;
		String infoTableName = getInfoTable(properties);
		try {
			connection = connectionSource.getReadOnlyConnection(infoTableName);
			tableExists = connection.isTableExists(infoTableName);
			if (tableExists)
				connection.executeStatement(getVersionUpdateStatement(infoTableName, version),
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
			else {
				connection.executeStatement(getVersionCreateStatement(infoTableName),
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
				connection.executeStatement(getVersionInsertStatement(infoTableName, version),
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
			}
		} catch (SQLException e) {
			throw new PersistenceException(e);
		} finally {
			try {
				connectionSource.releaseConnection(connection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns list result of native query in Android SQLite API format
	 * 
	 * @param connectionSource Open ConnectionSource object
	 * @param queryInfo        QueryInfo
	 * @param startPosition    int
	 * @param maxResults       int
	 * @return List of Objects
	 */
	@Override
	public List<Object> getResultList(ConnectionSource connectionSource, QueryInfo queryInfo, int startPosition,
			int maxResults) {
		List<Object> resultList = new ArrayList<>();
		DatabaseConnection connection = null;
		String databaseName = databaseType.getDatabaseName();
		try {
			connection = connectionSource.getReadWriteConnection(queryInfo.getTable());
			DatabaseResults results = getDatabaseResults(connection, queryInfo, startPosition, maxResults);
			if (results.first()) {
				int position = 0;
				do {
					ResultRow resultRow = new SqliteResultRow(position, results);
					resultList.add(queryInfo.getRowMapper().mapRow(resultRow));
					++position;
				} while (results.next());
			}
		} catch (SQLException e) {
			throw new PersistenceException("Error getting database connection for database \"" + databaseName + "\"",
					e);
		}
		return resultList;
	}

	/**
	 * Returns single result of native query in Android SQLite API format
	 * 
	 * @param connectionSource Open ConnectionSource object
	 * @param queryInfo        QueryInfo
	 * @return Object
	 */
	@Override
	public Object getSingleResult(ConnectionSource connectionSource, QueryInfo queryInfo) {
		List<Object> resultList = getResultList(connectionSource, queryInfo, 0, 1);
		return resultList.size() > 0 ? resultList.get(0) : null;
	}

	/**
	 * Build an SQL query string from the given clauses.
	 *
	 * @param tables  The table names to compile the query against.
	 * @param columns A list of which columns to return. Passing null will return
	 *                all columns, which is discouraged to prevent reading data from
	 *                storage that isn't going to be used.
	 * @param where   A filter declaring which rows to return, formatted as an SQL
	 *                WHERE clause (excluding the WHERE itself). Passing null will
	 *                return all rows for the given URL.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL
	 *                GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *                will cause the rows to not be grouped.
	 * @param having  A filter declare which row groups to include in the results,
	 *                if row grouping is being used, formatted as an SQL HAVING
	 *                clause (excluding the HAVING itself). Passing null will cause
	 *                all row groups to be included, and is required when row
	 *                grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
	 *                (excluding the ORDER BY itself). Passing null will use the
	 *                default sort order, which may be unordered.
	 * @param limit   Limits the number of rows returned by the query, formatted as
	 *                LIMIT clause. Passing null denotes no LIMIT clause.
	 * @return the SQL query string
	 */
	public static String buildQueryString(String tables, String[] columns, String where, String groupBy, String having,
			String orderBy, String limit) {
		if (isEmpty(groupBy) && !isEmpty(having)) {
			throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
		}
		if (!isEmpty(limit) && !LIMIT_PATTERN.matcher(limit).matches()) {
			throw new IllegalArgumentException("Invalid LIMIT clauses:" + limit);
		}

		StringBuilder query = new StringBuilder(120);

		query.append("SELECT ");
		if (columns != null && columns.length != 0) {
			appendColumns(query, columns);
		} else {
			query.append("* ");
		}
		query.append("FROM ");
		query.append(tables);
		appendClause(query, " WHERE ", where);
		appendClause(query, " GROUP BY ", groupBy);
		appendClause(query, " HAVING ", having);
		appendClause(query, " ORDER BY ", orderBy);
		appendClause(query, " LIMIT ", limit);

		return query.toString();
	}

	protected String appendProperties(String url, Properties properties, JdbcDataSource jdbcDataSource) {
		String newUrl = url;
		Properties filtered = filterProperties(properties);
		if (!filtered.isEmpty()) {
			StringBuilder builder = new StringBuilder(url);
			for (Entry<Object, Object> entry : filtered.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				if ("USER".equalsIgnoreCase(key))
					jdbcDataSource.setUser(value);
				else if ("PASSWORD".equalsIgnoreCase(key))
					jdbcDataSource.setPassword(value);
				else
					builder.append(';').append(key).append('=').append(value);
			}
			newUrl = builder.toString();
		}
		return newUrl;
	}

	protected String appendProperties(String url, Properties properties) {
		String newUrl = url;
		Properties filtered = filterProperties(properties);
		if (!filtered.isEmpty()) {
			StringBuilder builder = new StringBuilder(url);
			for (Entry<Object, Object> entry : filtered.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				builder.append(';').append(key).append('=').append(value);
			}
			newUrl = builder.toString();
		}
		return newUrl;
	}

	protected Properties filterProperties(Properties properties) {
		Properties filtered = new Properties();
		if ((properties != null) && !properties.isEmpty()) {
			for (Entry<Object, Object> entry : properties.entrySet()) {
				String key = entry.getKey().toString();
				if (!key.toUpperCase().startsWith(JTA_PREFIX))
					filtered.put(key, entry.getValue().toString());
			}
		}
		return filtered;
	}

	/**
	 * Builds a SQL query, compiles and runs it and finally returns result
	 * 
	 * @param connection    DatabaseConnection object
	 * @param queryInfo     QueryInfo object containing query elements
	 * @param startPosition int
	 * @param maxResults    int
	 * @return DatabaseResults
	 * @throws SQLException if database operation fails
	 */
	protected DatabaseResults getDatabaseResults(DatabaseConnection connection, QueryInfo queryInfo, int startPosition,
			int maxResults) throws SQLException {
		String limitValue = queryInfo.getLimit();
		if (maxResults > 0) {
			limitValue = Integer.toString(maxResults);
			if (startPosition > 0) { // offset precedes limit
				StringBuilder builder = new StringBuilder(Integer.valueOf(startPosition));
				builder.append(',').append(limitValue);
				limitValue = builder.toString();
			}
		}
		String statement = buildQueryString(queryInfo.getTable(), queryInfo.getColumns(), queryInfo.getSelection(),
				queryInfo.getGroupBy(), queryInfo.getHaving(), queryInfo.getOrderBy(), limitValue);
		CompiledStatement compiledStatement = connection.compileStatement(statement, StatementType.SELECT_RAW,
				new FieldType[] {}, DatabaseConnection.DEFAULT_RESULT_FLAGS, CACHE_STORE);
		int parameterIndex = 0;
		for (String arg : queryInfo.getSelectionArgs()) {
			compiledStatement.setObject(parameterIndex, arg, SqlType.STRING);
			if (++parameterIndex >= compiledStatement.getColumnCount())
				break;
		}
		return compiledStatement.runQuery(null /* objectCache */);
	}

	/**
	 * Close database connection
	 * 
	 * @param connection   DatabaseConnection object
	 * @param databaseName Database name
	 */
	protected void close(DatabaseConnection connection, String databaseName) {
		if (connection != null)
			try {
				connection.close();
			} catch (Exception e) {
				logger.error("Error closing database connection for database \"" + databaseName + "\"", e);
			}
	}

	protected String getInfoTable(Properties properties) {
		String puName = properties.getProperty(DatabaseSupport.JTA_PREFIX + PersistenceUnitInfoImpl.PU_NAME_PROPERTY);
		return puName + INFO_SUFFIX;
	}
	
	protected static boolean isEmpty(String text) {
		return (text == null) || (text.length() == 0);
	}

	protected static void appendClause(StringBuilder s, String name, String clause) {
		if (!isEmpty(clause)) {
			s.append(name);
			s.append(clause);
		}
	}

	/**
	 * Add the names that are non-null in columns to s, separating them with commas.
	 * 
	 * @param s       String builder
	 * @param columns Columns
	 */
	protected static void appendColumns(StringBuilder s, String[] columns) {
		int n = columns.length;

		for (int i = 0; i < n; i++) {
			String column = columns[i];

			if (column != null) {
				if (i > 0) {
					s.append(", ");
				}
				s.append(column);
			}
		}
		s.append(' ');
	}

}
