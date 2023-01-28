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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

/**
 * PersistenceConfigTest
 * @author Andrew Bowley
 * 12/05/2014
 */
public class PersistenceConfigTest
{
    private static final String QUERY_NAME = "my_query";
    private static final String QUERY_NAME_EXISTS = "Query name already exists: " + QUERY_NAME;

	static LogRecordHandler logRecordHandler;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() {
		TestLogHandler.getLogRecordHandler().clear();
	}
    
    @Test
    public void test_addNamedQuery()
    {
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        @SuppressWarnings("unchecked")
		DaoQueryFactory<RecordCategory> daoQueryFactory = mock(DaoQueryFactory.class);
        persistenceConfig.addNamedQuery(RecordCategory.class, QUERY_NAME, daoQueryFactory);
        assertThat(persistenceConfig.getNamedQuery(QUERY_NAME)).isInstanceOf(NamedDaoQuery.class);
    }
    
    @Test
    public void test_addNamedQuery_already_exists()
    {   // Check error does not throw exception or change existing setting
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        @SuppressWarnings("unchecked")
		DaoQueryFactory<RecordCategory> daoQueryFactory = mock(DaoQueryFactory.class);
        persistenceConfig.addNamedQuery(RecordCategory.class, QUERY_NAME, daoQueryFactory);
        assertThat(persistenceConfig.getNamedQuery(QUERY_NAME)).isInstanceOf(NamedDaoQuery.class);
        try {
            persistenceConfig.addNamedQuery(RecordCategory.class, QUERY_NAME, daoQueryFactory);
            assertThat(persistenceConfig.getNamedQuery(QUERY_NAME)).isInstanceOf(NamedDaoQuery.class);
        } catch (Throwable t) {
        	fail(t.getMessage());
        }
        assertThat(logRecordHandler.match(0, QUERY_NAME_EXISTS)).isTrue();
    }

    @Test
    public void test_addNamedSqlQuery()
    {
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        SqlQueryFactory sqlQueryFactory = mock(SqlQueryFactory.class);
        QueryInfo queryInfo = mock(QueryInfo.class);
        persistenceConfig.addNamedQuery(QUERY_NAME, queryInfo, sqlQueryFactory);
        assertThat(persistenceConfig.getNativeQuery(QUERY_NAME)).isInstanceOf(NamedSqlQuery.class);
    }
    
    @Test
    public void test_addNamedSqlQuery_already_exists()
    {   // Check error does not throw exception or change existing setting
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        SqlQueryFactory sqlQueryFactory = mock(SqlQueryFactory.class);
        QueryInfo queryInfo = mock(QueryInfo.class);
        persistenceConfig.addNamedQuery(QUERY_NAME, queryInfo, sqlQueryFactory);
        assertThat(persistenceConfig.getNativeQuery(QUERY_NAME)).isInstanceOf(NamedSqlQuery.class);
        try {
            persistenceConfig.addNamedQuery(QUERY_NAME, queryInfo, sqlQueryFactory);
            assertThat(persistenceConfig.getNativeQuery(QUERY_NAME)).isInstanceOf(NamedSqlQuery.class);
        } catch (Throwable t) {
        	fail(t.getMessage());
        }
        assertThat(logRecordHandler.match(0, QUERY_NAME_EXISTS)).isTrue();
     }
}
