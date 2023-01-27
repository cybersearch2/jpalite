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
package au.com.cybersearch2.classyjpa.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * ClassyEntityTransactionTest
 * @author Andrew Bowley
 * 18/07/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityTransactionImplTest
{
    class CallableBase
    {
        PersistenceException persistenceException;
        IllegalArgumentException illegalArgumentException;
        IllegalStateException illegalStateException;
        UnsupportedOperationException unsupportedOperationException;
        // Sample RuntimeException
        boolean doThrowNpe;
        NullPointerException nullPointerException;
        SQLException sqlException;
        Boolean result = Boolean.valueOf(true);
        boolean isCalled;
        
        @SuppressWarnings("null")
        protected void throwExceptionIfSet() throws Exception
        {
            isCalled = true;
            if (persistenceException != null)
                throw persistenceException;
            if (illegalArgumentException != null)
                throw illegalArgumentException;
            if (illegalStateException != null)
                throw illegalStateException;
            if (unsupportedOperationException != null)
                throw unsupportedOperationException;
            if (doThrowNpe)
                try
                {
                    Object object = null;
                    object.toString();
                }
                catch(NullPointerException e)
                {
                    nullPointerException = e;
                    throw(e);
                }
            if (sqlException != null)
                throw sqlException;
        }
    }
    
    class TestPrecommitCallable extends CallableBase implements TransactionCallable
    {

        @Override
        public Boolean call(DatabaseConnection databaseConnection) throws Exception 
        {
            throwExceptionIfSet();
            return result;
        }
        
    }

    class TestPostcommitCallable extends CallableBase implements Callable<Boolean>
    {

        @Override
        public Boolean call() throws Exception 
        {
            throwExceptionIfSet();
            return result;
        }
        
    }

    @Mock
    TransactionState transState;
    @Mock
    ConnectionSource connectionSource;
    @Mock
    TransactionStateFactory transStateFactory;
 
    @Test
    public void test_begin()
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
        verify(transStateFactory).transactionStateInstance();
        assertThat(entityTransImpl.isActive()).isTrue();
    }

    @Test
    public void test_begin_already_active()
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.begin();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch (IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("begin() called while active");
        }
    }
    
    @Test
    public void test_begin_sql_exception() throws SQLException
    {
    	SQLException sqlException = new SQLException("Database error");
        doThrow(new PersistenceException("SQL error while creating TransactionState", sqlException))
            .when(transStateFactory).transactionStateInstance();
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
        try
        {
        	entityTransImpl.begin();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("SQL error while creating TransactionState");
            assertThat(e.getCause()).isInstanceOf(SQLException.class);
        }
    }
    
    @Test
    public void test_commit() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_commit_not_active()
    {
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch (IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("commit() called while not active");
        }
    }
    
    @Test
    public void test_commit_sql_exception() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
        doThrow(new SQLException("Database error")).when(transState).doCommit();
        try
        {
        	entityTransImpl.commit();
           failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Exception on commit/rollback: Database error");
            assertThat(e.getCause()).isInstanceOf(SQLException.class);
        }
    }
   
    @Test
    public void test_commit_rollback_only() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
        assertThat(entityTransImpl.getRollbackOnly()).isFalse();
        entityTransImpl.setRollbackOnly();
        assertThat(entityTransImpl.getRollbackOnly()).isTrue();
        entityTransImpl.commit();
        verify(transState).doRollback();
        assertThat(entityTransImpl.isActive()).isFalse();
        entityTransImpl.begin();
        assertThat(entityTransImpl.getRollbackOnly()).isFalse();
    }
        
    @Test
    public void test_commit_rollback_only_sql_exception() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
    	entityTransImpl.setRollbackOnly();
        doThrow(new SQLException("Database error")).when(transState).doRollback();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Exception on commit/rollback: Database error");
            assertThat(e.getCause()).isInstanceOf(SQLException.class);
        }
    }
    
    @Test
    public void test_rollback() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
    	entityTransImpl.rollback();
        verify(transState).doRollback();
        assertThat(entityTransImpl.isActive()).isFalse();
     }
    
    @Test
    public void test_rollback_sql_exception() throws SQLException
    {
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory);
    	entityTransImpl.begin();
        doThrow(new SQLException("Database error")).when(transState).doRollback();
        // Do not throw exception from rollback so it can be called from a finally clause
        entityTransImpl.rollback();
    }
     
    @Test
    public void test_precommit() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
        assertThat(callable.isCalled).isTrue();
    }
    
    @Test
    public void test_precommit_fail() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.result = Boolean.FALSE;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
            assertThat(e.getCause()).isEqualTo(callable.sqlException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_persistence_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.persistenceException = new PersistenceException("Not an entity");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
            assertThat(e.getCause()).isEqualTo(callable.persistenceException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_illegalArgument_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.illegalArgumentException = new IllegalArgumentException("Invalid parameter");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
            assertThat(e.getCause()).isEqualTo(callable.illegalArgumentException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_illegalStateException_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.illegalStateException = new IllegalStateException("Invalid state");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
            assertThat(e.getCause()).isEqualTo(callable.illegalStateException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }

    @Test
    public void test_precommit_unsupportedOperation_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.unsupportedOperationException = new UnsupportedOperationException("Not supported");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
            assertThat(e.getCause()).isEqualTo(callable.unsupportedOperationException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_nullpointer_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.doThrowNpe = true;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            assertThat(e).isEqualTo(callable.nullPointerException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_sql_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.sqlException = new SQLException("Database kaput!");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
            assertThat(e.getCause()).isEqualTo(callable.sqlException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_precommit_rollback_sql_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.result = Boolean.FALSE;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, callable);
    	entityTransImpl.begin();
        doThrow(new SQLException("Database error")).when(transState).doRollback();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
            assertThat(e.getCause()).isEqualTo(callable.sqlException);
        }
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        assertThat(callable.isCalled);
        verify(transState).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
        assertThat(callable.isCalled).isTrue();
    }
    
    @Test
    public void test_postcommit_fail() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.result = Boolean.FALSE;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_persistence_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.persistenceException = new PersistenceException("Not an entity");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_illegalArgument_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.illegalArgumentException = new IllegalArgumentException("Invalid parameter");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_illegalStateException_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.illegalStateException = new IllegalStateException("Invalid state");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_unsupportedOperation_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.unsupportedOperationException = new UnsupportedOperationException("Not supported");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_nullpointer_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.doThrowNpe = true;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            assertThat(e).isEqualTo(callable.nullPointerException);
        }
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
   
    @Test
    public void test_postcommit_sql_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.sqlException = new SQLException("Database kaput!");
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, null, callable);
    	entityTransImpl.begin();
    	entityTransImpl.commit();
        verify(transState, times(0)).doRollback();
        verify(transState, times(1)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
    
    @Test
    public void test_postcommit_rollback_sql_exception() throws SQLException
    {
        TestPostcommitCallable postcallable = new TestPostcommitCallable();
        TestPrecommitCallable precallable = new TestPrecommitCallable();
        precallable.result = Boolean.FALSE;
    	when(transStateFactory.transactionStateInstance()).thenReturn(transState);
    	EntityTransactionImpl entityTransImpl = new EntityTransactionImpl(transStateFactory, precallable, postcallable);
    	entityTransImpl.begin();
        // This just gets logged
        doThrow(new SQLException("Database error")).when(transState).doRollback();
        try
        {
        	entityTransImpl.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
             assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
             assertThat(e.getCause()).isNull();
        }
        assertThat(postcallable.isCalled).isFalse();
        verify(transState, times(1)).doRollback();
        verify(transState, times(0)).doCommit();
        assertThat(entityTransImpl.isActive()).isFalse();
    }
}
