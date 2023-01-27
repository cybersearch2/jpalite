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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.query.EntityQuery;
import au.com.cybersearch2.classyjpa.transaction.TransactionState;
import au.com.cybersearch2.classyjpa.transaction.TransactionStateFactory;

/**
 * ClassyEntityManagerTest
 * @author Andrew Bowley
 * 02/05/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityManagerImplTest
{
	private static class TestReturnType {
		
	}
	
	@Mock
    private ConnectionSource connectionSource;
	@Mock
    private Map<String,OrmDaoHelperFactory<RecordCategory>> helperFactoryMap;
	@Mock
	private Map<String,OrmDaoHelperFactory<? extends OrmEntity>> genericHelperFactoryMap;
	@Mock
	private OrmDaoHelperFactory<RecordCategory> ormDaoHelperFactory;
    @Mock
    private PersistenceConfig persistenceConfig;
    @Mock
    private OrmDaoHelper<RecordCategory> ormDaoHelper;
    @Mock
    private TransactionStateFactory transStateFactory;
    @Mock
    private MonitoredTransaction transaction;
    @Mock
    TransactionState transState;
    @Mock
    private OrmEntityMonitor entityMonitor;
    @Mock
    private DatabaseConnection connection;
    @Mock
    private PersistenceDao<RecordCategory> dao;
    @Mock
    private EntityQuery<RecordCategory> entityQuery;
    @Mock
    private TypedQuery<TestReturnType> query;
    private EntityManagerImpl entityManagerImpl;
     
    @Before
    public void setUp() throws Exception 
    {
    	when(transaction.getEntityMonitor()).thenReturn(entityMonitor);
    	when(transaction.getConnectionSource()).thenReturn(connectionSource);
        entityManagerImpl = new EntityManagerImpl(transaction, persistenceConfig);
    }

    @Test
    public void test_constructor() throws Exception
    {
        assertThat(entityManagerImpl.isOpen()).isTrue();
    }
    
    @Test 
    public void test_persist() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(entityMonitor.monitorNewEntity(entity, id, id)).thenReturn(true);
        entityManagerImpl.persist(entity);
        verify(transaction, times(0)).begin();
    }

    @Test 
    public void test_persist_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(entityMonitor.monitorNewEntity(entity, id, id)).thenReturn(true);
        entityManagerImpl.persist(entity);
        verify(transaction).begin();
    }

    @Test 
    public void test_persist_primary_key_create_error() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(0);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(0);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(entityMonitor.monitorNewEntity(entity, id, id)).thenReturn(false);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains("No Primary key or matches one belonging to managed entity");
        }
        verify(transaction).begin();
        verify(transaction).rollback();
    }

   @Test 
    public void test_contains_managed() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(entity);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(true);
    }

    @Test 
    public void test_contains_exists() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(true);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(true);
    }

    @Test 
    public void test_contains_not_exists() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(false);
    }
    
    @Test 
    public void test_contains_extract_null_id() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(0);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(false);
    }

    @Test 
    public void test_merge() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.merge)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(true);
        assertThat(entityManagerImpl.merge(entity)).isEqualTo(entity);
        verify(transaction, times(0)).begin();
    }
 
    @Test 
    public void test_merge_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.merge)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(false);
        assertThat(entityManagerImpl.merge(entity)).isEqualTo(entity);
        verify(transaction).begin();
    }
 
    @Test 
    public void test_refresh() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.refresh(entity)).thenReturn(1);
        entityManagerImpl.refresh(entity);
        verify(transaction, times(0)).begin();
    }
 
    @Test 
    public void test_refresh_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.refresh(entity)).thenReturn(1);
        entityManagerImpl.refresh(entity);
        verify(transaction).begin();
    }
 
    @Test 
    public void test_remove() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.delete(entity)).thenReturn(1);
        entityManagerImpl.remove(entity);
        verify(transaction, times(0)).begin();
        verify(entityMonitor).markForRemoval(RecordCategory.class, id);
    }
 
    @Test 
    public void test_remove_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.delete(entity)).thenReturn(1);
        entityManagerImpl.remove(entity);
        verify(transaction).begin();
        verify(entityMonitor).markForRemoval(RecordCategory.class, id);
    }
 
    @Test 
    public void test_persist_already_managed() throws Exception
    { 
        RecordCategory managed = new RecordCategory();
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(managed);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(EntityExistsException.class);
        }
        catch(EntityExistsException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_fresh_not_managed() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(null);
        try
        {
            entityManagerImpl.refresh(entity);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_persist_already_created() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(true);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(EntityExistsException.class);
        }
        catch(EntityExistsException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_persist_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.persist(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("persist");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_persist_null_entity() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.persist(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"entity\" is null");
        }
    }
 
    @Test 
    public void test_merge_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.merge(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("merge");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_refresh_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.refresh(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("refresh");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_remove_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.remove(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("remove");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_find_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        Integer primaryKey = Integer.valueOf(1);
        try
        {
            entityManagerImpl.find(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("find");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_get_reference_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        Integer primaryKey = Integer.valueOf(1);
        try
        {
            entityManagerImpl.getReference(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getReference");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_flush_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.flush();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("flush");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_clear_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.clear();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("clear");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_contains_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.contains(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("contains");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_getDelegate_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.getDelegate();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getDelegate");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_persist_unregistered_class() throws Exception
    { 
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenThrow(
           new PersistenceException("Class " + RecordCategory.class.getName() + " not an entity in this persistence context"));
        try
        {
            entityManagerImpl.persist(new RecordCategory());
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
        }
    }
 
    @Test 
    public void test_persist_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.create(entity)).thenReturn(0);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("persist");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test 
    public void test_refresh_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(entityMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        try
        {
            entityManagerImpl.refresh(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("refresh");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test 
    public void test_delete_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        try
        {
            entityManagerImpl.remove(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("remove");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }

    @Test
    public void test_find() throws Exception
    {
        RecordCategory entity = prepareHelperMap();
        Integer primaryKey = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(entity);
        assertThat(entityManagerImpl.find(RecordCategory.class, primaryKey)).isEqualTo(entity);
    }

    @Test
    public void test_find_not_found() throws Exception
    {
        Integer primaryKey = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(null);
        assertThat(entityManagerImpl.find(RecordCategory.class, primaryKey)).isEqualTo(null);
    }
    
    @Test
    public void test_get_reference() throws Exception
    {
        RecordCategory entity = prepareHelperMap();
        Integer primaryKey = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(entity);
        assertThat(entityManagerImpl.getReference(RecordCategory.class, primaryKey)).isEqualTo(entity);
    }
    
    @Test
    public void test_get_reference_not_found() throws Exception
    {
        Integer primaryKey = Integer.valueOf(1);
        when(entityMonitor.getOrmDaoHelperForClass(RecordCategory.class)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(null);
        try
        {
            entityManagerImpl.getReference(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(EntityNotFoundException.class);
        }
        catch(EntityNotFoundException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(primaryKey.toString());
        }
    }

    @Test
    public void test_flush_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(true);
        entityManagerImpl.flush();
        verify(transaction).commit();
        verify(transaction).begin();
     }

    @Test
    public void test_flush_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.flush();
        verify(transaction).begin();
     }

    @Test
    public void test_close_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(true);
        entityManagerImpl.close();
        assertThat(entityManagerImpl.isOpen()).isFalse();
        verify(transaction).commit();
        verify(entityMonitor).release();
     }

    @Test
    public void test_close_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.close();
        verify(entityMonitor).release();
    }

    @Test
    public void test_clear_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(true);
        entityManagerImpl.clear();
        verify(transaction).rollback();
        verify(entityMonitor).release();
        verify(transaction).begin();
     }

    @Test
    public void test_clear_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.clear();
        verify(transaction, never()).rollback();
        verify(entityMonitor).release();
        verify(transaction).begin();
     }

    @Test
    public void test_get_delegate() throws Exception
    {
    	when(persistenceConfig.getHelperFactoryMap()).thenReturn(genericHelperFactoryMap);
        EntityManagerDelegate delegate = (EntityManagerDelegate) entityManagerImpl.getDelegate();
        assertThat(delegate.connectionSource).isEqualTo(connectionSource);
        assertThat(delegate.helperFactoryMap).isEqualTo(genericHelperFactoryMap);
        assertThat(delegate.getTransaction()).isEqualTo(transaction);
    }

    @Test
    public void test_user_rollback() throws Exception
    {
        EntityTransaction testTransaction = entityManagerImpl.getTransaction(); 
        assertThat(testTransaction).isNotNull();
        assertThat(testTransaction).isNotEqualTo(transaction);
        testTransaction.begin();
        testTransaction.commit();
        testTransaction.setRollbackOnly();
        verify(transaction).setRollbackOnly();
        when(transaction.isActive()).thenReturn(true);
        entityManagerImpl.close();
        verify(transaction).commit(); // Will cause rollback because rollback only flagged
        verify(entityMonitor).release();
    }
    
    @Test
    public void test_user_transaction_mode() throws Exception
    {
        assertThat(entityManagerImpl.getTransaction()).isNotNull();
        assertThat(entityManagerImpl.getTransaction()).isNotEqualTo(transaction);
        entityManagerImpl.setUserTransaction(true);
        assertThat(entityManagerImpl.getTransaction()).isEqualTo(transaction);
    }
    
	@Test
    public void test_create_named_query() 
    {
        String QUERY_NAME = "my_query";
        when(persistenceConfig.createNamedQuery(QUERY_NAME, RecordCategory.class, connectionSource)).thenReturn(entityQuery);
        Query result = entityManagerImpl.createNamedQuery(QUERY_NAME, RecordCategory.class);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(entityQuery);
    }

    @Test
    public void test_create_sql_named_query() 
    {
        String QUERY_NAME = "my_sql_query";
        when(persistenceConfig.createNamedQuery(QUERY_NAME, TestReturnType.class, connectionSource)).thenReturn(query);
        Query result = entityManagerImpl.createNamedQuery(QUERY_NAME, TestReturnType.class);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(query);
    }

    @Test
    public void test_create_named_query_not_found() 
    {
        String QUERY_NAME = "my_query";
        when(persistenceConfig.createNamedQuery(QUERY_NAME, RecordCategory.class, connectionSource)).thenThrow(
        	new IllegalArgumentException("Named query '" + QUERY_NAME + "' not found"));
        try
        {
            entityManagerImpl.createNamedQuery(QUERY_NAME, RecordCategory.class);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Named query '" + QUERY_NAME + "' not found");
        }
    }
    
    @Test
    public void test_set_flush_mode()
    {
        entityManagerImpl.setFlushMode(FlushModeType.COMMIT);
        assertThat(entityManagerImpl.getFlushMode()).isEqualTo(FlushModeType.COMMIT);
        try
        {
            entityManagerImpl.setFlushMode(FlushModeType.AUTO);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("FlushModeType.AUTO not supported");
        }
    }
    
    @Test 
    public void test_set_flush_mode_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.setFlushMode(FlushModeType.COMMIT);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("setFlushMode");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_get_flush_mode_after_close() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.getFlushMode();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getFlushMode");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_join_transaction() throws Exception
    { 
        try
        {
            entityManagerImpl.joinTransaction();
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("joinTransaction() not available");
        }
    }

    @Test 
    public void test_lock() throws Exception
    { 
        entityManagerImpl.setOpen(false);
        try
        {
            entityManagerImpl.lock(new Object(), LockModeType.READ);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("lock() not available");
        }
    }
  
    private RecordCategory prepareHelperMap()
    {
        RecordCategory entity = new RecordCategory();
        return entity;
        
    }
}
