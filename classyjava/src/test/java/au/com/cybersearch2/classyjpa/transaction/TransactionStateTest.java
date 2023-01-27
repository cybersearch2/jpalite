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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

/**
 * TransactionStateTest
 * @author Andrew Bowley
 * 09/05/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionStateTest
{
	static LogRecordHandler logRecordHandler;

	@Mock
	TransactionConnection transConnection;
	int transactionId;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() {
		TestLogHandler.getLogRecordHandler().clear();
	}
	
    @Test
    public void test_begin() throws Exception
    {
    	transactionId += 1;
    	when(transConnection.isActive()).thenReturn(true);
        TransactionState transactionState = new TransactionState(transConnection, transactionId);
        assertThat(transactionState.getTransactionId()).isEqualTo(transactionId);
        assertThat(transactionState.isActive()).isTrue();
    }

    @Test
    public void test_commit() throws Exception
    {
       	transactionId += 1;
        TransactionState transactionState = new TransactionState(transConnection, transactionId);
        when(transConnection.canCommit()).thenReturn(true);
        transactionState.doCommit();
        verify(transConnection).commit();
        verify(transConnection).release();
        assertThat(transactionState.isActive()).isFalse();
        String logMessage = String.format("Committed transaction id %d", transactionId);
        assertThat(logRecordHandler.match(0, logMessage)).isTrue();
    }

    @Test
    public void test_cannot_commit() throws Exception
    {
       	transactionId += 1;
        TransactionState transactionState = new TransactionState(transConnection, transactionId);
        when(transConnection.canCommit()).thenReturn(false);
        when(transConnection.excludeAutoCommit()).thenReturn(true);
        transactionState.doCommit();
        verify(transConnection).release();
        assertThat(transactionState.isActive()).isFalse();
        assertThat(logRecordHandler.match(0, "doCommit() called while connection in invalid state")).isTrue();
    }

    @Test
    public void test_rollback() throws Exception
    {
       	transactionId += 1;
        TransactionState transactionState = new TransactionState(transConnection, transactionId);
        when(transConnection.canCommit()).thenReturn(true);
        transactionState.doRollback();
        verify(transConnection).rollback();
        verify(transConnection).release();
        assertThat(transactionState.isActive()).isFalse();
        String logMessage = String.format("Rolled back transaction id %d", transactionId);
        logRecordHandler.printAll();
        assertThat(logRecordHandler.match(0, logMessage)).isTrue();
    }

    @Test
    public void test_commit_rollback() throws Exception
    {
       	transactionId += 1;
        TransactionState transactionState = new TransactionState(transConnection, transactionId);
        when(transConnection.canCommit()).thenReturn(true);
        doThrow(new SQLException("doCommit failed")).when(transConnection).commit();
        when(transConnection.getHasSavePoint()).thenReturn(true);
        try
        {
            transactionState.doCommit();
            failBecauseExceptionWasNotThrown(SQLException.class);
        }
        catch(SQLException e)
        {
            assertThat(e.getMessage()).contains("doCommit failed");
        }
        verify(transConnection).rollback();
        verify(transConnection).release();
        assertThat(transactionState.isActive()).isFalse();
        String logMessage = String.format("Rolled back transaction id %d - \"doCommit failed\"", transactionId);
        assertThat(logRecordHandler.match(0, logMessage)).isTrue();
        logRecordHandler.printAll();
    }
}
