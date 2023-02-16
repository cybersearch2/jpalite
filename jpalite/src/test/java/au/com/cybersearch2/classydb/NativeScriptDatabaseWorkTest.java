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
package au.com.cybersearch2.classydb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

/**
 * NativeScriptDatabaseWorkTest
 * @author Andrew Bowley
 * 01/08/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class NativeScriptDatabaseWorkTest
{
    static final String CREATE_SQL = "create table models ( _id integer primary key autoincrement, name text, _description text);\n";
    public static final String CREATE_SQL_FILENAME = "create.sql";
    public static final String DROP_SQL_FILENAME = "drop.sql";
	static LogRecordHandler logRecordHandler;

	@Mock
    ResourceEnvironment resourceEnvironment;
	@Mock
    DatabaseConnection databaseConnection;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() {
		TestLogHandler.getLogRecordHandler().clear();
	}
    
    @Test
    public void test_constructor()
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, CREATE_SQL_FILENAME);
        assertThat(databaseWork.getResourceEnvironment()).isNotNull();
    }
    
    @Test
    public void test_call() throws Exception
    {
        TestByteArrayInputStream bais = new TestByteArrayInputStream(CREATE_SQL.getBytes());
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, CREATE_SQL_FILENAME);
        when(resourceEnvironment.openResource(CREATE_SQL_FILENAME)).thenReturn(bais);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(true);
        assertThat(bais.isClosed()).isTrue();
        verify(databaseConnection).executeStatement(CREATE_SQL.trim(), DatabaseConnection.DEFAULT_RESULT_FLAGS);
    }

    @Test
    public void test_call_null_filename() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, (String)null);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(false);
   }
 

    @Test
    public void test_call_empty_filename() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, "");
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(false);
   }

    @Test
    public void test_doInBackground_ioexception_on_open() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, CREATE_SQL_FILENAME);
        doThrow(new IOException("File not found")).when(resourceEnvironment).openResource(CREATE_SQL_FILENAME);
        try
        {
            databaseWork.call(databaseConnection);
            failBecauseExceptionWasNotThrown(IOException.class);
        }
        catch (IOException e)
        {
            assertThat(e.getMessage()).isEqualTo("File not found");
        }
    }

    @Test
    public void test_doInBackground_ioexception_on_close() throws Exception
    {
        TestByteArrayInputStream bais = new TestByteArrayInputStream(CREATE_SQL.getBytes());
        bais.throwExceptionOnClose = true;
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, CREATE_SQL_FILENAME);
        when(resourceEnvironment.openResource(CREATE_SQL_FILENAME)).thenReturn(bais);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(true);
        verify(databaseConnection).executeStatement(CREATE_SQL.trim(), DatabaseConnection.DEFAULT_RESULT_FLAGS);
        assertThat(logRecordHandler.match(0, "Executed 1 statements from create.sql")).isTrue();
        assertThat(logRecordHandler.match(1, "Error closing file create.sql")).isTrue();
    }
    
}
