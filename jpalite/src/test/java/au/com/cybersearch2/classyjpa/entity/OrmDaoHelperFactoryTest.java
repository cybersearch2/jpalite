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

import java.sql.SQLException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * OrmDaoHelperFactoryTest
 * @author Andrew Bowley
 * 19/08/2014
 */
public class OrmDaoHelperFactoryTest
{
    // Override internal methods which create concrete objects to replace them with mocks 
    class TestOrmDaoHelperFactory extends OrmDaoHelperFactory<RecordCategory>
    {
        @SuppressWarnings("unchecked")
        PersistenceDao<RecordCategory> dao = mock(PersistenceDao.class);
        ConnectionSource connectionSource;
        boolean tableCreated;
        SQLException toThrowOnTableCreate;
        SQLException toThrowOnDaoCreate;

        public TestOrmDaoHelperFactory()
        {
            super(RecordCategory.class);
            connectionSource = null;
        }
        
        @Override
        protected void createTable(ConnectionSource connectionSource) throws SQLException 
        {
            this.connectionSource = connectionSource;
            if (toThrowOnTableCreate != null)
                throw toThrowOnTableCreate;
            tableCreated = true;
        }
        
        @Override
        protected PersistenceDao<RecordCategory> createDao(ConnectionSource connectionSource) throws SQLException
        {
            this.connectionSource = connectionSource;
            if (toThrowOnDaoCreate != null)
                throw toThrowOnDaoCreate;
            return dao;
        }
    }
    
    @Test 
    public void test_create_for_no_entity_table_case() throws Exception
    {
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(false);
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory> ormDaoHelper = helperFactory.getOrmDaoHelper(connectionSource);
        assertThat(ormDaoHelper).isNotNull();
        assertThat(helperFactory.tableCreated).isTrue();
        assertThat(helperFactory.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_for_entity_table_exists_case() throws Exception
    {
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(true);
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory> ormDaoHelper = helperFactory.getOrmDaoHelper(connectionSource);
        assertThat(ormDaoHelper).isNotNull();
        assertThat(helperFactory.tableCreated).isFalse();
        assertThat(helperFactory.connectionSource).isEqualTo(connectionSource);
    }
    
    @Test 
    public void test_table_create_sql_exception() throws Exception
    {
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(false);
        SQLException exception = new SQLException();
        helperFactory.toThrowOnTableCreate = exception;
        try
        {
            helperFactory.getOrmDaoHelper(connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating table for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test 
    public void test_dao_create_sql_exception() throws Exception
    {
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        SQLException exception = new SQLException();
        helperFactory.toThrowOnDaoCreate = exception;
        try
        {
            helperFactory.getOrmDaoHelper(connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating DAO for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }
}
