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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Savepoint;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

@RunWith(MockitoJUnitRunner.class)
public class TransactionConnectionTest {

	static LogRecordHandler logRecordHandler;

	@Mock
	ConnectionSource connectionSource;
	@Mock
	DatabaseConnection connection;
	@Mock
	DatabaseType databaseType;
	@Mock
	Savepoint savePoint;
	int transactionId;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}
	
    @Test
    public void test_create() throws Exception
    {
        transactionId += 1;
        String savepointName = "ORMLITE" + transactionId;
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        TransactionConnection transactionConn = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        assertThat(transactionConn.getHasSavePoint()).isTrue();
        assertThat(transactionConn.getSavePointName()).isEqualTo(savepointName);
        assertThat(transactionConn.excludeAutoCommit()).isTrue();
        //logRecordHandler.printAll();
        String logMessage = String.format("Started savePoint transaction ORMLITE%d", transactionId);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, logMessage)).isTrue();
    }

    @Test
    public void test_begin_NestedSavePointsSupported() throws Exception
    {
        transactionId += 1;
        when(databaseType.isNestedSavePointsSupported()).thenReturn(true);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(false);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connection.isAutoCommit()).thenReturn(true);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        TransactionConnection transactionConn = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        assertThat(transactionConn.excludeAutoCommit()).isTrue();
        String logMessage = String.format("Started savePoint transaction ORMLITE%d", transactionId);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, logMessage)).isTrue();
    }

    @Test
    public void test_begin_connection_exception() throws Exception
    {
        transactionId += 1;
        SQLException exception = new SQLException("Connection failed");
        doThrow(exception).when(connectionSource).getReadWriteConnection("");
        try
        {
            new TransactionConnection(connectionSource, transactionId);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("Connection failed");
        }
   }

    @Test
    public void test_save_special_connection_exception() throws Exception
    {
        transactionId += 1;
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenThrow(new SQLException("saveSpecialConnection failed"));
        try
        {
            new TransactionConnection(connectionSource, transactionId);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("saveSpecialConnection failed");
        }
        verify(connectionSource).clearSpecialConnection(connection);
        verify(connection, times(0)).setAutoCommit(true);
   }

    @Test
    public void test_begin_connection_source_exception_on_release() throws Exception
    {
        transactionId += 1;
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenThrow(new SQLException("isAutoCommitSupported failed"));
        doThrow(new SQLException("releaseConnection failed")).when(connectionSource).releaseConnection(connection);
        try
        {
            new TransactionConnection(connectionSource, transactionId);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("isAutoCommitSupported failed");
        }
        verify(connectionSource).clearSpecialConnection(connection);
    }

    @Test
    public void test_begin_auto_commit_supported_exception() throws Exception
    {
        transactionId += 1;
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(false);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        when(connection.setSavePoint(isA(String.class))).thenThrow(new SQLException("setSavePoint failed"));
        try
        {
            new TransactionConnection(connectionSource, transactionId);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("setSavePoint failed");
        }
        verify(connection).setAutoCommit(false);
        verify(connection).setAutoCommit(true);
        verify(connectionSource, times(0)).clearSpecialConnection(connection);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, "restored auto-commit to true")).isTrue();
   }

    @Test
    public void test_activate_double_exception() throws Exception
    {
        transactionId += 1;
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        TransactionConnection transConnection = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        assertThat(transConnection.canCommit()).isTrue();
        transConnection.commit();
        verify(connection).commit(savePoint);
        assertThat(transConnection.isActive()).isTrue();
        String logMessage = String.format("Started savePoint transaction ORMLITE%d", transactionId);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, logMessage)).isTrue();
        transConnection.release();
        assertThat(logRecordHandler.match(2, "restored auto-commit to true")).isTrue();
        verify(connection).setAutoCommit(true);
        assertThat(transConnection.isActive()).isFalse();
        transactionId += 1;
        String savepointName = "ORMLITE" + transactionId;
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        when(connection.setSavePoint(savepointName)).thenThrow(new SQLException("setSavePoint failed"));
        doThrow(new SQLException("releaseConnection failed")).when(connectionSource).releaseConnection(connection);
        try
        {
        	transConnection.activate( transactionId);
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("setSavePoint failed");
        }
        verify(connection, times(2)).setAutoCommit(false);
        verify(connectionSource, times(2)).clearSpecialConnection(connection);
        assertThat(logRecordHandler.match(3, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(4, "restored auto-commit to true")).isTrue();
        assertThat(logRecordHandler.match(5, "releaseConnection() failed - \"releaseConnection failed\"")).isTrue();
    }

    @Test
    public void test_commit() throws Exception
    {
        transactionId += 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        TransactionConnection transConnection = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        assertThat(transConnection.canCommit()).isTrue();
        transConnection.commit();
        verify(connection).commit(savePoint);
        assertThat(transConnection.isActive()).isTrue();
        String logMessage = String.format("Started savePoint transaction ORMLITE%d", transactionId);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, logMessage)).isTrue();
    }

    @Test
    public void test_rollback() throws Exception
    {
        transactionId += 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        TransactionConnection transConnection = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        transConnection.rollback();
        verify(connection).rollback(savePoint);
        assertThat(transConnection.isActive()).isTrue();
        String logMessage = String.format("Started savePoint transaction ORMLITE%d", transactionId);
        assertThat(logRecordHandler.match(0, "Had to set auto-commit to false")).isTrue();
        assertThat(logRecordHandler.match(1, logMessage)).isTrue();
    }

    @Test
    public void test_commit_rollback() throws Exception
    {
        transactionId += 1;
        String savepointName = "ORMLITE" + transactionId;
        when(connectionSource.getDatabaseType()).thenReturn(databaseType);
        when(databaseType.isNestedSavePointsSupported()).thenReturn(false);
        when(connectionSource.getReadWriteConnection("")).thenReturn(connection);
        when(connectionSource.saveSpecialConnection(connection)).thenReturn(true);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true, false);
        when(connection.setSavePoint(savepointName)).thenReturn(savePoint);
        TransactionConnection transConnection = new TransactionConnection(connectionSource, transactionId);
        verify(connectionSource).saveSpecialConnection(connection);
        verify(connection).setAutoCommit(false);
        doThrow(new SQLException("doCommit failed")).when(connection).commit(savePoint);
        try
        {
            assertThat(transConnection.canCommit()).isTrue();
            transConnection.commit();
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("doCommit failed");
        }
        assertThat(transConnection.getHasSavePoint()).isTrue();
        transConnection.rollback();
        verify(connection).rollback(savePoint);
        assertThat(transConnection.isActive()).isTrue();
    }
}
