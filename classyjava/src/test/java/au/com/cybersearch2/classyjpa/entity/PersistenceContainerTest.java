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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityExistsException;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitAdmin;
import au.com.cybersearch2.classyjpa.persist.TestEntityManagerFactory;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * PersistenceContainerTest
 * @author Andrew Bowley
 * 27/06/2014
 */
public class PersistenceContainerTest
{
    public static class PersistenceContainerTestModule
    {
    	private PersistenceContext persistenceContext;

        PersistenceFactory providePersistenceFactory() 
        {
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            PersistenceUnitAdmin persistenceUnitAdmin = mock(PersistenceUnitAdmin.class);
            when(persistenceFactory.getPersistenceUnit(isA(String.class))).thenReturn(persistenceUnitAdmin);
            PersistenceAdmin persistenceAdmin = mock(PersistenceAdmin.class);
            ConnectionSource connectionSource = mock(ConnectionSource.class);
            when(persistenceAdmin.isSingleConnection()).thenReturn(false);
            when(persistenceAdmin.getConnectionSource()).thenReturn(connectionSource);
            when(persistenceUnitAdmin.getPersistenceAdmin()).thenReturn(persistenceAdmin);
            when(persistenceAdmin.getEntityManagerFactory()).thenReturn(new TestEntityManagerFactory());
            return persistenceFactory;
        }
        
        PersistenceContext providePersistenceContext()
        {
        	if (persistenceContext == null)
        	{
        		ConnectionSourceFactory connectionSourceFactory = mock(ConnectionSourceFactory.class);
        		persistenceContext = new PersistenceContext(providePersistenceFactory(), connectionSourceFactory);
        	}
        	return persistenceContext;
        }
        
        DefaultTaskExecutor provideTaskManager()
        {
            return new DefaultTaskExecutor();
        }
        
    }

    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
    }

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

    private EntityManagerImpl entityManager;
    private ApplicationComponent component;
    private PersistenceWorkModule persistenceWorkModule;
    private Transcript transcript;
    private EntityTransactionImpl transaction;

    @BeforeClass
    public static void before() {
    	// Set ORMLite system property to select local Logger
        System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, LogBackendType.LOG4J2.name());
    }
    
    @Before
    public void setUp() throws Exception 
    {
        component = new ApplicationComponent() {

        	PersistenceContainerTestModule module = new PersistenceContainerTestModule();
        	
			@Override
			public PersistenceContext persistenceContext() {
				return module.providePersistenceContext();
			}
		};
        transcript = new Transcript();
        transaction = TestEntityManagerFactory.setEntityManagerInstance();
        entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();
    }

    @Test 
    public void test_background_called() throws InterruptedException
    {
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        verify(transaction).begin();
        verify(entityManager).close();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    @Test 
    public void test_exception_thrown() throws InterruptedException
    {   
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(entityManager).persist(entity);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        verify(entityManager).close();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        doThrow(exception).when(entityManager).close();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_close_following_previous_exception() throws InterruptedException
    {   // Expected behavior: The first exception is reported
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(entityManager).persist(entity);
        doThrow(exception).when(entityManager).close();
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_begin() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on connect: SQLException");
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(transaction).begin();
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("onRollback " + exception.toString());
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_null_pointer_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(entityManager).close();
        when(transaction.isActive()).thenReturn(true);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_null_pointer_exception_thrown_on_persist() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(transaction.isActive()).thenReturn(true, false);
        doThrow(exception).when(entityManager).persist(entity);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(entityManager, never()).close();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_persist_NullPointerException_no_active_transaction() throws InterruptedException
    {
        transaction = mock(EntityTransactionImpl.class);
        TestEntityManagerFactory.setEntityManagerInstance(transaction);
        entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();

        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(transaction.isActive()).thenReturn(false);
        doThrow(exception).when(entityManager).persist(entity);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        persistenceWorkModule.setUserTransactions(true);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(transaction, times(0)).begin();
        verify(transaction, times(0)).rollback();
        verify(entityManager, never()).close();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_background_user_transaction() throws InterruptedException
    {
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        persistenceWorkModule.setUserTransactions(true);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        verify(entityManager, times(2)).setUserTransaction(true);
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        verify(entityManager).close();
        verify(transaction, times(0)).getRollbackOnly();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    }    

    @Test
    public void test_persist_EntityExistsException() throws InterruptedException
    {
        do_persist_exception(new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists"));
    }
    
    @Test
    public void test_persist_IllegalArgumentException() throws InterruptedException
    {
        do_persist_exception(new IllegalArgumentException("persist entity has null primary key"));
    }
    
    @Test
    public void test_persist_IllegalStateException() throws InterruptedException
    {
        do_persist_exception(new IllegalStateException("persist called after EntityManager has been closed"));
    }

    @Test
    public void test_persist_UnsupportedOperationException() throws InterruptedException
    {
        UnsupportedOperationException exception = new UnsupportedOperationException("FlushModeType.AUTO not supported");
        Mockito.doThrow(exception).when(entityManager).setFlushMode(FlushModeType.AUTO);
        do_persist_exception(exception, new FlushModeWork(transcript));
    }
    
    private void do_persist_exception(Throwable exception) throws InterruptedException
    {
        do_persist_exception(exception, null);
    }
    
    private void do_persist_exception(Throwable exception, PersistenceWork persistenceWork) throws InterruptedException
    {
        final RecordCategory entity = new RecordCategory();
        if (persistenceWork == null)
        {
            persistenceWork = new EntityManagerWork(entity, transcript);
            doThrow(exception).when(entityManager).persist(entity);
        }
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        transcript.assertEventsInclude("onRollback " + exception.toString());
        verify(entityManager).close();
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FAILED);
    }

}
