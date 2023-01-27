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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityExistsException;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext.EntityManagerProvider;
import au.com.cybersearch2.classyjpa.persist.TestEntityManagerFactory;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * JavaPersistenceContextTest
 * @author Andrew Bowley
 * 26 Jun 2015
 */
public class JavaPersistenceContextTest
{
    class EntityManagerWork extends TestPersistenceWork
    {
        RecordCategory entity;
        
        public EntityManagerWork(RecordCategory entity, Transcript transcript)
        {
            super(transcript);
            this.entity = entity;
        }
        
        @Override
        public void doTask(EntityManagerLite entityManager) 
        {
            super.doTask(entityManager);
            entityManager.persist(entity);
        }
    }

    class FlushModeWork extends TestPersistenceWork
    {
        RecordCategory entity;
        
        public FlushModeWork(Transcript transcript)
        {
            super(transcript);
        }
        
        @Override
        public void doTask(EntityManagerLite entityManager) 
        {
            super.doTask(entityManager);
            entityManager.setFlushMode(FlushModeType.AUTO);
        }
    }


    static class TestMocks
    {
        public EntityManagerProvider entityManagerProvider;
        public EntityManagerLite entityManager;
        public EntityTransactionImpl transaction;

        public TestMocks()
        {
            transaction = mock(EntityTransactionImpl.class);
            // Need UserTransactionSupport interface, so mock implementation
            entityManager = mock(EntityManagerImpl.class);
            // = mock(EntityManagerLite.class);
            when(entityManager.getTransaction()).thenReturn(transaction);
            entityManagerProvider = new EntityManagerProvider(){

                @Override
                public EntityManagerLite entityManagerInstance()
                {
                    return entityManager;
                }};
        }
        
    }

    protected TestMocks testMocks;
    
    @Before
    public void setUp()
    {
        testMocks = new TestMocks();
    }
    
    @Test 
    public void test_doTask()
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.PENDING);
        Boolean success = jpaContext.doTask();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.RUNNING);
        transcript.assertEventsSoFar("background task");
        verify(testMocks.transaction).begin();
        verify(testMocks.entityManager).close();
        assertThat(success).isTrue();
        assertThat(jpaContext.getTransactionInfo().getTransaction()).isEqualTo(testMocks.transaction);
        assertThat(jpaContext.getTransactionInfo().isUserTransaction()).isFalse();
    }

    @Test 
    public void test_onPostExecute()
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        jpaContext.onPostExecute(Boolean.TRUE);
        transcript.assertEventsSoFar("onPostExecute true");
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    @Test 
    public void test_doTask_no_UserTransactionSupport()
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        // Use EntityManagerLite interface to remove UserTransactionSupport. 
        // NOTE: No plans to actually use this in production.
        testMocks.entityManager = mock(EntityManagerLite.class);
        when(testMocks.entityManager.getTransaction()).thenReturn(testMocks.transaction);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.PENDING);
        Boolean success = jpaContext.doTask();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.RUNNING);
        transcript.assertEventsSoFar("background task");
        verify(testMocks.transaction).begin();
        verify(testMocks.entityManager).close();
        assertThat(success).isTrue();
        assertThat(jpaContext.getTransactionInfo().getTransaction()).isEqualTo(testMocks.transaction);
        assertThat(jpaContext.getTransactionInfo().isUserTransaction()).isFalse();
        jpaContext.onPostExecute(Boolean.TRUE);
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    @Test 
    public void test_doTask_no_UserTransactionSupport_user_transaction()
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        // Use EntityManagerLite interface to remove UserTransactionSupport. 
        // NOTE: No plans to actually use this in production.
        testMocks.entityManager = mock(EntityManagerLite.class);
        when(testMocks.entityManager.getTransaction()).thenReturn(testMocks.transaction);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        jpaContext.getTransactionInfo().setUserTransaction(true);
        PersistenceException persistenceException = null;
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("EntityManger does not support user transactions");
            jpaContext.setExecutionException(new ExecutionException(e));
            persistenceException = e;
        }
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.RUNNING);
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("onRollback " + persistenceException.toString());
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }



    @Test 
    public void test_exception_thrown()
    {   
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final RecordCategory entity = new RecordCategory();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(testMocks.entityManager).persist(entity);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        verify(testMocks.transaction).begin();
        verify(testMocks.transaction).setRollbackOnly();
        verify(testMocks.entityManager).close();
        assertThat(success).isFalse();
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_exception_thrown_on_entity_manager_close()
    {
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        doThrow(exception).when(testMocks.entityManager).close();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        assertThat(success).isFalse();
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_close_following_previous_exception() throws InterruptedException
    {   // Expected behavior: The first exception is reported
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        final RecordCategory entity = new RecordCategory();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(testMocks.entityManager).persist(entity);
        doThrow(exception).when(testMocks.entityManager).close();
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        assertThat(success).isFalse();
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_begin() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on connect: SQLException");
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(testMocks.transaction).begin();
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        Boolean success = jpaContext.doTask();
        assertThat(success).isNull();
        jpaContext.onPostExecute(null);
        transcript.assertEventsSoFar("onRollback " + exception.toString());
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_runtime_exception_thrown_on_instantiate_entity_manager() throws InterruptedException
    {
        final ConcurrentModificationException exception = new ConcurrentModificationException("Thread trouble");
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        // Throwing runtime exception before transaction created
        testMocks.entityManagerProvider = new EntityManagerProvider(){

            @Override
            public EntityManagerLite entityManagerInstance()
            {
                throw exception;
            }};
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(ConcurrentModificationException.class);
        }
        catch (ConcurrentModificationException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        jpaContext.onPostExecute(null);
        transcript.assertEventsSoFar("onRollback " + exception.toString());
        verify(testMocks.transaction, never()).begin();
        verify(testMocks.transaction, never()).rollback();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_null_pointer_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(testMocks.entityManager).close();
        when(testMocks.transaction.isActive()).thenReturn(true);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        transcript.assertEventsSoFar("background task");
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(testMocks.transaction).rollback();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
   @Test 
    public void test_null_pointer_exception_thrown_no_active_transaction() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(testMocks.entityManager).close();
        when(testMocks.transaction.isActive()).thenReturn(true, false);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        transcript.assertEventsSoFar("background task");
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(testMocks.transaction, never()).rollback();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_null_pointer_exception_thrown_on_persist() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(testMocks.transaction.isActive()).thenReturn(true, false);
        doThrow(exception).when(testMocks.entityManager).persist(entity);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        transcript.assertEventsSoFar("background task");
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(testMocks.transaction).begin();
        verify(testMocks.transaction).rollback();
        verify(testMocks.entityManager, never()).close();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_runtime_exception_after_persistence_exception() throws InterruptedException
    {
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final RecordCategory entity = new RecordCategory();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        // Expected behavior: The first exception is reported
        doThrow(persistException).when(testMocks.entityManager).persist(entity);
        NullPointerException exception = new NullPointerException("The parameter is null");
        when(testMocks.transaction.isActive()).thenReturn(true, false);
        doThrow(exception).when(testMocks.entityManager).close();
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        transcript.assertEventsSoFar("background task");
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        verify(testMocks.transaction).begin();
        verify(testMocks.transaction).rollback();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
   @Test 
    public void test_persist_NullPointerException_no_active_transaction() throws InterruptedException
    {
        testMocks.transaction = mock(EntityTransactionImpl.class);
        TestEntityManagerFactory.setEntityManagerInstance(testMocks.transaction);
        testMocks.entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();

        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(testMocks.transaction.isActive()).thenReturn(false);
        doThrow(exception).when(testMocks.entityManager).persist(entity);
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        jpaContext.getTransactionInfo().setUserTransaction(true);
        try
        {
            jpaContext.doTask();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            jpaContext.setExecutionException(new ExecutionException(e));
        }
        transcript.assertEventsSoFar("background task");
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(testMocks.transaction, times(0)).begin();
        verify(testMocks.transaction, times(0)).rollback();
        verify(testMocks.entityManager, never()).close();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_user_transaction() throws InterruptedException
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        // Need UserTransactionSupport interface, so mock implementation
        final EntityManagerImpl entityManagerImpl = mock(EntityManagerImpl.class);
        when(entityManagerImpl.getTransaction()).thenReturn(testMocks.transaction);
        EntityManagerProvider entityManagerImplProvider = new EntityManagerProvider(){

            @Override
            public EntityManagerLite entityManagerInstance()
            {
                return entityManagerImpl;
            }};
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, entityManagerImplProvider);
        jpaContext.getTransactionInfo().setUserTransaction(true);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        assertThat(success).isTrue();
        jpaContext.onPostExecute(Boolean.TRUE);
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        verify(entityManagerImpl).close();
        verify(testMocks.transaction, times(0)).getRollbackOnly();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FINISHED);
        assertThat(jpaContext.getTransactionInfo().isUserTransaction()).isTrue();
    }    

    @Test 
    public void test_user_transaction_rollbackonly() throws InterruptedException
    {
        Transcript transcript = new Transcript();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        EntityTransactionImpl rollbackTransaction = mock(EntityTransactionImpl.class);
        when(rollbackTransaction.isActive()).thenReturn(true);
        when(rollbackTransaction.getRollbackOnly()).thenReturn(true);
        // Need UserTransactionSupport interface, so mock implementation
        final EntityManagerImpl entityManagerImpl = mock(EntityManagerImpl.class);
        when(entityManagerImpl.getTransaction()).thenReturn(rollbackTransaction);
        EntityManagerProvider entityManagerImplProvider = new EntityManagerProvider(){

            @Override
            public EntityManagerLite entityManagerInstance()
            {
                return entityManagerImpl;
            }};
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, entityManagerImplProvider);
        jpaContext.getTransactionInfo().setUserTransaction(true);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        assertThat(success).isFalse();
        jpaContext.onPostExecute(Boolean.FALSE);
        transcript.assertEventsSoFar("background task", "onPostExecute false");
        verify(entityManagerImpl).close();
        verify(testMocks.transaction, times(0)).getRollbackOnly();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }    

    @Test
    public void test_persist_EntityExistsException()
    {
        do_persist_exception(new Transcript(), new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists"));
    }
    
    @Test
    public void test_persist_IllegalArgumentException()
    {
        do_persist_exception(new Transcript(), new IllegalArgumentException("persist entity has null primary key"));
    }
    
    @Test
    public void test_persist_IllegalStateException()
    {
        do_persist_exception(new Transcript(), new IllegalStateException("persist called after EntityManager has been closed"));
    }

    @Test
    public void test_persist_UnsupportedOperationException()
    {
        UnsupportedOperationException exception = new UnsupportedOperationException("FlushModeType.AUTO not supported");
        Mockito.doThrow(exception).when(testMocks.entityManager).setFlushMode(FlushModeType.AUTO);
        Transcript transcript = new Transcript();
        do_persist_exception(transcript, exception, new FlushModeWork(transcript));
    }
    
    private void do_persist_exception(Transcript transcript, Throwable exception)
    {
        do_persist_exception(transcript, exception, null);
    }
    
    private void do_persist_exception(Transcript transcript, Throwable exception, PersistenceWork persistenceWork)
    {
        final RecordCategory entity = new RecordCategory();
        if (persistenceWork == null)
        {
            persistenceWork = new EntityManagerWork(entity, transcript);
            doThrow(exception).when(testMocks.entityManager).persist(entity);
        }
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, testMocks.entityManagerProvider);
        Boolean success = jpaContext.doTask();
        transcript.assertEventsSoFar("background task");
        assertThat(success).isFalse();
        jpaContext.onPostExecute(Boolean.FALSE);
        verify(testMocks.transaction).begin();
        verify(testMocks.transaction).setRollbackOnly();
        transcript.assertEventsInclude("onRollback " + exception.toString());
        verify(testMocks.entityManager).close();
        assertThat(jpaContext.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

}
