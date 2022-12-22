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
// Originally com.j256.ormlite.daoRuntimeExceptionDao, globally edited 
// PersistenceException to PerisistenceException to allow the EntityManger
// to catch these exceptions
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
package au.com.cybersearch2.classyjpa.entity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.DatabaseResultsMapper;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RawRowObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;
import com.j256.ormlite.table.TableInfo;

/**
 * Adapted com.j256.ormlite.dao.RuntimeExceptionDao to throw javax.persistence.PersistenceException instead.
 * Proxy to a {@link Dao} that wraps each Exception and rethrows it as PersistenceException. 
 * 
 * <pre>
 * PersistenceDao&lt;Account, String&gt; accountDao = PersistenceDao.createDao(connectionSource, Account.class);
 * </pre>
 * 
 * @author graywatson
 */
public class PersistenceDao<T extends OrmEntity> implements Dao<T, Integer> {

	/*
	 * We use debug here because we don't want these messages to be logged by default. The user will need to turn on
	 * logging for this class to FINE to see the messages.
	 */
	private static final Level LOG_LEVEL = Level.FINE;
    private static final String TAG = "PersistenceDao";
    static Log log = JavaLogger.getLogger(TAG);

	private Dao<T, Integer> dao;

	public PersistenceDao(Dao<T, Integer> dao) {
		this.dao = dao;
	}

	public Dao<T, Integer> getDao() {
		return dao;
	}
	
	/**
	 * Call through to {@link DaoManager#createDao(ConnectionSource, Class)} with the returned DAO wrapped in a
	 * PersistenceDao.
	 * @param connectionSource Connection source
	 * @param clazz Entity class
	 * @param <T> Entity class type
	 * @return PersistenceDao object
	 * @throws java.sql.SQLException if database operation fails
	 */
	public static <T extends OrmEntity> PersistenceDao<T> createDao(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		@SuppressWarnings("unchecked")
		Dao<T, Integer> castDao = (Dao<T, Integer>) DaoManager.createDao(connectionSource, clazz);
		return new PersistenceDao<T>(castDao);
	}

	/**
	 * Call through to {@link DaoManager#createDao(ConnectionSource, DatabaseTableConfig)} with the returned DAO wrapped
	 * in a PersistenceDao.
	 * @param connectionSource Connection source
	 * @param tableConfig Table configuration
	 * @param <T> Entity class type
	 * @return PersistenceDao object
	 * @throws java.sql.SQLException if database operation fails
	 */
	public static <T extends OrmEntity> PersistenceDao<T> createDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		@SuppressWarnings("unchecked")
		Dao<T, Integer> castDao = (Dao<T, Integer>) DaoManager.createDao(connectionSource, tableConfig);
		return new PersistenceDao<T>(castDao);
	}

	/**
	 * @see Dao#queryForId(Object)
	 */
	@Override
	public T queryForId(Integer id) {
		try {
			return dao.queryForId(id);
		} catch (SQLException e) {
			logMessage(e, "queryForId threw exception on: " + id);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForFirst(PreparedQuery)
	 */
	@Override
	public T queryForFirst(PreparedQuery<T> preparedQuery) {
		try {
			return dao.queryForFirst(preparedQuery);
		} catch (SQLException e) {
			logMessage(e, "queryForFirst threw exception on: " + preparedQuery);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForFirst()
	 */
	@Override
	public T queryForFirst() {
		try {
			return dao.queryForFirst();
		} catch (SQLException e) {
			logMessage(e, "queryForFirst threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForAll()
	 */
	@Override
	public List<T> queryForAll() {
		try {
			return dao.queryForAll();
		} catch (SQLException e) {
			logMessage(e, "queryForAll threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForEq(String, Object)
	 */
	@Override
	public List<T> queryForEq(String fieldName, Object value) {
		try {
			return dao.queryForEq(fieldName, value);
		} catch (SQLException e) {
			logMessage(e, "queryForEq threw exception on: " + fieldName);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForMatching(Object)
	 */
	@Override
	public List<T> queryForMatching(T matchObj) {
		try {
			return dao.queryForMatching(matchObj);
		} catch (SQLException e) {
			logMessage(e, "queryForMatching threw exception on: " + matchObj);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForMatchingArgs(Object)
	 */
	@Override
	public List<T> queryForMatchingArgs(T matchObj) {
		try {
			return dao.queryForMatchingArgs(matchObj);
		} catch (SQLException e) {
			logMessage(e, "queryForMatchingArgs threw exception on: " + matchObj);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForFieldValues(Map)
	 */
	@Override
	public List<T> queryForFieldValues(Map<String, Object> fieldValues) {
		try {
			return dao.queryForFieldValues(fieldValues);
		} catch (SQLException e) {
			logMessage(e, "queryForFieldValues threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForFieldValuesArgs(Map)
	 */
	@Override
	public List<T> queryForFieldValuesArgs(Map<String, Object> fieldValues) {
		try {
			return dao.queryForFieldValuesArgs(fieldValues);
		} catch (SQLException e) {
			logMessage(e, "queryForFieldValuesArgs threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryForSameId(Object)
	 */
	@Override
	public T queryForSameId(T data) {
		try {
			return dao.queryForSameId(data);
		} catch (SQLException e) {
			logMessage(e, "queryForSameId threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryBuilder()
	 */
	@Override
	public QueryBuilder<T, Integer> queryBuilder() {
		return dao.queryBuilder();
	}

	/**
	 * @see Dao#updateBuilder()
	 */
	@Override
	public UpdateBuilder<T, Integer> updateBuilder() {
		return dao.updateBuilder();
	}

	/**
	 * @see Dao#deleteBuilder()
	 */
	@Override
	public DeleteBuilder<T, Integer> deleteBuilder() {
		return dao.deleteBuilder();
	}

	/**
	 * @see Dao#query(PreparedQuery)
	 */
	@Override
	public List<T> query(PreparedQuery<T> preparedQuery) {
		try {
			return dao.query(preparedQuery);
		} catch (SQLException e) {
			logMessage(e, "query threw exception on: " + preparedQuery);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#create(Object)
	 */
	@Override
	public int create(T data) {
		try {
			return dao.create(data);
		} catch (SQLException e) {
			logMessage(e, "create threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	@Override
    public int create(Collection<T> datas) {
        try {
            return dao.create(datas);
        } catch (SQLException e) {
            logMessage(e, "create threw exception on: " + datas);
            throw new PersistenceException(e);
        }
    }
	/**
	 * @see Dao#createIfNotExists(Object)
	 */
	@Override
	public T createIfNotExists(T data) {
		try {
			return dao.createIfNotExists(data);
		} catch (SQLException e) {
			logMessage(e, "createIfNotExists threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#createOrUpdate(Object)
	 */
	@Override
	public CreateOrUpdateStatus createOrUpdate(T data) {
		try {
			return dao.createOrUpdate(data);
		} catch (SQLException e) {
			logMessage(e, "createOrUpdate threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#update(Object)
	 */
	@Override
	public int update(T data) {
		try {
			return dao.update(data);
		} catch (SQLException e) {
			logMessage(e, "update threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#updateId(Object, Object)
	 */
	@Override
	public int updateId(T data, Integer newId) {
		try {
			return dao.updateId(data, newId);
		} catch (SQLException e) {
			logMessage(e, "updateId threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#update(PreparedUpdate)
	 */
	@Override
	public int update(PreparedUpdate<T> preparedUpdate) {
		try {
			return dao.update(preparedUpdate);
		} catch (SQLException e) {
			logMessage(e, "update threw exception on: " + preparedUpdate);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#refresh(Object)
	 */
	@Override
	public int refresh(T data) {
		try {
			return dao.refresh(data);
		} catch (SQLException e) {
			logMessage(e, "refresh threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#delete(Object)
	 */
	@Override
	public int delete(T data) {
		try {
			return dao.delete(data);
		} catch (SQLException e) {
			logMessage(e, "delete threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#deleteById(Object)
	 */
	@Override
	public int deleteById(Integer id) {
		try {
			return dao.deleteById(id);
		} catch (SQLException e) {
			logMessage(e, "deleteById threw exception on: " + id);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#delete(Collection)
	 */
	@Override
	public int delete(Collection<T> datas) {
		try {
			return dao.delete(datas);
		} catch (SQLException e) {
			logMessage(e, "delete threw exception on: " + datas);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#deleteIds(Collection)
	 */
	@Override
	public int deleteIds(Collection<Integer> ids) {
		try {
			return dao.deleteIds(ids);
		} catch (SQLException e) {
			logMessage(e, "deleteIds threw exception on: " + ids);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#delete(PreparedDelete)
	 */
	@Override
	public int delete(PreparedDelete<T> preparedDelete) {
		try {
			return dao.delete(preparedDelete);
		} catch (SQLException e) {
			logMessage(e, "delete threw exception on: " + preparedDelete);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#iterator()
	 */
	@Override
	public CloseableIterator<T> iterator() {
		return dao.iterator();
	}

	@Override
	public CloseableIterator<T> closeableIterator() {
		return dao.closeableIterator();
	}

	/**
	 * @see Dao#iterator(int)
	 */
	@Override
	public CloseableIterator<T> iterator(int resultFlags) {
		return dao.iterator(resultFlags);
	}

	/**
	 * @see Dao#getWrappedIterable()
	 */
	@Override
	public CloseableWrappedIterable<T> getWrappedIterable() {
		return dao.getWrappedIterable();
	}

	/**
	 * @see Dao#getWrappedIterable(PreparedQuery)
	 */
	@Override
	public CloseableWrappedIterable<T> getWrappedIterable(PreparedQuery<T> preparedQuery) {
		return dao.getWrappedIterable(preparedQuery);
	}

	/**
	 * @see Dao#closeLastIterator()
	 */
	@Override
	public void closeLastIterator() {
		try {
			dao.closeLastIterator();
		} catch (Exception e) {
			logMessage(e, "closeLastIterator threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#iterator(PreparedQuery)
	 */
	@Override
	public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery) {
		try {
			return dao.iterator(preparedQuery);
		} catch (SQLException e) {
			logMessage(e, "iterator threw exception on: " + preparedQuery);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#iterator(PreparedQuery, int)
	 */
	@Override
	public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery, int resultFlags) {
		try {
			return dao.iterator(preparedQuery, resultFlags);
		} catch (SQLException e) {
			logMessage(e, "iterator threw exception on: " + preparedQuery);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRaw(String, String...)
	 */
	public GenericRawResults<String[]> queryRaw(String query, String... arguments) {
		try {
			return dao.queryRaw(query, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRaw threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRaw(String, DatabaseResultsMapper, String...)
	 */
	@Override
	public <UO> GenericRawResults<UO> queryRaw(String query, DatabaseResultsMapper<UO> mapper, String... arguments) {
		try {
			return dao.queryRaw(query, mapper, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRaw threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRawValue(String, String...)
	 */
	@Override
	public long queryRawValue(String query, String... arguments) {
		try {
			return dao.queryRawValue(query, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRawValue threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRaw(String, RawRowMapper, String...)
	 */
	@Override
	public <UO> GenericRawResults<UO> queryRaw(String query, RawRowMapper<UO> mapper, String... arguments) {
		try {
			return dao.queryRaw(query, mapper, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRaw threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRaw(String, DataType[], RawRowObjectMapper, String...)
	 */
	@Override
	public <UO> GenericRawResults<UO> queryRaw(String query, DataType[] columnTypes, RawRowObjectMapper<UO> mapper,
			String... arguments) {
		try {
			return dao.queryRaw(query, columnTypes, mapper, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRaw threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#queryRaw(String, DataType[], String...)
	 */
	@Override
	public GenericRawResults<Object[]> queryRaw(String query, DataType[] columnTypes, String... arguments) {
		try {
			return dao.queryRaw(query, columnTypes, arguments);
		} catch (SQLException e) {
			logMessage(e, "queryRaw threw exception on: " + query);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#executeRaw(String, String...)
	 */
	@Override
	public int executeRaw(String statement, String... arguments) {
		try {
			return dao.executeRaw(statement, arguments);
		} catch (SQLException e) {
			logMessage(e, "executeRaw threw exception on: " + statement);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#executeRawNoArgs(String)
	 */
	@Override
	public int executeRawNoArgs(String statement) {
		try {
			return dao.executeRawNoArgs(statement);
		} catch (SQLException e) {
			logMessage(e, "executeRawNoArgs threw exception on: " + statement);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#updateRaw(String, String...)
	 */
	@Override
	public int updateRaw(String statement, String... arguments) {
		try {
			return dao.updateRaw(statement, arguments);
		} catch (SQLException e) {
			logMessage(e, "updateRaw threw exception on: " + statement);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#callBatchTasks(Callable)
	 */
	@Override
	public <CT> CT callBatchTasks(Callable<CT> callable) {
		try {
			return dao.callBatchTasks(callable);
		} catch (Exception e) {
			logMessage(e, "callBatchTasks threw exception on: " + callable);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#objectToString(Object)
	 */
	@Override
	public String objectToString(T data) {
		return dao.objectToString(data);
	}

	/**
	 * @see Dao#objectsEqual(Object, Object)
	 */
	@Override
	public boolean objectsEqual(T data1, T data2) {
		try {
			return dao.objectsEqual(data1, data2);
		} catch (SQLException e) {
			logMessage(e, "objectsEqual threw exception on: " + data1 + " and " + data2);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#extractId(Object)
	 */
	@Override
	public Integer extractId(T data) {
		try {
			return dao.extractId(data);
		} catch (SQLException e) {
			logMessage(e, "extractId threw exception on: " + data);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#getDataClass()
	 */
	@Override
	public Class<T> getDataClass() {
		return dao.getDataClass();
	}

	/**
	 * @see Dao#findForeignFieldType(Class)
	 */
	@Override
	public FieldType findForeignFieldType(Class<?> clazz) {
		return dao.findForeignFieldType(clazz);
	}

	/**
	 * @see Dao#isUpdatable()
	 */
	@Override
	public boolean isUpdatable() {
		return dao.isUpdatable();
	}

	/**
	 * @see Dao#isTableExists()
	 */
	@Override
	public boolean isTableExists() {
		try {
			return dao.isTableExists();
		} catch (SQLException e) {
			logMessage(e, "isTableExists threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#countOf()
	 */
	@Override
	public long countOf() {
		try {
			return dao.countOf();
		} catch (SQLException e) {
			logMessage(e, "countOf threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#countOf(PreparedQuery)
	 */
	@Override
	public long countOf(PreparedQuery<T> preparedQuery) {
		try {
			return dao.countOf(preparedQuery);
		} catch (SQLException e) {
			logMessage(e, "countOf threw exception on " + preparedQuery);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#assignEmptyForeignCollection(Object, String)
	 */
	@Override
	public void assignEmptyForeignCollection(T parent, String fieldName) {
		try {
			dao.assignEmptyForeignCollection(parent, fieldName);
		} catch (SQLException e) {
			logMessage(e, "assignEmptyForeignCollection threw exception on " + fieldName);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#getEmptyForeignCollection(String)
	 */
	@Override
	public <FT> ForeignCollection<FT> getEmptyForeignCollection(String fieldName) {
		try {
			return dao.getEmptyForeignCollection(fieldName);
		} catch (SQLException e) {
			logMessage(e, "getEmptyForeignCollection threw exception on " + fieldName);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#setObjectCache(boolean)
	 */
	@Override
	public void setObjectCache(boolean enabled) {
		try {
			dao.setObjectCache(enabled);
		} catch (SQLException e) {
			logMessage(e, "setObjectCache(" + enabled + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#getObjectCache()
	 */
	@Override
	public ObjectCache getObjectCache() {
		return dao.getObjectCache();
	}

	/**
	 * @see Dao#setObjectCache(ObjectCache)
	 */
	@Override
	public void setObjectCache(ObjectCache objectCache) {
		try {
			dao.setObjectCache(objectCache);
		} catch (SQLException e) {
			logMessage(e, "setObjectCache threw exception on " + objectCache);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#clearObjectCache()
	 */
	@Override
	public void clearObjectCache() {
		dao.clearObjectCache();
	}

	/**
	 * @see Dao#mapSelectStarRow(DatabaseResults)
	 */
	@Override
	public T mapSelectStarRow(DatabaseResults results) {
		try {
			return dao.mapSelectStarRow(results);
		} catch (SQLException e) {
			logMessage(e, "mapSelectStarRow threw exception on results");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#getSelectStarRowMapper()
	 */
	@Override
	public GenericRowMapper<T> getSelectStarRowMapper() {
		try {
			return dao.getSelectStarRowMapper();
		} catch (SQLException e) {
			logMessage(e, "getSelectStarRowMapper threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#idExists(Object)
	 */
	@Override
	public boolean idExists(Integer id) {
		try {
			return dao.idExists(id);
		} catch (SQLException e) {
			logMessage(e, "idExists threw exception on " + id);
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#startThreadConnection()
	 */
	@Override
	public DatabaseConnection startThreadConnection() {
		try {
			return dao.startThreadConnection();
		} catch (SQLException e) {
			logMessage(e, "startThreadConnection() threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#endThreadConnection(DatabaseConnection)
	 */
	@Override
	public void endThreadConnection(DatabaseConnection connection) {
		try {
			dao.endThreadConnection(connection);
		} catch (SQLException e) {
			logMessage(e, "endThreadConnection(" + connection + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#setAutoCommit(boolean)
	 */

	/**
	 * @see Dao#setAutoCommit(DatabaseConnection, boolean)
	 */
	@Override
	public void setAutoCommit(DatabaseConnection connection, boolean autoCommit) {
		try {
			dao.setAutoCommit(connection, autoCommit);
		} catch (SQLException e) {
			logMessage(e, "setAutoCommit(" + connection + "," + autoCommit + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#isAutoCommit(DatabaseConnection)
	 */
	@Override
	public boolean isAutoCommit(DatabaseConnection connection) {
		try {
			return dao.isAutoCommit(connection);
		} catch (SQLException e) {
			logMessage(e, "isAutoCommit(" + connection + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#commit(DatabaseConnection)
	 */
	@Override
	public void commit(DatabaseConnection connection) {
		try {
			dao.commit(connection);
		} catch (SQLException e) {
			logMessage(e, "commit(" + connection + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#rollBack(DatabaseConnection)
	 */
	@Override
	public void rollBack(DatabaseConnection connection) {
		try {
			dao.rollBack(connection);
		} catch (SQLException e) {
			logMessage(e, "rollBack(" + connection + ") threw exception");
			throw new PersistenceException(e);
		}
	}

	/**
	 * @see Dao#setObjectFactory(ObjectFactory)
	 */
	@Override
	public void setObjectFactory(ObjectFactory<T> objectFactory) {
		dao.setObjectFactory(objectFactory);
	}

	/**
	 * @see Dao#getRawRowMapper()
	 */
	@Override
	public RawRowMapper<T> getRawRowMapper() {
		return dao.getRawRowMapper();
	}

	/**
	 * @see Dao#getConnectionSource()
	 */
	@Override
	public ConnectionSource getConnectionSource() {
		return dao.getConnectionSource();
	}

	@Override
    public void registerObserver(DaoObserver observer) {
        dao.registerObserver(observer);
    }

	@Override
    public void unregisterObserver(DaoObserver observer) {
        dao.unregisterObserver(observer);
    }

	@Override
    public void notifyChanges() {
        dao.notifyChanges();
    }

	@Override
	public String getTableName() {
		return dao.getTableName();
    }


	/**
	 *  Log message if logging level permits
	 *@param e Exception thrown
	 *@param message
	 */
	private void logMessage(Exception e, String message) {
	    if (log.isLoggable(TAG, LOG_LEVEL))
	    {
	        if (LOG_LEVEL == Level.SEVERE)
	            log.error(TAG, message, e);
	        else if (LOG_LEVEL == Level.WARNING)
	            log.warn(TAG, message, e);
            else if (LOG_LEVEL == Level.INFO)
                log.info(TAG, message, e);
            else
                log.debug(TAG, message, e);
	    }
	}

	@Override
	public T createObjectInstance() throws SQLException {
		return dao.createObjectInstance();
	}

	@Override
	public TableInfo<T, Integer> getTableInfo() {
		return dao.getTableInfo();
	}
}
