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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterable;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
//import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.Dao.DaoObserver;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;
import com.j256.ormlite.table.TableUtils;

import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

public class PersistenceDaoTest 
{
	public static final String FOO_TABLE_NAME = "foo"; 
	static LogRecordHandler logRecordHandler;
	
	@DatabaseTable(tableName = FOO_TABLE_NAME)
    protected static class Foo implements OrmEntity {
        public static final String ID_COLUMN_NAME = "id";
        public static final String VAL_COLUMN_NAME = "val";
        public static final String EQUAL_COLUMN_NAME = "equal";
        public static final String STRING_COLUMN_NAME = "string";
        @DatabaseField(generatedId = true, columnName = ID_COLUMN_NAME)
        public int id;
        @DatabaseField(columnName = VAL_COLUMN_NAME)
        public int val;
        @DatabaseField(columnName = EQUAL_COLUMN_NAME)
        public int equal;
        @DatabaseField(columnName = STRING_COLUMN_NAME)
        public String stringField;
        public Foo() {
        }
        @Override
        public String toString() {
            return "Foo:" + id;
        }
        @Override
        public boolean equals(Object other) {
            if (other == null || other.getClass() != getClass())
                return false;
            return id == ((Foo) other).id;
        }
        @Override
        public int hashCode() {
            return id;
        }
    }

    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";
    protected ConnectionSource connectionSource;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() throws SQLException {
		TestLogHandler.getLogRecordHandler().clear();
        connectionSource = new JdbcConnectionSource(IN_MEMORY_PATH );
        DaoManager.clearCache();
    }

    @After
    public void after() throws Exception 
    {
        connectionSource.close();
        connectionSource = null;
    }

    protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception 
    {
        if (connectionSource == null) 
        {
            throw new SQLException("Connection source is null");
        }
        @SuppressWarnings("unchecked")
        BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, clazz);
        return configDao(dao, createTable);
    }
    
    private <T, ID> Dao<T, ID> configDao(BaseDaoImpl<T, ID> dao, boolean createTable) throws Exception 
    {
        if (connectionSource == null) {
            throw new SQLException("Connection source is null");
        }
        if (createTable) {
            DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
            if (tableConfig == null) {
                tableConfig = DatabaseTableConfig.fromClass(connectionSource.getDatabaseType(), dao.getDataClass());
            }
            createTable(tableConfig, true);
        }
        return dao;
    }

    protected <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception 
    {
        try {
            // first we drop it in case it existed before
            dropTable(tableConfig, true);
        } catch (SQLException ignored) {
            // ignore any errors about missing tables
        }
        TableUtils.createTable(connectionSource, tableConfig);
    }

    protected <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception 
    {
        // drop the table and ignore any errors along the way
        TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
    }


	@Test
	public void testIfAllMethodsAreThere() 
	{
		List<String> failedMessages = new ArrayList<>();

		List<Method> runtimeMethods =
				new ArrayList<>(Arrays.asList(PersistenceDao.class.getDeclaredMethods()));

		List<Method> daoMethods = new ArrayList<>(Arrays.asList(Dao.class.getDeclaredMethods()));
		daoMethods.addAll(Arrays.asList(CloseableIterable.class.getDeclaredMethods()));
		daoMethods.addAll(Arrays.asList(Iterable.class.getDeclaredMethods()));
		Iterator<Method> daoIterator = daoMethods.iterator();
		while (daoIterator.hasNext()) 
		{
			Method daoMethod = daoIterator.next();
			boolean found = false;

			// coverage magic
			if (daoMethod.getName().equals("$VRi") || daoMethod.getName().equals("spliterator") /* java 8 method */
					|| daoMethod.getName().equals("forEach") /* java 8 method */) {
				continue;
			}

			Iterator<Method> runtimeIterator = runtimeMethods.iterator();
			while (runtimeIterator.hasNext()) 
			{
				Method runtimeMethod = runtimeIterator.next();
				if (daoMethod.getName().equals(runtimeMethod.getName())
						&& Arrays.equals(daoMethod.getParameterTypes(), runtimeMethod.getParameterTypes())
						&& daoMethod.getReturnType().equals(runtimeMethod.getReturnType())) {
					found = true;
					daoIterator.remove();
					runtimeIterator.remove();
					break;
				}
			}

			// make sure we found the method in PersistenceDao
			if (!found) 
			{
				failedMessages.add("Could not find Dao method: " + daoMethod);
			}
		}

		// now see if we have any extra methods left over in PersistenceDao
		for (Method runtimeMethod : runtimeMethods) 
		{
			// coverage magic
			if (runtimeMethod.getName().startsWith("$"))
			{
				continue;
			}
			// skip these
			if (runtimeMethod.getName().equals("createDao") || 
				runtimeMethod.getName().equals("queryForId") ||
				runtimeMethod.getName().equals("updateId") ||
				runtimeMethod.getName().equals("extractId") ||
				runtimeMethod.getName().equals("deleteById") ||
				runtimeMethod.getName().equals("idExists") ||
				runtimeMethod.getName().equals("logMessage") ||
				runtimeMethod.getName().equals("getDao") ||
				runtimeMethod.getName().equals("update") ||
				runtimeMethod.getName().equals("delete") ||
				runtimeMethod.getName().equals("create") ||
				runtimeMethod.getName().equals("queryForMatching") ||
				runtimeMethod.getName().equals("queryForMatchingArgs") ||
				runtimeMethod.getName().equals("queryForSameId") ||
				runtimeMethod.getName().equals("createIfNotExists") ||
				runtimeMethod.getName().equals("refresh") ||
				runtimeMethod.getName().equals("createOrUpdate") ||
				runtimeMethod.getName().equals("objectsEqual") ||
				runtimeMethod.getName().equals("objectToString") ||
				runtimeMethod.getName().equals("mapSelectStarRow") ||
				runtimeMethod.getName().equals("queryForFirst") ||
				runtimeMethod.getName().equals("assignEmptyForeignCollection") ||
				runtimeMethod.getName().equals("createObjectInstance"))
			{
				continue;
			}
			failedMessages.add("Unknown PersistenceDao method: " + runtimeMethod);
		}

		if (!failedMessages.isEmpty()) 
		{
			for (String message : failedMessages) 
			{
				System.err.println(message);
			}
			fail("See the console for details");
		}
	}

	@Test
	public void testCoverage() throws Exception 
	{
		Dao<Foo,Integer> exceptionDao = createDao(Foo.class, true);
		PersistenceDao<Foo> dao = new PersistenceDao<Foo>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(val, result.val);
		List<Foo> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(val, iterator.next().val);
		assertFalse(iterator.hasNext());

		results = dao.queryForEq(Foo.ID_COLUMN_NAME, foo.id);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForMatching(foo);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForMatchingArgs(foo);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		result = dao.queryForSameId(foo);
		assertNotNull(results);
		assertEquals(val, result.val);

		result = dao.createIfNotExists(foo);
		assertNotSame(results, foo);
		assertNotNull(results);
		assertEquals(val, result.val);

		int val2 = 342342343;
		foo.val = val2;
		assertEquals(1, dao.update(foo));
		assertEquals(1, dao.refresh(foo));
		assertEquals(1, dao.delete(foo));
		assertNull(dao.queryForId(foo.id));
		results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(0, results.size());

		iterator = dao.iterator();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testCoverage2() throws Exception 
	{
		Dao<Foo,Integer> exceptionDao = createDao(Foo.class, true);
		PersistenceDao<Foo> dao = new PersistenceDao<Foo>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		int id1 = foo.id;

		Map<String, Object> fieldValueMap = new HashMap<>();
		fieldValueMap.put(Foo.ID_COLUMN_NAME, foo.id);
		List<Foo> results = dao.queryForFieldValues(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForFieldValuesArgs(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		QueryBuilder<Foo,Integer> qb = dao.queryBuilder();
		results = dao.query(qb.prepare());
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		UpdateBuilder<Foo,Integer> ub = dao.updateBuilder();
		int val2 = 65809;
		ub.updateColumnValue(Foo.VAL_COLUMN_NAME, val2);
		assertEquals(1, dao.update(ub.prepare()));
		results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val2, results.get(0).val);

		CreateOrUpdateStatus status = dao.createOrUpdate(foo);
		assertNotNull(status);
		assertTrue(status.isUpdated());

		int id2 = foo.id + 1;
		assertEquals(1, dao.updateId(foo, id2));
		assertNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		dao.iterator();
		dao.closeLastIterator();

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		try 
		{
			for (Foo fooLoop : wrapped) 
			{
				assertEquals(id2, fooLoop.id);
			}
		} 
		finally 
		{
			wrapped.close();
		}

		wrapped = dao.getWrappedIterable(dao.queryBuilder().prepare());
		try 
		{
			for (Foo fooLoop : wrapped) 
			{
				assertEquals(id2, fooLoop.id);
			}
		} 
		finally 
		{
			wrapped.close();
		}

		CloseableIterator<Foo> iterator = dao.iterator(dao.queryBuilder().prepare());
		assertTrue(iterator.hasNext());
		iterator.next();
		assertFalse(iterator.hasNext());
		dao.iterator(DatabaseConnection.DEFAULT_RESULT_FLAGS).close();
		dao.iterator(dao.queryBuilder().prepare(), DatabaseConnection.DEFAULT_RESULT_FLAGS).close();

		assertTrue(dao.objectsEqual(foo, foo));
		assertTrue(dao.objectToString(foo).contains("val=" + val));

		assertEquals((Integer) id2, dao.extractId(foo));
		assertEquals(Foo.class, dao.getDataClass());
		assertTrue(dao.isTableExists());
		assertTrue(dao.isUpdatable());
		assertEquals(1, dao.countOf());

		dao.setObjectCache(false);
		dao.setObjectCache(null);
		assertNull(dao.getObjectCache());
		dao.clearObjectCache();
	}

	@Test
	public void testDeletes() throws Exception 
	{
		Dao<Foo,Integer> exceptionDao = createDao(Foo.class, true);
		PersistenceDao<Foo> dao = new PersistenceDao<Foo>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.deleteById(foo.id));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.delete(Arrays.asList(foo)));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.deleteIds(Arrays.asList(foo.id)));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		DeleteBuilder<Foo, Integer> db = dao.deleteBuilder();
		dao.delete(db.prepare());
		assertNull(dao.queryForId(foo.id));
	}

	@Test
	public void testCoverage3() throws Exception 
	{
		Dao<Foo,Integer> exceptionDao = createDao(Foo.class, true);
		PersistenceDao<Foo> dao = new PersistenceDao<Foo>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		GenericRawResults<String[]> rawResults = dao.queryRaw("select * from foo");
		assertEquals(1, rawResults.getResults().size());
		GenericRawResults<Foo> mappedResults = dao.queryRaw("select * from foo", new RawRowMapper<Foo>() 
		{
			@Override
			public Foo mapRow(String[] columnNames, String[] resultColumns) 
			{
				Foo fooResult = new Foo();
				for (int i = 0; i < resultColumns.length; i++) 
				{
					if (columnNames[i].equals(Foo.ID_COLUMN_NAME)) 
					{
						fooResult.id = Integer.parseInt(resultColumns[i]);
					}
				}
				return fooResult;
			}
		});
		assertEquals(1, mappedResults.getResults().size());
		GenericRawResults<Object[]> dataResults =
				dao.queryRaw("select id,val from foo", new DataType[] { DataType.STRING, DataType.INTEGER });
		assertEquals(1, dataResults.getResults().size());
		assertEquals(0, dao.executeRaw("delete from foo where id = ?", Integer.toString(foo.id + 1)));
		assertEquals(0, dao.updateRaw("update foo set val = 100 where id = ?", Integer.toString(foo.id + 1)));
		final String someVal = "fpowejfpjfwe";
		assertEquals(someVal, dao.callBatchTasks(new Callable<String>() 
		{
			@Override
			public String call()
			{
				return someVal;
			}
		}));
		assertNull(dao.findForeignFieldType(Void.class));
		assertEquals(1, dao.countOf());
		assertEquals(1, dao.countOf(dao.queryBuilder().setCountOf(true).prepare()));
		PreparedQuery<Foo> prepared = dao.queryBuilder().prepare();
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(FOO_TABLE_NAME);
		CompiledStatement compiled = null;
		try 
		{
			compiled = prepared.compile(conn, StatementType.SELECT);
			DatabaseResults results = compiled.runQuery(null);
			assertTrue(results.next());
			Foo result = dao.mapSelectStarRow(results);
			assertEquals(foo.id, result.id);
			GenericRowMapper<Foo> mapper = dao.getSelectStarRowMapper();
			result = mapper.mapRow(results);
			assertEquals(foo.id, result.id);
		} 
		finally 
		{
			if (compiled != null) 
			{
				compiled.close();
			}
			connectionSource.releaseConnection(conn);
		}
		assertTrue(dao.idExists(foo.id));
		Foo result = dao.queryForFirst(prepared);
		assertEquals(foo.id, result.id);
		assertNull(dao.getEmptyForeignCollection(Foo.ID_COLUMN_NAME));
		conn = dao.startThreadConnection();
		dao.setAutoCommit(conn, false);
		assertFalse(dao.isAutoCommit(conn));
		dao.commit(conn);
		dao.rollBack(conn);
		dao.endThreadConnection(conn);
		ObjectFactory<Foo> objectFactory = new ObjectFactory<Foo>() 
	    {
			@Override
			public Foo createObject(Constructor<Foo> construcor, Class<Foo> dataClass) 
			{
				return new Foo();
			}
		};
		dao.setObjectFactory(objectFactory);
		dao.setObjectFactory(null);
		assertNotNull(dao.getRawRowMapper());
	}

	@Test
	public void testCreateDao() throws Exception 
	{
		createDao(Foo.class, true);
		PersistenceDao<Foo> dao = PersistenceDao.createDao(connectionSource, Foo.class);
		assertEquals(0, dao.countOf());
	}

	@Test
	public void testCreateDaoTableConfig() throws Exception 
	{
		createDao(Foo.class, true);
		PersistenceDao<Foo> dao =
				PersistenceDao.createDao(connectionSource,
						DatabaseTableConfig.fromClass(connectionSource.getDatabaseType(), Foo.class));
		assertEquals(0, dao.countOf());
	}

   @Test
    public void testCreateCollection() throws Exception {
        Dao<Foo,Integer> dao = createDao(Foo.class, true);
        int numToCreate = 100;
        List<Foo> fooList = new ArrayList<>(numToCreate);
        for (int i = 0; i < numToCreate; i++) {
            Foo foo = new Foo();
            foo.val = i;
            fooList.add(foo);
        }

        // create them all at once
        assertEquals(numToCreate, dao.create(fooList));

        for (int i = 0; i < numToCreate; i++) {
            Foo result = dao.queryForId(fooList.get(i).id);
            assertEquals(i, result.val);
        }
    }

    @Test
    public void testDaoObserver() throws Exception {
        Dao<Foo, Integer> dao = createDao(Foo.class, true);

        final AtomicInteger changeCount = new AtomicInteger();
        DaoObserver observer = new DaoObserver() {
            public void onChange() {
                changeCount.incrementAndGet();
            }
        };
        dao.registerObserver(observer);

        assertEquals(0, changeCount.get());
        Foo foo = new Foo();
        foo.val = 21312313;
        assertEquals(1, dao.create(foo));
        assertEquals(1, changeCount.get());

        foo.val = foo.val + 1;
        assertEquals(1, dao.create(foo));
        assertEquals(2, changeCount.get());

        // shouldn't change anything
        dao.queryForAll();
        assertEquals(2, changeCount.get());

        assertEquals(1, dao.delete(foo));
        assertEquals(3, changeCount.get());

        dao.unregisterObserver(observer);

        assertEquals(1, dao.create(foo));
        // shouldn't change not that we have removed the observer
        assertEquals(3, changeCount.get());
    }


}
