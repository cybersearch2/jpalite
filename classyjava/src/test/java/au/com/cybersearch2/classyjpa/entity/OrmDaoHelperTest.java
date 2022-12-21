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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * OrmDaoHelperTest
 * @author Andrew Bowley
 * 03/05/2014
 */
public class OrmDaoHelperTest
{
/*    
    // TODO Reinstate tests if worthy
    // Overide internal methods which create concrete objects to replace them with mocks 
    class OrmDaoHelper<T,ID> extends OrmDaoHelper<T,ID>
    {
        @SuppressWarnings("unchecked")
        PersistenceDao<RecordCategory, ID> dao = mock(PersistenceDao.class);
        MockTableCreator tableCreator = mock(MockTableCreator.class);
        ConnectionSource connectionSource;

        public OrmDaoHelper(Class<T> entityClass)
        {
            super(entityClass);
            connectionSource = null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public PersistenceDao<T, ID> getDao(ConnectionSource connectionSource) 
        {
            this.connectionSource = connectionSource;
            return (PersistenceDao<T, ID>)dao;
        }
        @Override
        protected void createEntityTable(Class<T> clazz, ConnectionSource connectionSource) throws SQLException
        {
            this.connectionSource = connectionSource;
            tableCreator.createTable(clazz, connectionSource);
        }
    }
    
    @Test 
    public void test_create_for_no_entity_table_case() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(false);
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        helper.create(entity, connectionSource);
        verify(helper.tableCreator).createTable(RecordCategory.class, connectionSource);
        verify(dao).create(entity);
        assertThat(helper.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_for_entity_table_exists_case() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(true);
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        helper.create(entity, connectionSource);
        verifyZeroInteractions(helper.tableCreator);
        verify(dao).create(entity);
        assertThat(helper.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_sql_exception() throws Exception
    {
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(false);
        SQLException exception = new SQLException();
        Mockito.doThrow(exception).when(helper.tableCreator).createTable(RecordCategory.class, connectionSource);
        try
        {
            helper.create(entity, connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating table for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }
*/
    PersistenceDao<RecordCategory, Integer> dao;

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        dao = mock(PersistenceDao.class);
    }
    
    @Test
    public void test_query_for_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.queryForId(id)).thenReturn(entity);
        assertThat(helper.queryForId(id)).isEqualTo(entity);
    }
    
    @Test
    public void test_query_for_same_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        RecordCategory entity2 = new RecordCategory();
        when(dao.queryForSameId(entity1)).thenReturn(entity2);
        assertThat(helper.queryForSameId(entity1)).isEqualTo(entity2);
    }

    @Test
    public void test_extract_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.extractId(entity1)).thenReturn(id);
        assertThat(helper.extractId(entity1)).isEqualTo(id);
    }

    @Test
    public void test_entity_exists() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.extractId(entity1)).thenReturn(id);
        when(dao.isTableExists()).thenReturn(true);
        when(dao.idExists(id)).thenReturn(true);
        assertThat(helper.entityExists(entity1)).isTrue();
    }

    @Test
    public void test_update() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.update(entity1)).thenReturn(1);
        assertThat(helper.update(entity1)).isEqualTo(1);
    }

    @Test
    public void test_delete() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.delete(entity1)).thenReturn(1);
        assertThat(helper.delete(entity1)).isEqualTo(1);
    }
}
