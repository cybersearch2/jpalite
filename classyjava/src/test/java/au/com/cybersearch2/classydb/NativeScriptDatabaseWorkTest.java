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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyapp.ResourceEnvironment;

/**
 * NativeScriptDatabaseWorkTest
 * @author Andrew Bowley
 * 01/08/2014
 */
public class NativeScriptDatabaseWorkTest
{
    static final String CREATE_SQL = "create table models ( _id integer primary key autoincrement, name text, _description text);\n";
    public static final String CREATE_SQL_FILENAME = "create.sql";
    public static final String DROP_SQL_FILENAME = "drop.sql";

    ResourceEnvironment resourceEnvironment;
    DatabaseConnection databaseConnection;

    @Before
    public void setUp()
    {
        databaseConnection = mock(DatabaseConnection.class);
        resourceEnvironment = mock(ResourceEnvironment.class);
    }
    
    @Test
    public void test_constructor()
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(resourceEnvironment, CREATE_SQL_FILENAME);
        assertThat(databaseWork.resourceEnvironment).isNotNull();
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
    }
    
}
