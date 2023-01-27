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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;

/**
 * ObjectMonitorTest
 * @author Andrew Bowley
 * 07/05/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjectMonitorTest
{
    @Mock
    private EntityStore managedObjects;
    @Mock
    private EntityStore removedObjects;
    @Mock
    private OrmDaoHelperFactory<RecordCategory> ormDaoHelperFactory;
    @Mock
    private OrmDaoHelper<RecordCategory> ormDaoHelper;
    @Mock
    private ConnectionSource connectionSource;
    @Mock
    private PersistenceConfig persistenceConfig;
    @Captor
    private ArgumentCaptor<EntityKey> entityKeyCaptor;   
    private RecordCategory entity1;
    private RecordCategory entity2;
    private Date created;
    private Date modified;
  
    
    @Before
    public void setUp() throws Exception 
    {
        entity1 = new RecordCategory();
        entity2 = new RecordCategory();
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        created = cal.getTime();
        modified = new Date();

    }
    
    @Test 
    public void test_start_managing_persist()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(key)).thenReturn(false);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.persist) == null);
        verify( managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }

    @Test 
    public void test_start_managing_merge()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(key)).thenReturn(false);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.merge) == null);
        verify( managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().isDirty()).isTrue();
    }
    
    @Test 
    public void test_start_managing_refresh()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(key)).thenReturn(true);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.refresh) == null);
        verify( managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test 
    public void test_start_managing_contains()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(key)).thenReturn(true);
        when(managedObjects.get(key)).thenReturn(entity1);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.contains) == entity1);
    }

    @Test 
    public void test_start_managing_persist_already_managed()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id1);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        when(managedObjects.get(key)).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.persist).equals(entity2));
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test 
    public void test_start_managing_merge_already_managed()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        prepareMerge();
        EntityKey key = new EntityKey(RecordCategory.class, id1);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        when(managedObjects.get(key)).thenReturn(entity1);
        assertThat(monitor.startManagingEntity(entity2, id1, PersistOp.merge).equals(entity2));
        verify(managedObjects).remove(entityKeyCaptor.getValue());
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(entityKeyCaptor.getValue().isDirty()).isTrue();
        verify(managedObjects).put(entityKeyCaptor.getValue(), entity2);
        verifyMerge();
    }
    
    @Test 
    public void test_start_managing_refresh_already_managed()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id1);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        assertThat(monitor.startManagingEntity(entity2, id1, PersistOp.refresh).equals(entity2));
        verify(managedObjects).remove(entityKeyCaptor.getValue());
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
        verify(managedObjects).put(entityKeyCaptor.getValue(), entity2);
    }
    
    @Test 
    public void test_start_managing_contains_already_managed()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id1);
        when(removedObjects.containsKey(key)).thenReturn(false);
        when(managedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        when(managedObjects.get(key)).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.contains).equals(entity2));
        verify(managedObjects).get(entityKeyCaptor.getValue());
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test 
    public void test_start_managing_persist_removed_object_match()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        when(removedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.persist) == null);
        verify(removedObjects).remove(entityKeyCaptor.getValue());
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }
    
    @Test 
    public void test_start_managing_merge_removed_object_match()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        when(removedObjects.containsKey(any())).thenReturn(true);
        try
        {
            monitor.startManagingEntity(entity1, id1, PersistOp.merge);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
    }
    
    @Test 
    public void test_start_managing_refresh_removed_object_match()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        when(removedObjects.containsKey(any())).thenReturn(true);
        try
        {
            monitor.startManagingEntity(entity1, id1, PersistOp.refresh);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
    }
    
    @Test 
    public void test_start_managing_consists_removed_object_match()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        when(removedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(true);
        when(removedObjects.get(any(EntityKey.class))).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.contains).equals(entity2));
        verify(removedObjects).get(entityKeyCaptor.getValue());
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }
 
    @Test
    public void test_monitor_new_entity()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id);
        when(managedObjects.containsKey(key)).thenReturn(false, false);
        assertThat(monitor.monitorNewEntity(entity1, id, id)).isFalse();
    }
    
    @Test
    public void test_monitor_new_entity_primary_key_different()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        Integer id2 = Integer.valueOf(2);
        EntityKey key1 = new EntityKey(RecordCategory.class, id1);
        EntityKey key2 = new EntityKey(RecordCategory.class, id2);
        when(managedObjects.containsKey(key2)).thenReturn(false, false);
        when(managedObjects.containsKey(key1)).thenReturn(false);
        assertThat(monitor.monitorNewEntity(entity1, id1, id2)).isTrue();
        verify(managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(2);
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test
    public void test_monitor_new_entity_primary_key_already_managed()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        Integer id2 = Integer.valueOf(2);
        EntityKey key1 = new EntityKey(RecordCategory.class, id1);
        EntityKey key2 = new EntityKey(RecordCategory.class, id2);
        when(managedObjects.containsKey(key2)).thenReturn(false, false);
        when(managedObjects.containsKey(key1)).thenReturn(true);
        when(removedObjects.containsKey(key2)).thenReturn(false);
        assertThat(monitor.monitorNewEntity(entity1, id1, id2)).isTrue();
        verify(managedObjects).remove(key1);
        verify(managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(2);
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test
    public void test_monitor_new_entity_primary_key1_Null()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id2 = Integer.valueOf(1);
        EntityKey key2 = new EntityKey(RecordCategory.class, id2);
        when(managedObjects.containsKey(key2)).thenReturn(false, false);
        when(removedObjects.containsKey(key2)).thenReturn(false);
        assertThat(monitor.monitorNewEntity(entity1, null, id2)).isTrue();
        verify(managedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(1);
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
      
    @Test
    public void test_monitor_new_entity_no_primary_key()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = null;
        assertThat(monitor.monitorNewEntity(entity1, id1, id1)).isFalse();
    }
 
     @Test 
    public void test_mark_for_removal()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        EntityKey key = new EntityKey(RecordCategory.class, id1);
        when(managedObjects.containsKey(key)).thenReturn(true);
        when(managedObjects.remove(key)).thenReturn(entity1);
        monitor.markForRemoval(RecordCategory.class, id1);
        verify(removedObjects).put(entityKeyCaptor.capture(), eq(entity1));
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(1);
        assertThat(entityKeyCaptor.getValue().isDirty()).isFalse();
    }
    
    @Test 
    public void test_mark_for_removal_unmanaged()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        when(managedObjects.containsKey(entityKeyCaptor.capture())).thenReturn(false);
        try
        {
            monitor.markForRemoval(RecordCategory.class, id1);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
        assertThat(entityKeyCaptor.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(entityKeyCaptor.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }

    @Test
    public void test_release()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        monitor.release();
        verify(managedObjects).release();
        verify(removedObjects).release();
    }
     
    @Test
    public void test_update_all_managed_objects()
    {
        OrmEntityMonitor monitor = new OrmEntityMonitor(connectionSource, persistenceConfig, managedObjects, removedObjects);
        Integer id1 = Integer.valueOf(1);
        Integer id2 = Integer.valueOf(2);
        EntityKey key1 = new EntityKey(RecordCategory.class, id1);
        EntityKey key2 = new EntityKey(RecordCategory.class, id2);
        key1.setDirty(true);
        key2.setDirty(true);
        List<OrmEntity> toUpdate = new ArrayList<>();
        toUpdate.add(entity1);
        toUpdate.add(entity2);
        when(managedObjects.getObjectsToUpdate()).thenReturn(toUpdate);
        when(persistenceConfig.getHelperFactory(RecordCategory.class)).thenReturn(ormDaoHelperFactory);
        when(ormDaoHelperFactory.getOrmDaoHelper(connectionSource)).thenReturn(ormDaoHelper);
        when(ormDaoHelper.update(entity1)).thenReturn(1);
        when(ormDaoHelper.update(entity2)).thenReturn(1);
        monitor.updateAllManagedObjects();
    }
    
    private void prepareMerge()
    {
        entity1.set_id(1);
        entity1.setCreated(created);
        entity1.setCreator("George");
        entity1.setDescription("original description");
        entity2.set_id(1);
        entity2.setCreated(created);
        entity2.setCreator("George");
        entity2.setDescription("new description");
        entity2.setModified(modified);
        entity2.setModifier("Harry");
    }
    
    private void verifyMerge()
    {
        assertThat(entity1.get_id()).isEqualTo(1);
        assertThat(entity1.getCreated()).isEqualTo(created);
        assertThat(entity1.getCreator()).isEqualTo("George");
        assertThat(entity1.getDescription()).isEqualTo("new description");
        assertThat(entity1.getModified()).isEqualTo(modified);
        assertThat(entity1.getModifier()).isEqualTo("Harry");
    }
}
