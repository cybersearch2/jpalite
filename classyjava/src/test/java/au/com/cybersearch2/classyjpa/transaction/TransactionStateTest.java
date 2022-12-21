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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.db.DatabaseType;

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * ClassyFyEntityTransactionTest
 * @author Andrew Bowley
 * 09/05/2014
 */
public class TransactionStateTest
{
	private static final String DATABASE_INFO_NAME = "";
	
    private ConnectionSource connectionSource;
    private DatabaseConnection connection;


    @Before
    public void setUp() throws Exception 
    {
        connectionSource = mock(ConnectionSource.class);
        connection = mock(DatabaseConnection.class);

    }
    
    @Test
    public void test_begin() throws Exception
    {
        int transactionId = TransactionState.savePointCounter.get() + 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn(savepointName);
        TransactionState transactionState = new TransactionState(connectionSource);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        assertThat(transactionState.transactionId).isEqualTo(transactionId);
        assertThat(transactionState.hasSavePoint).isTrue();
        assertThat(transactionState.savePointName).isEqualTo(savepointName);
    }

    @Test
    public void test_begin_NestedSavePointsSupported() throws Exception
    {
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(true);
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(false);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connection.isAutoCommit()).thenReturn(true);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn("mySavePoint");
        new TransactionState(connectionSource);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
    }


    @Test
    public void test_begin_connection_exception() throws Exception
    {
        SQLException exception = new SQLException("Connection failed");
        doThrow(exception).when(connectionSource).getReadWriteConnection(DATABASE_INFO_NAME);
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("Connection failed");
        }
   }

    @Test
    public void test_begin_connection_source_exception() throws Exception
    {
        SQLException exception = new SQLException("saveSpecialConnection failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        doThrow(exception).when(connectionSource).saveSpecialConnection(connection);
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("Connection failed");
        }
        verify(connectionSource).clearSpecialConnection(connection);
   }

    @Test
    public void test_begin_connection_source_exception_on_release() throws Exception
    {
        SQLException exception = new SQLException("saveSpecialConnection failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        doThrow(exception).when(connectionSource).saveSpecialConnection(connection);
        doThrow(new SQLException()).when(connectionSource).releaseConnection(connection);
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("Connection failed");
        }
        verify(connectionSource).clearSpecialConnection(connection);
   }

    @Test
    public void test_begin_auto_commit_supported_exception() throws Exception
    {
        SQLException exception = new SQLException("isAutoCommitSupported failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        doThrow(exception).when(connection).isAutoCommitSupported();
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("isAutoCommitSupported failed");
        }
        verify(connectionSource).saveSpecialConnection(connection);
   }

    @Test
    public void test_begin_get_auto_commit_exception() throws Exception
    {
        SQLException exception = new SQLException("isAutoCommit failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        doThrow(exception).when(connection).isAutoCommit();
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("isAutoCommit failed");
        }
        verify(connectionSource).saveSpecialConnection(connection);
   }

    @Test
    public void test_begin_set_auto_commit_exception() throws Exception
    {
        SQLException exception = new SQLException("setAutoCommit failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        doThrow(exception).when(connection).setAutoCommit(false);;
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("setAutoCommit failed");
        }
        verify(connectionSource).saveSpecialConnection(connection);
   }

    @Test
    public void test_begin_set_save_point_exception() throws Exception
    {
        SQLException exception = new SQLException("setSavePoint failed");
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        doThrow(exception).when(connection).setSavePoint(isA(String.class));
        try
        {
            new TransactionState(connectionSource);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("setSavePoint failed");
        }
        verify(connection).setAutoCommit(false);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(true);
   }

    @Test
    public void test_commit() throws Exception
    {
        int transactionId = TransactionState.savePointCounter.get() + 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn("mySavePoint");
        TransactionState transactionState = new TransactionState(connectionSource);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        when(savePoint.getSavepointName()).thenReturn(savepointName);
        transactionState.doCommit();
        verify(connection).commit(savePoint);
        assertThat(transactionState.hasSavePoint).isNull();
        assertThat(transactionState.savePoint).isNull();
        assertThat(transactionState.connection).isNull();
    }

    @Test
    public void test_rollback() throws Exception
    {
        int transactionId = TransactionState.savePointCounter.get() + 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn("mySavePoint");
        TransactionState transactionState = new TransactionState(connectionSource);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        when(savePoint.getSavepointName()).thenReturn(savepointName);
        transactionState.doRollback();
        verify(connection).rollback(savePoint);
        assertThat(transactionState.hasSavePoint).isNull();
        assertThat(transactionState.savePoint).isNull();
        assertThat(transactionState.connection).isNull();
    }

    @Test
    public void test_commit_rollback() throws Exception
    {
        SQLException exception = new SQLException("doCommit failed");
        int transactionId = TransactionState.savePointCounter.get() + 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getReadWriteConnection(DATABASE_INFO_NAME)).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn(savepointName);
        TransactionState transactionState = new TransactionState(connectionSource);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        when(savePoint.getSavepointName()).thenReturn("mySavePoint");
        doThrow(exception).when(connection).commit(savePoint);
        try
        {
            transactionState.doCommit();
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("doCommit failed");
        }
        verify(connection).rollback(savePoint);
        assertThat(transactionState.hasSavePoint).isNull();
        assertThat(transactionState.savePoint).isNull();
        assertThat(transactionState.connection).isNull();
    }
}
