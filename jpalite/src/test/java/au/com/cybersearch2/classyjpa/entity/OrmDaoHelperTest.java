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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * OrmDaoHelperTest
 * @author Andrew Bowley
 * 03/05/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class OrmDaoHelperTest
{
    @Mock
	private PersistenceDao<RecordCategory> dao;

    @Test 
    public void test_create_for_no_entity_table_case() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
         RecordCategory entity = new RecordCategory();
        helper.create(entity);
        verify(dao).create(entity);
    }
    
    @Test
    public void test_query_for_id() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.queryForId(id)).thenReturn(entity);
        assertThat(helper.queryForId(id)).isEqualTo(entity);
    }
    
    @Test
    public void test_query_for_same_id() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity1 = new RecordCategory();
        RecordCategory entity2 = new RecordCategory();
        when(dao.queryForSameId(entity1)).thenReturn(entity2);
        assertThat(helper.queryForSameId(entity1)).isEqualTo(entity2);
    }

    @Test
    public void test_extract_id() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.extractId(entity1)).thenReturn(id);
        assertThat(helper.extractId(entity1)).isEqualTo(id);
    }

    @Test
    public void test_entity_exists() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = Integer.valueOf(1);
        when(dao.extractId(entity1)).thenReturn(id);
        when(dao.idExists(id)).thenReturn(true);
        assertThat(helper.entityExists(entity1)).isTrue();
    }

    @Test
    public void test_update() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.update(entity1)).thenReturn(1);
        assertThat(helper.update(entity1)).isEqualTo(1);
    }

    @Test
    public void test_delete() throws Exception
    {
        OrmDaoHelper<RecordCategory> helper = new OrmDaoHelper<RecordCategory>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.delete(entity1)).thenReturn(1);
        assertThat(helper.delete(entity1)).isEqualTo(1);
    }
}
